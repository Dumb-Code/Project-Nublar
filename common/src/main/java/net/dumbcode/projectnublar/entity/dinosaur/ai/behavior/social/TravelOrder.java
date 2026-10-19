package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

/**
 * Travel formation (spec "Travel", lead decision 1, plan review decision 9). Followers are ordered
 * nearest-first at the journey start. Rows lie along the trail behind the leader, {@link #openSpacing}
 * apart in open ground and {@link #tightSpacing} apart where the ground is tight; row r (0 = nearest)
 * holds at most {@code FIRST_ROW + r} followers, never more than {@link #MAX_LANES} and never more than
 * the ground allows there: a wedge in open ground that narrows to 1-3 abreast and to single file in gaps.
 * Every follower has its own (arc, lane offset). A follower steers to a look-ahead point on the trail at
 * a speed from its arc gap. Pure.
 */
final class TravelOrder {

    /** Followers in the row nearest the leader; each row behind holds one more, up to {@link #MAX_LANES}. */
    static final int FIRST_ROW = 2;
    static final int MAX_LANES = 3;
    /** In open ground, slots are this much farther apart than the hard minimum (the personal space): 4.2 for Gallimimus. */
    static final double OPEN_MARGIN = 2.0D;
    /** In tight ground, slots are this much farther apart than the personal space (3.2 for Gallimimus), so neighbours are not on the mask limit. */
    static final double TIGHT_MARGIN = 1.0D;
    /** A follower steers to the trail point this far ahead of its own projection (spec: 3). */
    static final double LOOK_AHEAD = 3.0D;
    /** The lane offset is clamped by the side room up to this far past the look-ahead point: about one turning radius. */
    static final double ROOM_AHEAD = 4.0D;
    /** At this arc gap (target minus own arc; negative = ahead of the target) a follower stands. */
    static final double AHEAD_GAP = -0.5D;
    /** At this arc gap and beyond a follower walks at {@link #CATCH_UP_SPEED}. */
    static final double BEHIND_GAP = 3.0D;
    /**
     * The old build's catch-up and regroup modifier: 0.194 blocks per tick ({@link GroundSpeed}), 2.25 times
     * the leader's journey ground speed at {@link JourneyPolicy#JOURNEY_SPEED}.
     */
    static final double CATCH_UP_SPEED = 1.2D;
    /** Two lanes need side room of this fraction of the spacing (half a spacing: a pair sits one spacing apart, so each is half a spacing off the centre). */
    static final double TWO_LANE_ROOM = 0.5D;

    /** A follower's steering target and wanted speed modifier. */
    record Target(double x, double y, double z, double speed) {
    }

    private TravelOrder() {
    }

    static double openSpacing(double personalSpace) {
        return personalSpace + OPEN_MARGIN;
    }

    static double tightSpacing(double personalSpace) {
        return personalSpace + TIGHT_MARGIN;
    }

    /**
     * Lanes the ground allows at a trail point, from the side room on its tighter side: 3 if there is room
     * for an offset of one spacing, 2 if there is room for half a spacing, else 1. Unknown room (NaN) counts as 1.
     */
    static int lanes(double room, double spacing) {
        if (Double.isNaN(room)) {
            return 1;
        }
        if (room >= spacing) {
            return MAX_LANES;
        }
        return room >= spacing * TWO_LANE_ROOM ? 2 : 1;
    }

    /** The followers sorted by distance to the leader, nearest first. {@code xs} and {@code zs} parallel {@code ids}. */
    static List<UUID> byDistance(double leaderX, double leaderZ, List<UUID> ids, double[] xs, double[] zs) {
        Integer[] order = new Integer[ids.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingDouble(i -> (xs[i] - leaderX) * (xs[i] - leaderX) + (zs[i] - leaderZ) * (zs[i] - leaderZ)));
        List<UUID> sorted = new ArrayList<>(order.length);
        for (int i : order) {
            sorted.add(ids.get(i));
        }
        return sorted;
    }

    /**
     * Writes each follower's target arc and lane offset (follower index = travel order). Each row is tried
     * at the open spacing behind the row ahead; if the ground there allows fewer lanes than the row wants,
     * the row is laid at the tight spacing instead. A row's spacing is both its step from the row ahead and
     * the gap between its lanes. A partly filled row is centred. Offsets are positive toward
     * {@code (-dirZ, dirX)} of the trail direction.
     *
     * @param roomAtArc side room on the tighter side of the trail point at an arc; NaN when unknown
     */
    static void layout(double leaderArc, int followers, double openSpacing, double tightSpacing,
                       DoubleUnaryOperator roomAtArc, double[] arcs, double[] offsets) {
        int placed = 0;
        double rowAhead = leaderArc;
        for (int row = 0; placed < followers; row++) {
            int wanted = Math.min(FIRST_ROW + row, MAX_LANES);
            double spacing = openSpacing;
            int lanes = lanes(roomAtArc.applyAsDouble(rowAhead - spacing), spacing);
            if (lanes < wanted) {
                spacing = tightSpacing;
                lanes = lanes(roomAtArc.applyAsDouble(rowAhead - spacing), spacing);
            }
            double arc = rowAhead - spacing;
            int inRow = Math.min(Math.min(wanted, lanes), followers - placed);
            for (int j = 0; j < inRow; j++) {
                arcs[placed] = arc;
                offsets[placed] = (j - (inRow - 1) / 2D) * spacing;
                placed++;
            }
            rowAhead = arc;
        }
    }

