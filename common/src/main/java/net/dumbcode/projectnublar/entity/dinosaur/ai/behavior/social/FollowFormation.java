package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.util.DebugFlags;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * How a follower moves, per herd state (spec: redesign). Each tick it picks a target and hands it to
 * {@link HerdMoveControl}, which steers round blocks and members:
 * <ul>
 *   <li><b>Graze</b>: hold a reserved spot ({@link GrazeSchedule}): the roomiest of
 *       {@link HerdArea#SPOT_CANDIDATES} candidates inside the herd area that is farther than the graze
 *       spacing from every reserved spot and reachable in a straight line by the clearance box. Without a
 *       spot: walk into the area, then stand only where a herd mate can pass ({@link #roomBeside}).</li>
 *   <li><b>Travel</b>: see {@link #travel}. While the leader rests on a roaming journey at night or longer than
 *       {@link JourneyPolicy#SHORT_REST_PAUSE_TICKS}, the herd spreads out to rest instead ({@link #restSpread}).</li>
 *   <li><b>Stuck</b> ({@link ProgressMonitor}, or every heading masked by blocks for
 *       {@link #BLOCKED_FALLBACK_TICKS}): a fallback vanilla path to a fixed point, within the herd's
 *       {@link FallbackBudget}, asked for every tick for one window. Setting the walk target ends this
 *       behaviour until the path is done.</li>
 * </ul>
 * Followers only: the leader moves by its own path. Each brain gets its own instance (task lists are
 * built per entity), so the fields below are per animal.
 */
public class FollowFormation<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));

    /**
     * The old build's graze modifier: 0.049 blocks per tick (speed is quadratic in the modifier, {@link GroundSpeed}).
     * A member walks to its graze spot at this speed only inside the herd area ({@link #spotWalkSpeed}).
     */
    static final double GRAZE_SPEED = 0.6D;
    /** A grazing follower within this distance of its spot stands. */
    static final double SPOT_TOLERANCE = 1.5D;
    /** Spec: two failed fallback paths in graze pick a new spot. */
    static final int FAILED_FALLBACKS_BEFORE_REPICK = 2;
    /**
     * Spec: fallback range. Only spots this close are picked; a spot farther away (after a journey) is
     * dropped, and the member walks back inside the herd area before it picks again.
     */
    static final double FALLBACK_RANGE = 24D;
    /**
     * No fallback this close to the target: beyond the path's close-enough ({@link #FALLBACK_CLOSE_ENOUGH},
     * Manhattan between block positions), so a granted path is never "already reached" when it starts.
     */
    static final double FALLBACK_MIN_DISTANCE = 4.0D;
    static final int FALLBACK_CLOSE_ENOUGH = 2;
    /** A member without a spot standing outside the herd area walks in until it is this far inside the edge. */
    static final double EDGE_MARGIN = 1.0D;
    /** Spec "Joining": a follower farther than this from the trail asks for a fallback path to its projection. */
    static final double JOIN_PATH_DISTANCE = 12D;
    /** The projection searches the trail this far either side of last tick's arc. */
    static final double PROJECT_WINDOW = 12D;
    /** Spec "Fallback goal": the trail point this far ahead of the follower's projection (the leader stood there). */
    static final double FALLBACK_AHEAD = 8D;
    /** Lead decision 14: every heading masked by blocks for this many ticks is stuck at once, without the 40-tick window. */
    static final int BLOCKED_FALLBACK_TICKS = 10;
    /** Review Focus 7: a follower this far ahead of the leader at most checks whether it stands in its line. */
    static final double MAKE_WAY_RANGE = 16D;
    /**
     * It steps out until it is the standing personal space (the hard minimum once it is up) plus this margin
     * beside the leader's line toward the journey target.
     */
    static final double MAKE_WAY_MARGIN = 0.5D;
    /** After the behaviour stops (a walk target was set, or the member stopped being idle) it starts again after this many ticks. */
    static final int RESTART_COOLDOWN_TICKS = 10;
    private static final long UNSET = Long.MIN_VALUE;

    private final GrazeSchedule schedule = new GrazeSchedule();
    private final ProgressMonitor progress = new ProgressMonitor();
    /** The entity's random for {@link GrazeSchedule}, bound once (a method reference per tick would allocate). */
    private @Nullable DoubleSupplier chance;
    private @Nullable HerdState lastState;
    /** {@link DinoGroup#journeys()} when this instance last noticed a state change, so a whole journey missed while not running still counts as one. */
    private int lastJourneys = -1;
    /**
     * Feet height of the reserved spot, NaN until this instance picked one. The group keeps a spot across
     * unload and reload but this instance is new then, so a held spot with no height is treated as invalid.
     */
    private double spotY = Double.NaN;
    /** Fallback paths granted toward the current spot since it was picked or last reached. */
    private int fallbacksForSpot;
    /** Until this tick the member asks the herd budget for a fallback path every tick (the budget queues members that keep asking). */
    private long askingUntil = UNSET;
    /** Last tick's arc on the trail, the hint for the next projection; NaN after a state change. */
    private double lastArc = Double.NaN;
    /** Set once the member has checked whether it stands in the leader's line at a journey start (Task 17). */
    private boolean madeWay;
    /**
     * Without a spot, inside the herd area: the member stands where it is, because a herd mate can pass it
     * there ({@link #roomBeside}) or because it could not get on toward the centre. Cleared when it walks
     * out of that state ({@link #retarget}, outside the area).
     */
    private boolean standHere;
    /**
     * This member saw the herd spread out to rest (P13a ruling Q1, {@link #restSpread}); cleared, with the travel
     * state, when the herd forms up again.
     */
    private boolean inRestSpread;
    /**
     * {@link DinoGroup#spreads} when this member picked its rest spot (fix round 1, item 6): a member still lying
     * down from one spread picks a new spot for the next one.
     */
    private int seenSpread = -1;
    /** Feet height of the member's rest spot while the herd rests spread out. */
    private double restY = Double.NaN;

    public FollowFormation() {
        noTimeout();
        cooldownFor(entity -> RESTART_COOLDOWN_TICKS);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return canFollow(entity);
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return canFollow(entity) && !BrainUtils.hasMemory(entity, MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void start(E entity) {
        this.progress.reset(Double.NaN, entity.level().getGameTime());
        if (this.chance == null) {
            this.chance = entity.getRandom()::nextDouble;
        }
    }

    @Override
    protected void stop(E entity) {
        HerdMoveControl control = control(entity);
        if (control != null) {
            control.stopSteering();
        }
    }

    @Override
    protected void tick(E entity) {
        ServerLevel level = (ServerLevel) entity.level();
        long start = System.nanoTime();
        level.getProfiler().push("herdFollow");
        follow(level, entity);
        level.getProfiler().pop();
        HerdTimings.record(level.getGameTime(), System.nanoTime() - start);
    }

    /**
     * A follower of a group, idle and not sitting down. {@code isIdle} ignores the ~69-tick sit phase of
     * {@code Rest} (it has many other callers), so the sit-down is checked here: a follower that sits down
     * stands still and is not steered (playtest 4 fix P12b, lead decision).
     */
    private static boolean canFollow(Dinosaur dinosaur) {
        DinoGroup group = GroupManager.groupOf(dinosaur);
        return group != null && !group.isLeader(dinosaur.getUUID()) && dinosaur.isIdle() && !dinosaur.isSitting()
                && HerdSpacing.applies(dinosaur);
    }

    private static @Nullable HerdMoveControl control(Dinosaur dinosaur) {
        return dinosaur.getMoveControl() instanceof HerdMoveControl control ? control : null;
    }

    private void follow(ServerLevel level, E entity) {
        DinoGroup group = GroupManager.groupOf(entity);
        HerdMoveControl control = control(entity);
        if (group == null || control == null) {
            return;
        }
        Dinosaur leader = GroupManager.leaderOf(group, level);
        if (leader == null || leader == entity) {
            control.stopSteering();
            return;
        }
        long now = level.getGameTime();
        if (group.state() != this.lastState || group.journeys() != this.lastJourneys) {
            // Also when the state is back where it was: a whole journey went by while this member was not
            // running (a long need action), so it enters GRAZE with the stagger like the others did.
            this.lastState = group.state();
            this.lastJourneys = group.journeys();
            onStateChange(entity, now);
        }
        if (group.state() == HerdState.TRAVEL) {
            travel(level, entity, group, leader, control, now);
        } else {
            graze(level, entity, group, leader, control, now);
        }
    }

    private void onStateChange(E entity, long now) {
        retarget(now);
        this.lastArc = Double.NaN;
        this.madeWay = false;
        this.inRestSpread = false;
        if (this.lastState == HerdState.GRAZE) {
            this.schedule.enter(now, entity.getRandom().nextDouble());
        }
    }

    /**
     * Travel (spec "Following"): steer to a look-ahead point on the leader's trail, offset by the lane, at a
     * speed from the arc gap to the follower's own target arc. Wait in place while ahead of the slot or while
     * the slot is not on the trail yet (stepping out of the leader's line first: {@link #makeWay}). Far from
     * the trail: a fallback path to the projection point. Stuck: a fallback path to the trail point
     * {@link #FALLBACK_AHEAD} ahead.
     */
    private void travel(ServerLevel level, E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control, long now) {
        if (group.herdSpread()) {
            restSpread(level, entity, group, leader, control, now);
            return;
        }
        if (this.inRestSpread) {
            // The leader is up: the wedge re-forms from where this member is (new projection, new make-way check).
            this.inRestSpread = false;
            retarget(now);
            this.lastArc = Double.NaN;
            this.madeWay = false;
        }
        LeaderTrail trail = group.trail();
        if (trail.size() == 0) {
            control.stopSteering();
            control.markProgress(now);
            return;
        }
        double width = entity.getBbWidth();
        double personalSpace = HerdSpacing.personalSpace(width, width);
        double tight = TravelOrder.tightSpacing(personalSpace);
        group.refreshLayout(now, DebugFlags.fixedTravelSpacing ? tight : TravelOrder.openSpacing(personalSpace), tight);
        double targetArc = group.targetArcOf(entity.getUUID());
        if (Double.isNaN(targetArc)) {
            // Joined this tick: the leader rebuilds the order next tick.
            control.stopSteering();
            control.markProgress(now);
            return;
        }
        double arc = trail.project(entity.getX(), entity.getZ(), this.lastArc, PROJECT_WINDOW);
        this.lastArc = arc;
        TravelOrder.Target target = TravelOrder.follow(trail, arc, targetArc, group.laneOffsetOf(entity.getUUID()));
        if (target == null) {
            control.stopSteering();
            return;
        }
        if (target.speed() <= 0D) {
            makeWay(entity, group, leader, control);
            control.steerTo(target.x(), target.z(), 0D);
            this.progress.reset(arc, now);
            control.markProgress(now);
            this.askingUntil = UNSET;
            return;
        }
        LeaderTrail.TrailPoint onTrail = trail.pointAt(arc);
        if (horizontalDistance(entity, onTrail.x(), onTrail.z()) > JOIN_PATH_DISTANCE) {
            // Spec "Joining": a fixed trail point is the target now, not the look-ahead, so a stuck window
            // measured on the trail does not carry over to the tick the member is back within range.
            retarget(now);
            requestFallback(level, entity, group, control, onTrail.x(), onTrail.y(), onTrail.z(), TravelOrder.CATCH_UP_SPEED);
            control.markProgress(now);
            return;
        }
        // P13a: a paused journey (the leader rests) counts as a standing leader; the straggler
        // wait no longer stops the leader (a slow walk, ruling 3a).
        boolean leaderStands = group.journeyPaused() || group.settling() || group.dragStanding();
        // A real block stop (blocks or guard walls counted), not a yield to members that outlasted
        // ContextSteering.MAX_WAIT_TICKS while the leader stands: the leader's pause and settle (100)
        // are longer than that time-out, and the fallback goal ahead is the leader's own position for a
        // front row.
        boolean blockStop = control.blockedTicks() >= BLOCKED_FALLBACK_TICKS
                && !(leaderStands && control.yieldTimedOut());
        if ((TravelOrder.nearStopLine(targetArc, arc) || leaderStands) && !blockStop) {
            // Easing into the slot, or the leader stands for the herd: too little arc is left to show
            // progress, and the spec says a waiting leader is not stuck. Keep easing in; no stuck window,
            // or the fallback goal ahead would send a front-row member into the leader's feet. A member
            // pressing into blocks all the while is still stuck (lead decision 14), as is one stopped behind
            // members longer than the time-out while the leader walks: they fall through.
            control.steerTo(target.x(), target.z(), target.speed());
            this.progress.reset(arc, now);
            control.markProgress(now);
            this.askingUntil = UNSET;
            return;
        }
        if (isStuck(control, arc, now)) {
            LeaderTrail.TrailPoint ahead = trail.pointAt(Math.min(arc + FALLBACK_AHEAD, trail.headArc()));
            if (requestFallback(level, entity, group, control, ahead.x(), ahead.y(), ahead.z(), TravelOrder.CATCH_UP_SPEED)) {
                this.askingUntil = UNSET;
            }
            return;
        }
        control.steerTo(target.x(), target.z(), target.speed());
    }

    /**
     * P13a ruling Q1: the leader rests on a roaming journey at night, or longer than
     * {@link JourneyPolicy#SHORT_REST_PAUSE_TICKS}, so the herd leaves its slots and spreads out round the leader
     * to rest, as when grazing. Once per pause the member picks a rest spot the way it picks a graze spot
     * ({@link #pickSpot}: inside the herd radius R round the leader, the graze spacing from every reserved spot and
     * the leader, reachable in a straight line, within {@link #FALLBACK_RANGE}); with none, where it stands. It
     * walks there at {@link #GRAZE_SPEED} (a fallback path when stuck; stuck within
     * {@link #FALLBACK_MIN_DISTANCE}, that place is its spot) and lies down there, tired or not
     * ({@link JourneyPolicy#sleepsAtSpot}: always at night, in the daytime below its maximum stamina; never once
     * the leader gets up). A member that slept through a spread's start picks again for the next spread.
     */
    private void restSpread(ServerLevel level, E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control, long now) {
        if (!this.inRestSpread || this.seenSpread != group.spreads()) {
            this.inRestSpread = true;
            this.seenSpread = group.spreads();
            retarget(now);
            double width = entity.getBbWidth();
            double personalSpace = HerdSpacing.personalSpace(width, width);
            Vec3 picked = pickSpot(level, entity, group, leader, control, leader.getX(), leader.getZ(),
                    group.radius(personalSpace, width), HerdArea.grazeSpacing(personalSpace), null);
            if (picked == null) {
                picked = entity.position();
            }
            group.reserveSpot(entity.getUUID(), picked.x, picked.z);
            this.restY = picked.y;
        }
        HerdArea.Point spot = group.spotOf(entity.getUUID());
        if (spot == null) {
            control.stopSteering();
            return;
        }
        double distance = horizontalDistance(entity, spot.x(), spot.z());
        boolean there = distance <= SPOT_TOLERANCE;
        if (!there && isStuck(control, -distance, now)) {
            if (distance > FALLBACK_MIN_DISTANCE) {
                if (requestFallback(level, entity, group, control, spot.x(), this.restY, spot.z(), GRAZE_SPEED)) {
                    this.askingUntil = UNSET;
                }
                return;
            }
            // Stuck close by: this place is the rest spot now.
            group.reserveSpot(entity.getUUID(), entity.getX(), entity.getZ());
            this.restY = entity.getY();
            there = true;
        }
        if (!there) {
            control.steerTo(spot.x(), spot.z(), GRAZE_SPEED);
            return;
        }
        control.stopSteering();
        this.progress.reset(-distance, now);
        control.markProgress(now);
        this.askingUntil = UNSET;
        boolean restHours = JourneyPolicy.restHours(level.getDayTime(), entity.getDinoBehaviour().isNocturnal());
        // Fix round 1, item 2: not once the leader gets up.
        if (JourneyPolicy.sleepsAtSpot(restHours, group.leaderRising(), DinoNeedsUtils.getCurrentStamina(entity),
                DinoNeedsUtils.getMaxStamina(entity))) {
            HerdNeeds.lieDown(entity, group);
        }
    }

    /**
     * Review Focus 7: followers whose slot is not on the trail yet stand still, and the leader walks a
     * vanilla path that cannot go round them. So a follower standing ahead of the leader, within
     * {@link #MAKE_WAY_RANGE}, inside the standing hard minimum (tight where either stands in tight ground,
     * {@link HerdSpacing#hardMinimum}) plus {@link #MAKE_WAY_MARGIN} of the leader's line toward the journey
     * target, steps out of that line once, perpendicular to it ({@link HerdMoveControl#requestSideStepOutOf}).
     * The standing minimum, not the resting one: this behaviour runs only while the member is up (a member
     * that sits down or rests is never asked to step aside, playtest 4 fix P12b).
     */
    private void makeWay(E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control) {
        if (this.madeWay) {
            return;
        }
        this.madeWay = true;
        double dirX = group.journeyX() - leader.getX();
        double dirZ = group.journeyZ() - leader.getZ();
        double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (length < 1.0E-6D) {
            return;
        }
        dirX /= length;
        dirZ /= length;
        double along = (entity.getX() - leader.getX()) * dirX + (entity.getZ() - leader.getZ()) * dirZ;
        if (along <= 0D || along > MAKE_WAY_RANGE) {
            return;
        }
        boolean tight = control.tightGround() || HerdSpacing.tightGroundOf(leader);
        control.requestSideStepOutOf(leader.getX(), leader.getZ(), dirX, dirZ,
                HerdSpacing.hardMinimum(leader.getBbWidth(), entity.getBbWidth(), false, tight) + MAKE_WAY_MARGIN);
    }

    private void graze(ServerLevel level, E entity, DinoGroup group, Dinosaur leader, HerdMoveControl control, long now) {
        if (!group.hasCentre()) {
            // No herd area yet (a leader change ended a journey): stand until the leader's tick recentres.
            control.stopSteering();
            return;
        }
        double width = entity.getBbWidth();
        double personalSpace = HerdSpacing.personalSpace(width, width);
        double spacing = HerdArea.grazeSpacing(personalSpace);
        double radius = group.radius(personalSpace, width);
        double centreX = group.centreX();
        double centreZ = group.centreZ();

        HerdArea.Point spot = group.spotOf(entity.getUUID());
        if (spot != null && horizontalDistance(entity, spot.x(), spot.z()) > FALLBACK_RANGE) {
            // Too far for a fallback path (a straggler after a journey): drop it. The no-spot branch below
            // walks back inside the herd area, and the next pick takes only spots within range.
            group.releaseSpot(entity.getUUID());
            this.schedule.noSpot(now);
            retarget(now);
            spot = null;
        }
        boolean valid = spot != null && !Double.isNaN(this.spotY)
                && HerdArea.inside(centreX, centreZ, radius, spot.x(), spot.z());
        GrazeSchedule.Action action = this.schedule.decide(now, spot != null, valid, this.chance);
        if (action != GrazeSchedule.Action.HOLD) {
            // Every branch below calls spotTaken or noSpot (after releasing an invalid spot), so a pick is
            // never retried every tick (GrazeSchedule's caller contract).
            HerdArea.Point near = action == GrazeSchedule.Action.PICK_NEAR ? spot : null;
            Vec3 picked = pickSpot(level, entity, group, leader, control, centreX, centreZ, radius, spacing, near);
            if (picked != null) {
                group.reserveSpot(entity.getUUID(), picked.x, picked.z);
                this.spotY = picked.y;
                this.schedule.spotTaken(now, this.chance);
                retarget(now);
            } else if (action == GrazeSchedule.Action.PICK_NEAR) {
                this.schedule.spotTaken(now, this.chance);
            } else {
                group.releaseSpot(entity.getUUID());
                this.schedule.noSpot(now);
                retarget(now);
            }
            spot = group.spotOf(entity.getUUID());
        }

        if (spot == null) {
            // No spot: stand still and try again later (spec), but first walk into the herd area if outside
            // it (Review Focus 1). The centre is where the leader stood, so a fallback path can go there, at
            // the centre's own ground height (playtest 2 fix P8a: the leader's height is elsewhere while it
            // is dragged on a lead). At the catch-up speed, as the old build regrouped: a straggler after a
            // journey has 20 or more blocks to cover, 0.19 blocks per tick against 0.05 at the graze speed.
            if (!HerdArea.inside(centreX, centreZ, radius - EDGE_MARGIN, entity.getX(), entity.getZ())) {
                this.standHere = false;
                walkTo(level, entity, group, control, centreX, group.centreY(), centreZ, TravelOrder.CATCH_UP_SPEED, now);
                return;
            }
            if (!this.standHere) {
                // Playtest 2 fix P8a (traffic cause R2): not in a passage. A spotless member stands only where
                // a herd mate can pass it; otherwise it keeps on toward the centre at the graze speed, and
                // stands anyway once it cannot get on (blocked, or a yield that outlasted the time-out): no
                // fallback path inside the area, or every member in a tight place would path to the centre.
                this.standHere = roomBeside(level, entity, group, control)
                        || stuck(control, -horizontalDistance(entity, centreX, centreZ), now);
                if (!this.standHere) {
                    control.steerTo(centreX, centreZ, GRAZE_SPEED);
                    return;
                }
            }
            control.stopSteering();
            control.markProgress(now);
            this.progress.reset(Double.NaN, now);
            this.askingUntil = UNSET;
            return;
        }
        double distance = horizontalDistance(entity, spot.x(), spot.z());
        if (distance <= SPOT_TOLERANCE) {
            control.stopSteering();
            this.progress.reset(-distance, now);
            control.markProgress(now);
            this.askingUntil = UNSET;
            this.fallbacksForSpot = 0;
            return;
        }
        if (isStuck(control, -distance, now)) {
            if (distance <= FALLBACK_MIN_DISTANCE) {
                // Stuck close by: this place is the spot now (stay put), but only if it keeps the graze
                // spacing from every other reserved spot (the spot rule, T2); otherwise pick again.
                control.stopSteering();
                HerdArea.Point here = new HerdArea.Point(entity.getX(), entity.getZ());
                if (HerdArea.roominess(here, reservedSpots(group, entity, leader)) > spacing) {
                    group.reserveSpot(entity.getUUID(), here.x(), here.z());
                    this.spotY = entity.getY();
                } else {
                    group.releaseSpot(entity.getUUID());
                    this.schedule.repickNow(now);
                }
                retarget(now);
                return;
            }
            if (this.fallbacksForSpot >= FAILED_FALLBACKS_BEFORE_REPICK) {
                group.releaseSpot(entity.getUUID());
                this.schedule.repickNow(now);
                retarget(now);
                control.stopSteering();
                return;
            }
            if (requestFallback(level, entity, group, control, spot.x(), this.spotY, spot.z(),
                    spotWalkSpeed(entity, centreX, centreZ, radius))) {
                this.fallbacksForSpot++;
                this.askingUntil = UNSET;
            }
            return;
        }
        control.steerTo(spot.x(), spot.z(), spotWalkSpeed(entity, centreX, centreZ, radius));
    }

    /**
     * Speed of the walk to the graze spot (lead decision, playtest 1 fix P4): {@link TravelOrder#CATCH_UP_SPEED}
     * (0.194 blocks per tick) while the member is outside the herd area (radius R), so a straggler after a
     * journey regroups as fast as one without a spot; {@link #GRAZE_SPEED} (0.049) once inside. A fallback path
     * keeps the speed it was granted with to its end.
     */
    private static double spotWalkSpeed(Dinosaur entity, double centreX, double centreZ, double radius) {
        return HerdArea.inside(centreX, centreZ, radius, entity.getX(), entity.getZ())
                ? GRAZE_SPEED : TravelOrder.CATCH_UP_SPEED;
    }

    /**
     * The target changed (a new spot, a dropped spot, the adopted position): a stuck window measured
     * against the old target must not carry over, or the member would ask for a fallback path at once
     * toward a target it was never stuck on.
     */
    private void retarget(long now) {
        this.askingUntil = UNSET;
        this.progress.reset(Double.NaN, now);
        this.fallbacksForSpot = 0;
        this.standHere = false;
    }

    /**
     * Playtest 2 fix P8a: can a herd mate pass this member where it stands? The clearance box's side room
     * ({@link BodyFit#sideRoom}) on both sides of the body yaw, against the hard minimum
     * ({@link HerdSpacing#roomToPass}). The yaw is the way the member walked in, so the sides lie across the
     * passage it stands in. The <b>tight</b> minimum is the one to measure against (P8b): a place with less
     * than {@link HerdSpacing#tightThreshold} of room on its roomier side is tight ground, where a passer
     * keeps only the tight minimum, and a place with more is open and passable at the personal space anyway.
     * So at most four stand probes per side. Stand results are cached, so a standing member costs nothing
     * after the first look.
     */
    private static boolean roomBeside(ServerLevel level, Dinosaur entity, DinoGroup group, HerdMoveControl control) {
        double yaw = Math.toRadians(entity.getYRot());
        double sideX = Math.cos(yaw);
        double sideZ = Math.sin(yaw);
        BodyFit fit = group.fit();
        ClearanceBox box = control.box();
        double width = entity.getBbWidth();
        double minimum = HerdSpacing.hardMinimum(width, width, false, true);
        // Measured only up to the minimum, and the other side only when the first falls short (fix round 1).
        float roomOneSide = fit.sideRoom(level, entity, box, entity.getX(), entity.getY(), entity.getZ(), sideX, sideZ, minimum);
        if (HerdSpacing.roomToPass(roomOneSide, 0F, minimum)) {
            return true;
        }
        float roomOtherSide = fit.sideRoom(level, entity, box, entity.getX(), entity.getY(), entity.getZ(), -sideX, -sideZ, minimum);
        return HerdSpacing.roomToPass(roomOneSide, roomOtherSide, minimum);
    }

    /**
     * Spec "Stuck": less than 0.5 blocks of progress in 40 ticks while not waiting, or every heading masked
     * by blocks for {@link #BLOCKED_FALLBACK_TICKS} (lead decision 14). Waiting means held back on purpose
     * or yielding to members, and a yield counts as blocked after {@link ContextSteering#MAX_WAIT_TICKS}
     * ({@link HerdMoveControl#isWaiting}), so a stop behind members always ends here. Once stuck, the
     * member asks the herd budget for a fallback path every tick for one window, because
     * {@link FallbackBudget} queues only members that keep asking. {@code measure} grows as the member gets on.
     *
     * @return {@code true} while the member should ask for a fallback path this tick
     */
    private boolean isStuck(HerdMoveControl control, double measure, long now) {
        if (stuck(control, measure, now)) {
            this.askingUntil = now + ProgressMonitor.WINDOW_TICKS;
        }
        return now < this.askingUntil;
    }

    /** The stuck rule of {@link #isStuck} alone: this tick's progress measured, no fallback path asked for. */
    private boolean stuck(HerdMoveControl control, double measure, long now) {
        boolean stuck = this.progress.update(measure, control.isWaiting(), now)
                || control.blockedTicks() >= BLOCKED_FALLBACK_TICKS;
        control.markProgress(this.progress.lastProgressTick());
        return stuck;
    }

    /** Steer to a fixed point the leader stood at; when stuck, a fallback path there. */
    private void walkTo(ServerLevel level, E entity, DinoGroup group, HerdMoveControl control,
                        double x, double y, double z, double speed, long now) {
        if (isStuck(control, -horizontalDistance(entity, x, z), now)) {
            if (requestFallback(level, entity, group, control, x, y, z, speed)) {
                this.askingUntil = UNSET;
            }
            return;
        }
        control.steerTo(x, z, speed);
    }

    /**
     * A graze spot: {@link HerdArea#SPOT_CANDIDATES} random points inside the herd area (or within
     * {@link GrazeSchedule#SHORT_MOVE_RANGE} of {@code near}) and within {@link #FALLBACK_RANGE} of the
     * member, the roomiest that is farther than the graze spacing from every reserved spot and from the
     * leader, and that the clearance box reaches in a straight line from here (so never behind a wall or
     * outside an arena). Returns the spot with its feet height.
     */
    private static @Nullable Vec3 pickSpot(ServerLevel level, Dinosaur entity, DinoGroup group, Dinosaur leader,
                                           HerdMoveControl control, double centreX, double centreZ, double radius,
                                           double spacing, @Nullable HerdArea.Point near) {
        RandomSource random = entity.getRandom();
        List<HerdArea.Point> candidates = new ArrayList<>(HerdArea.SPOT_CANDIDATES);
        for (int attempt = 0; attempt < HerdArea.SPOT_CANDIDATES; attempt++) {
            HerdArea.Point point = near == null
                    ? HerdArea.sample(centreX, centreZ, radius, random.nextDouble(), random.nextDouble())
                    : HerdArea.sampleRing(near.x(), near.z(), 0D, GrazeSchedule.SHORT_MOVE_RANGE, random.nextDouble(), random.nextDouble());
            if (HerdArea.inside(centreX, centreZ, radius, point.x(), point.z())
                    && horizontalDistance(entity, point.x(), point.z()) <= FALLBACK_RANGE) {
                candidates.add(point);
            }
        }
        List<HerdArea.Point> reserved = reservedSpots(group, entity, leader);
        ClearanceBox box = control.box();
        BodyFit fit = group.fit();
        double[] reachY = new double[candidates.size()];
        int best = HerdArea.pickSpot(candidates, reserved, spacing, point -> {
            int index = candidates.indexOf(point);
            reachY[index] = fit.reachY(level, entity, box, entity.getX(), entity.getY(), entity.getZ(), point.x(), point.z());
            return !Double.isNaN(reachY[index]);
        });
        if (best < 0) {
            return null;
        }
        HerdArea.Point point = candidates.get(best);
        return new Vec3(point.x(), reachY[best], point.z());
    }

    /** The points a spot must keep the graze spacing from: the other members' spots and the leader's position (spec spot rule). */
    private static List<HerdArea.Point> reservedSpots(DinoGroup group, Dinosaur entity, Dinosaur leader) {
        List<HerdArea.Point> reserved = group.reservedSpotsExcept(entity.getUUID());
        reserved.add(new HerdArea.Point(leader.getX(), leader.getZ()));
        return reserved;
    }

    /**
     * Within the herd's budget, hand over to vanilla pathfinding toward the fixed point (x, y, z): a trail
     * point in travel, the spot in graze; never a moving slot. While queued, keep steering there.
     *
     * @return {@code true} if the path was granted
     */
    private boolean requestFallback(ServerLevel level, E entity, DinoGroup group, HerdMoveControl control,
                                    double x, double y, double z, double speed) {
        if (!group.fallbacks().tryAcquire(entity.getUUID(), level.getGameTime())) {
            control.steerTo(x, z, speed);
            return false;
        }
        control.stopSteering();
        BlockPos goal = BlockPos.containing(x, y, z);
        BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(goal, (float) speed, FALLBACK_CLOSE_ENOUGH));
        // The path the walk behaviour computes for this goal is a herd fallback path: the guard's hold may end it.
        control.fallbackRequested(goal);
        this.progress.reset(Double.NaN, level.getGameTime());
        return true;
    }

    private static double horizontalDistance(Dinosaur entity, double x, double z) {
        double dx = x - entity.getX();
        double dz = z - entity.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
