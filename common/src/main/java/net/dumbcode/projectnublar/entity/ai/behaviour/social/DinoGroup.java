package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

/**
 * One herd or pack: a capacity, the members and the leader, and the herd's movement state (spec: redesign).
 * <p>
 * {@link GroupManager} owns membership: only it adds or removes members, so the member set and its
 * index always agree. This class owns leadership. It works on UUIDs only and asks a {@link Roster}
 * which members are present and how dominant they are, so it holds no entity and no level, and a
 * unit test can drive it with a fake roster.
 * <p>
 * Movement state: GRAZE or TRAVEL, set by journeys ({@link #startJourney}, {@link #endJourney}), never
 * inferred from a path. The herd area has a fixed centre that moves only at the end of a journey or when
 * the leader has stood outside it ({@link #recentre}). A leader dragged out of the area starts an urgent journey
 * with no fixed target ({@link #startDrag}). Followers reserve graze spots here. A journey has a
 * travel order (nearest-first at the start, rebuilt when a member joins or leaves) and a per-tick layout of
 * target arcs and lane offsets. A roaming journey may pause while the leader is down ({@link #startPause});
 * the pause is not a path end. A leader change ends any journey. Each journey end is
 * recorded once for the server log ({@link #recordEnd}). The group also holds the caches its
 * members share each tick: {@link BodyFit}, {@link HerdBodies}, the layout and the evaluation cap.
 * <p>
 * An unloaded member stays in the set, but it cannot lead: the leader is always a present member.
 */
public final class DinoGroup {

    /** What the group needs to know about its members. {@link GroupManager#roster} gives the live one. */
    interface Roster {

        /** {@code true} if the member is alive and loaded. */
        boolean isPresent(UUID member);

        /** The member's dominance. Only called for a present member. */
        float dominance(UUID member);
    }

    /** A tick or time not set yet. */
    static final long UNSET = Long.MIN_VALUE;
    /**
     * Debug log lines for the straggler slow walk start at most this often (fix round 1, item 7), so a herd at the
     * 75% edge does not log every few ticks. Debug only.
     */
    static final int STRAGGLER_LOG_MIN_TICKS = 100;
    /** At most this many context-steering evaluations per herd per tick (spec: performance budget). */
    static final int MAX_EVALUATIONS_PER_TICK = 10;
    /** Lanes per row come from the tightest side room within this many blocks of the row's arc, so they do not flicker between trail points. */
    static final double ROW_ROOM_WINDOW = 2.0D;

    private final int capacity;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> memberView = Collections.unmodifiableSet(this.members);
    private @Nullable UUID leader;
    /** The lead changed hands and the walks of the leaders involved are not reset yet ({@link #leaderChanged}). */
    private boolean leaderChanged;
    /** Leaders that lost the lead since the last {@link #leaderChangeSettled}. */
    private final List<UUID> deposed = new ArrayList<>();
    private final List<UUID> deposedView = Collections.unmodifiableList(this.deposed);

    private HerdState state = HerdState.GRAZE;
    private final LeaderTrail trail = new LeaderTrail();
    private final FallbackBudget fallbacks = new FallbackBudget();
    private final BodyFit fit = new BodyFit();
    private final HerdBodies bodies = new HerdBodies();

    private boolean hasCentre;
    private double centreX;
    /** Feet height of the ground at the centre: where the leader stood when the area was centred. */
    private double centreY;
    private double centreZ;
    private int outsideTicks;
    /** This herd's area scale ({@link #radius}): {@link HerdArea#HERD_AREA_SCALE} unless a GameTest sets its own. Not saved. */
    private double areaScale = HerdArea.HERD_AREA_SCALE;
    private final Map<UUID, HerdArea.Point> spots = new HashMap<>();
    private final Collection<HerdArea.Point> spotView = Collections.unmodifiableCollection(this.spots.values());
    /** The lead switched this herd's debug visuals on with the debug stick ({@code HerdDebugVisuals}). Not saved. */
    private boolean debugVisuals;

