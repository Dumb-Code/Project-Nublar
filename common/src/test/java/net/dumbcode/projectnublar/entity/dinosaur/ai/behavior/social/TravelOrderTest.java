package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TravelOrderTest {

    private static final double EPSILON = 1.0E-9D;
    /** Personal space 3 (a 2-wide body), so 5 in open ground and 4 in tight ground. Gallimimus is 1.2 wide: 4.2 and 3.2. */
    private static final double OPEN = 5D;
    private static final double TIGHT = 4D;

    private static LeaderTrail straightTrail(int length) {
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x <= length; x++) {
            trail.record(x, 64D, 0D);
        }
        return trail;
    }

    @Test
    void spacingIsPersonalSpacePlusTwoInTheOpenAndPlusOneWhenTight() {
        assertEquals(OPEN, TravelOrder.openSpacing(3D), EPSILON);
        assertEquals(TIGHT, TravelOrder.tightSpacing(3D), EPSILON);
    }

    @Test
    void justShortOfTheStopLineCountsAsWaiting() {
        // The stop line is AHEAD_GAP past the target arc; less than MIN_PROGRESS of arc is left before it.
        double arc = 10D;
        double targetArcAtStopLine = arc + TravelOrder.AHEAD_GAP;
        assertTrue(TravelOrder.nearStopLine(targetArcAtStopLine + ProgressMonitor.MIN_PROGRESS - 0.01D, arc));
    }

    @Test
    void farBehindTheStopLineIsNotWaiting() {
        assertFalse(TravelOrder.nearStopLine(20D, 10D));
        // Exactly MIN_PROGRESS short: the member can still make the progress the monitor wants.
        assertFalse(TravelOrder.nearStopLine(10D + TravelOrder.AHEAD_GAP + ProgressMonitor.MIN_PROGRESS, 10D));
    }

    @Test
    void lanesFollowTheSideRoom() {
        assertEquals(3, TravelOrder.lanes(5D, OPEN));
        assertEquals(2, TravelOrder.lanes(2.5D, OPEN));
        assertEquals(1, TravelOrder.lanes(2.4D, OPEN));
        assertEquals(1, TravelOrder.lanes(Double.NaN, OPEN));
        assertEquals(3, TravelOrder.lanes(4D, TIGHT));
    }

    @Test
    void byDistanceIsNearestFirst() {
        UUID a = new UUID(0L, 1L);
        UUID b = new UUID(0L, 2L);
        UUID c = new UUID(0L, 3L);
        List<UUID> order = TravelOrder.byDistance(0D, 0D, List.of(a, b, c), new double[]{9D, 1D, 4D}, new double[]{0D, 0D, 0D});
        assertEquals(List.of(b, c, a), order);
    }

    @Test
    void openGroundMakesAWideWedge() {
        double[] arcs = new double[9];
        double[] offsets = new double[9];
        TravelOrder.layout(100D, 9, OPEN, TIGHT, arc -> 6D, arcs, offsets);
        double[] expectedArcs = {95D, 95D, 90D, 90D, 90D, 85D, 85D, 85D, 80D};
        double[] expectedOffsets = {-2.5D, 2.5D, -5D, 0D, 5D, -5D, 0D, 5D, 0D};
        for (int i = 0; i < 9; i++) {
            assertEquals(expectedArcs[i], arcs[i], EPSILON, "arc of follower " + i);
            assertEquals(expectedOffsets[i], offsets[i], EPSILON, "offset of follower " + i);
        }
    }

    @Test
    void tightGroundUsesTheTightSpacing() {
        // Room 4: the first row (2 lanes wanted) still fits 5 apart; rows of 3 fit only 4 apart.
        double[] arcs = new double[7];
        double[] offsets = new double[7];
        TravelOrder.layout(100D, 7, OPEN, TIGHT, arc -> 4D, arcs, offsets);
        double[] expectedArcs = {95D, 95D, 91D, 91D, 91D, 87D, 87D};
        double[] expectedOffsets = {-2.5D, 2.5D, -4D, 0D, 4D, -2D, 2D};
        for (int i = 0; i < 7; i++) {
            assertEquals(expectedArcs[i], arcs[i], EPSILON, "arc of follower " + i);
            assertEquals(expectedOffsets[i], offsets[i], EPSILON, "offset of follower " + i);
        }
    }

    @Test
    void narrowGroundIsSingleFileAtTheTightSpacing() {
        double[] arcs = new double[5];
        double[] offsets = new double[5];
        TravelOrder.layout(100D, 5, OPEN, TIGHT, arc -> 1D, arcs, offsets);
        for (int i = 0; i < 5; i++) {
            assertEquals(100D - (i + 1) * TIGHT, arcs[i], EPSILON);
            assertEquals(0D, offsets[i], EPSILON);
        }
    }

    @Test
    void fixedSpacingDebugUsesOneSpacingEverywhere() {
        double[] arcs = new double[3];
        double[] offsets = new double[3];
        TravelOrder.layout(100D, 3, TIGHT, TIGHT, arc -> 6D, arcs, offsets);
        double[] expectedArcs = {96D, 96D, 92D};
        double[] expectedOffsets = {-2D, 2D, 0D};
        for (int i = 0; i < 3; i++) {
            assertEquals(expectedArcs[i], arcs[i], EPSILON, "arc of follower " + i);
            assertEquals(expectedOffsets[i], offsets[i], EPSILON, "offset of follower " + i);
        }
    }

    @Test
    void everyFollowerGetsItsOwnTargetInEveryKindOfGround() {
        for (double room : new double[]{Double.NaN, 1D, 2.5D, 4D, 6D}) {
            for (int followers = 1; followers <= 20; followers++) {
                double[] arcs = new double[followers];
                double[] offsets = new double[followers];
                TravelOrder.layout(200D, followers, OPEN, TIGHT, arc -> room, arcs, offsets);
                for (int i = 0; i < followers; i++) {
                    for (int j = i + 1; j < followers; j++) {
                        assertFalse(Math.abs(arcs[i] - arcs[j]) < EPSILON && Math.abs(offsets[i] - offsets[j]) < EPSILON,
                                "followers " + i + " and " + j + " share a target with side room " + room);
                    }
                }
            }
        }
    }

    @Test
    void aGapNarrowsOnlyTheRowsInsideIt() {
        // Rows that land in the gap (arc 86 to 91) go single file at the tight spacing; the open rows around it stay 5 apart.
        double[] arcs = new double[8];
        double[] offsets = new double[8];
        TravelOrder.layout(100D, 8, OPEN, TIGHT, arc -> arc >= 86D && arc <= 91D ? 1D : 6D, arcs, offsets);
        double[] expectedArcs = {95D, 95D, 91D, 87D, 82D, 82D, 82D, 77D};
        for (int i = 0; i < 8; i++) {
            assertEquals(expectedArcs[i], arcs[i], EPSILON, "arc of follower " + i);
        }
    }

    @Test
    void singleFollowerWalksOnTheTrail() {
        double[] arcs = new double[1];
        double[] offsets = new double[1];
        TravelOrder.layout(50D, 1, OPEN, TIGHT, arc -> 6D, arcs, offsets);
        assertEquals(45D, arcs[0], EPSILON);
        assertEquals(0D, offsets[0], EPSILON);
    }

    @Test
    void followSpeedIsZeroAheadAndCatchesUpBehind() {
        assertEquals(0D, TravelOrder.followSpeed(-1D), EPSILON);
        assertEquals(0D, TravelOrder.followSpeed(TravelOrder.AHEAD_GAP), EPSILON);
        // Half-way up the ramp the ground speed is half the catch-up ground speed: the modifier is
        // CATCH_UP_SPEED x sqrt(0.5), because the ground speed is quadratic in the modifier.
        assertEquals(TravelOrder.CATCH_UP_SPEED * Math.sqrt(0.5D), TravelOrder.followSpeed(1.25D), EPSILON);
        assertEquals(TravelOrder.CATCH_UP_SPEED, TravelOrder.followSpeed(3D), EPSILON);
        assertEquals(TravelOrder.CATCH_UP_SPEED, TravelOrder.followSpeed(30D), EPSILON);
    }

    @Test
    void followerHoldsItsSlotBehindALeaderAtJourneySpeed() {
        // The ramp must reach the leader's journey speed well inside BEHIND_GAP, so a follower settles
        // about a block behind its slot instead of falling back to the catch-up gap.
        double fraction = (JourneyPolicy.JOURNEY_SPEED / TravelOrder.CATCH_UP_SPEED) * (JourneyPolicy.JOURNEY_SPEED / TravelOrder.CATCH_UP_SPEED);
        double equilibriumGap = TravelOrder.AHEAD_GAP + fraction * (TravelOrder.BEHIND_GAP - TravelOrder.AHEAD_GAP);
        assertEquals(JourneyPolicy.JOURNEY_SPEED, TravelOrder.followSpeed(equilibriumGap), 1.0E-6D);
        assertTrue(equilibriumGap < TravelOrder.TIGHT_MARGIN + 0.5D, "settles " + equilibriumGap + " behind the slot");
        // A follower in its slot still walks (above the animation stop threshold) until it is nearly there.
        assertTrue(TravelOrder.followSpeed(0D) >= GroundSpeed.WALK_STOP_MODIFIER);
    }

    @Test
    void followAimsAheadOfItsProjectionButNeverPastTheLeader() {
        LeaderTrail trail = straightTrail(20);
        assertEquals(13D, TravelOrder.follow(trail, 10D, 6D, 0D).x(), EPSILON);
        assertEquals(20D, TravelOrder.follow(trail, 19D, 15D, 0D).x(), EPSILON);
    }

    @Test
    void followKeepsTheLaneOffsetInsideTheSideRoom() {
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x <= 20; x++) {
            trail.setRoom(trail.record(x, 64D, 0D), 1F, 5F);
        }
        // Travel along +x: positive offsets lie toward (-dirZ, dirX) = +z, negative toward -z.
        TravelOrder.Target left = TravelOrder.follow(trail, 10D, 6D, 4D);
        assertEquals(13D, left.x(), EPSILON);
        assertEquals(1D, left.z(), EPSILON);
        assertEquals(-4D, TravelOrder.follow(trail, 10D, 6D, -4D).z(), EPSILON);
    }

    @Test
    void followKeepsTheLaneOffsetInsideTheRoomAhead() {
        // Side room 5 up to x 15, then 1. A follower at arc 10 (look-ahead 13) already sees the narrowing
        // ROOM_AHEAD past its look-ahead point; one at arc 5 does not.
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x <= 20; x++) {
            float room = x >= 16 ? 1F : 5F;
            trail.setRoom(trail.record(x, 64D, 0D), room, room);
        }
        assertEquals(1D, TravelOrder.follow(trail, 10D, 6D, 4D).z(), EPSILON);
        assertEquals(4D, TravelOrder.follow(trail, 5D, 1D, 4D).z(), EPSILON);
    }

    @Test
    void slotIsClampedLikeTheFollowersOwnTarget() {
        // Side room 1 toward +z and 5 toward -z up to x 15, then 1 both sides. The slot at arc 6 with lane
        // +4 is clamped to 1 (its side); lane -4 keeps -4; the slot at arc 10 with lane -4 sees the
        // narrowing LOOK_AHEAD + ROOM_AHEAD past it (x 17), as the follower standing there would.
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x <= 20; x++) {
            trail.setRoom(trail.record(x, 64D, 0D), 1F, x >= 16 ? 1F : 5F);
        }
        TravelOrder.Target slot = TravelOrder.slot(trail, 6D, 4D);
        assertEquals(6D, slot.x(), EPSILON);
        assertEquals(64D, slot.y(), EPSILON);
        assertEquals(1D, slot.z(), EPSILON);
        assertEquals(-4D, TravelOrder.slot(trail, 6D, -4D).z(), EPSILON);
        assertEquals(-1D, TravelOrder.slot(trail, 10D, -4D).z(), EPSILON);
        // A follower standing at its slot steers to the same clamp.
        assertEquals(TravelOrder.follow(trail, 10D, 10D, -4D).z(), TravelOrder.slot(trail, 10D, -4D).z(), EPSILON);
    }

    @Test
    void slotHasNoOffsetWhereTheRoomIsUnknownAndNoneOnAnEmptyTrail() {
        assertEquals(0D, TravelOrder.slot(straightTrail(20), 6D, 4D).z(), EPSILON);
        assertNull(TravelOrder.slot(new LeaderTrail(), 6D, 4D));
    }

    @Test
    void followWaitsWhileItsTargetIsNotOnTheTrailYet() {
        LeaderTrail trail = straightTrail(5);
        assertEquals(0D, TravelOrder.follow(trail, 0D, -4D, 0D).speed(), EPSILON);
    }
}