    /**
     * Speed modifier from the arc gap: 0 at {@link #AHEAD_GAP} or more ahead; between there and
     * {@link #BEHIND_GAP} the <b>ground speed</b> rises linearly to the catch-up ground speed, so the
     * modifier is {@link #CATCH_UP_SPEED} times the square root of the ramp fraction
     * ({@link GroundSpeed#modifierFactor}). A follower behind a leader at
     * {@link JourneyPolicy#JOURNEY_SPEED} settles where the ramp gives that modifier: (0.8 / 1.2)^2 = 0.44
     * of the ramp, about 1.06 blocks behind its slot ({@code TravelOrderTest}).
     */
    static double followSpeed(double gap) {
        if (gap <= AHEAD_GAP) {
            return 0D;
        }
        if (gap >= BEHIND_GAP) {
            return CATCH_UP_SPEED;
        }
        return CATCH_UP_SPEED * GroundSpeed.modifierFactor((gap - AHEAD_GAP) / (BEHIND_GAP - AHEAD_GAP));
    }

    /**
     * {@code true} when less than {@link ProgressMonitor#MIN_PROGRESS} of arc is left before the follower's
     * stop line ({@link #AHEAD_GAP} past its target arc). Easing in behind a stopped leader (waiting for the
     * herd, settling, or stuck) the follower cannot make the progress the monitor wants, so it counts as
     * waiting, not stuck (spec: "the leader waiting, is not stuck").
     */
    static boolean nearStopLine(double targetArc, double arc) {
        return targetArc - arc - AHEAD_GAP < ProgressMonitor.MIN_PROGRESS;
    }

    /**
     * The look-ahead target: the trail point {@link #LOOK_AHEAD} past the follower's projection (never past
     * the leader), moved sideways by its lane offset but never farther than the tightest side room from the
     * projection to {@link #ROOM_AHEAD} past the look-ahead point. Speed 0 if its target arc is not on the
     * trail yet (it waits where it stands). {@code null} for an empty trail.
     */
    static @Nullable Target follow(LeaderTrail trail, double projectedArc, double targetArc, double offset) {
        if (trail.size() == 0) {
            return null;
        }
        double look = Math.max(trail.tailArc(), Math.min(projectedArc + LOOK_AHEAD, trail.headArc()));
        double speed = targetArc < trail.tailArc() ? 0D : followSpeed(targetArc - projectedArc);
        return offsetTarget(trail, look, offset, projectedArc, look + ROOM_AHEAD, speed);
    }

    /**
     * A follower's slot as the debug visuals draw it: the trail point at its target arc moved out by its
     * lane offset, clamped by the side room the follower itself would use standing at the slot
     * ({@link #follow} with the projection at the slot: from the slot to {@link #LOOK_AHEAD} +
     * {@link #ROOM_AHEAD} past it), so the dot never lies where the real target could not. Speed 0 (unused).
     * {@code null} for an empty trail.
     */
    static @Nullable Target slot(LeaderTrail trail, double targetArc, double offset) {
        if (trail.size() == 0) {
            return null;
        }
        return offsetTarget(trail, targetArc, offset, targetArc, targetArc + LOOK_AHEAD + ROOM_AHEAD, 0D);
    }

    /**
     * The one place a lane offset meets the ground: the trail point at {@code arc} (clamped to the trail)
     * moved sideways by {@code offset} toward {@code (-dirZ, dirX)}, but never farther than the tightest
     * side room on that side from {@code fromArc} to {@code toArc} (unknown room: no offset). The trail
     * must not be empty.
     */
    private static Target offsetTarget(LeaderTrail trail, double arc, double offset, double fromArc, double toArc, double speed) {
        LeaderTrail.TrailPoint point = trail.pointAt(arc);
        float room = trail.minRoom(fromArc, toArc, offset >= 0D);
        double lateral = Math.copySign(Float.isNaN(room) ? 0D : Math.min(Math.abs(offset), room), offset);
        return new Target(point.x() - point.dirZ() * lateral, point.y(), point.z() + point.dirX() * lateral, speed);
    }
}