    private boolean journeyActive;
    /** Journeys started so far: a follower that did not run across a whole journey still sees the count change. */
    private int journeys;
    private JourneyPolicy.Kind journeyKind = JourneyPolicy.Kind.ROAM;
    private double journeyX;
    private double journeyY;
    private double journeyZ;
    private long settleUntil = UNSET;
    private int waitedTicks;
    /** The leader's navigation speed was slowed for stragglers and not restored yet. */
    private boolean leaderSlowed;
    /** The last straggler check was a slow walk ({@link JourneyPolicy.Gathering#SLOW}): for the start and end log lines. */
    private boolean stragglerSlow;
    /** Why the roaming journey is paused (P13a): the leader is down. */
    private JourneyPolicy.Pause pause = JourneyPolicy.Pause.NONE;
    private long pauseStartTick = UNSET;
    /** The herd left its slots to rest during this pause (ruling Q1, {@link JourneyPolicy#herdSpreads}). */
    private boolean herdSpread;
    /** Spreads so far ({@link #spreads}): a member compares it to notice a new one, even one it slept through the start of. */
    private int spreads;
    /** The leader is in its get-up during the pause (fix round 1: no spread, no lie-down at a rest spot). */
    private boolean leaderRising;
    /** Members {@code HerdNeeds.lieDown} marked tired, until the leader's tick checks them after the pause ({@link #takeTiredMarks}). */
    private final List<UUID> tiredMarks = new ArrayList<>();
    /** The last tick a straggler slow-walk start was logged, and whether the current one was ({@link #mayLogStragglerStart}). */
    private long stragglerLogTick = UNSET;
    private boolean stragglerStartLogged;
    /** Where and when the journey started, for its end record; NaN when the leader's position was not known. */
    private double journeyStartX = Double.NaN;
    private double journeyStartZ = Double.NaN;
    private long journeyStartTick = UNSET;
    /** This journey's end is recorded ({@link #recordEnd}): one log line per journey. */
    private boolean endRecorded;
    private @Nullable JourneyPolicy.JourneyEnd unloggedEnd;
    private boolean hasPathEnd;
    private double pathEndX;
    private double pathEndZ;
    /** The journey is a drag (playtest 2 fix P8c): its target is the leader, it ends when the leader stood still. */
    private boolean dragged;
    /** Where the dragged leader stopped ({@link #countStill}). */
    private double stillX;
    private double stillZ;
    private int stillTicks;
    /** The leader's position on its previous tick ({@link #leaderStep}). */
    private long lastLeaderTick = UNSET;
    private double lastLeaderX;
    private double lastLeaderZ;
    /** Unit direction of the leader's last step of at least {@link JourneyPolicy#DRAG_MIN_STEP}; 0, 0 before one. */
    private double dragDirX;
    private double dragDirZ;
    private long nextJourneyTick = UNSET;
    private long nextGrazeMoveTick = UNSET;
    private boolean grazePinned;

    private final List<UUID> travelOrder = new ArrayList<>();
    private final List<UUID> travelOrderView = Collections.unmodifiableList(this.travelOrder);
    private boolean orderDirty;
    private double[] targetArcs = new double[0];
    private double[] laneOffsets = new double[0];
    private long layoutTick = UNSET;
    /** The trail head arc the latest layout was laid from; NaN when the layout has no slots. */
    private double layoutHeadArc = Double.NaN;
    /** {@link #roomAt}, held once so a layout refresh allocates nothing. */
    private final DoubleUnaryOperator roomAtArc = this::roomAt;

    private long evaluationTick = UNSET;
    private int evaluations;

    DinoGroup(int capacity) {
        this.capacity = capacity;
    }

    int capacity() {
        return this.capacity;
    }

    public int size() {
        return this.members.size();
    }

    /** A read-only view of the member UUIDs. */
    Set<UUID> memberIds() {
        return this.memberView;
    }

    /** {@code true} if {@code member} is the stored leader. Does not check that the leader is present. */
    boolean isLeader(UUID member) {
        return member.equals(this.leader);
    }

    /**
     * Adds a member. A newcomer that outranks the present leader leads at once, so the leader is always
     * the best of the group, whatever the order of joining. If there is no present leader, the best
     * present member is elected now, so a leader exists from the moment the group forms. The contest
     * margin applies only to changes through growth ({@link #challenge}). A join during travel rebuilds the
     * travel order.
     */
    void add(UUID member, Roster roster) {
        this.members.add(member);
        if (this.state == HerdState.TRAVEL) {
            this.orderDirty = true;
        }
        if (this.leader == null || !roster.isPresent(this.leader)) {
            electLeader(roster);
        } else if (roster.isPresent(member)
                && Dominance.outranks(roster.dominance(member), member, roster.dominance(this.leader), this.leader)) {
            UUID previous = this.leader;
            this.leader = member;
            onLeaderChange(previous);
        }
    }

    /** Removes the member, its spot and its place in the order. If it led, the next {@link #leader} call elects a new one. */
    void remove(UUID member) {
        this.members.remove(member);
        this.fallbacks.forget(member);
        this.spots.remove(member);
        if (this.travelOrder.remove(member)) {
            this.layoutTick = UNSET;
        }
        if (this.state == HerdState.TRAVEL) {
            this.orderDirty = true;
        }
        if (member.equals(this.leader)) {
            this.leader = null;
            onLeaderChange(member);
        }
    }

    /**
     * The leader. If the stored leader is not present, the highest-ranked present member takes its place
     * first, with no contest.
     *
     * @return the leader, or {@code null} if no member is present
     */
    @Nullable UUID leader(Roster roster) {
        if (this.leader == null || !roster.isPresent(this.leader)) {
            electLeader(roster);
        }
        return this.leader;
    }

