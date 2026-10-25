package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;

/**
 * Context steering for one herd member (spec: redesign, "Context steering"). The caller scores 12 headings
 * round the circle: it marks headings that are blocked (masks: the clearance box does not fit 1 or 2 blocks
 * ahead, a hazard or a drop, or a member's hard minimum within 10 ticks) and adds penalties (danger). This
 * class adds interest (how well a heading points at the desired direction, minus a turn cost), keeps the
 * previous heading unless another is clearly better (hysteresis), and stops when nothing beats standing
 * still. Heading {@code i} faces Minecraft yaw {@code i x 30} degrees, direction {@code (-sin yaw, cos yaw)}.
 * Pure arithmetic; {@link Dinosaur#MAX_TURN_PER_TICK} is a compile-time constant.
 */
final class ContextSteering {

    static final int HEADINGS = 12;
    /** {@link #pick} result for "stand still". */
    static final int STOP = -1;
    static final double STEP_DEGREES = 360D / HEADINGS;
    /** Interest lost per degree of turn beyond one tick's turn (the body turns 10 degrees per tick). */
    static final double TURN_COST_PER_DEGREE = 0.002D;
    static final double MAX_TURN_DEGREES = Dinosaur.MAX_TURN_PER_TICK;
    /** Half a heading step: a yaw within this of a heading's yaw walks that heading ({@link #walksHeading}). */
    static final double HALF_STEP_DEGREES = STEP_DEGREES / 2D;
    /**
     * A new heading must beat the previous one by this much. The spec's 0.15 was more than an adjacent heading
     * can ever gain: with the target straight along it, it scores 1 - 0.04 (the turn cost of one step once the
     * body has turned onto the previous heading) against the previous heading's cos 30 = 0.866, a gap of 0.094,
     * so a walker could never straighten up onto the heading next to its own and hugged walls until the mask
     * took over (playtest 2 fix P8a, geometry cause 6). At 0.05 the adjacent heading wins once the target lies
     * within about 4.5 degrees of it; the turn cost alone already keeps two equally good headings from
     * flip-flopping ({@code ContextSteeringTest}).
     */
    static final double HYSTERESIS = 0.05D;
    /**
     * Score of standing still. Walking sideways round an obstacle (interest about -0.16) beats it; turning
     * back (interest below -0.5) does not, so a dead end stops the walker instead of turning it round.
     */
    static final double STOP_SCORE = -0.3D;
    /** Danger when the clearance box does not fit 3 blocks ahead. */
    static final double DANGER_BLOCKED_FAR = 0.5D;
    /** Danger when a member stands within the graze spacing of the first step. */
    static final double DANGER_NEAR_MEMBER = 0.3D;
    /** Inside this distance from its target a walker slows down: about the turning radius at 10 degrees per tick. */
    static final double SLOW_RADIUS = 3.0D;
    static final double MIN_ARRIVAL_FACTOR = 0.3D;
    /**
     * A walker slows when the nearest member on its heading is closer than this past the hard minimum:
     * one block, {@link TravelOrder#TIGHT_MARGIN}. A follower in a tight travel slot stands exactly that far
     * past the member ahead's hard minimum and must not be slowed there, or it cannot hold the slot at the
     * leader's pace (playtest 1, spacing point F). Blocks never bring the free distance under 2 (the mid
     * probe masks the heading), so only members slow a walker.
     */
    static final double FREE_SLOW_DISTANCE = TravelOrder.TIGHT_MARGIN;
    static final double MIN_FREE_FACTOR = 0.4D;
    /**
     * Speed matching (playtest 4 fix P12b, motion cause 2): a walker closing on a moving herd mate ahead on its
     * heading settles this far past the mate's hard minimum. The tight travel margin ({@link TravelOrder#TIGHT_MARGIN}),
     * so a follower in a tight slot is not held back, and well outside the guard's look-ahead (0.6).
     */
    static final double MATCH_SETTLE_FREE = TravelOrder.TIGHT_MARGIN;
    /**
     * Speed matching: the walker may close on the mate ahead at this many blocks per tick for each block of
     * free distance beyond {@link #MATCH_SETTLE_FREE}, so the gap closes smoothly (about 30 ticks per e-fold).
     * P13b (ruling 8): 1.5 x gentler than P12b's 0.05. A catch-up follower behind a mate at the journey pace
     * starts to slow 4.3 blocks of free distance out (P12b: 3.2) and loses at most 0.0036 blocks per tick per
     * tick (P12b: 0.0054). Under 1 / {@link HerdBodies#PREDICT_TICKS} (0.1), so the closing over the member
     * mask's 10-tick prediction is always less than the free distance: a matched follow never masks its own
     * heading.
     */
    static final double MATCH_CLOSE_RATE = 0.033D;
    /**
     * Speed matching looks this far ahead (free distance) for the mate to match. At this range the allowed
     * closing speed (0.21 blocks per tick) is above the catch-up speed (0.19), so matching starts without a jump,
     * even behind a mate that hardly moves (it starts 6.9 blocks out). P13b: 7.5 (P12b: 5), 1.5 x farther, so the
     * gentler rate still starts without a jump.
     */
    static final double MATCH_RANGE = 7.5D;
    /**
     * A walker yields to members (stands as "waiting", lead decision 14) for at most this many ticks in a
     * row; a longer stop counts as blocked, so the stuck rule asks for a fallback path (playtest 1 fix P2:
     * before, a yield never ended). 160 ticks (8 s) is four stuck windows ({@link ProgressMonitor#WINDOW_TICKS}),
     * safely above the longest legitimate yield: a herd mate crossing the walker's path at the graze speed
     * clears the hard minimum zone plus its own body (2 x 2.2 + 1.2 = 5.6 blocks for Gallimimus at 0.049
     * blocks per tick, {@link GroundSpeed}) in about 115 ticks; at the journey speed in about 65, an unstack
     * or side step in at most 40. The margin keeps a crosser from timing the walker out just as it clears
     * ({@code ContextSteeringTest} checks the bound from the constants). A stop that lasts longer is a
     * deadlock (members masking each other, a standing member ahead in a corridor) that only a path breaks.
     * Not the leader's straggler wait ({@link JourneyPolicy#MAX_WAIT_TICKS}, 200): while the leader waits or
     * settles, {@code FollowFormation} ignores a timed-out yield, or a front row would path into the leader.
     */
    static final int MAX_WAIT_TICKS = 4 * ProgressMonitor.WINDOW_TICKS;

