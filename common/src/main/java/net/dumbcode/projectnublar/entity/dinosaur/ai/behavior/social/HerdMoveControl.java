package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Vanilla movement plus the herd's per-tick layer (spec: redesign, "Context steering" and "Guard"):
 * <ul>
 *   <li><b>Stop</b>: no wanted position; vanilla sets no forward input.</li>
 *   <li><b>Steer</b> ({@link #steerTo}): once per cadence (see {@link #EVALUATE_EVERY_TICKS}; the first
 *       evaluation is staggered by entity id, and a member the herd's cap of
 *       {@link DinoGroup#MAX_EVALUATIONS_PER_TICK} refused stays due) the 12 headings of
 *       {@link ContextSteering} are scored. A heading is masked when the clearance box cannot stand
 *       {@link #PROBE_NEAR} blocks ahead ({@link BodyFit}: fit, ground, hazards), when it would bring the
 *       body inside a member's hard minimum within 10 ticks ({@link HerdBodies}), or when the guard stopped
 *       the body at blocks on it since the last evaluation (the probes missed a corner). The previous heading
 *       and the {@link #DEEP_PROBES} best open headings are also probed {@link #PROBE_MID} (mask) and
 *       {@link #PROBE_FAR} (danger) blocks ahead; the other open headings are not probed and count as
 *       masked. Between evaluations the last heading is kept.</li>
 *   <li><b>Path</b>: navigation sets the wanted position (the leader, fallback paths, needs). Navigation
 *       always wins over steering.</li>
 *   <li><b>Unstack</b> and <b>side step</b> (spec "Spacing and stacking", Review Focus 7): a member inside
 *       a higher rank's hard minimum steers away ({@link #updateUnstack}); a standing member a walker's
 *       guard stopped at or yielded to ({@link #makeWay}, playtest 2 fix P8b: the spec's "leader blocked"
 *       for every walker, {@link HerdSpacing#asksToMakeWay}), or one in the leader's line at a journey
 *       start, steps out of the line ({@link #requestSideStepOutOf}). Both run through the same steering and take
 *       priority over the follower's {@link #steerTo} for their duration ({@link #stopSteering} leaves them
 *       alone); a path still wins.</li>
 *   <li><b>Ghosting</b> (lead decision, playtest 2 fix P8b): while the member walks a herd fallback path
 *       ({@link #ghosting}), herd mates do not stop it: the guard tests blocks only, and vanilla pushing
 *       sorts the bodies out. An unstack is not armed meanwhile, nor for {@link #GHOST_GRACE_TICKS} after.</li>
 * </ul>
 * The hard minimum to every herd mate is the tight one ({@link HerdSpacing#TIGHT_GAP}) while either body
 * stands in tight ground: side room across the body under {@link HerdSpacing#tightThreshold}, measured here
 * ({@link #tightGround}, re-measured once the body has moved {@link #TIGHT_REFRESH_DISTANCE}, never per
 * probe) and carried in the herd's {@link HerdBodies} snapshot, so the masks, the guard,
 * the unstack and the side-step clearance all keep one rule.
 * After vanilla has turned the body (at most 10 degrees per tick, eased in and out: {@link TurnEasing}) and set
 * the forward input, the <b>guard</b> tests the clearance box {@link #GUARD_AHEAD} blocks ahead along the real
 * body yaw, and the members' hard minimums the same distance ahead along the move direction (playtest 4 fix
 * P12b: a turn's yaw no longer brakes; {@link #GUARD_TURN_LIMIT_DEGREES}), in every
 * mode: if it does not fit, or it does not stand there (fix P12c: a drop past the rim, {@link GroundSupport#standTop};
 * not while the body walks a vanilla path facing its next node, {@link GroundSupport#guardChecksGround}), or it
 * enters a member's hard minimum, the forward input is 0 for this tick and
 * the horizontal velocity is cut to {@link #GUARD_VELOCITY_FACTOR} of itself (vanilla friction alone would
 * let the body coast on 1.2 times its last move: 0.23 blocks at the catch-up speed, {@link GroundSpeed}).
 * Speeds: the ground speed is quadratic in the modifier ({@link GroundSpeed}: 0.049 blocks per tick at the
 * graze speed 0.6, 0.086 at the journey speed 0.8, 0.19 at the catch-up speed 1.2); the slow-down factors
 * act on the ground speed, and a steering member never walks below {@link GroundSpeed#MIN_WALK_MODIFIER}
 * (the walk animation's floor). So spacing (T4) and fitting (T8) hold between evaluations and on
 * vanilla paths too. While steering, a guard stop at blocks counts as a blocked tick (lead decision 14:
 * blocked by blocks is stuck), so a corner the probes miss still leads to a fallback path. A fallback path the
 * guard holds for {@link #PATH_HOLD_TICKS} is stopped ({@link #holdPath}): vanilla's own stuck detection is
 * blind while the guard keeps the speed at 0. Herd work is timed.
 * <p>
 * Playtest 4 fix P12b: a walker closing on a moving herd mate ahead matches its speed early and smoothly
 * ({@link ContextSteering#matchedSpeed}, in the member masks and every tick), instead of running into the
 * mask and the guard. An animal that sits down or rests ({@link #lyingDown}) stands still: no steering, no
 * override, speed 0; crowding never gets it up, and herd mates walk round it as round any standing body. Only
 * the leader's make-way ask gets a resting member up, after its sit-down phase ({@link #askToMakeWay}).
 */
public class HerdMoveControl extends MoveControl {

    /** Steering stops this close to its target. */
    static final double ARRIVED = 1.0D;
    /** The guard tests the box this far ahead along the body yaw (spec: 0.6). */
    static final double GUARD_AHEAD = 0.6D;
    /** When the guard fires, the body keeps this fraction of its horizontal velocity. */
    static final double GUARD_VELOCITY_FACTOR = 0.2D;
    /**
     * Playtest 4 fix P12b (motion cause 3): the guard's member test looks along the move direction (toward the
     * wanted position: the steering heading or the next path node) while the yaw is within this many degrees
     * of it, so the yaw sweeping through a 30-degree heading change no longer brakes the body mid-turn. In a
     * larger turn the body arcs far off its heading, so the test looks along the yaw as before.
     * <p>
     * Trade-off (fix round 1, accepted): within this limit the member test looks along the move direction
     * while the body moves along its yaw, so up to about 0.42 blocks of the real path (0.6 x sin 45) beside
     * the tested point go untested at {@link #GUARD_AHEAD}; the masks tested the heading itself. Beyond the
     * limit the yaw test still brakes the body mid-turn.
     */
    static final double GUARD_TURN_LIMIT_DEGREES = 45D;
    /**
     * Fix P12c (cliff cause 3): on a vanilla path the guard's ground test is skipped while the yaw is within
     * this many degrees of the next node ({@link GroundSupport#guardChecksGround}), so the body stays within
     * 0.16 blocks (0.6 x sin 15) of the straight line between two nodes that passed the rim rules. Half a
     * steering step: the tolerance {@link ContextSteering#facesDirection} already uses for "faces its node".
     * A wider yaw lag (a turn, cliff cause 5) arcs the body off that line, and the ground is tested.
     */
    static final double GUARD_PATH_TRUST_DEGREES = ContextSteering.HALF_STEP_DEGREES;
    /** A wanted position closer than this (blocks) gives no move direction: the guard's member test uses the yaw. */
    static final double MOVE_DIRECTION_EPSILON = 1.0E-3D;
    static final double PROBE_NEAR = 1.0D;
    static final double PROBE_MID = 2.0D;
    static final double PROBE_FAR = 3.0D;
    /** Open headings probed 2 and 3 blocks ahead, besides the previous heading: the best by interest. */
    static final int DEEP_PROBES = 4;
    /**
     * The shortest cadence between two evaluations of one member. A herd larger than this times
     * {@link DinoGroup#MAX_EVALUATIONS_PER_TICK} gets a longer cadence ({@link #cadence}), so the cap spreads
     * the evaluations over the cadence and every member gets one within it instead of the last ones starving.
     */
    static final int EVALUATE_EVERY_TICKS = 2;
    /** The wanted position lies this far ahead along the chosen heading. */
    static final double WANTED_AHEAD = 2.0D;
    /** The clearance box is rebuilt after this many game ticks (growth changes it slowly). */
    static final int BOX_REFRESH_TICKS = 100;
    /**
     * A herd fallback path the guard has held this many ticks in a row, with the body facing its next node, is
     * stopped ({@link #holdPath}). Turning ticks do not count, so the limit is measured against the hold alone:
     * shorter than the stuck window ({@link ProgressMonitor#WINDOW_TICKS}, 40) and far shorter than the walk
     * behaviour's own run limit (150-249 ticks), which was the only exit before (playtest 2 fix P8a, geometry
     * cause 3).
     */
    static final int PATH_HOLD_TICKS = 20;
    /**
     * Spec "Unstack": closer than the hard minimum to a higher rank for this many ticks, the lower rank
     * steers away. 10, not the spec's 20: a legal pair that becomes too close (a resting body gets up) must
     * be apart again inside T4's one second.
     */
    static final int UNSTACK_AFTER_TICKS = 10;
    /** An unstacking member steers toward a point this far away from the members inside its hard minimum. */
    static final double UNSTACK_REACH = 3.0D;
    /** Speed modifier of an unstacking member. */
    static final double UNSTACK_SPEED = 0.6D;
    /** Spec "Leader blocked": the leader is held at the same standing member for this many ticks, then it asks it to make way. */
    static final int LEADER_BLOCKED_TICKS = 10;
    /**
     * Playtest 2 fix P8b (traffic causes R1, R5): a follower held by the same lower-ranked standing member,
     * by the guard or by a yield to it, for this many ticks asks it to make way ({@link #makeWay}). Twice the
     * leader's 10: a follower queues behind stop-and-go members all the time, and a short stop must not send
     * them sideways. Well under {@link ContextSteering#MAX_WAIT_TICKS} (160), the last resort before a path.
     */
    static final int MAKE_WAY_ASK_TICKS = 20;
    /**
     * The blocking member steps sideways until it is the hard minimum plus this margin beside the walker's
     * line (2.7 for Gallimimus in the open, 2.2 in tight ground). The spec's fixed 2 blocks lie inside the
     * 2.2-block minimum of lead decision 4, so the walker would stop at the member again.
     */
    static final double SIDE_STEP_MARGIN = 0.5D;
    /** Speed modifier of a side-stepping member. */
    static final double SIDE_STEP_SPEED = 0.6D;
    /** A side step gives up after this many ticks. */
    static final int SIDE_STEP_MAX_TICKS = 40;
    /** A side step that steering has stood on (no open heading) for this many ticks in a row has no room and ends. */
    static final int SIDE_STEP_STUCK_TICKS = 10;
    /**
     * A member asked to make way is not asked again for this long: one side step
     * ({@link #SIDE_STEP_MAX_TICKS}) plus one ask, so the asker has walked on before anyone asks the member
     * again, and two members can never ping-pong requests inside a side step.
     */
    static final int MAKE_WAY_COOLDOWN_TICKS = SIDE_STEP_MAX_TICKS + MAKE_WAY_ASK_TICKS;
    /** Debug log lines for this walker's make-way asks come at most this often (P13a fix round 1, item 7). Debug only. */
    static final int MAKE_WAY_LOG_MIN_TICKS = 100;
    /**
     * Tight ground ({@link #tightGround}) is re-measured once the body has moved this far since the measure
     * (a standing member keeps its measure): at most two {@code BodyFit.sideRoom} calls (7-8 stand probes each
     * in the open, 1-2 in a passage) per member per refresh, never per steering probe. 2 blocks: a walker at
     * the catch-up speed (0.19 blocks per tick) refreshes about every 10 ticks, at the graze speed every 40.
     */
    static final double TIGHT_REFRESH_DISTANCE = 2.0D;
    /**
     * After a herd fallback path ends, an unstack is not armed for this many ticks: the body ghosted through
     * its herd mates ({@link #ghosting}) and must not fight them the moment it stops; vanilla pushing and the
     * ordinary masks part them, and a pair still inside the minimum after the grace unstacks as usual.
     */
    static final int GHOST_GRACE_TICKS = 20;
    /**
     * An unstack gives up after this many ticks (a stack the masks cannot leave, such as a corner). If the
     * member is still crowded it starts again after another {@link #UNSTACK_AFTER_TICKS}.
     */
    static final int UNSTACK_MAX_TICKS = 40;

    private final Dinosaur dinosaur;
    private final boolean[] masked = new boolean[ContextSteering.HEADINGS];
    /** Headings open to blocks that a member's hard minimum masked in the current evaluation. */
    private final boolean[] crowded = new boolean[ContextSteering.HEADINGS];
    /** Per crowded heading, the index in that evaluation's {@link HerdBodies} of the nearest member that masked it. */
    private final int[] crowder = new int[ContextSteering.HEADINGS];
    private final double[] danger = new double[ContextSteering.HEADINGS];
    /** Headings probed 2 and 3 blocks ahead in the current evaluation. */
    private final boolean[] deep = new boolean[ContextSteering.HEADINGS];
    /** Feet height the box stands at {@link #PROBE_NEAR} ahead per heading (NaN: cannot stand); the mid probe starts from it. */
    private final double[] nearY = new double[ContextSteering.HEADINGS];
    private boolean steering;
    private double targetX;
    private double targetZ;
    private double targetSpeed;
    private int heading = ContextSteering.STOP;
    /**
     * How far the last evaluation found the picked heading free of blocks ({@link #PROBE_MID} or
     * {@link #PROBE_FAR}). The members' part of the free distance is measured every tick ({@link #moveToward}).
     */
    private double blockFree = PROBE_FAR;
    /** The unit direction of the guard's member test this tick ({@link #guardDirection}); the line a held walker asks the blocker to leave. */
    private double guardDirX;
    private double guardDirZ = 1D;
    /**
     * The last evaluation stopped for members: a heading only members masked would otherwise have been
     * walked ({@link ContextSteering#yielding}). Stopped for blocks otherwise.
     */
    private boolean yieldingOnly;
    /** The member the last evaluation yielded to (it masked the heading that would have won), or {@code null}; with the heading's direction. */
    private @Nullable UUID yieldBlocker;
    private double yieldDirX;
    private double yieldDirZ;
    /**
     * Ticks in a row steering picked {@link ContextSteering#STOP} or stood matched behind a mate
     * ({@link #matchStanding}), against {@link ContextSteering#MAX_WAIT_TICKS}.
     */
    private int stopTicks;
    /**
     * Playtest 4 fix P12b, fix round 1: this tick steering walks a heading but stands, because speed matching
     * ({@link ContextSteering#matchedSpeed}) holds it behind a moving mate slower than the walk floor. A wait
     * for members like a yield: counted in {@link #stopTicks} against {@link ContextSteering#MAX_WAIT_TICKS},
     * then blocked. Set by {@link #moveToward}, cleared at the start of every {@link #tick}.
     */
    private boolean matchStanding;
    /** Game time of this member's last evaluation; {@link DinoGroup#UNSET} before the first (then staggered by id). */
    private long lastEvaluationTick = DinoGroup.UNSET;
    /** The heading the guard stopped at blocks since the last evaluation, or {@link ContextSteering#STOP}: masked next time. */
    private int walledHeading = ContextSteering.STOP;
    /** The guard stopped the body at blocks this tick. */
    private boolean guardWall;
    /** The guard stopped the body at blocks this tick on the heading it walks (the yaw within half a step of it). */
    private boolean wallOnHeading;
    private boolean waiting;
    private int blockedTicks;
    private long lastProgressTick;
    private int wallBlockedTicks;
    /** Ticks in a row the guard held the body on a fallback path, against {@link #PATH_HOLD_TICKS}. */
    private int pathHoldTicks;
    /**
     * The goal of the fallback path the follower asked for ({@link #fallbackRequested}), or {@code null}: the
     * hold exit applies only to a path toward it. Dropped once no walk is wanted any more (the walk behaviour
     * cleared the walk target: the path is done, failed or was stopped) or another path replaced it.
     */
    private @Nullable BlockPos fallbackGoal;
    /** Index in this tick's {@link HerdBodies} of the member the guard stopped for, or -1. */
    private int guardBlocker = -1;
    /** This tick the member walks its herd fallback path: herd mates do not stop it ({@link #ghosting}). */
    private boolean ghosting;
    /** Game time until which no unstack is armed: the path ends ({@link #GHOST_GRACE_TICKS}); {@link DinoGroup#UNSET} never ghosted. */
    private long ghostGraceUntil = DinoGroup.UNSET;
    /** Whether the ground across the body was tight at the last measure ({@link #refreshTightGround}). */
    private boolean tightGround;
    /** Whether tight ground was ever measured, and where. */
    private boolean tightMeasured;
    private double tightMeasuredX;
    private double tightMeasuredZ;
    private @Nullable ClearanceBox box;
    /** Game time the clearance box was built. */
    private long boxBuiltTick;
    /** Ticks in a row inside a higher rank's hard minimum, counted up to {@link #UNSTACK_AFTER_TICKS}. */
    private int crowdedTicks;
    private boolean unstacking;
    /** Ticks the current unstack has run, against {@link #UNSTACK_MAX_TICKS}. */
    private int unstackTicks;
    private boolean sideStepping;
    private double sideX;
    private double sideZ;
    private int sideTicks;
    /** Ticks in a row this walker was held (guard or yield) by {@link #makeWayBlocker}, a lower-ranked standing member. */
    private int makeWayTicks;
    private @Nullable UUID makeWayBlocker;
    /** The last tick this walker logged a make-way ask ({@link #MAKE_WAY_LOG_MIN_TICKS}). */
    private long makeWayLogTick = DinoGroup.UNSET;
    /** Game time this member was last asked to make way ({@link #askToMakeWay}); {@link DinoGroup#UNSET} never. */
    private long askedTick = DinoGroup.UNSET;
    /** The target of the latest {@link #moveToward} (follower steering, unstack or side step), for the debug visuals. */
    private double lastTargetX;
    private double lastTargetZ;
    /** Game time and test point of the guard's latest stop at blocks ({@link DinoGroup#UNSET}: never), for the debug visuals. */
    private long guardWallTick = DinoGroup.UNSET;
    private double guardWallX;
    private double guardWallY;
    private double guardWallZ;

    /** What the herd layer is doing with the body this tick, for the debug visuals ({@link #debugState}). */
    enum DebugState {
        /** Walking a heading, or standing at its target. */
        WALKING,
        /** Standing on purpose: held back (speed 0) or yielding to members. */
        WAITING,
        /** Held by a lower-ranked standing herd mate it asks to make way ({@link #makeWay}), on a heading or yielding. */
        ASKING,
        /** Stopped by blocks: every heading masked, or the guard stopped the body at blocks this tick. */
        BLOCKED
    }

    public HerdMoveControl(Dinosaur dinosaur) {
        super(dinosaur);
        this.dinosaur = dinosaur;
    }

    /**
     * Steer toward (x, z) at the speed modifier {@code speed} until {@link #stopSteering}. Called every tick
     * by the follower behaviour. Speed 0 means "wait where you stand" (counts as waiting).
     */
    public void steerTo(double x, double z, double speed) {
        this.steering = true;
        this.targetX = x;
        this.targetZ = z;
        this.targetSpeed = speed;
    }

    /**
     * The follower no longer wants to steer. While an unstack or side step runs (they take priority over
     * follower steering for their duration) only the request is dropped: the heading and the counts are
     * theirs, and a reset every tick would cancel their move. They reset them when they end.
     */
    public void stopSteering() {
        boolean wasSteering = this.steering;
        this.steering = false;
        if (overriding()) {
            return;
        }
        if (wasSteering) {
            this.operation = Operation.WAIT;
        }
        resetHeading();
    }

    /** {@code true} while an unstack or side step moves the animal instead of follower steering. */
    private boolean overriding() {
        return this.unstacking || this.sideStepping;
    }

    /** Forget the heading and the counts of whatever moved the animal, so the next mover starts afresh. */
    private void resetHeading() {
        this.heading = ContextSteering.STOP;
        this.walledHeading = ContextSteering.STOP;
        this.waiting = false;
        this.yieldingOnly = false;
        this.yieldBlocker = null;
        this.blockedTicks = 0;
        this.stopTicks = 0;
    }

    /** The unstack ended: stop, unless a side step is running too (it keeps the heading and goes on). */
    private void endUnstack() {
        this.unstacking = false;
        this.unstackTicks = 0;
        if (!this.sideStepping) {
            resetHeading();
            this.operation = Operation.WAIT;
        }
    }

    /** The side step ended: stop and start afresh (an unstack that is still due re-arms next tick). */
    private void endSideStep() {
        this.sideStepping = false;
        resetHeading();
        this.operation = Operation.WAIT;
    }

    /**
     * A path, or the loss of the group, cancels every override. The heading and the counts are reset as
     * {@link #stopSteering} would have (it returns early during an override), so a blocked count from the
     * override does not read as stuck after the path; the operation is left to navigation.
     */
    private void cancelOverrides() {
        if (overriding()) {
            this.unstacking = false;
            this.unstackTicks = 0;
            this.sideStepping = false;
            resetHeading();
        }
    }

    /**
     * {@code true} while the herd layer moves the animal: follower steering that walks a heading (not a
     * speed-0 wait, not a stop pick: standing on purpose is not self-propelled), an unstack or a side step.
     * Read by the stamina rule in {@code Dinosaur} (such moves are self-propelled).
     */
    public boolean isSteering() {
        return this.steering && !this.waiting && this.heading != ContextSteering.STOP || overriding();
    }

    /**
     * {@code true} only while something means to move the body (a path in progress, a wanted position, or
     * {@link #isSteering}) <b>and</b> the body moves ({@link GroundSpeed#moving}: the stored velocity above the
     * walking threshold). The stamina rule in {@code Dinosaur} drains on this (playtest 2 fix P8a, traffic
     * cause R6): a body the guard holds (speed 0, velocity cut each tick) stops counting within a tick, so a
     * jammed member does not tire and lie down in the jam; a shove from a herd mate is velocity without
     * intent and does not count either.
     */
    public boolean isSelfPropelled() {
        boolean intends = this.mob.getNavigation().isInProgress() || hasWanted() || isSteering();
        Vec3 velocity = this.mob.getDeltaMovement();
        // zza is last tick's forward input after the guard: 0 for a held body, so a shove while held is no drain.
        return intends && this.mob.zza > 0F && GroundSpeed.moving(velocity.x, velocity.z);
    }

    /**
     * The follower was granted a fallback path to {@code goal} ({@code FollowFormation.requestFallback}): the
     * walk the behaviour starts for it is a herd fallback path, the only kind the hold exit ends
     * ({@link #holdPath}). Need walks (water, food, a feeder) and the leader's journey walks keep vanilla's
     * exit (the walk behaviour's run limit): they have no budget or cooldown, so a hold exit would give them a
     * new path every {@link #PATH_HOLD_TICKS}, and an urgent journey would "arrive" at a single hold.
     */
    void fallbackRequested(BlockPos goal) {
        this.fallbackGoal = goal;
        this.pathHoldTicks = 0;
    }

    /** Idle and fully up: no steering while it sits down ({@code isIdle} ignores that) or during the get-up animation. */
    private boolean standingIdle() {
        return this.dinosaur.isIdle() && !this.dinosaur.isSitting() && !this.dinosaur.isRising();
    }

    /**
     * Playtest 4 fix P12b (lead decision): {@code true} while the animal sits down (the ~69-tick sit phase of
     * {@code Rest}) or rests. It then stands still: the move control holds speed 0 and runs no steering or
     * override, and crowding never clears its rest state (herd mates walk round it as round any standing body;
     * only the leader's make-way ask gets a resting member up, {@link #askToMakeWay}).
     * <p>
     * The hold ({@link #tick}) is a deliberate rule outside {@link HerdSpacing#applies} (fix round 1 ruling):
     * it runs for every dinosaur, in a herd or not, because an animal that lies down must not walk. The
     * spacing switch still gates all herd work (steering, guard, unstack, side steps).
     */
    private boolean lyingDown() {
        return this.dinosaur.isSitting() || this.dinosaur.isResting();
    }

    /**
     * {@code true} if the animal stood this tick on purpose: held back (speed 0) or yielding to members
     * (for at most {@link ContextSteering#MAX_WAIT_TICKS} in a row).
     */
    public boolean isWaiting() {
        return this.waiting;
    }

    /**
     * Ticks in a row that steering stood because of blocks, not members: every heading masked by blocks, the
     * guard stopping the body at blocks on the heading it walks, or a yield to members that outlasted
     * {@link ContextSteering#MAX_WAIT_TICKS} (counted from then on).
     */
    public int blockedTicks() {
        return this.blockedTicks;
    }

    /**
     * {@code true} while steering stands for members (a yield, or a matched stand, {@link #matchStanding}) and
     * has done so longer than {@link ContextSteering#MAX_WAIT_TICKS}: the ticks {@link #blockedTicks} counts now come from the
     * time-out, not from blocks. The follower ignores such a stop while the leader waits or settles.
     */
    public boolean yieldTimedOut() {
        return (this.heading == ContextSteering.STOP && this.yieldingOnly || this.matchStanding)
                && this.stopTicks > ContextSteering.MAX_WAIT_TICKS;
    }

    /** Game time of the last progress, wait or path move. Public for GameTests. */
    public long lastProgressTick() {
        return this.lastProgressTick;
    }

    void markProgress(long gameTime) {
        this.lastProgressTick = gameTime;
    }

    /** Ticks in a row the guard stopped the animal at blocks (a wall or trunk), in every mode. Public for GameTests. */
    public int wallBlockedTicks() {
        return this.wallBlockedTicks;
    }

    /**
     * Read-only, for {@code HerdDebugVisuals}: what the herd layer does with the body, or {@code null} while
     * it does nothing (no steering request, no unstack, no side step: a path or idling moves the animal). A
     * guard stop at blocks shows as blocked whatever the heading; a hold by a lower-ranked standing member
     * (the make-way count runs) shows as asking; a stop pick is waiting when steering counted it as such,
     * blocked otherwise; a body that has arrived (stop pick, nothing counted) walks.
     */
    @Nullable DebugState debugState() {
        if (!this.steering && !overriding()) {
            return null;
        }
        if (this.guardWall || this.heading == ContextSteering.STOP && !this.waiting && this.blockedTicks > 0) {
            return DebugState.BLOCKED;
        }
        if (this.makeWayTicks > 0) {
            return DebugState.ASKING;
        }
        return this.waiting ? DebugState.WAITING : DebugState.WALKING;
    }

    /** Read-only: this tick the member walks its herd fallback path and herd mates do not stop it (lead decision, playtest 2 fix P8b). */
    boolean ghosting() {
        return this.ghosting;
    }

    /**
     * {@code true} while the member ghosts through herd mates ({@link #ghosting}) or is within
     * {@link #GHOST_GRACE_TICKS} after its fallback path ended: the lead allowed such pairs inside the hard
     * minimum, so the spacing GameTests leave them out (T4 is measured on the other pairs). Public for GameTests.
     */
    public boolean ghostExempt() {
        return this.ghosting || this.mob.level().getGameTime() < this.ghostGraceUntil;
    }

    /**
     * {@code true} while something means to move the body: a path in progress, an unstack or side step, or
     * follower steering that walks a heading or yields to a member (a yield is a wish to move). {@code false}
     * when it stands at its target, is held back on purpose (speed 0), is stopped by blocks, rests or eats
     * (no steering then). Carried in the herd's snapshot for {@link HerdSpacing#asksToMakeWay}: a standing
     * member that does not want to move is asked to make way by a walker of any rank.
     */
    boolean wantsToMove() {
        return this.mob.getNavigation().isInProgress() || overriding()
                || this.steering && (this.heading != ContextSteering.STOP && !this.waiting || this.yieldingOnly);
    }

    /**
     * Whether the ground across this body was tight at the last measure ({@link #refreshTightGround}): neither
     * side has {@link HerdSpacing#tightThreshold} of room for its clearance box. The hard minimum to every
     * herd mate is then the tight one ({@link HerdSpacing#hardMinimum}). Open before the first measure (a
     * member that just joined). Public for GameTests ({@link HerdSpacing#hardMinimumOf}).
     */
    public boolean tightGround() {
        return this.tightGround;
    }

    /**
     * Read-only: a side step (a make-way step out of a walker's line) is in progress, its wait through a
     * get-up included. {@code HerdNeeds.mayRest} starts no rest meanwhile (playtest 4 fix P12b, fix round 2).
     */
    boolean sideStepping() {
        return this.sideStepping;
    }

    /** Read-only: the heading index steering walks, or {@link ContextSteering#STOP}. */
    int heading() {
        return this.heading;
    }

    /** Read-only: x of the latest {@link #moveToward} target (meaningful while {@link #debugState} is not {@code null}). */
    double lastTargetX() {
        return this.lastTargetX;
    }

    /** Read-only: z of the latest {@link #moveToward} target. */
    double lastTargetZ() {
        return this.lastTargetZ;
    }

    /** Read-only: game time of the guard's latest stop at blocks, {@link DinoGroup#UNSET} if it never stopped at blocks. */
    long guardWallTick() {
        return this.guardWallTick;
    }

    /** Read-only: x of the guard's test point at its latest stop at blocks (the box's bottom centre). */
    double guardWallX() {
        return this.guardWallX;
    }

    /** Read-only: feet height of the guard's test at its latest stop at blocks. */
    double guardWallY() {
        return this.guardWallY;
    }

    /** Read-only: z of the guard's test point at its latest stop at blocks. */
    double guardWallZ() {
        return this.guardWallZ;
    }

    /**
     * Spec "Leader blocked" and Review Focus 7: step to (x, z), beside the leader's line. A side step itself
     * never gets a sitting or resting animal up (playtest 4 fix P12b): only the leader's make-way ask gets a
     * resting member up, and never during its sit-down phase ({@link #askToMakeWay}); an override that is
     * running ends when the animal sits down ({@link #tick}).
     */
    public void requestSideStep(double x, double z) {
        this.sideStepping = true;
        this.sideX = x;
        this.sideZ = z;
        this.sideTicks = 0;
        // Afresh: the counts of the steering it overrides (a long yield) must not end it as stuck at once.
        resetHeading();
    }

    /**
     * Step out of the line through (lineX, lineZ) along the unit direction (dirX, dirZ): sideways,
     * perpendicular to the line, until the body is {@code clear} beside the line, so one step clears the
     * way. On this body's own side ({@link HerdSpacing#sideStep}) unless that side has less room than the
     * step needs and crossing the line ends clearer of it ({@link HerdSpacing#crossesLine}: the side with
     * room, playtest 2 fix P8b; at most two {@code BodyFit.sideRoom} measures per request); a step goes no
     * farther than the room found on its side (fix P12c for its own side, {@link HerdSpacing#ownSideReach}: the
     * room ends at a wall or a rim). The target lies one {@link #ARRIVED} beyond that, because
     * steering stops ARRIVED short of its target. A walker's guard uses
     * the direction of its member test as the line ({@link #guardDirection}), a yield the heading it yielded
     * on; a journey start the line to the journey target.
     *
     * @return {@code false} if the body already stands {@code clear} beside the line (nothing requested)
     */
    public boolean requestSideStepOutOf(double lineX, double lineZ, double dirX, double dirZ, double clear) {
        double x = this.mob.getX();
        double z = this.mob.getZ();
        double dx = x - lineX;
        double dz = z - lineZ;
        double lateral = Math.abs(dz * dirX - dx * dirZ);
        if (lateral >= clear) {
            return false;
        }
        Vec3 side = HerdSpacing.sideStep(dirX, dirZ, dx, dz);
        // How far the body must get on its own side; the target lies ARRIVED beyond it.
        double needed = clear - lateral;
        double reach = needed + ARRIVED;
        DinoGroup group = GroupManager.groupOf(this.dinosaur);
        if (group != null && this.mob.level() instanceof ServerLevel level) {
            BodyFit fit = group.fit();
            ClearanceBox box = box();
            float ownRoom = fit.sideRoom(level, this.mob, box, x, this.mob.getY(), z, side.x, side.z, needed);
            // Fix P12c: on its own side the step goes no farther than the room there (a wall, a rim).
            reach = HerdSpacing.ownSideReach(needed, ownRoom, ARRIVED);
            if (ownRoom < needed) {
                // Crossing the line: from lateral on this side to clear on the other.
                double crossNeeded = clear + lateral;
                float otherRoom = fit.sideRoom(level, this.mob, box, x, this.mob.getY(), z, -side.x, -side.z, crossNeeded);
                if (HerdSpacing.crossesLine(ownRoom, otherRoom, lateral, needed)) {
                    side = new Vec3(-side.x, 0D, -side.z);
                    reach = Math.min(crossNeeded, otherRoom) + ARRIVED;
                }
            }
        }
        requestSideStep(x + side.x * reach, z + side.z * reach);
        return true;
    }

    /**
     * A walker held by this standing member asks it to step out of its line ({@link #makeWay}): honoured at
     * most once per {@link #MAKE_WAY_COOLDOWN_TICKS}, never during a side step or a path of this member's
     * own, through {@link #requestSideStepOutOf}. A member that sits down or rests answers only by
     * {@link HerdBodies#answersMakeWay} (playtest 4 fix P12b, fix round 1): the leader's ask gets a resting
     * member up (its resting memory is cleared, so {@code Rest} stops and the getting-up behaviour plays; the
     * side step waits for it, {@link #continueSideStep}), once its sit-down phase has ended. A follower's ask
     * never gets it up: the follower goes round it, waits, or after the yield time-out walks a fallback path.
     * A refusal for lying down does not start the cooldown.
     *
     * @param byLeader the walker that asks is the herd leader
     * @return {@code true} if a side step was requested
     */
    boolean askToMakeWay(long now, double lineX, double lineZ, double dirX, double dirZ, double clear, boolean byLeader) {
        if (!HerdBodies.answersMakeWay(this.dinosaur.isSitting(), this.dinosaur.isResting(), byLeader)) {
            return false;
        }
        if (this.askedTick != DinoGroup.UNSET && now - this.askedTick < MAKE_WAY_COOLDOWN_TICKS) {
            return false;
        }
        if (this.sideStepping || this.mob.getNavigation().isInProgress()) {
            return false;
        }
        this.askedTick = now;
        boolean requested = requestSideStepOutOf(lineX, lineZ, dirX, dirZ, clear);
        if (requested && this.dinosaur.isResting()) {
            // The leader's ask (answersMakeWay): get up first, as before P12b. Rest stops on the brain's next
            // tick, which runs before this move control's next tick, so tick() no longer sees it lying down.
            BrainUtils.clearMemory(this.dinosaur, MemoryModuleTypeInit.IS_RESTING.get());
        }
        return requested;
    }

    /** This animal's clearance box, rebuilt every {@link #BOX_REFRESH_TICKS} game ticks. */
    ClearanceBox box() {
        long now = this.mob.level().getGameTime();
        if (this.box == null || now - this.boxBuiltTick >= BOX_REFRESH_TICKS) {
            this.box = BodyFit.boxOf(this.dinosaur);
            this.boxBuiltTick = now;
        }
        return this.box;
    }

    @Override
    public void tick() {
        ServerLevel level = (ServerLevel) this.mob.level();
        long start = System.nanoTime();
        level.getProfiler().push("herdMove");
        this.matchStanding = false;
        if (this.mob.getNavigation().isInProgress()) {
            // A path wins over steering, unstacking and side steps alike.
            this.steering = false;
            this.waiting = false;
            cancelOverrides();
        }
        // Deliberately outside HerdSpacing.applies (lyingDown): every dinosaur that lies down stands still.
        boolean lying = lyingDown();
        if (lying) {
            // P12b: sitting down or resting, the animal stands still; an override must not walk it afterwards.
            cancelOverrides();
        }
        DinoGroup group = HerdSpacing.applies(this.dinosaur) ? GroupManager.groupOf(this.dinosaur) : null;
        HerdBodies bodies = group == null ? null : GroupManager.bodiesOf(group, level);
        int self = bodies == null ? -1 : bodies.indexOf(this.dinosaur.getUUID());
        if (self >= 0) {
            refreshTightGround(level, group);
            updateUnstack(level, bodies, self);
            // Lying down: no steering either (FollowFormation has stopped; the hold below covers anything else).
            if (!lying) {
                if (this.sideStepping) {
                    continueSideStep(level, group, bodies, self);
                } else if (this.unstacking) {
                    unstack(level, group, bodies, self);
                } else if (this.steering) {
                    moveToward(level, group, bodies, self, this.targetX, this.targetZ, this.targetSpeed);
                }
            }
        }
        if (lying) {
            // Holds speed 0 whatever set a wanted position (a need walk's path included): vanilla WAIT sets no input.
            this.operation = Operation.WAIT;
        }
        // The move direction for the guard's member test, read before vanilla consumes the wanted position.
        boolean moves = this.operation == Operation.MOVE_TO;
        double moveX = this.wantedX - this.mob.getX();
        double moveZ = this.wantedZ - this.mob.getZ();
        super.tick();
        if (self >= 0) {
            this.ghosting = onFallbackPath();
            if (this.ghosting) {
                this.ghostGraceUntil = level.getGameTime() + GHOST_GRACE_TICKS;
            }
            guardDirection(moves, moveX, moveZ);
            boolean facesNextNode = moves && ContextSteering.facesDirection(this.mob.getYRot(), moveX, moveZ, GUARD_PATH_TRUST_DEGREES);
            guard(level, group, bodies, self, this.mob.getNavigation().isInProgress(), facesNextNode);
            makeWay(level, group, bodies, self);
            holdPath(level, group, bodies);
        } else {
            this.wallBlockedTicks = 0;
            this.pathHoldTicks = 0;
            this.guardBlocker = -1;
            this.guardWall = false;
            this.wallOnHeading = false;
            this.ghosting = false;
            this.makeWayTicks = 0;
            this.makeWayBlocker = null;
            cancelOverrides();
        }
        if ((this.steering || overriding()) && this.heading != ContextSteering.STOP && !this.matchStanding) {
            // Walking a heading the probes called open: a guard stop at blocks on it (a trunk corner on a
            // diagonal) still counts as blocked (lead decision 14); a free step clears the count. A stop while
            // the yaw still lags the heading is neither (the stop was not on this heading): the count holds.
            // A matched stand settled the count in moveToward (P12b fix round 1).
            if (!this.guardWall) {
                this.blockedTicks = 0;
            } else if (this.wallOnHeading) {
                this.blockedTicks++;
            }
        }
        Vec3 velocity = this.mob.getDeltaMovement();
        if (this.mob.getNavigation().isInProgress() && GroundSpeed.moving(velocity.x, velocity.z)) {
            // Progress on a path: the body moves (a held body coasts under the threshold within a tick).
            markProgress(level.getGameTime());
        }
        level.getProfiler().pop();
        HerdTimings.record(level.getGameTime(), System.nanoTime() - start);
    }

    /** Ticks between two evaluations of one member of a herd this size: the cap spread over the whole herd. */
    static int cadence(int groupSize) {
        int spread = (groupSize + DinoGroup.MAX_EVALUATIONS_PER_TICK - 1) / DinoGroup.MAX_EVALUATIONS_PER_TICK;
        return Math.max(EVALUATE_EVERY_TICKS, spread);
    }

    /** The ground speed of the slowest walk ({@link GroundSpeed#MIN_WALK_MODIFIER}) for this attribute. */
    private static double walkFloor(double attribute) {
        return GroundSpeed.groundSpeed(GroundSpeed.MIN_WALK_MODIFIER, attribute);
    }

    /**
     * The walker's move per tick along the unit direction (hx, hz) for the member masks: {@code fullPerTick},
     * or, when the nearest member ahead on that line moves by the masks' own rule
     * ({@link HerdBodies#predictedMoving}), the speed matched to it ({@link ContextSteering#matchedSpeed},
     * playtest 4 fix P12b) as the body will really walk it (0 to stand, else at least the floor). So the
     * heading behind a slower mate stays open while the walker slows, instead of being masked by a
     * prediction at full speed (motion cause 2). One pass over the snapshot per heading.
     */
    private static double maskedSpeed(HerdBodies bodies, int self, double hx, double hz, double fullPerTick, double attribute) {
        int ahead = bodies.nearestAhead(self, hx, hz, ContextSteering.MATCH_RANGE);
        if (ahead < 0 || !bodies.predictedMoving(self, ahead)) {
            return fullPerTick;
        }
        double matched = ContextSteering.matchedSpeed(fullPerTick, bodies.speedAlong(ahead, hx, hz),
                bodies.freeTo(self, ahead, hx, hz), walkFloor(attribute));
        return GroundSpeed.groundSpeed(GroundSpeed.walkModifier(GroundSpeed.modifierFor(matched, attribute)), attribute);
    }

    /** One tick of context steering toward (targetX, targetZ): evaluate when due, then walk the heading. */
    private void moveToward(ServerLevel level, DinoGroup group, HerdBodies bodies, int self,
                            double targetX, double targetZ, double speed) {
        this.lastTargetX = targetX;
        this.lastTargetZ = targetZ;
        if (GroundSpeed.walkModifier(speed) <= 0D) {
            // Held back (speed 0, or under the stop threshold: the slow-down factors only lower it): waiting,
            // wherever the body stands (checked before arrival, so it reports as waiting). A deliberate wait,
            // not a yield: it does not count against MAX_WAIT_TICKS.
            this.waiting = true;
            this.yieldBlocker = null;
            this.blockedTicks = 0;
            this.stopTicks = 0;
            this.operation = Operation.WAIT;
            return;
        }
        double dx = targetX - this.mob.getX();
        double dz = targetZ - this.mob.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < ARRIVED) {
            this.heading = ContextSteering.STOP;
            this.waiting = false;
            this.yieldingOnly = false;
            this.yieldBlocker = null;
            this.blockedTicks = 0;
            this.stopTicks = 0;
            this.operation = Operation.WAIT;
            return;
        }
        long now = level.getGameTime();
        int cadence = cadence(group.size());
        if (this.lastEvaluationTick == DinoGroup.UNSET) {
            // The first evaluation is staggered by entity id, so a herd that spawned together does not all
            // evaluate on the same tick.
            this.lastEvaluationTick = now - cadence + Math.floorMod(this.dinosaur.getId(), cadence);
        }
        // Due from one cadence after the last evaluation until one happens: a member the herd's cap refused
        // stays due, so the members that tick last are not starved. A stopped member re-evaluates on the
        // same cadence, not every tick.
        if (now - this.lastEvaluationTick >= cadence && group.tryEvaluate(now)) {
            this.lastEvaluationTick = now;
            this.heading = evaluate(level, group, bodies, self, dx, dz, speed);
        }
        if (this.heading == ContextSteering.STOP) {
            // Stopped by members (a heading they alone masked would have been walked): waiting, not stuck.
            // Stopped by blocks, or standing longer than MAX_WAIT_TICKS whatever the cause: not waiting, and
            // counted, so the follower asks for a fallback path (decision 14: blocked by blocks is stuck; a
            // yield that never ends is a deadlock only a path breaks). The count runs over every stop tick,
            // so evaluations that alternate between the two causes cannot hold both counts at zero.
            this.stopTicks++;
            this.waiting = ContextSteering.waiting(this.yieldingOnly, this.stopTicks);
            this.blockedTicks = this.waiting ? 0 : this.blockedTicks + 1;
            this.operation = Operation.WAIT;
            return;
        }
        // stopTicks is settled below: a matched stand counts on, an easing-in stand or a walk clears it.
        double attribute = this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double hx = ContextSteering.dirX(this.heading);
        double hz = ContextSteering.dirZ(this.heading);
        // The free distance every tick (P12b; before, only at evaluations, so the slow-down stepped): blocks
        // from the last evaluation, members from this tick's snapshot (one pass).
        int ahead = bodies.nearestAhead(self, hx, hz, ContextSteering.MATCH_RANGE);
        double memberFree = ahead < 0 ? ContextSteering.MATCH_RANGE : bodies.freeTo(self, ahead, hx, hz);
        double free = Math.min(this.blockFree, Math.min(PROBE_FAR, memberFree));
        // The slow-down factors are fractions of the ground speed, so the modifier takes their square root.
        double ground = GroundSpeed.groundSpeed(speed * GroundSpeed.modifierFactor(ContextSteering.speedFactor(distance, free)), attribute);
        boolean matchHolds = false;
        if (ahead >= 0 && bodies.predictedMoving(self, ahead)) {
            // P12b: closing on a moving mate ahead, match its speed early and smoothly (motion cause 2).
            // A stand only the matching causes (the unmatched speed would walk) is a wait for the mate.
            matchHolds = GroundSpeed.walkModifier(GroundSpeed.modifierFor(ground, attribute)) > 0D;
            ground = ContextSteering.matchedSpeed(ground, bodies.speedAlong(ahead, hx, hz), memberFree, walkFloor(attribute));
        }
        // Floored at the walk animation's modifier, or the body stands if it is under the stop threshold (a
        // follower easing into its slot behind a stopped leader, or matched behind a mate slower than the floor).
        double wanted = GroundSpeed.walkModifier(GroundSpeed.modifierFor(ground, attribute));
        if (wanted <= 0D) {
            if (matchHolds) {
                // P12b fix round 1: matched behind a mate slower than the floor, counted like a yield, so a mate
                // that never gets clear times the wait out (MAX_WAIT_TICKS) and the stuck rule takes over.
                this.matchStanding = true;
                this.stopTicks++;
                this.waiting = ContextSteering.waiting(true, this.stopTicks);
                this.blockedTicks = this.waiting ? 0 : this.blockedTicks + 1;
            } else {
                // Easing in: a deliberate wait, not counted.
                this.stopTicks = 0;
                this.waiting = true;
                this.blockedTicks = 0;
            }
            this.operation = Operation.WAIT;
            return;
        }
        this.stopTicks = 0;
        // blockedTicks is settled in tick() after the guard: a wall stop on this heading counts, a free step clears it.
        this.waiting = false;
        this.setWantedPosition(this.mob.getX() + ContextSteering.dirX(this.heading) * WANTED_AHEAD, this.mob.getY(),
                this.mob.getZ() + ContextSteering.dirZ(this.heading) * WANTED_AHEAD, wanted);
    }

    /**
     * Scores the 12 headings (spec steps 1-5) and returns the pick; also sets the pick's free distance from
     * blocks ({@link #blockFree}) for the speed.
     * Stage 1 probes every heading one block ahead and the members; the heading the guard stopped at blocks
     * since the last evaluation is masked without a probe. Stage 2 probes only the previous heading (if open)
     * and the {@link #DEEP_PROBES} best open headings two blocks ahead (mask) and three blocks ahead (danger);
     * the other open headings are not probed and count as masked. So a wall ahead still leaves the headings
     * along the wall, and a dead end still gives "stop", at a third of the probes. The probes chain heights
     * like {@link BodyFit#reachY}: the mid probe starts from the near probe's stand height and the far probe
     * from the mid one's, so a 1-in-1 rise (one step up per block) stays open.
     */
    private int evaluate(ServerLevel level, DinoGroup group, HerdBodies bodies, int self, double dx, double dz, double speed) {
        ClearanceBox box = box();
        BodyFit fit = group.fit();
        double x = this.mob.getX();
        double y = this.mob.getY();
        double z = this.mob.getZ();
        float yaw = this.mob.getYRot();
        // The walker's own move per tick for the member masks, by the same law as the measured velocities
        // of the others: the most it will walk this evaluation (the floor can lift a slow wanted speed; the
        // slow-down factors only lower it), matched per heading to a moving mate ahead (P12b, maskedSpeed).
        double attribute = this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double fullPerTick = GroundSpeed.groundSpeed(GroundSpeed.walkModifier(speed), attribute);
        double width = this.mob.getBbWidth();
        double spacing = HerdArea.grazeSpacing(HerdSpacing.personalSpace(width, width));
        // Spec danger "a member within the graze spacing" applies while grazing; travel slots are 3.2 to 4.2 apart (Gallimimus),
        // so in TRAVEL it would push every follower off the line behind the member ahead.
        boolean memberDanger = group.state() == HerdState.GRAZE;
        for (int h = 0; h < ContextSteering.HEADINGS; h++) {
            double hx = ContextSteering.dirX(h);
            double hz = ContextSteering.dirZ(h);
            // A guard wall stop is a block mask the probes missed (a corner between 0 and 1 block), so the
            // heading is not open and does not count as yielding.
            this.nearY[h] = h == this.walledHeading ? Double.NaN
                    : fit.standY(level, this.mob, box, x + hx * PROBE_NEAR, y, z + hz * PROBE_NEAR);
            boolean open = !Double.isNaN(this.nearY[h]);
            double perTick = open ? maskedSpeed(bodies, self, hx, hz, fullPerTick, attribute) : 0D;
            this.crowder[h] = open ? bodies.crowder(self, hx * perTick, hz * perTick) : -1;
            boolean crowded = this.crowder[h] >= 0;
            this.masked[h] = !open || crowded;
            this.crowded[h] = crowded;
            this.danger[h] = !this.masked[h] && memberDanger
                    && bodies.anyWithin(self, x + hx * PROBE_NEAR, z + hz * PROBE_NEAR, spacing)
                    ? ContextSteering.DANGER_NEAR_MEMBER : 0D;
            this.deep[h] = false;
        }
        this.walledHeading = ContextSteering.STOP;
        int next = this.heading >= 0 && !this.masked[this.heading] ? this.heading : -1;
        for (int n = 0; n <= DEEP_PROBES; n++) {
            if (next < 0) {
                double bestScore = Double.NEGATIVE_INFINITY;
                for (int h = 0; h < ContextSteering.HEADINGS; h++) {
                    if (!this.masked[h] && !this.deep[h]) {
                        double score = ContextSteering.interest(h, dx, dz, yaw) - this.danger[h];
                        if (score > bestScore) {
                            bestScore = score;
                            next = h;
                        }
                    }
                }
                if (next < 0) {
                    break;
                }
            }
            this.deep[next] = true;
            double hx = ContextSteering.dirX(next);
            double hz = ContextSteering.dirZ(next);
            double midY = fit.standY(level, this.mob, box, x + hx * PROBE_MID, this.nearY[next], z + hz * PROBE_MID);
            if (Double.isNaN(midY)) {
                this.masked[next] = true;
            } else if (!fit.canStand(level, this.mob, box, x + hx * PROBE_FAR, midY, z + hz * PROBE_FAR)) {
                this.danger[next] += ContextSteering.DANGER_BLOCKED_FAR;
            }
            next = -1;
        }
        for (int h = 0; h < ContextSteering.HEADINGS; h++) {
            if (!this.deep[h]) {
                this.masked[h] = true;
            }
        }
        int pick = ContextSteering.pick(dx, dz, yaw, this.heading, this.masked, this.danger);
        if (pick != ContextSteering.STOP) {
            this.blockFree = this.danger[pick] >= ContextSteering.DANGER_BLOCKED_FAR ? PROBE_MID : PROBE_FAR;
        }
        // A stop is a yield only if a member-masked heading would have won (lead decision 14). A crowded
        // heading was not probed deeper, so its score is its interest alone: the member is the nearer cause.
        // The member that masked that heading is the one the walker yields to (the make-way ask, P8b).
        int yieldHeading = pick == ContextSteering.STOP
                ? ContextSteering.yieldHeading(dx, dz, yaw, this.crowded, this.masked, this.danger) : ContextSteering.STOP;
        this.yieldingOnly = yieldHeading != ContextSteering.STOP;
        if (this.yieldingOnly) {
            this.yieldBlocker = bodies.id(this.crowder[yieldHeading]);
            this.yieldDirX = ContextSteering.dirX(yieldHeading);
            this.yieldDirZ = ContextSteering.dirZ(yieldHeading);
        } else {
            this.yieldBlocker = null;
        }
        return pick;
    }

    /**
     * Playtest 4 fix P12b (motion cause 3): the direction of the guard's member test this tick. The move
     * direction (toward the wanted position vanilla walked to: the steering heading's point or the next path
     * node) while the yaw is within {@link #GUARD_TURN_LIMIT_DEGREES} of it; the yaw otherwise, and when no
     * wanted position was set. During a heading change the yaw sweeps across ground between the two headings
     * for several ticks, and a mate's minimum there braked the body mid-turn; the heading itself was tested
     * free by the masks.
     */
    private void guardDirection(boolean moves, double moveX, double moveZ) {
        double yaw = Math.toRadians(this.mob.getYRot());
        this.guardDirX = -Math.sin(yaw);
        this.guardDirZ = Math.cos(yaw);
        double length = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (!moves || length < MOVE_DIRECTION_EPSILON
                || !ContextSteering.facesDirection(this.mob.getYRot(), moveX, moveZ, GUARD_TURN_LIMIT_DEGREES)) {
            return;
        }
        this.guardDirX = moveX / length;
        this.guardDirZ = moveZ / length;
    }

    /**
     * The guard (spec): no forward input this tick if the box 0.6 ahead along the real yaw does not fit, or
     * does not stand there (fix P12c, cliff cause 3: {@link BodyFit#canStand}, the centre column rule; a planned
     * drop of up to 3 blocks still stands), or the point 0.6 ahead along the move direction
     * ({@link #guardDirection}) enters a hard minimum. The ground is tested only for a body on the ground and
     * out of water, and on a vanilla path only while the yaw lags the next node by more than
     * {@link #GUARD_PATH_TRUST_DEGREES} ({@link GroundSupport#guardChecksGround}): the nodes passed the rim rules,
     * so a leader on a rim node or a ridge is never held; a held body keeps turning toward its node and walks on
     * once it faces it. The horizontal velocity is cut too: with friction alone a body coasts on 1.2
     * times its last move (0.23 blocks at the catch-up speed), a good part of the 0.6 the probe looked
     * ahead. A stop at blocks (a wall or a drop) while steering remembers the heading, so the next evaluation
     * masks it and picks another.
     *
     * @param onPath        a vanilla path is in progress (the leader's walks, need walks, fallback paths)
     * @param facesNextNode the yaw is within {@link #GUARD_PATH_TRUST_DEGREES} of the wanted position
     */
    private void guard(ServerLevel level, DinoGroup group, HerdBodies bodies, int self, boolean onPath, boolean facesNextNode) {
        this.guardBlocker = -1;
        this.guardWall = false;
        this.wallOnHeading = false;
        if (this.mob.zza <= 0F) {
            this.wallBlockedTicks = 0;
            return;
        }
        double yaw = Math.toRadians(this.mob.getYRot());
        double aheadX = this.mob.getX() - Math.sin(yaw) * GUARD_AHEAD;
        double aheadZ = this.mob.getZ() + Math.cos(yaw) * GUARD_AHEAD;
        // Blocks ahead: the box must fit, and (fix P12c) stand unless guardChecksGround exempts this tick.
        boolean clearAhead = group.fit().fits(level, this.mob, box(), aheadX, this.mob.getY(), aheadZ)
                && (!GroundSupport.guardChecksGround(this.mob.onGround() && !this.mob.isInWater(), onPath, facesNextNode)
                || group.fit().canStand(level, this.mob, box(), aheadX, this.mob.getY(), aheadZ));
        // Ghosting: on its herd fallback path the body passes herd mates (vanilla pushing keeps them apart).
        // Members along the move direction (P12b), so a turn's yaw does not brake the body for a mate it turns away from.
        int blocker = this.ghosting ? -1 : bodies.blockerAt(self, this.mob.getX() + this.guardDirX * GUARD_AHEAD,
                this.mob.getZ() + this.guardDirZ * GUARD_AHEAD);
        if (!clearAhead || blocker >= 0) {
            this.mob.setSpeed(0F);
            Vec3 velocity = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(velocity.x * GUARD_VELOCITY_FACTOR, velocity.y, velocity.z * GUARD_VELOCITY_FACTOR);
        }
        if (!clearAhead) {
            this.guardWall = true;
            this.guardWallTick = level.getGameTime();
            this.guardWallX = aheadX;
            this.guardWallY = this.mob.getY();
            this.guardWallZ = aheadZ;
            // Only a stop on the heading the body walks masks it (playtest 2 fix P8a, geometry cause 4): the
            // guard tests along the yaw, which lags a new heading by up to 30 degrees for about six ticks after
            // a pick (the eased turn, TurnEasing: 2, 4, 6, 8, 6.3, 3.7 degrees), so a stop during the turn (0.3
            // blocks beside the heading's line) says nothing about the heading; the next evaluation probes it
            // again instead of masking it blind.
            this.wallOnHeading = (this.steering || overriding()) && ContextSteering.walksHeading(this.mob.getYRot(), this.heading);
            if (this.wallOnHeading) {
                this.walledHeading = this.heading;
            }
        }
        this.wallBlockedTicks = clearAhead ? 0 : this.wallBlockedTicks + 1;
        this.guardBlocker = blocker;
    }

    /**
     * Whether the path in progress is the herd fallback path the follower asked for ({@link #fallbackGoal}:
     * the path's own target tells it from a need walk or a journey walk), with the goal's bookkeeping: it is
     * dropped when another path replaced the fallback, or when the walk ended (done, failed, or stopped by
     * {@link #holdPath}: no walk target is wanted any more). Settled once per tick, before the guard
     * ({@link #ghosting}).
     */
    private boolean onFallbackPath() {
        if (this.fallbackGoal == null) {
            return false;
        }
        Path path = this.mob.getNavigation().getPath();
        boolean inProgress = this.mob.getNavigation().isInProgress() && path != null;
        if (inProgress ? !this.fallbackGoal.equals(path.getTarget())
                : !BrainUtils.hasMemory(this.dinosaur, MemoryModuleType.WALK_TARGET)) {
            this.fallbackGoal = null;
            return false;
        }
        return inProgress;
    }

    /**
     * Playtest 2 fix P8a (geometry cause 3): a vanilla path the guard holds has no exit of its own. Vanilla's
     * stuck detection and node timeout scale by {@code getSpeed()}, which the guard sets to 0, so they never
     * fire, and only the walk behaviour's run limit (150-249 ticks) ended the walk. A <b>herd fallback path</b>
     * ({@link #onFallbackPath}; need walks and journey walks keep vanilla's exit) held {@link #PATH_HOLD_TICKS}
     * ticks in a row at blocks (herd mates never hold it: {@link #ghosting}), while the body faces its next
     * node ({@link ContextSteering#facesDirection}: turning ticks do not count), is stopped: the walk
     * behaviour ends on {@code isDone()}, the walk target is cleared, and the follower's stuck rule re-plans
     * from where the body stands. The member's fallback cooldown restarts ({@link FallbackBudget#holdEnded}),
     * so it does not ask for the same path again at once. The leader never walks fallback paths; its blocker
     * steps aside instead ({@link #makeWay}).
     */
    private void holdPath(ServerLevel level, DinoGroup group, HerdBodies bodies) {
        if (!this.ghosting || !this.guardWall) {
            this.pathHoldTicks = 0;
            return;
        }
        Path path = this.mob.getNavigation().getPath();
        Vec3 next = path.getNextEntityPos(this.mob);
        if (!ContextSteering.facesDirection(this.mob.getYRot(), next.x - this.mob.getX(), next.z - this.mob.getZ())) {
            // Still turning toward the node: the guard stop was on the arc, not a hold on the path. The count waits.
            return;
        }
        if (++this.pathHoldTicks >= PATH_HOLD_TICKS) {
            this.pathHoldTicks = 0;
            this.fallbackGoal = null;
            this.mob.getNavigation().stop();
            group.fallbacks().holdEnded(this.dinosaur.getUUID(), level.getGameTime());
        }
    }

    /**
     * Spec "Unstack": after {@link #UNSTACK_AFTER_TICKS} inside a higher rank's hard minimum, an idle member
     * steers away until clear; an eating member finishes first ({@code Eat} runs on a timer, and ending it
     * early would skip the meal). A member that sits down or rests ({@link #lyingDown}) stays down (playtest 4
     * fix P12b, replacing lead decision 7's get-up): crowding never clears its rest state, and herd mates walk
     * round it. Its count is kept, so once it is up and idle it unstacks at once if still crowded.
     */
    private void updateUnstack(ServerLevel level, HerdBodies bodies, int self) {
        // Not armed while ghosting through herd mates on a fallback path, nor for GHOST_GRACE_TICKS after it.
        boolean ghostGrace = level.getGameTime() < this.ghostGraceUntil;
        if (ghostGrace || bodies.crowdedBy(self) < 0) {
            this.crowdedTicks = 0;
            if (this.unstacking) {
                endUnstack();
            }
            return;
        }
        if (this.crowdedTicks < UNSTACK_AFTER_TICKS) {
            this.crowdedTicks++;
        }
        if (this.crowdedTicks < UNSTACK_AFTER_TICKS) {
            return;
        }
        if (lyingDown()) {
            // An unstack that was running ended in tick() (cancelOverrides); none starts while it lies down.
            return;
        }
        boolean next = standingIdle() && !this.mob.getNavigation().isInProgress();
        if (this.unstacking && !next) {
            endUnstack();
        }
        this.unstacking = next;
    }

    /**
     * Steer away from the members inside the hard minimum (masks as usual: moving apart is never masked).
     * After {@link #UNSTACK_MAX_TICKS} it gives up and the crowded count starts over.
     */
    private void unstack(ServerLevel level, DinoGroup group, HerdBodies bodies, int self) {
        List<HerdSpacing.Neighbour> inside = bodies.inside(self);
        Vec3 away = HerdSpacing.away(inside);
        if (++this.unstackTicks > UNSTACK_MAX_TICKS || away.lengthSqr() < 1.0E-12D) {
            this.crowdedTicks = 0;
            endUnstack();
            return;
        }
        moveToward(level, group, bodies, self, this.mob.getX() + away.x * UNSTACK_REACH,
                this.mob.getZ() + away.z * UNSTACK_REACH, UNSTACK_SPEED);
    }

    /**
     * Walk the requested side step; wait while not idle (getting up, eating); give up after
     * {@link #SIDE_STEP_MAX_TICKS} of walking (the wait does not count: a get-up alone takes about 69 ticks).
     */
    private void continueSideStep(ServerLevel level, DinoGroup group, HerdBodies bodies, int self) {
        double dx = this.sideX - this.mob.getX();
        double dz = this.sideZ - this.mob.getZ();
        if (dx * dx + dz * dz < ARRIVED * ARRIVED) {
            endSideStep();
            return;
        }
        if (!standingIdle()) {
            this.operation = Operation.WAIT;
            return;
        }
        if (this.heading == ContextSteering.STOP && this.stopTicks >= SIDE_STEP_STUCK_TICKS) {
            // No room: steering has stood on the step (every heading masked) for a while. The ask cooldown is
            // dropped, so the walker may ask again as soon as its own count runs out, not 60 ticks later.
            this.askedTick = DinoGroup.UNSET;
            endSideStep();
            return;
        }
        if (++this.sideTicks > SIDE_STEP_MAX_TICKS) {
            endSideStep();
            return;
        }
        moveToward(level, group, bodies, self, this.sideX, this.sideZ, SIDE_STEP_SPEED);
    }

    /**
     * Measures whether the ground across the body is tight ({@link HerdSpacing#tightGround}): the clearance
     * box's side room on both sides of the body yaw against {@link HerdSpacing#tightLimit} (the threshold,
     * one step more while it was tight: hysteresis), the second side only when the first falls short.
     * Measured once, then again each time the body has moved {@link #TIGHT_REFRESH_DISTANCE}; the yaw is the
     * way the body walked or walks, so the sides lie across the passage it is in. Read through
     * {@link #tightGround} by the herd's snapshot.
     */
    private void refreshTightGround(ServerLevel level, DinoGroup group) {
        double x = this.mob.getX();
        double z = this.mob.getZ();
        if (this.tightMeasured) {
            double dx = x - this.tightMeasuredX;
            double dz = z - this.tightMeasuredZ;
            if (dx * dx + dz * dz < TIGHT_REFRESH_DISTANCE * TIGHT_REFRESH_DISTANCE) {
                return;
            }
        }
        this.tightMeasured = true;
        this.tightMeasuredX = x;
        this.tightMeasuredZ = z;
        ClearanceBox box = box();
        BodyFit fit = group.fit();
        double threshold = HerdSpacing.tightThreshold(this.mob.getBbWidth(), box.width());
        double limit = HerdSpacing.tightLimit(threshold, this.tightGround);
        double yaw = Math.toRadians(this.mob.getYRot());
        double sideX = Math.cos(yaw);
        double sideZ = Math.sin(yaw);
        float roomOneSide = fit.sideRoom(level, this.mob, box, x, this.mob.getY(), z, sideX, sideZ, limit);
        float roomOtherSide = roomOneSide >= limit ? 0F
                : fit.sideRoom(level, this.mob, box, x, this.mob.getY(), z, -sideX, -sideZ, limit);
        this.tightGround = HerdSpacing.tightGround(roomOneSide, roomOtherSide, threshold, this.tightGround);
    }

    /**
     * Spec "Leader blocked", for every walker (playtest 2 fix P8b, traffic causes R1 and R5): this walker was
     * held by the same <b>standing</b> member, one that does not want to move or one it <b>outranks</b>
     * ({@link HerdSpacing#asksToMakeWay}; the herd leader is never asked, spec "Who yields: leader first":
     * followers go round a standing leader or wait), for {@link #LEADER_BLOCKED_TICKS} (the leader) or
     * {@link #MAKE_WAY_ASK_TICKS} (a follower) ticks in a row, by the guard (on a heading or on a path) or by
     * a yield to it (steering stopped because that member masked the heading it would have walked,
     * {@link #yieldBlocker}). That member is asked to step out of the walker's line, the guard's member-test
     * direction for a guard hold ({@link #guardDirection}) or the yielded heading, until it is the standing hard
     * minimum (tight where either stands in tight ground) plus {@link #SIDE_STEP_MARGIN} beside it
     * ({@link #askToMakeWay}; a blocker that sits down refuses, and a resting one gets up only for the leader,
     * playtest 4 fix P12b and its fix round 1). Two walkers
     * that meet head-on both stop; the higher rank asks, the lower one
     * steps aside, or in a corridor walks along it away from the asker (the headings toward it are masked),
     * which is its back-off. Between two members that want to move, asks go down the ranks only; a member
     * that does not want to move is never held, so it never asks back; and each member answers at most once
     * per {@link #MAKE_WAY_COOLDOWN_TICKS} (sooner after a side step that found no room): requests never
     * ping-pong. A walking blocker is not a hold.
     */
    private void makeWay(ServerLevel level, DinoGroup group, HerdBodies bodies, int self) {
        int blocker = -1;
        double dirX = 0D;
        double dirZ = 0D;
        if (this.guardBlocker >= 0) {
            blocker = this.guardBlocker;
            dirX = this.guardDirX;
            dirZ = this.guardDirZ;
        } else if ((this.steering || overriding()) && this.heading == ContextSteering.STOP && this.yieldBlocker != null) {
            blocker = bodies.indexOf(this.yieldBlocker);
            dirX = this.yieldDirX;
            dirZ = this.yieldDirZ;
        }
        if (blocker < 0 || !HerdSpacing.asksToMakeWay(group.isLeader(bodies.id(blocker)), bodies.walking(blocker),
                bodies.wantsToMove(blocker), bodies.outranks(self, blocker))) {
            this.makeWayTicks = 0;
            this.makeWayBlocker = null;
            return;
        }
        UUID id = bodies.id(blocker);
        this.makeWayTicks = id.equals(this.makeWayBlocker) ? this.makeWayTicks + 1 : 1;
        this.makeWayBlocker = id;
        boolean leader = group.isLeader(this.dinosaur.getUUID());
        int askAfter = leader ? LEADER_BLOCKED_TICKS : MAKE_WAY_ASK_TICKS;
        if (this.makeWayTicks < askAfter) {
            return;
        }
        this.makeWayTicks = 0;
        Dinosaur member = GroupManager.resolve(level, id);
        if (member != null && member.getMoveControl() instanceof HerdMoveControl control) {
            boolean tight = this.tightGround || bodies.tight(blocker);
            double clear = HerdSpacing.hardMinimum(this.mob.getBbWidth(), member.getBbWidth(), false, tight) + SIDE_STEP_MARGIN;
            boolean stepped = control.askToMakeWay(level.getGameTime(), this.mob.getX(), this.mob.getZ(), dirX, dirZ, clear, leader);
            long now = level.getGameTime();
            if (group.debugVisuals() && (this.makeWayLogTick == DinoGroup.UNSET || now - this.makeWayLogTick >= MAKE_WAY_LOG_MIN_TICKS)) {
                // P13a item 8: make-way asks while the herd's debug visuals are on, at most one line per interval.
                this.makeWayLogTick = now;
                Constants.LOG.info("[herd debug] make-way ask: {} {} asks {}: {}", leader ? "leader" : "member",
                        this.dinosaur.getUUID(), id, stepped ? "steps aside" : "refused (lying down, cooldown, own path or no room)");
            }
        }
    }
}