    /**
     * Gives the lead to {@code challenger} if its dominance is more than {@code margin} above the leader's.
     * The margin stops two near-equal animals from swapping the lead back and forth as they grow.
     *
     * @param margin at least 0; a negative margin would let the lead swap at every contest
     * @return {@code true} if the challenger is now the leader
     */
    boolean challenge(UUID challenger, float margin, Roster roster) {
        UUID current = leader(roster);
        if (current == null || current.equals(challenger) || !this.members.contains(challenger)
                || !roster.isPresent(challenger)
                || roster.dominance(challenger) <= roster.dominance(current) + margin) {
            return false;
        }
        this.leader = challenger;
        onLeaderChange(current);
        return true;
    }

    public HerdState state() {
        return this.state;
    }

    /**
     * Starts a journey: TRAVEL from this tick, a fresh trail, no graze spots, and the given travel order
     * (followers nearest-first). Refused while the herd is pinned to grazing. {@code y} is NaN for a far
     * roaming target that has no ground height yet ({@link #journeyTargetKnown}).
     */
    boolean startJourney(JourneyPolicy.Kind kind, double x, double y, double z, List<UUID> followerOrder, long now) {
        if (this.grazePinned) {
            return false;
        }
        if (this.journeyActive) {
            // A GameTest's beginRoam over a running journey: that one ends here.
            recordEnd(JourneyPolicy.End.REPLACED, this.leader, this.lastLeaderX, this.lastLeaderZ, now);
        }
        this.endRecorded = false;
        this.journeyStartTick = now;
        // The leader's latest tick position (this tick from HerdLeading, the previous one from a brain or a GameTest).
        boolean known = this.lastLeaderTick != UNSET;
        this.journeyStartX = known ? this.lastLeaderX : Double.NaN;
        this.journeyStartZ = known ? this.lastLeaderZ : Double.NaN;
        this.stragglerSlow = false;
        clearPause();
        this.state = HerdState.TRAVEL;
        this.journeyActive = true;
        this.journeys++;
        this.journeyKind = kind;
        this.journeyX = x;
        this.journeyY = y;
        this.journeyZ = z;
        this.dragged = false;
        this.settleUntil = UNSET;
        this.waitedTicks = 0;
        this.leaderSlowed = false;
        this.hasPathEnd = false;
        this.trail.clear();
        this.spots.clear();
        setTravelOrder(followerOrder);
        return true;
    }

    boolean journeyActive() {
        return this.journeyActive;
    }

    /**
     * Starts a drag journey (playtest 2 fix P8c): an urgent journey ({@link #startJourney}) for a leader moved
     * out of the herd area with no walk of its own. Its target is the leader at (x, y, z) and follows it
     * ({@link #followDrag}); it ends when the leader has stood still ({@link #countStill}) or takes over a walk
     * target ({@link #adoptTarget}). Refused while the herd is pinned to grazing.
     */
    boolean startDrag(double x, double y, double z, List<UUID> followerOrder, long now) {
        if (!startJourney(JourneyPolicy.Kind.URGENT, x, y, z, followerOrder, now)) {
            return false;
        }
        this.dragged = true;
        this.stillX = x;
        this.stillZ = z;
        this.stillTicks = 0;
        // Ahead from the first tick: a follower's one make-way check per journey may come right after this.
        followDrag(x, y, z);
        return true;
    }

    /**
     * Turns the journey in progress into a drag (P8c fix round 1: a leashed leader is followed along its route,
     * whatever journey it was on). Unlike {@link #startDrag} it keeps the trail, the travel order and the
     * journey count, so followers keep their slots. The leader is at (x, y, z); the still count starts there.
     * The straggler wait, a pause and the walk's path ends are dropped; the caller restores the leader's navigation
     * speed if {@link #leaderSlowed} and clears its walk target first.
     */
    void becomeDrag(double x, double y, double z) {
        this.journeyKind = JourneyPolicy.Kind.URGENT;
        this.dragged = true;
        this.stillX = x;
        this.stillZ = z;
        this.stillTicks = 0;
        this.waitedTicks = 0;
        this.leaderSlowed = false;
        this.stragglerSlow = false;
        this.hasPathEnd = false;
        clearPause();
        followDrag(x, y, z);
    }

    /** {@code true} while the journey is a drag ({@link #startDrag}, {@link #becomeDrag}). */
    boolean dragged() {
        return this.dragged;
    }

    /**
     * {@code true} while a dragged leader stands (it has stood still at least one tick, {@link #countStill}):
     * followers treat it like a leader that waits or settles.
     */
    boolean dragStanding() {
        return this.dragged && this.stillTicks > 0;
    }

    /**
     * A drag has no target of its own: it follows the leader at (x, y, z), {@link JourneyPolicy#DRAG_AHEAD}
     * ahead along the direction the leader was last dragged ({@link #leaderStep}), so the line from the leader
     * to it is the leader's line of travel (the make-way line, the debug readout).
     */
    void followDrag(double x, double y, double z) {
        this.journeyX = x + this.dragDirX * JourneyPolicy.DRAG_AHEAD;
        this.journeyY = y;
        this.journeyZ = z + this.dragDirZ * JourneyPolicy.DRAG_AHEAD;
    }