    private static final double[] DIR_X = new double[HEADINGS];
    private static final double[] DIR_Z = new double[HEADINGS];

    static {
        for (int heading = 0; heading < HEADINGS; heading++) {
            double yaw = Math.toRadians(heading * STEP_DEGREES);
            DIR_X[heading] = -Math.sin(yaw);
            DIR_Z[heading] = Math.cos(yaw);
        }
    }

    private ContextSteering() {
    }

    static double dirX(int heading) {
        return DIR_X[heading];
    }

    static double dirZ(int heading) {
        return DIR_Z[heading];
    }

    static float yawOf(int heading) {
        return (float) (heading * STEP_DEGREES);
    }

    /** An angle in degrees, wrapped into [-180, 180). */
    static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360D;
        if (wrapped >= 180D) {
            wrapped -= 360D;
        }
        if (wrapped < -180D) {
            wrapped += 360D;
        }
        return wrapped;
    }

    /** Cosine between the heading and the desired direction, minus the turn cost beyond one tick's turn. */
    static double interest(int heading, double desiredX, double desiredZ, float currentYaw) {
        double length = Math.sqrt(desiredX * desiredX + desiredZ * desiredZ);
        if (length < 1.0E-9D) {
            return 0D;
        }
        double cosine = (DIR_X[heading] * desiredX + DIR_Z[heading] * desiredZ) / length;
        double turn = Math.abs(wrapDegrees(heading * STEP_DEGREES - currentYaw));
        return cosine - TURN_COST_PER_DEGREE * Math.max(0D, turn - MAX_TURN_DEGREES);
    }

    /**
     * The heading to walk, or {@link #STOP}.
     *
     * @param previous the heading picked last time, or {@link #STOP}
     * @param masked   per heading: removed from the choice
     * @param danger   per heading: subtracted from the interest
     */
    static int pick(double desiredX, double desiredZ, float currentYaw, int previous, boolean[] masked, double[] danger) {
        int best = STOP;
        double bestScore = STOP_SCORE;
        for (int heading = 0; heading < HEADINGS; heading++) {
            if (masked[heading]) {
                continue;
            }
            double score = interest(heading, desiredX, desiredZ, currentYaw) - danger[heading];
            if (score > bestScore) {
                bestScore = score;
                best = heading;
            }
        }
        if (best != STOP && previous >= 0 && previous < HEADINGS && previous != best && !masked[previous]) {
            double keep = interest(previous, desiredX, desiredZ, currentYaw) - danger[previous];
            if (keep > STOP_SCORE && keep >= bestScore - HYSTERESIS) {
                return previous;
            }
        }
        return best;
    }

    /**
     * Lead decision 14 for a {@link #STOP} pick: the walker is <b>yielding</b> (stopped by members: waiting,
     * not stuck) only if a heading that members alone masked would otherwise have been walked, that is its
     * score beats {@link #STOP_SCORE} and every heading open to both blocks and members. Otherwise blocks
     * stopped it (or nothing was worth walking), and the caller counts the stop as blocked. A member mask
     * on a heading the walker would not have taken anyway (behind it, or worse than an open one) is not a
     * yield: before this rule any such mask made the stop a wait, and a member stopped by trees with a herd
     * mate behind it waited for ever (playtest 1, C1).
     *
     * @param crowded per heading: open to blocks, masked by a member's hard minimum (a subset of {@code masked})
     * @param masked  per heading: removed from the choice (blocks, members, or not probed)
     * @param danger  per heading: subtracted from the interest
     */
    static boolean yielding(double desiredX, double desiredZ, float currentYaw, boolean[] crowded, boolean[] masked, double[] danger) {
        return yieldHeading(desiredX, desiredZ, currentYaw, crowded, masked, danger) != STOP;
    }

    /**
     * The heading a {@link #yielding} walker would have walked: the best-scoring crowded heading that beats
     * {@link #STOP_SCORE} and every open heading, or {@link #STOP} when the walker is not yielding. The
     * member that masked it is the one the walker yields to, and the one it asks to make way (playtest 2 fix
     * P8b). Same arguments as {@link #yielding}.
     */
    static int yieldHeading(double desiredX, double desiredZ, float currentYaw, boolean[] crowded, boolean[] masked, double[] danger) {
        double bestOpen = STOP_SCORE;
        for (int heading = 0; heading < HEADINGS; heading++) {
            if (!masked[heading]) {
                bestOpen = Math.max(bestOpen, interest(heading, desiredX, desiredZ, currentYaw) - danger[heading]);
            }
        }
        int best = STOP;
        double bestScore = bestOpen;
        for (int heading = 0; heading < HEADINGS; heading++) {
            if (crowded[heading]) {
                double score = interest(heading, desiredX, desiredZ, currentYaw) - danger[heading];
                if (score > bestScore) {
                    bestScore = score;
                    best = heading;
                }
            }
        }
        return best;
    }

    /**
     * Whether a stop that has lasted {@code stopTicks} ticks in a row still counts as waiting: it must be a
     * yield ({@link #yielding}) no longer than {@link #MAX_WAIT_TICKS}. After that it counts as blocked.
     */
    static boolean waiting(boolean yielding, int stopTicks) {
        return yielding && stopTicks <= MAX_WAIT_TICKS;
    }

    /**
     * Fraction of the wanted <b>ground speed</b>: slower within {@link #SLOW_RADIUS} of the target (never
     * below {@link #MIN_ARRIVAL_FACTOR}) and slower as the nearest member on the heading gets close (never
     * below {@link #MIN_FREE_FACTOR}). The ground speed is quadratic in the modifier, so the caller applies
     * {@link GroundSpeed#modifierFactor} (the square root) to the modifier.
     */
    static double speedFactor(double distanceToTarget, double freeDistance) {
        double arrival = distanceToTarget >= SLOW_RADIUS ? 1D : Math.max(MIN_ARRIVAL_FACTOR, distanceToTarget / SLOW_RADIUS);
        double free = freeDistance >= FREE_SLOW_DISTANCE ? 1D : Math.max(MIN_FREE_FACTOR, freeDistance / FREE_SLOW_DISTANCE);
        return arrival * free;
    }

    /**
     * Speed matching (playtest 4 fix P12b, motion cause 2): the ground speed for a walker that closes on a
     * moving herd mate ahead on its heading. The walker may close at {@link #MATCH_CLOSE_RATE} per block of
     * free distance beyond {@link #MATCH_SETTLE_FREE}, so it slows down early and smoothly and comes to the
     * mate's own speed near the settle distance, before the member mask or the guard can fire. Before, a
     * follower at the catch-up speed ran at full speed into the mask (1.08 blocks) and the guard (0.6) and
     * then stopped dead or zigzagged 30 degrees. A mate slower than the walk floor makes the walker stand once
     * it is at the settle distance (the floor would otherwise lift it into the guard).
     *
     * @param wanted the walker's wanted ground speed, in blocks per tick
     * @param ahead  the mate's ground speed along the heading (0 or more)
     * @param free   free distance from the walker to the mate's hard minimum along the heading
     * @param floor  the ground speed of the slowest walk ({@link GroundSpeed#MIN_WALK_MODIFIER})
     * @return the ground speed to walk, at most {@code wanted}; 0 to stand
     */
    static double matchedSpeed(double wanted, double ahead, double free, double floor) {
        double cap = ahead + MATCH_CLOSE_RATE * (free - MATCH_SETTLE_FREE);
        if (cap >= wanted) {
            return wanted;
        }
        if (cap < floor && free <= MATCH_SETTLE_FREE) {
            return 0D;
        }
        return Math.max(0D, cap);
    }

    /**
     * Whether a body with this yaw walks {@code heading}: within {@link #HALF_STEP_DEGREES} of its yaw. The
     * guard tests along the yaw and the probes along the heading, and the yaw lags a new heading by up to 30
     * degrees for about six ticks after a pick (the eased turn, {@link TurnEasing}: 2, 4, 6, 8, 6.3, 3.7 degrees;
     * more than this half step off for the first four), so a guard stop during the turn is not a stop on the
     * heading (playtest 2 fix P8a, geometry cause 4). {@code false} for {@link #STOP}.
     */
    static boolean walksHeading(float yaw, int heading) {
        return heading >= 0 && heading < HEADINGS
                && Math.abs(wrapDegrees(yaw - heading * STEP_DEGREES)) <= HALF_STEP_DEGREES;
    }

    /**
     * Whether a body with this yaw faces the direction (dx, dz) within {@link #HALF_STEP_DEGREES}: the path
     * hold ({@code HerdMoveControl.holdPath}) counts a tick only while the body points at its next node, so the
     * ticks of a turn toward it are not held ticks: up to 19 for a half turn with the eased turn
     * ({@link TurnEasing}: 2, 4, 6, 8, then 10 degrees per tick; it lands in 22). A zero direction (on the node)
     * counts as faced.
     */
    static boolean facesDirection(float yaw, double dx, double dz) {
        return facesDirection(yaw, dx, dz, HALF_STEP_DEGREES);
    }

    /**
     * Whether a body with this yaw faces the direction (dx, dz) within {@code limitDegrees}; a zero direction
     * counts as faced. The guard's member test uses 45 degrees (playtest 4 fix P12b).
     */
    static boolean facesDirection(float yaw, double dx, double dz, double limitDegrees) {
        if (dx * dx + dz * dz < 1.0E-12D) {
            return true;
        }
        double toward = Math.toDegrees(Math.atan2(-dx, dz));
        return Math.abs(wrapDegrees(yaw - toward)) <= limitDegrees;
    }

    /** The heading closest to a yaw. */
    static int nearestHeading(float yaw) {
        return Math.floorMod((int) Math.round(yaw / STEP_DEGREES), HEADINGS);
    }
}
