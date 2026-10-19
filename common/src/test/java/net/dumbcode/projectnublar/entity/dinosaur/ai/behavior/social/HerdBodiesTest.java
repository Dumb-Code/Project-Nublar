package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdBodiesTest {

    private static final double EPSILON = 1.0E-9D;
    private static final double WIDTH = 2D;

    private static UUID id(long n) {
        return new UUID(0L, n);
    }

    private static HerdBodies bodies() {
        HerdBodies bodies = new HerdBodies();
        bodies.begin(1L);
        return bodies;
    }

    @Test
    void beginEmptiesTheSnapshot() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 0);
        assertEquals(1, bodies.count());
        bodies.begin(2L);
        assertEquals(0, bodies.count());
        assertEquals(-1, bodies.indexOf(id(1)));
        assertEquals(2L, bodies.tick());
    }

    @Test
    void growsPastItsFirstSize() {
        HerdBodies bodies = bodies();
        for (int i = 0; i < 20; i++) {
            bodies.add(id(i), i, 0D, WIDTH, false, false, false, i);
        }
        assertEquals(19, bodies.indexOf(id(19)));
        assertEquals(19D, bodies.x(19), EPSILON);
    }

    @Test
    void growsAfterAPreviousSnapshotToo() {
        HerdBodies bodies = bodies();
        for (int i = 0; i < 6; i++) {
            bodies.add(id(i), i, 0D, WIDTH, false, false, false, i);
        }
        bodies.begin(2L);
        for (int i = 0; i < 20; i++) {
            bodies.add(id(i), i + (i < 6 ? 0.3D : 0D), 0D, WIDTH, false, false, false, i);
        }
        assertEquals(19, bodies.indexOf(id(19)));
        assertTrue(bodies.walking(5), "a member of the previous snapshot that moved walks");
        assertFalse(bodies.walking(19), "a member new to the snapshot has no velocity");
    }

    @Test
    void outranksByRankThenId() {
        HerdBodies bodies = bodies();
        bodies.add(id(5), 0D, 0D, WIDTH, false, false, false, 0);
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 1);
        bodies.add(id(2), 0D, 0D, WIDTH, false, false, false, 1);
        assertTrue(bodies.outranks(0, 1));
        assertFalse(bodies.outranks(1, 0));
        assertTrue(bodies.outranks(1, 2));
    }

    @Test
    void standingMemberAheadMasksTheWalker() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 1);
        bodies.add(id(2), 0D, 4D, WIDTH, false, false, false, 2);
        assertTrue(bodies.wouldCrowd(0, 0D, 0.5D));
        assertFalse(bodies.wouldCrowd(0, 0D, -0.5D));
    }

    /**
     * Velocity comes from the previous snapshot: member 2 (the higher rank) moved 0.4 along x since last
     * tick, so it is predicted along that velocity and crosses the walker's path.
     */
    @Test
    void velocityIsTheMoveSinceThePreviousSnapshot() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, -0.2D, WIDTH, false, false, false, 2);
        bodies.add(id(2), -3.9D, 2D, WIDTH, false, false, false, 1);
        bodies.begin(2L);
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), -3.5D, 2D, WIDTH, false, false, false, 1);
        assertTrue(bodies.walking(0));
        assertTrue(bodies.walking(1));
        assertTrue(bodies.wouldCrowd(0, 0D, 0.2D), "the higher rank's path must be kept clear");
    }

    @Test
    void memberNewToTheSnapshotHasNoVelocity() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, -0.2D, WIDTH, false, false, false, 2);
        bodies.begin(2L);
        bodies.add(id(2), -3.5D, 2D, WIDTH, false, false, false, 1);
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        assertFalse(bodies.walking(0), "not in the previous snapshot: standing");
        assertTrue(bodies.walking(1), "found by id, not by index");
        assertFalse(bodies.wouldCrowd(1, 0D, 0.2D), "a standing member 3.5 away is not crossed");
    }

    @Test
    void velocityIsPerTickAcrossASkippedSnapshot() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), -4.3D, 2D, WIDTH, false, false, false, 1);
        bodies.begin(3L);
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), -3.5D, 2D, WIDTH, false, false, false, 1);
        // 0.8 over two ticks is 0.4 per tick: it still crosses the walker's path within 10 ticks.
        assertTrue(bodies.wouldCrowd(0, 0D, 0.2D));
        bodies.begin(4L);
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), -3.5D, 2D, WIDTH, false, false, false, 1);
        assertFalse(bodies.walking(1), "it stood this tick");
    }

    @Test
    void lowerRankWalkerCountsAsStanding() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, -0.2D, WIDTH, false, false, false, 1);
        bodies.add(id(2), -3.9D, 2D, WIDTH, false, false, false, 2);
        bodies.begin(2L);
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 1);
        bodies.add(id(2), -3.5D, 2D, WIDTH, false, false, false, 2);
        assertTrue(bodies.walking(1));
        assertFalse(bodies.wouldCrowd(0, 0D, 0.2D), "the lower rank yields, so it counts as standing");
    }

    @Test
    void movingAwayFromAStackedMemberIsAllowed() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), 0.5D, 0D, WIDTH, false, false, false, 1);
        assertFalse(bodies.wouldCrowd(0, -0.3D, 0D));
        assertTrue(bodies.wouldCrowd(0, 0.3D, 0D));
    }

    @Test
    void tightGroundOfEitherMemberLowersTheirMinimum() {
        // Playtest 2 fix P8b: a pair's hard minimum is the tight one when either body stands in tight ground.
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), 0D, 2.7D, WIDTH, false, true, false, 1);
        bodies.add(id(3), 0D, -2.7D, WIDTH, false, false, false, 3);
        assertEquals(WIDTH + HerdSpacing.TIGHT_GAP, bodies.minimum(0, 1), EPSILON);
        assertEquals(WIDTH + HerdSpacing.GAP, bodies.minimum(0, 2), EPSILON);
        // 0.1 closer within 10 ticks: 2.6 is inside the open minimum (3) but outside the tight one (2.5).
        assertFalse(bodies.wouldCrowd(0, 0D, 0.01D));
        assertTrue(bodies.wouldCrowd(0, 0D, -0.01D));
    }

    @Test
    void wantsToMoveIsCarriedPerMember() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, true, 1);
        bodies.add(id(2), 0D, 4D, WIDTH, false, false, false, 2);
        assertTrue(bodies.wantsToMove(0));
        assertFalse(bodies.wantsToMove(1));
    }

    @Test
    void crowderIsTheNearestMemberThatMasksTheMove() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), 0D, 6D, WIDTH, false, false, false, 3);
        bodies.add(id(3), 0D, 3.5D, WIDTH, false, false, false, 4);
        assertEquals(2, bodies.crowder(0, 0D, 0.5D), "both are reached within 10 ticks; the nearer one is the blocker");
        assertEquals(-1, bodies.crowder(0, 0D, -0.5D));
        assertTrue(bodies.wouldCrowd(0, 0D, 0.5D));
    }

    @Test
    void blockerAtFindsTheMemberAhead() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 0);
        bodies.add(id(2), 0D, 2.8D, WIDTH, false, false, false, 1);
        assertEquals(1, bodies.blockerAt(0, 0D, 0.6D));
        assertEquals(-1, bodies.blockerAt(0, 0D, -0.6D));
    }

    @Test
    void freeToMeasuresToTheMemberAhead() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 0);
        bodies.add(id(2), 0D, 5D, WIDTH, false, false, false, 1);
        assertEquals(1, bodies.nearestAhead(0, 0D, 1D, 10D));
        assertEquals(2D, bodies.freeTo(0, 1, 0D, 1D), EPSILON);
        assertEquals(-1, bodies.nearestAhead(0, 1D, 0D, 10D), "nobody on a free line");
        assertEquals(Double.POSITIVE_INFINITY, bodies.freeTo(0, 1, 1D, 0D), "beside the line");
    }

    @Test
    void nearestAheadFindsTheMemberThatLimitsTheFreeDistance() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), 0D, 8D, WIDTH, false, false, false, 1);
        bodies.add(id(3), 0D, 5D, WIDTH, false, false, false, 0);
        bodies.add(id(4), 5D, 0D, WIDTH, false, false, false, 3);
        assertEquals(2, bodies.nearestAhead(0, 0D, 1D, 10D));
        assertEquals(2D, bodies.freeTo(0, 2, 0D, 1D), EPSILON);
        assertEquals(-1, bodies.nearestAhead(0, 0D, 1D, 1.5D), "beyond the cap");
        assertEquals(-1, bodies.nearestAhead(0, 0D, -1D, 10D), "nobody behind");
        assertEquals(Double.POSITIVE_INFINITY, bodies.freeTo(0, 2, 0D, -1D), "behind the walker");
    }

    @Test
    void onlyTheLeaderGetsARestingMemberUpAndNeverDuringTheSitDown() {
        // P12b fix round 1: a member that is up always answers a make-way ask.
        assertTrue(HerdBodies.answersMakeWay(false, false, false));
        assertTrue(HerdBodies.answersMakeWay(false, false, true));
        // A resting member: only the leader's ask gets it up (the leader walks a vanilla path that cannot go round it).
        assertTrue(HerdBodies.answersMakeWay(false, true, true));
        assertFalse(HerdBodies.answersMakeWay(false, true, false), "a follower's ask never gets a resting member up");
        // Sitting down (the ~69-tick sit phase): nobody gets it up; the leader's wait is bounded by the sit phase.
        assertFalse(HerdBodies.answersMakeWay(true, false, true));
        assertFalse(HerdBodies.answersMakeWay(true, false, false));
        assertFalse(HerdBodies.answersMakeWay(true, true, true));
    }

    @Test
    void predictedSpeedFollowsTheMaskRule() {
        // P12b: speed matching uses the member masks' rule: a walking member that outranks the walker moves
        // along its velocity; every other member counts as standing.
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 1);
        bodies.add(id(2), 0D, 5D, WIDTH, false, false, false, 0);
        bodies.add(id(3), 3D, 5D, WIDTH, false, false, false, 2);
        bodies.begin(2L);
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 1);
        bodies.add(id(2), 0D, 5.1D, WIDTH, false, false, false, 0);
        bodies.add(id(3), 3D, 5.1D, WIDTH, false, false, false, 2);
        assertTrue(bodies.predictedMoving(0, 1), "a walking higher rank moves");
        assertFalse(bodies.predictedMoving(0, 2), "a walking lower rank counts as standing");
        assertFalse(bodies.predictedMoving(1, 0), "a standing member counts as standing");
        assertEquals(0.1D, bodies.speedAlong(1, 0D, 1D), 1.0E-6D);
        assertEquals(0D, bodies.speedAlong(1, 0D, -1D), EPSILON, "moving away from the line's direction counts as 0");
    }

    @Test
    void anyWithinSeesMembersInsideTheSpacing() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 0);
        bodies.add(id(2), 0D, 6D, WIDTH, false, false, false, 1);
        assertTrue(bodies.anyWithin(0, 0D, 2D, 5D));
        assertFalse(bodies.anyWithin(0, 0D, 0D, 5D));
    }

    @Test
    void crowdedByFindsTheHigherRankInside() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), 1D, 0D, WIDTH, false, false, false, 1);
        assertEquals(1, bodies.crowdedBy(0));
        assertEquals(-1, bodies.crowdedBy(1), "the higher rank stays put");
    }

    @Test
    void crowdedByIgnoresMembersOutsideTheMinimum() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), 3.5D, 0D, WIDTH, false, false, false, 1);
        assertEquals(-1, bodies.crowdedBy(0));
    }

    @Test
    void insideListsOnlyCloseMembers() {
        HerdBodies bodies = bodies();
        bodies.add(id(1), 0D, 0D, WIDTH, false, false, false, 2);
        bodies.add(id(2), 1D, 0D, WIDTH, false, false, false, 1);
        bodies.add(id(3), 9D, 0D, WIDTH, false, false, false, 3);
        assertEquals(1, bodies.inside(0).size());
        assertEquals(1D, bodies.inside(0).get(0).dx(), EPSILON);
        assertTrue(bodies.inside(2).isEmpty());
    }
}