    /**
     * Counts the ticks a dragged leader has stood still: within {@link JourneyPolicy#DRAG_STILL_RADIUS} of
     * where it stopped. Moving farther makes the current position the new place it stopped, and the count
     * starts again from 0. Measured from where it stopped, not from the previous tick, so a slow creep
     * still counts as movement.
     *
     * @return the ticks stood still so far
     */
    int countStill(double x, double z) {
        if (JourneyPolicy.leftStillSpot(x - this.stillX, z - this.stillZ)) {
            this.stillX = x;
            this.stillZ = z;
            this.stillTicks = 0;
        } else {
            this.stillTicks++;
        }
        return this.stillTicks;
    }

    /**
     * The dragged leader took a walk target of its own (a need): the drag becomes an ordinary urgent journey
     * to it, with the usual arrival rules (within 3 blocks, or the walk ended).
     */
    void adoptTarget(double x, double y, double z) {
        this.dragged = false;
        this.journeyX = x;
        this.journeyY = y;
        this.journeyZ = z;
        this.hasPathEnd = false;
    }

    /**
     * The leader's horizontal move since its previous tick, for {@link JourneyPolicy#dragStarts}; records the
     * position for the next call, and the direction of a step of at least {@link JourneyPolicy#DRAG_MIN_STEP}
     * ({@link #followDrag}). 0 on the first tick, after a skipped tick (the leader was not ticked, for
     * example unloaded) and after a change of lead. Call once per leader tick.
     */
    double leaderStep(double x, double z, long now) {
        double step = 0D;
        if (this.lastLeaderTick != UNSET && now == this.lastLeaderTick + 1) {
            double dx = x - this.lastLeaderX;
            double dz = z - this.lastLeaderZ;
            step = Math.sqrt(dx * dx + dz * dz);
            if (step >= JourneyPolicy.DRAG_MIN_STEP) {
                this.dragDirX = dx / step;
                this.dragDirZ = dz / step;
            }
        }
        this.lastLeaderTick = now;
        this.lastLeaderX = x;
        this.lastLeaderZ = z;
        return step;
    }

    /** How many journeys have started (refused ones do not count). Followers compare it to notice a journey they missed. */
    int journeys() {
        return this.journeys;
    }

    JourneyPolicy.Kind journeyKind() {
        return this.journeyKind;
    }

    double journeyX() {
        return this.journeyX;
    }

    /** The journey target's feet height; NaN while a far roaming target is only a heading and a distance ({@link #journeyTargetKnown}). */
    double journeyY() {
        return this.journeyY;
    }

    double journeyZ() {
        return this.journeyZ;
    }

    /**
     * {@code true} if the journey target has a ground height (playtest fix P11). A roaming target that
     * {@code LeaderJourney} picks has none (it reads no blocks there) until its last leg resolves it
     * ({@link #retarget}); a GameTest goal, a need's walk target and a drag have one from the start.
     */
    boolean journeyTargetKnown() {
        return !Double.isNaN(this.journeyY);
    }

    /**
     * The last leg of a roaming journey resolved the target (playtest fix P11): the spot the leader stands on
     * at (or, when the target itself is not usable, beside) the target. Arrival is measured to it from now on.
     */
    void retarget(double x, double y, double z) {
        this.journeyX = x;
        this.journeyY = y;
        this.journeyZ = z;
    }

    /**
     * The leader's walk ended at (x, z) before the journey target (Review Focus 6: a vanilla walk reaches at
     * most the follow range and runs 150-249 ticks). {@code true} if the leader has moved at least
     * {@link JourneyPolicy#REISSUE_PROGRESS} since the previous end, or this is the first end, so the walk
     * target is set again; {@code false} when the walk ended twice in one place, which counts as arrival.
     */
    boolean pathEnded(double x, double z) {
        double dx = x - this.pathEndX;
        double dz = z - this.pathEndZ;
        boolean progressed = !this.hasPathEnd
                || dx * dx + dz * dz >= JourneyPolicy.REISSUE_PROGRESS * JourneyPolicy.REISSUE_PROGRESS;
        this.hasPathEnd = true;
        this.pathEndX = x;
        this.pathEndZ = z;
        return progressed;
    }

    /** The leader arrived: the herd stays in TRAVEL for {@link JourneyPolicy#SETTLE_TICKS} more ticks. */
    void arrive(long now) {
        if (this.journeyActive && this.settleUntil == UNSET) {
            this.settleUntil = now + JourneyPolicy.SETTLE_TICKS;
        }
        this.leaderSlowed = false;
        this.stragglerSlow = false;
        clearPause();
    }

    boolean settling() {
        return this.settleUntil != UNSET;
    }

    boolean settleDone(long now) {
        return settling() && now >= this.settleUntil;
    }

