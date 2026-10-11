package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * The leader's timing and rules (spec "Leader", lead decisions 2 and 3). Pure: {@link LeaderJourney} and
 * {@link HerdLeading} apply it.
 * <ul>
 *   <li>Graze move every 400-800 ticks: walk 3-8 blocks to a free spot inside the herd area at
 *       {@link #GRAZE_MOVE_SPEED}.</li>
 *   <li>Roaming journey every 2400-6000 ticks: {@link #JOURNEY_MIN}-{@link #JOURNEY_MAX} (156-314) blocks at
 *       {@link #JOURNEY_SPEED}; a try that finds no target is tried again after {@link #JOURNEY_RETRY_TICKS}. Needs (water, food) also start journeys:
 *       those are urgent. A roaming journey starts only outside the rest hours (night for a day animal, from
 *       {@link #SUNSET_DAY_TIME}) and with at least {@link #JOURNEY_MIN_STAMINA_FRACTION} of the maximum stamina
 *       ({@link #roamMayStart}, P13a rulings Q2 and Q3); otherwise the try fails and is retried as usual.</li>
 *   <li>Legs (playtest fix P11): the roaming target is a heading and a distance; the leader walks toward it in
 *       legs of at most {@link #LEG_LENGTH}, each resolved only when it is needed ({@link #chooseLeg}). The pick
 *       reads only the first leg's points (loaded and entity-ticking chunks only), never the target. No usable
 *       leg ends the journey where the leader is, as an arrival.</li>
 *   <li>Arrival: within 3 blocks of the target, or when the path ends without progress. A vanilla walk
 *       reaches at most the follow range (35 blocks) and runs 150-249 ticks, so on a roaming journey the
 *       leader re-sets its walk target each time a walk ends while it has moved at least
 *       {@link #REISSUE_PROGRESS} since the previous end ({@code DinoGroup.pathEnded}); a walk that ends
 *       twice in one place is the arrival. Then {@link #SETTLE_TICKS} more in TRAVEL before GRAZE.</li>
 *   <li>Pause (roaming only, P13a): a leader that is down (sits down, rests, gets up) pauses the journey
 *       ({@link #pause}): no path end, no leg, no wait. When it is up it walks the next leg toward the same
 *       target. The herd keeps its slots in a daytime rest up to {@link #SHORT_REST_PAUSE_TICKS}; at night or
 *       after that it spreads out to rest ({@link #herdSpreads}), but never while the leader gets up. In the
 *       rest hours the leader lies down (on dry ground), so a journey caught by night pauses until morning
 *       (ruling Q2).</li>
 *   <li>Waiting (roaming only): fewer than 75% of the counted followers close to their own slot in the
 *       wedge ({@link Tally}). Only followers whose slot is on the trail count; members farther than 48
 *       blocks do not. The leader walks on at {@link #WAIT_SLOW_SPEED} (P13a ruling 3a: no full stop) for at
 *       most {@link #MAX_WAIT_TICKS} per journey. Urgent trips never wait.</li>
 *   <li>Dragged leader (playtest 2 fix P8c): a leader that moves out of the herd area in GRAZE with no walk of
 *       its own (dragged on a lead, pushed, knocked back, a short teleport) starts an urgent journey at once
 *       ({@link #dragStarts}), so the herd follows its trail. It has no fixed target; it ends when the leader
 *       has stood still for {@link #DRAG_SETTLE_TICKS} ({@link #dragSettled}), or by the usual urgent rules
 *       once the leader takes a walk target of its own.</li>
 *   <li>Recentre the herd area when the leader has stood outside it for 100 ticks: only for a leader that is
 *       not dragged (it stands outside, or its own walk inside the area took it out).</li>
 * </ul>
 */
final class JourneyPolicy {

    /** Why the herd travels. */
    enum Kind {
        /** A roaming journey the leader chose; the leader waits for stragglers. */
        ROAM,
        /** A need (water, food) outside the herd area; the leader never waits. */
        URGENT
    }

    /** What the leader does about stragglers this tick (P13a ruling 3a: a slow walk, never a stop). */
    enum Gathering {
        GO,
        SLOW
    }

    /** Why a roaming journey is paused this tick ({@link #pause}). */
    enum Pause {
        NONE,
        /** The leader is down: it sits down, rests or gets up. */
        REST
    }

    /** Why a journey ended (P13a item 8: one server log line per journey end). */
    enum End {
        /** Within {@link #ARRIVE_DISTANCE} of the target. */
        ARRIVED,
        /** A roaming walk ended twice in one place. */
        NO_PROGRESS,
        /** No usable leg on a re-issue. */
        NO_LEG,
        /** No usable first leg when the leader's journey call ended. */
        NO_FIRST_LEG,
        /** An urgent trip's walk (the need's) ended before the target. */
        URGENT_WALK_END,
        /** A drag journey: the leader stood still for {@link #DRAG_SETTLE_TICKS}. */
        DRAG_SETTLED,
        /** The lead changed hands. */
        LEADER_CHANGE,
        /** A new journey started over this one (a GameTest's {@code beginRoam}). */
        REPLACED
    }

    /**
     * One journey end, for the server log ({@link #describe}).
     *
     * @param leader       the leader that led the journey; {@code null} if unknown
     * @param distanceLeft horizontal distance from the leader to the journey target at the end
     * @param fromStart    straight horizontal distance from where the leader was at the start; NaN if unknown
     * @param walked       trail length the leader walked
     */
    record JourneyEnd(@Nullable UUID leader, End reason, Kind kind, double distanceLeft, double fromStart, double walked, long ticks) {

        String describe() {
            return String.format(Locale.ROOT, "%s journey ended %s: %.1f blocks from the target, %.1f from the start, walked %.1f, %d ticks",
                    this.kind, this.reason, this.distanceLeft, this.fromStart, this.walked, this.ticks);
        }
    }

    static final int GRAZE_MOVE_MIN_TICKS = 400;
    static final int GRAZE_MOVE_SPREAD_TICKS = 400;
    static final double GRAZE_MOVE_MIN = 3.0D;
    static final double GRAZE_MOVE_MAX = 8.0D;
    /** The old build's leader wander modifier: 0.086 blocks per tick (speed is quadratic in the modifier, {@link GroundSpeed}). */
    static final float GRAZE_MOVE_SPEED = 0.8F;
    static final int JOURNEY_MIN_TICKS = 2400;
    static final int JOURNEY_SPREAD_TICKS = 3600;
    /**
     * Shortest roaming journey (lead, playtest fix P11): P9's 78 (32 blocks x sqrt(6) = 78.4, rounded) x 2 = 156.
     * With the herd area at 6x ({@link HerdArea#HERD_AREA_SCALE}) a journey still always leaves it: 156 is more
     * than 2R (54.9 for 20 Gallimimus).
     */
    static final double JOURNEY_MIN = 156.0D;
    /** Longest roaming journey (lead, playtest fix P11): P9's 157 (64 blocks x sqrt(6) = 156.8, rounded) x 2 = 314. */
    static final double JOURNEY_MAX = 314.0D;
    /**
     * A roaming journey is walked in legs of at most this many blocks toward its target (playtest fix P11): the
     * target is 156-314 blocks away, mostly in chunks the leader cannot read. 24 is well inside the 35-block
     * reach of a vanilla path (the follow range), so the path can bend round obstacles, and longer than one walk
     * (at most 249 ticks at {@link #JOURNEY_SPEED}: 21.4 blocks), so a walk usually times out before its leg
     * point and the leader takes the next leg on the usual re-issue.
     */
    static final double LEG_LENGTH = 24.0D;
    /** A leg point that is not usable is tried again with the heading turned by this much, to each side. */
    static final double LEG_OFFSET_DEGREES = 20.0D;
    /** Heading offsets tried to each side: +20, -20, +40, -40 degrees ({@link #legOffset}). */
    static final int LEG_OFFSET_PAIRS = 2;
    /** Leg points tried per leg: straight ahead, then each offset ({@link #chooseLeg}). */
    static final int LEG_TRIES = 1 + 2 * LEG_OFFSET_PAIRS;
    /** The old build's leader journey modifier: 0.086 blocks per tick (1.7 per second). */
    static final float JOURNEY_SPEED = 0.8F;
    /**
     * Lead ruling 14 (fix P12c): a roaming journey's leg point, and so the journey target its last leg resolves,
     * lies at most this many blocks above or below the leader's feet where the leg is chosen
     * ({@link #withinJourneyHeight}). The surface probe takes the highest ground in a column (a cliff top, a
     * roof), which the herd often cannot walk to. Measured from the leader, not the herd centre: the centre's
     * height is where the leader stood when the graze area was centred, often 100 blocks back on a journey,
     * and the leader is where the leg starts. 8 over a 24-block leg ({@link #LEG_LENGTH}) is a 1-in-3 slope,
     * which a path with step height 1 climbs. A point that fails counts as an unusable point: the heading
     * offsets are tried, then the usual failure (no leg).
     */
    static final double JOURNEY_MAX_HEIGHT_DIFF = 8.0D;
    /** Random positions tried for a journey target before the journey is put off. */
    static final int JOURNEY_ATTEMPTS = 10;
    /**
     * A journey try that started no journey (no target found) is tried again this many ticks later, not a whole
     * interval later (playtest 1, finding R3: one failed try cost 2-5 minutes and hid the failure).
     */
    static final int JOURNEY_RETRY_TICKS = 200;
    static final double ARRIVE_DISTANCE = 3.0D;
    /** A walk that ends at least this far from the previous end counts as progress: the walk target is set again. */
    static final double REISSUE_PROGRESS = 0.5D;
    static final int SETTLE_TICKS = 100;
    static final double GATHERED_FRACTION = 0.75D;
    static final double STRAGGLER_IGNORE_DISTANCE = 48.0D;
    /**
     * A follower is close up to this far beyond its slot's distance from the leader
     * ({@link #followerClose}). It covers the 1.06 blocks a follower keeps behind its slot at
     * {@link #JOURNEY_SPEED} ({@code TravelOrder.followSpeed}) and the up to 1 block the leader is past the
     * newest trail point, with about 3 blocks to spare; a follower a whole open row (4.2 for Gallimimus)
     * behind its slot is a straggler. Playtest 1 (finding D) replaced "within 2R of the leader", which a
     * correct herd could not meet: a 20-animal wedge is 29.4 deep and 2R is 22.4, so only 11 of 19 followers
     * in their slots were close (14 of 19, 73.7%, at the old spacing), under 75%.
     */
    static final double GATHER_MARGIN = 5.0D;
    /**
     * Slowed for stragglers, for the whole wait (P13a ruling 3a; 0.6 is a lead ruling): 0.135 x 0.6^2 = about
     * 0.049 blocks per tick, 56% of the journey ground speed (0.086). Not below
     * {@link GroundSpeed#MIN_WALK_MODIFIER}, or the leader would glide in the idle pose ({@code JourneyPolicyTest}).
     * The walk target and path stay; only the navigation speed changes.
     */
    static final float WAIT_SLOW_SPEED = 0.6F;
    static final int MAX_WAIT_TICKS = 200;
    /**
     * Ruling Q1: a daytime rest pause up to this long (20 s) keeps every member in its formation slot; a longer
     * one, or any at night, lets the herd spread out to rest ({@link #herdSpreads}).
     */
    static final int SHORT_REST_PAUSE_TICKS = 400;
    /** Sunset: from this day time on it is night, the same test as {@code Rest} ({@link #isNight}). */
    static final long SUNSET_DAY_TIME = 12000L;
    static final long DAY_TICKS = 24000L;
    /** Ruling Q3: a roaming journey starts only with at least this fraction of the maximum stamina (150 of 200 for Gallimimus). */
    static final double JOURNEY_MIN_STAMINA_FRACTION = 0.75D;
    static final int OUTSIDE_RECENTRE_TICKS = 100;
    /**
     * A leader beyond the herd area (plus {@link #DRAG_EDGE_MARGIN}) that moved at least this far since its
     * previous tick, with no walk of its own, is dragged ({@link #dragStarts}). Above
     * {@link HerdBodies#WALKING_SPEED} (0.01, the herd's own threshold for a moving body), so a body that only
     * sways does not count. Well below the slowest drag: within 6 blocks of the holder the lead moves the mob by
     * its own navigation at {@code followLeashSpeed} (vanilla 1.0), so the slowest drag is the leader's own
     * walk on a lead (0.135 blocks per tick at the base attribute); farther out the lead pulls by velocity,
     * faster still. A push that passes it is harmless: a short journey, then the usual recentre.
     */
    static final double DRAG_MIN_STEP = 0.03D;
    /**
     * A dragged leader counts as standing still while it stays within this distance of where it stopped
     * ({@link #leftStillSpot}). Small shoves from herd mates and a leash's slack stay inside; one trail spacing
     * ({@code LeaderTrail.SPACING}) of real movement does not.
     */
    static final double DRAG_STILL_RADIUS = 1.0D;
    /**
     * A drag starts only this far beyond the herd radius R (P8c fix round 1). The brain erases the walk target
     * when the leader's own graze move ends, before the leader's herd tick, while the leader still moves a
     * stride (0.086 blocks per tick) plus some momentum (about 0.05, then 0.03); graze spots have no edge
     * margin, so a normal graze move to the edge can leave the leader just outside R with no walk target and a
     * step above {@link #DRAG_MIN_STEP}. 2 blocks is far more than that coast, and small next to R (27.4 for
     * 20 Gallimimus), so a real drag is still caught within about 10 ticks of a walking drag. A leader that
     * stops between R and R + 2 is the recentre rule's.
     */
    static final double DRAG_EDGE_MARGIN = 2.0D;
    /**
     * A drag journey ends (GRAZE, recentre on the leader) when the leader has stood still this long (5 s). It
     * is also the settle time: the herd closes up in TRAVEL while the leader stands, so no
     * {@link #SETTLE_TICKS} follow. A pause in the drag shorter than this (the player turns, opens a gate)
     * keeps the journey and its trail.
     */
    static final int DRAG_SETTLE_TICKS = 100;
    /**
     * A drag journey's target lies this far ahead of the leader along the direction it was last dragged
     * ({@code DinoGroup.followDrag}). It only gives the leader's line of travel to followers that step out of
     * it ({@code FollowFormation.makeWay}); it is never walked to. Longer than one tick's drag (a sprinting
     * player moves 0.28 blocks per tick), so the line points forward even to a follower that reads it after
     * the leader's move in the same tick.
     */
    static final double DRAG_AHEAD = 4.0D;

    private JourneyPolicy() {
    }

    static long nextGrazeMove(long now, double unit) {
        return now + GRAZE_MOVE_MIN_TICKS + (long) (unit * GRAZE_MOVE_SPREAD_TICKS);
    }

    static long nextJourney(long now, double unit) {
        return now + JOURNEY_MIN_TICKS + (long) (unit * JOURNEY_SPREAD_TICKS);
    }

    /**
     * The next journey tick after a try at {@code now}: a full interval when the try started a journey, else
     * {@link #JOURNEY_RETRY_TICKS} later.
     */
    static long nextJourneyAfterTry(long now, double unit, boolean started) {
        return started ? nextJourney(now, unit) : now + JOURNEY_RETRY_TICKS;
    }

    /** Lead ruling 14: a leg point's feet height {@code y} is within {@link #JOURNEY_MAX_HEIGHT_DIFF} of the leader's {@code fromY}. */
    static boolean withinJourneyHeight(double y, double fromY) {
        return Math.abs(y - fromY) <= JOURNEY_MAX_HEIGHT_DIFF;
    }

    static boolean isJourneyDistance(double distance) {
        return distance >= JOURNEY_MIN && distance <= JOURNEY_MAX;
    }

    /** The ground height where the leader's clearance box stands at (x, z), or NaN if the point is not usable. */
    @FunctionalInterface
    interface StandHeight {
        double at(double x, double z);
    }

    /**
     * The walk target of one leg.
     *
     * @param last the leg ends at the journey target (the target was within {@link #LEG_LENGTH})
     */
    record Leg(double x, double y, double z, boolean last) {
    }

    /**
     * The heading offset of leg try {@code attempt}, in radians: 0 for try 0 (straight toward the target), then
     * +{@link #LEG_OFFSET_DEGREES}, -{@link #LEG_OFFSET_DEGREES}, +2 x, -2 x, and so on.
     */
    static double legOffset(int attempt) {
        if (attempt == 0) {
            return 0D;
        }
        int step = (attempt + 1) / 2;
        double sign = attempt % 2 == 1 ? 1D : -1D;
        return sign * step * Math.toRadians(LEG_OFFSET_DEGREES);
    }

    /**
     * The point of leg try {@code attempt} from (fromX, fromZ) toward (targetX, targetZ): along the heading turned
     * by {@link #legOffset}, at {@link #LEG_LENGTH} or at the target's distance when the target is nearer. Try 0
     * with the target within {@link #LEG_LENGTH} is the target itself, exactly.
     */
    static HerdArea.Point legPoint(double fromX, double fromZ, double targetX, double targetZ, int attempt) {
        double dx = targetX - fromX;
        double dz = targetZ - fromZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (attempt == 0 && distance <= LEG_LENGTH) {
            return new HerdArea.Point(targetX, targetZ);
        }
        double heading = Math.atan2(dz, dx) + legOffset(attempt);
        double reach = Math.min(distance, LEG_LENGTH);
        return new HerdArea.Point(fromX + Math.cos(heading) * reach, fromZ + Math.sin(heading) * reach);
    }

    /**
     * The next leg of a roaming journey (playtest fix P11): the first usable point of {@link #LEG_TRIES}, straight
     * toward the target and then at the heading offsets ({@link #legOffset}), each at {@link #legPoint}; the leg
     * is the last one when the target is within {@link #LEG_LENGTH}. A point whose ground is more than
     * {@link #JOURNEY_MAX_HEIGHT_DIFF} above or below {@code fromY} is not usable (lead ruling 14). A target whose height is known
     * ({@code knownTargetY} not NaN: a last leg already resolved, or a GameTest goal) is the walk target as it
     * is, with no probe and no legs in between. {@code null} if no point is usable: the journey then ends where
     * the leader is, as an arrival. {@code LeaderJourney} also uses it when it picks a journey: a heading counts
     * only if its first leg is usable. At most {@link #LEG_TRIES} probes per call.
     *
     * @param fromY the leader's feet height, for the height limit
     * @param stand the leader's ground height at a point, NaN where it cannot stand or the chunk is not loaded
     */
    static Leg chooseLeg(double fromX, double fromY, double fromZ, double targetX, double targetZ, double knownTargetY, StandHeight stand) {
        if (!Double.isNaN(knownTargetY)) {
            return new Leg(targetX, knownTargetY, targetZ, true);
        }
        double dx = targetX - fromX;
        double dz = targetZ - fromZ;
        boolean last = dx * dx + dz * dz <= LEG_LENGTH * LEG_LENGTH;
        for (int attempt = 0; attempt < LEG_TRIES; attempt++) {
            HerdArea.Point point = legPoint(fromX, fromZ, targetX, targetZ, attempt);
            double y = stand.at(point.x(), point.z());
            if (!Double.isNaN(y) && withinJourneyHeight(y, fromY)) {
                return new Leg(point.x(), y, point.z(), last);
            }
        }
        return null;
    }

    /** @param pathEndedWithoutProgress the walk ended and the leader had not moved since the previous end */
    static boolean arrived(double distanceToTarget, boolean pathEndedWithoutProgress) {
        return distanceToTarget <= ARRIVE_DISTANCE || pathEndedWithoutProgress;
    }

    /**
     * Why an arrival ({@link #arrived}) ended the journey: within {@link #ARRIVE_DISTANCE}, an urgent trip's walk
     * ended, no usable leg on the re-issue, or (roaming) the walk ended twice in one place.
     */
    static End endReason(double distanceToTarget, Kind kind, boolean noLeg) {
        if (distanceToTarget <= ARRIVE_DISTANCE) {
            return End.ARRIVED;
        }
        if (kind == Kind.URGENT) {
            return End.URGENT_WALK_END;
        }
        return noLeg ? End.NO_LEG : End.NO_PROGRESS;
    }

    /** {@code true} if at least 75% of the counted members are close (or none are counted). */
    static boolean isGathered(int close, int counted) {
        return counted == 0 || close >= GATHERED_FRACTION * counted;
    }

    /**
     * {@code true} if a follower's slot is on the trail: its target arc is at or after the trail's first
     * point. {@code false} without a slot or a trail (NaN). Before that the follower stands where it is by
     * design, and a leader that stood for it would stop the trail from growing (playtest 1, finding D).
     */
    static boolean slotOnTrail(double targetArc, double trailTailArc) {
        return targetArc >= trailTailArc;
    }

    /**
     * {@code true} if a follower is close for the straggler wait: its straight-line distance to the leader is
     * at most its slot's distance plus {@link #GATHER_MARGIN}. The slot's distance (sqrt(depth^2 + offset^2)
     * behind the trail head) is the straight line from the leader to the slot on a straight trail, so a
     * follower in its slot is always close; a bend only shortens the real distance.
     */
    static boolean followerClose(double distanceToLeader, double slotDistance) {
        return distanceToLeader <= slotDistance + GATHER_MARGIN;
    }

    /**
     * The herd-level straggler count for one leader tick (lead ruling, playtest 1 fix P4, round 2). Only
     * followers whose slot is on the trail ({@link #slotOnTrail}) and that are within
     * {@link #STRAGGLER_IGNORE_DISTANCE} count, in both the numerator and the denominator; of those, the close
     * ones ({@link #followerClose}). The herd is gathered when at least 75% of the counted followers are close,
     * or nobody is counted ({@link #isGathered}): at a journey start no slot is on the trail, so the leader
     * goes on (no deadlock), and from the first row on, stragglers among the counted followers are waited for.
     * Pure; one per tick, no collection.
     */
    static final class Tally {

        private int counted;
        private int close;

        /**
         * @param slotDistance sqrt(depth^2 + offset^2) of the slot behind the trail head; NaN without a slot
         * @param targetArc    the slot's target arc; NaN without a slot
         * @param trailTailArc the arc of the trail's first kept point; NaN for an empty trail
         */
        void add(double distanceToLeader, double slotDistance, double targetArc, double trailTailArc) {
            if (distanceToLeader > STRAGGLER_IGNORE_DISTANCE || !slotOnTrail(targetArc, trailTailArc)
                    || Double.isNaN(slotDistance)) {
                return;
            }
            this.counted++;
            if (followerClose(distanceToLeader, slotDistance)) {
                this.close++;
            }
        }

        boolean gathered() {
            return isGathered(this.close, this.counted);
        }

        int counted() {
            return this.counted;
        }

        int close() {
            return this.close;
        }
    }

    /** @param waitedTicks ticks this journey has already spent slowed for stragglers */
    static Gathering gathering(Kind kind, boolean gathered, int waitedTicks) {
        if (kind == Kind.URGENT || gathered || waitedTicks >= MAX_WAIT_TICKS) {
            return Gathering.GO;
        }
        return Gathering.SLOW;
    }

    /** {@code true} at night: day time (mod {@link #DAY_TICKS}) after {@link #SUNSET_DAY_TIME}, the same test as {@code Rest}. */
    static boolean isNight(long dayTime) {
        return Math.floorMod(dayTime, DAY_TICKS) > SUNSET_DAY_TIME;
    }

    /**
     * {@code true} in the species' rest hours: the night for a day animal, the day for a nocturnal one (the
     * hours in which {@code Rest} does not end at full stamina).
     */
    static boolean restHours(long dayTime, boolean nocturnal) {
        return isNight(dayTime) != nocturnal;
    }

    /** P13a rulings Q2 and Q3: a roaming journey may start only outside the rest hours and with enough stamina. */
    static boolean roamMayStart(boolean restHours, float stamina, float maxStamina) {
        return !restHours && stamina >= maxStamina * JOURNEY_MIN_STAMINA_FRACTION;
    }

    /**
     * Why a roaming journey pauses this tick (P13a item 1).
     *
     * @param down the leader sits down, rests or gets up ({@code HerdNeeds.isDown})
     */
    static Pause pause(boolean down) {
        return down ? Pause.REST : Pause.NONE;
    }

    /**
     * Ruling Q1: the herd leaves its slots to rest at night, or once the rest pause is longer than
     * {@link #SHORT_REST_PAUSE_TICKS}; never while the leader gets up (fix round 1: a rest of about 400 ticks
     * passes the short pause during its 69-tick get-up, and the herd would lie down as the leader walks on).
     *
     * @param leaderRising the leader is in its get-up ({@code isRising} or the getting-up memory)
     */
    static boolean herdSpreads(int pausedTicks, boolean restHours, boolean leaderRising) {
        return !leaderRising && (restHours || pausedTicks > SHORT_REST_PAUSE_TICKS);
    }

    /**
     * A member that spread out for the rest lies down at its spot, tired or not (ruling Q1: the herd sleeps):
     * always in the rest hours, where {@code Rest} lasts until morning; in the daytime only below its maximum
     * stamina, because a daytime rest ends at full stamina at once. Never once the leader gets up (fix round 1).
     */
    static boolean sleepsAtSpot(boolean restHours, boolean leaderRising, float stamina, float maxStamina) {
        return !leaderRising && (restHours || stamina < maxStamina);
    }

    static boolean shouldRecentre(int outsideTicks) {
        return outsideTicks >= OUTSIDE_RECENTRE_TICKS;
    }

    /**
     * {@code true} if a grazing leader is dragged (playtest 2 fix P8c, finding R7): it is more than
     * {@link #DRAG_EDGE_MARGIN} beyond the herd radius, it has no walk of its own, and it moved at least
     * {@link #DRAG_MIN_STEP} since its previous tick.
     * A leader with its own walk is decision 16's case (a walk target out of the area: an urgent journey to
     * it) or the recentre rule's (a graze move that ran outside); one that stands outside is the recentre
     * rule's. A leashed leader has no walk of its own: the lead moves it.
     *
     * @param distanceFromCentre the leader's horizontal distance from the herd area's centre
     * @param radius             the herd radius R
     * @param step               horizontal distance moved since the leader's previous tick; 0 when unknown
     */
    static boolean dragStarts(double distanceFromCentre, double radius, boolean ownWalk, double step) {
        return distanceFromCentre > radius + DRAG_EDGE_MARGIN && !ownWalk && step >= DRAG_MIN_STEP;
    }

    /** {@code true} if a dragged leader is farther than {@link #DRAG_STILL_RADIUS} from where it stopped (dx, dz from there). */
    static boolean leftStillSpot(double dx, double dz) {
        return dx * dx + dz * dz > DRAG_STILL_RADIUS * DRAG_STILL_RADIUS;
    }

    /** {@code true} if a dragged leader has stood still long enough to end the journey. */
    static boolean dragSettled(int stillTicks) {
        return stillTicks >= DRAG_SETTLE_TICKS;
    }
}