    /** Ends the journey: GRAZE, and the herd area recentres on the leader. */
    void endJourney(double leaderX, double leaderY, double leaderZ) {
        this.state = HerdState.GRAZE;
        this.journeyActive = false;
        this.dragged = false;
        this.settleUntil = UNSET;
        this.leaderSlowed = false;
        this.stragglerSlow = false;
        this.waitedTicks = 0;
        clearPause();
        recentre(leaderX, leaderY, leaderZ);
    }

    void addWait() {
        this.waitedTicks++;
    }

    int waitedTicks() {
        return this.waitedTicks;
    }

    /** {@code true} while the straggler wait slows the leader ({@code HerdLeading.gather}; the log lines at its start and end). */
    boolean stragglerSlow() {
        return this.stragglerSlow;
    }

    void setStragglerSlow(boolean slow) {
        this.stragglerSlow = slow;
    }

    /**
     * Pauses the roaming journey for {@code kind} (P13a items 1 and 5): {@code HerdLeading} sets no leg, counts
     * no path end and no wait while it lasts. {@code true} if this starts a pause (none before, or another kind:
     * the pause ticks count from now).
     */
    boolean startPause(JourneyPolicy.Pause kind, long now) {
        if (kind == this.pause) {
            return false;
        }
        this.pause = kind;
        this.pauseStartTick = now;
        return true;
    }

    JourneyPolicy.Pause pause() {
        return this.pause;
    }

    /** {@code true} while the journey is paused: followers treat the leader like one that settles (no stuck window). */
    boolean journeyPaused() {
        return this.pause != JourneyPolicy.Pause.NONE;
    }

    /** Ticks since the current pause started; 0 without a pause. */
    int pausedTicks(long now) {
        return journeyPaused() ? (int) Math.min(Integer.MAX_VALUE, now - this.pauseStartTick) : 0;
    }

    /** The herd leaves its slots to rest for the rest of this pause (ruling Q1); counted once per pause ({@link #spreads}). */
    void spreadHerd() {
        if (!this.herdSpread) {
            this.herdSpread = true;
            this.spreads++;
        }
    }

    /** How many times a herd spread out to rest (fix round 1, item 6). */
    int spreads() {
        return this.spreads;
    }

    /** Sets whether the leader is in its get-up during the pause ({@link JourneyPolicy#herdSpreads}). */
    void setLeaderRising(boolean rising) {
        this.leaderRising = rising;
    }

    /** {@code true} while the paused leader gets up: members at a rest spot no longer lie down ({@link JourneyPolicy#sleepsAtSpot}). */
    boolean leaderRising() {
        return this.leaderRising;
    }

    /** {@code HerdNeeds.lieDown} marked this member tired (fix round 1, item 5); kept once. */
    void markTired(UUID member) {
        if (!this.tiredMarks.contains(member)) {
            this.tiredMarks.add(member);
        }
    }

    boolean hasTiredMarks() {
        return !this.tiredMarks.isEmpty();
    }

    /**
     * The members marked tired so far, as a new list, and forgets them. The leader's tick takes them once no
     * pause is on, and clears the tired memory of each that never started its rest.
     */
    List<UUID> takeTiredMarks() {
        List<UUID> marks = new ArrayList<>(this.tiredMarks);
        this.tiredMarks.clear();
        return marks;
    }

    /**
     * {@code true} if a straggler slow-walk start may be logged at {@code now}: at least
     * {@link #STRAGGLER_LOG_MIN_TICKS} after the last logged start. Records the result for its end line
     * ({@link #takeStragglerStartLogged}). Debug only.
     */
    boolean mayLogStragglerStart(long now) {
        this.stragglerStartLogged = this.stragglerLogTick == UNSET || now - this.stragglerLogTick >= STRAGGLER_LOG_MIN_TICKS;
        if (this.stragglerStartLogged) {
            this.stragglerLogTick = now;
        }
        return this.stragglerStartLogged;
    }

    /** {@code true} once if the current slow walk's start was logged, so its end is logged too. */
    boolean takeStragglerStartLogged() {
        boolean logged = this.stragglerStartLogged;
        this.stragglerStartLogged = false;
        return logged;
    }

    /** {@code true} while the herd rests spread out round the leader (members pick rest spots, {@code FollowFormation}). */
    boolean herdSpread() {
        return this.herdSpread;
    }

    /**
     * The leader is up and done (P13a item 1): the pause ends, and the previous path end is forgotten, so the
     * walk ends of the pause do not make the next end look like "twice in one place". The straggler wait
     * starts again (fix round 1). A herd that spread out drops its rest spots.
     *
     * @return {@code true} if the herd had spread out: the caller rebuilds the travel order from the members'
     *         positions, so the wedge re-forms
     */
    boolean endPause() {
        boolean spread = this.herdSpread;
        clearPause();
        this.hasPathEnd = false;
        // Fix round 1 (lead ruling Q1): members that rose late follow, and the straggler wait covers them again.
        this.waitedTicks = 0;
        this.stragglerSlow = false;
        return spread;
    }

    private void clearPause() {
        if (this.herdSpread) {
            this.spots.clear();
        }
        this.pause = JourneyPolicy.Pause.NONE;
        this.pauseStartTick = UNSET;
        this.herdSpread = false;
        this.leaderRising = false;
    }

    /**
     * Records why the journey ended, once per journey (the first reason wins), for one server log line
     * ({@link #takeEnd}). Nothing without a journey.
     *
     * @param leader the leader that led it; the leader at (leaderX, leaderZ) at tick {@code now}
     */
    void recordEnd(JourneyPolicy.End reason, @Nullable UUID leader, double leaderX, double leaderZ, long now) {
        if (!this.journeyActive || this.endRecorded) {
            return;
        }
        this.endRecorded = true;
        double headArc = this.trail.headArc();
        this.unloggedEnd = new JourneyPolicy.JourneyEnd(leader, reason, this.journeyKind,
                Math.hypot(this.journeyX - leaderX, this.journeyZ - leaderZ),
                Math.hypot(leaderX - this.journeyStartX, leaderZ - this.journeyStartZ),
                Double.isNaN(headArc) ? 0D : headArc,
                this.journeyStartTick == UNSET ? 0L : now - this.journeyStartTick);
    }

    /** The journey end recorded since the last call, or {@code null}; each end is handed out once. */
    @Nullable JourneyPolicy.JourneyEnd takeEnd() {
        JourneyPolicy.JourneyEnd end = this.unloggedEnd;
        this.unloggedEnd = null;
        return end;
    }

    boolean leaderSlowed() {
        return this.leaderSlowed;
    }

    void setLeaderSlowed(boolean slowed) {
        this.leaderSlowed = slowed;
    }

    /** While pinned, no journey starts (roaming or urgent). For GameTests that need a grazing herd. */
    public void pinGraze(boolean pinned) {
        this.grazePinned = pinned;
    }

    boolean grazePinned() {
        return this.grazePinned;
    }

    long nextJourneyTick() {
        return this.nextJourneyTick;
    }

    void setNextJourneyTick(long tick) {
        this.nextJourneyTick = tick;
    }

    long nextGrazeMoveTick() {
        return this.nextGrazeMoveTick;
    }

    void setNextGrazeMoveTick(long tick) {
        this.nextGrazeMoveTick = tick;
    }

    boolean hasCentre() {
        return this.hasCentre;
    }

    public double centreX() {
        return this.centreX;
    }

    public double centreZ() {
        return this.centreZ;
    }

    /**
     * Feet height of the centre, the ground where the leader stood: the height a member walking back into the
     * area paths to (playtest 2 fix P8a: the leader's current height is elsewhere while it is dragged on a lead).
     */
    double centreY() {
        return this.centreY;
    }

    /** Moves the herd area's centre (spec: only after a journey, or when the leader stood outside it) to the leader's feet. */
    void recentre(double x, double y, double z) {
        this.hasCentre = true;
        this.centreX = x;
        this.centreY = y;
        this.centreZ = z;
        this.outsideTicks = 0;
    }

    /** Counts ticks the leader stands outside the herd area in a row; returns the count. */
    int countOutside(boolean outside) {
        this.outsideTicks = outside ? this.outsideTicks + 1 : 0;
        return this.outsideTicks;
    }

    /** Herd area radius for members of this personal space and body width, at this herd's {@link #areaScale}. */
    public double radius(double personalSpace, double bodyWidth) {
        return HerdArea.radius(size(), HerdArea.grazeSpacing(personalSpace), bodyWidth, this.areaScale);
    }

    double areaScale() {
        return this.areaScale;
    }

    /**
     * Sets this herd's area scale (the radius factor, {@link HerdArea#radius}). For GameTests whose arenas were
     * sized for scale 1. The scale goes with the group: a group merged away loses it.
     */
    public void setAreaScale(double scale) {
        this.areaScale = scale;
    }

    void reserveSpot(UUID member, double x, double z) {
        this.spots.put(member, new HerdArea.Point(x, z));
    }

    void releaseSpot(UUID member) {
        this.spots.remove(member);
    }

    @Nullable HerdArea.Point spotOf(UUID member) {
        return this.spots.get(member);
    }

    /** Every reserved graze spot, the leader's included, as a read-only view (debug visuals). */
    Collection<HerdArea.Point> reservedSpots() {
        return this.spotView;
    }

    /** {@code true} while the lead has this herd's debug visuals switched on. */
    public boolean debugVisuals() {
        return this.debugVisuals;
    }

    /** Switches this herd's debug visuals (the debug stick). The flag goes with the group: a group merged away loses it. */
    public void setDebugVisuals(boolean on) {
        this.debugVisuals = on;
    }

    /** The other members' reserved spots, as a new list (spot picks only). */
    List<HerdArea.Point> reservedSpotsExcept(UUID member) {
        List<HerdArea.Point> reserved = new ArrayList<>(this.spots.size());
        for (Map.Entry<UUID, HerdArea.Point> entry : this.spots.entrySet()) {
            if (!entry.getKey().equals(member)) {
                reserved.add(entry.getValue());
            }
        }
        return reserved;
    }

    /** x of the member's graze spot, NaN without one. Public for GameTests. */
    public double spotX(UUID member) {
        HerdArea.Point spot = this.spots.get(member);
        return spot == null ? Double.NaN : spot.x();
    }

    /** z of the member's graze spot, NaN without one. Public for GameTests. */
    public double spotZ(UUID member) {
        HerdArea.Point spot = this.spots.get(member);
        return spot == null ? Double.NaN : spot.z();
    }

    /** The followers in travel order, read-only. */
    List<UUID> travelOrder() {
        return this.travelOrderView;
    }

    boolean orderDirty() {
        return this.orderDirty;
    }

    /** Sets the travel order (followers nearest-first); the leader and non-members are skipped. */
    void setTravelOrder(List<UUID> followerOrder) {
        this.travelOrder.clear();
        for (UUID member : followerOrder) {
            if (this.members.contains(member) && !member.equals(this.leader) && !this.travelOrder.contains(member)) {
                this.travelOrder.add(member);
            }
        }
        this.orderDirty = false;
        this.layoutTick = UNSET;
    }

    /** Who goes first (spec "Who yields"): the leader 0, then the travel order from 1, others last. */
    int rank(UUID member) {
        if (member.equals(this.leader)) {
            return 0;
        }
        int index = this.travelOrder.indexOf(member);
        return index < 0 ? Integer.MAX_VALUE : index + 1;
    }

    /** Lays out the travel targets once per tick, from the trail's head (the leader) back (see {@link TravelOrder#layout}). */
    void refreshLayout(long now, double openSpacing, double tightSpacing) {
        if (this.layoutTick == now) {
            return;
        }
        this.layoutTick = now;
        int followers = this.travelOrder.size();
        if (this.targetArcs.length < followers) {
            this.targetArcs = new double[followers];
            this.laneOffsets = new double[followers];
        }
        double head = this.trail.headArc();
        this.layoutHeadArc = head;
        if (Double.isNaN(head)) {
            Arrays.fill(this.targetArcs, 0, followers, Double.NaN);
            Arrays.fill(this.laneOffsets, 0, followers, 0D);
            return;
        }
        TravelOrder.layout(head, followers, openSpacing, tightSpacing, this.roomAtArc, this.targetArcs, this.laneOffsets);
    }

    /** The member's target arc from the latest layout; NaN if it has none. Public for GameTests. */
    public double targetArcOf(UUID member) {
        int index = this.travelOrder.indexOf(member);
        return index < 0 || this.layoutTick == UNSET ? Double.NaN : this.targetArcs[index];
    }

    /** The member's lane offset from the latest layout; NaN if it has none. Public for GameTests. */
    public double laneOffsetOf(UUID member) {
        int index = this.travelOrder.indexOf(member);
        return index < 0 || this.layoutTick == UNSET ? Double.NaN : this.laneOffsets[index];
    }

    /**
     * The member's slot distance from the latest layout: sqrt(depth^2 + offset^2), with the depth of its
     * target arc behind the trail head the layout was laid from and its lane offset
     * ({@link JourneyPolicy.Tally}). NaN if it has no slot.
     */
    double slotDistanceOf(UUID member) {
        int index = this.travelOrder.indexOf(member);
        if (index < 0 || this.layoutTick == UNSET) {
            return Double.NaN;
        }
        double depth = this.layoutHeadArc - this.targetArcs[index];
        return Math.sqrt(depth * depth + this.laneOffsets[index] * this.laneOffsets[index]);
    }

    /** {@code true} if one more context-steering evaluation fits this herd's budget for this tick. */
    boolean tryEvaluate(long now) {
        if (now != this.evaluationTick) {
            this.evaluationTick = now;
            this.evaluations = 0;
        }
        if (this.evaluations >= MAX_EVALUATIONS_PER_TICK) {
            return false;
        }
        this.evaluations++;
        return true;
    }

    LeaderTrail trail() {
        return this.trail;
    }

    FallbackBudget fallbacks() {
        return this.fallbacks;
    }

    BodyFit fit() {
        return this.fit;
    }

    HerdBodies bodies() {
        return this.bodies;
    }

    /** One line for the debug stick: herd state from the first step of a journey, centre, spot, travel target (on a journey), rank. */
    public String describe(UUID member) {
        StringBuilder text = new StringBuilder("Herd: ").append(this.state);
        if (this.journeyActive) {
            text.append(", journey ").append(this.journeyKind)
                    .append(String.format(" to %.0f %.0f", this.journeyX, this.journeyZ))
                    .append(this.dragged ? " (dragged)" : "")
                    .append(settling() ? " (settling)" : "")
                    .append(this.leaderSlowed ? " (leader slowed)" : "")
                    .append(this.pause == JourneyPolicy.Pause.REST ? " (leader resting)" : "")
                    .append(this.herdSpread ? " (herd resting)" : "");
        }
        if (this.hasCentre) {
            text.append(String.format(", centre %.1f %.1f", this.centreX, this.centreZ));
        }
        HerdArea.Point spot = this.spots.get(member);
        text.append(spot == null ? ", spot: none" : String.format(", spot %.1f %.1f", spot.x(), spot.z()));
        double arc = this.journeyActive ? targetArcOf(member) : Double.NaN;
        if (!Double.isNaN(arc)) {
            text.append(String.format(", target arc %.1f lane %.1f", arc, laneOffsetOf(member)));
        }
        int rank = rank(member);
        return text.append(", rank ").append(rank == Integer.MAX_VALUE ? "-" : String.valueOf(rank)).toString();
    }

    /**
     * One line for the debug stick on the leader: ticks until the leader's next graze move and next journey
     * try (negative: due, waiting for the leader to be idle with no walk target in GRAZE), so an idle leader
     * can be told apart from a broken one.
     */
    public String describeSchedule(long now) {
        return "Leader schedule: next graze move " + ticksUntil(this.nextGrazeMoveTick, now)
                + ", next journey " + ticksUntil(this.nextJourneyTick, now) + (this.grazePinned ? " (pinned to graze)" : "");
    }

    private static String ticksUntil(long tick, long now) {
        return tick == UNSET ? "not scheduled" : "in " + (tick - now) + " ticks";
    }

    /**
     * Side room on the tighter side near an arc: the tightest point within {@link #ROW_ROOM_WINDOW} either
     * way, so lanes per row do not flicker between trail points. NaN when not measured (no point, or either
     * side unknown anywhere in the window).
     */
    private double roomAt(double arc) {
        if (this.trail.size() == 0) {
            return Double.NaN;
        }
        return Math.min(this.trail.minRoom(arc - ROW_ROOM_WINDOW, arc + ROW_ROOM_WINDOW, true),
                this.trail.minRoom(arc - ROW_ROOM_WINDOW, arc + ROW_ROOM_WINDOW, false));
    }

    /**
     * {@code true} if the lead changed hands and the new leader's walk is not reset yet. The first leader of
     * a new group is no change. {@code HerdLeading.settleLeaderChange} resets the walks (entity side) and
     * then calls {@link #leaderChangeSettled}.
     */
    boolean leaderChanged() {
        return this.leaderChanged;
    }

    /** Read-only: the animals that lost the lead (here, or in a group merged into this one) since the last settle. */
    List<UUID> deposedLeaders() {
        return this.deposedView;
    }

    void leaderChangeSettled() {
        this.leaderChanged = false;
        this.deposed.clear();
    }

    /** A merge dissolves {@code from}: its deposed leaders are this group's to settle now (its own leader is one). */
    void takeDeposed(DinoGroup from) {
        for (UUID member : from.deposed) {
            depose(member);
        }
        from.leaderChangeSettled();
    }

    private void depose(UUID member) {
        if (!member.equals(this.leader) && !this.deposed.contains(member)) {
            this.deposed.add(member);
        }
    }

    /**
     * A new leader walks a new route: the trail goes, and a journey in progress ends (the herd grazes). The
     * herd area's centre is dropped too (spec: the centre moves to the leader at the end of a journey), so
     * followers do not turn back to a centre the old leader left behind; the leader's tick recentres on the
     * new leader when there is no centre. The walks of the old and the new leader are reset entity-side
     * (playtest 1, L1 and L2): the old one could keep the journey walk at a slowed speed, the new one a
     * follower's fallback walk that starts an urgent trip.
     *
     * @param previous the leader before the change, or {@code null} if the group had none (a new group)
     */
    private void onLeaderChange(@Nullable UUID previous) {
        if (previous != null) {
            this.leaderChanged = true;
            depose(previous);
        }
        // Before the trail and the last leader tick go: they give the end's distances and time.
        recordEnd(JourneyPolicy.End.LEADER_CHANGE, previous, this.lastLeaderX, this.lastLeaderZ,
                this.lastLeaderTick == UNSET ? this.journeyStartTick : this.lastLeaderTick);
        this.trail.clear();
        this.layoutTick = UNSET;
        this.lastLeaderTick = UNSET;
        if (this.state == HerdState.TRAVEL || this.journeyActive) {
            this.state = HerdState.GRAZE;
            this.journeyActive = false;
            this.dragged = false;
            this.settleUntil = UNSET;
            this.leaderSlowed = false;
            this.stragglerSlow = false;
            clearPause();
            this.hasCentre = false;
        }
    }

    private void electLeader(Roster roster) {
        UUID best = null;
        float bestDominance = 0F;
        for (UUID member : this.members) {
            if (!roster.isPresent(member)) {
                continue;
            }
            float dominance = roster.dominance(member);
            if (best == null || Dominance.outranks(dominance, member, bestDominance, best)) {
                best = member;
                bestDominance = dominance;
            }
        }
        boolean changed = best == null ? this.leader != null : !best.equals(this.leader);
        UUID previous = this.leader;
        this.leader = best;
        if (changed) {
            onLeaderChange(previous);
        }
    }
}
