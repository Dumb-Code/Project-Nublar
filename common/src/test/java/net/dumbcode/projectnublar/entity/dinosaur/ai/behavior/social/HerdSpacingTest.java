package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the herd spacing arithmetic. Runs on the JUnit stand-in runner (tools/junit-standin). */
class HerdSpacingTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void personalSpaceIsHalfOfEachWidthPlusTheGap() {
        assertEquals(2D + HerdSpacing.GAP, HerdSpacing.personalSpace(2D, 2D), EPSILON);
        assertEquals(1.3D + HerdSpacing.GAP, HerdSpacing.personalSpace(0.6D, 2D), EPSILON);
    }

    @Test
    void hardMinimumIsThePersonalSpaceWhileMovingInTheOpen() {
        assertEquals(3D, HerdSpacing.hardMinimum(2D, 2D, false, false), EPSILON);
    }

    @Test
    void aRestingBodyMayBeCloser() {
        assertEquals(2D + HerdSpacing.RESTING_GAP, HerdSpacing.hardMinimum(2D, 2D, true, false), EPSILON);
    }

    @Test
    void tightGroundLowersTheHardMinimumToTheBodyWidthPlusTheTightGap() {
        // Playtest 2 fix P8b (lead decision): body width + 0.5 = 1.7 for Gallimimus where the ground is tight,
        // the personal space (2.2) in the open; resting and tight together take the smaller gap.
        assertEquals(1.2D + HerdSpacing.TIGHT_GAP, HerdSpacing.hardMinimum(1.2D, 1.2D, false, true), EPSILON);
        assertEquals(1.2D + Math.min(HerdSpacing.RESTING_GAP, HerdSpacing.TIGHT_GAP), HerdSpacing.hardMinimum(1.2D, 1.2D, true, true), EPSILON);
        assertTrue(HerdSpacing.hardMinimum(1.2D, 1.2D, false, true) < HerdSpacing.hardMinimum(1.2D, 1.2D, false, false));
    }

    @Test
    void groundIsTightWhereNeitherSideHasTheOpenMinimumPlusABoxWidthOfRoom() {
        // Gallimimus: 2.2 + 1.25 = 3.45; side room comes in half-block steps, so 3.5 is the least that is open.
        double threshold = HerdSpacing.tightThreshold(1.2D, 1.25D);
        assertEquals(HerdSpacing.personalSpace(1.2D, 1.2D) + 1.25D, threshold, EPSILON);
        assertFalse(HerdSpacing.tightGround(5.5F, 0F, threshold, false), "open on one side is open");
        assertFalse(HerdSpacing.tightGround(0F, 3.5F, threshold, false));
        assertTrue(HerdSpacing.tightGround(3F, 3F, threshold, false), "a 7.25-wide corridor with the body in the middle");
        assertTrue(HerdSpacing.tightGround(0F, 0F, threshold, false));
    }

    @Test
    void tightGroundStaysTightUntilTheRoomIsOneStepPastTheThreshold() {
        // Fix round 1 (hysteresis): once tight, 3.5 of room is still tight for Gallimimus; 4.0 is open again.
        double threshold = HerdSpacing.tightThreshold(1.2D, 1.25D);
        assertTrue(HerdSpacing.tightGround(3.5F, 0F, threshold, true));
        assertFalse(HerdSpacing.tightGround(3.5F, 0F, threshold, false));
        assertFalse(HerdSpacing.tightGround(4F, 0F, threshold, true));
        assertEquals(threshold + HerdSpacing.TIGHT_HYSTERESIS, HerdSpacing.tightLimit(threshold, true), EPSILON);
        assertEquals(threshold, HerdSpacing.tightLimit(threshold, false), EPSILON);
    }

    @Test
    void aSideStepCrossesTheLineOnlyWhenTheOtherSideEndsClearerOfTheLine() {
        // Staying ends lateral + ownRoom from the line, crossing ends otherRoom - lateral from it (fix round 1).
        assertTrue(HerdSpacing.crossesLine(1F, 3F, 0D, 2.7D));
        assertTrue(HerdSpacing.crossesLine(0F, 3F, 1D, 2.7D), "3 - 1 beyond the line beats 1 + 0 on this side");
        assertFalse(HerdSpacing.crossesLine(0F, 0.5F, 1D, 2.7D), "crossing would end 0.5 short of the line: worse than staying 1 beside it");
        assertFalse(HerdSpacing.crossesLine(0F, 2F, 1D, 2.7D), "2 - 1 = 1 + 0: no better");
        assertFalse(HerdSpacing.crossesLine(3F, 5.5F, 0D, 2.7D), "its own side has the needed room");
        assertFalse(HerdSpacing.crossesLine(1F, 1F, 0D, 2.7D), "no better side");
    }

    @Test
    void aSideStepOnItsOwnSideGoesNoFartherThanTheRoomThere() {
        // P12c (cliff cause 6): the target lies ARRIVED beyond the distance walked, and that distance is the
        // needed one only where the room reaches it; otherwise the room found (a rim, a wall).
        assertEquals(2.7D + 1D, HerdSpacing.ownSideReach(2.7D, 5.5F, 1D), EPSILON);
        assertEquals(1.5D + 1D, HerdSpacing.ownSideReach(2.7D, 1.5F, 1D), EPSILON);
        assertEquals(1D, HerdSpacing.ownSideReach(2.7D, 0F, 1D), EPSILON, "no room: the target is ARRIVED away, so the member stays");
    }

    @Test
    void aMemberIsAskedToMakeWayWhenItStandsAndIsOutrankedOrDoesNotWantToMove() {
        // Fix round 1: any rank may ask a standing member that is not trying to move (arrived, resting,
        // eating, stopped by blocks); between two members that both want to move only the higher rank asks.
        assertTrue(HerdSpacing.asksToMakeWay(false, false, true, true));
        assertTrue(HerdSpacing.asksToMakeWay(false, false, false, false));
        assertTrue(HerdSpacing.asksToMakeWay(false, false, false, true));
        assertFalse(HerdSpacing.asksToMakeWay(false, false, true, false), "a lower rank waits for a higher rank that wants to move");
        assertFalse(HerdSpacing.asksToMakeWay(false, true, false, true), "a walking blocker is not a hold");
        assertFalse(HerdSpacing.asksToMakeWay(false, true, true, true));
    }

    @Test
    void theLeaderIsNeverAskedToMakeWay() {
        // Spec "Who yields: leader first": followers go round a standing leader or wait, whatever it does.
        assertFalse(HerdSpacing.asksToMakeWay(true, false, false, false));
        assertFalse(HerdSpacing.asksToMakeWay(true, false, false, true));
        assertFalse(HerdSpacing.asksToMakeWay(true, false, true, true));
    }

    @Test
    void headingStraightAtAStandingBodyCrowds() {
        assertTrue(HerdSpacing.wouldCrowd(0D, 0D, 0D, 0.5D, 0D, 6D, 0D, 0D, 3D, 10));
    }

    @Test
    void passingWideDoesNotCrowd() {
        assertFalse(HerdSpacing.wouldCrowd(0D, 0D, 0D, 0.5D, 4D, 3D, 0D, 0D, 3D, 10));
    }

    @Test
    void steppingAwayWhenAlreadyTooCloseIsAllowed() {
        assertFalse(HerdSpacing.wouldCrowd(0D, 0D, -0.3D, 0D, 1D, 0D, 0D, 0D, 3D, 10));
        assertTrue(HerdSpacing.wouldCrowd(0D, 0D, 0.3D, 0D, 1D, 0D, 0D, 0D, 3D, 10));
    }

    @Test
    void standingStillNeverCrowds() {
        assertFalse(HerdSpacing.wouldCrowd(0D, 0D, 0D, 0D, 1D, 0D, 0D, 0D, 3D, 10));
    }

    @Test
    void stepCrowdsOnlyWhenGettingCloser() {
        assertTrue(HerdSpacing.stepCrowds(0D, 0D, 0D, 0.6D, 0D, 2.5D, 3D));
        assertFalse(HerdSpacing.stepCrowds(0D, 0D, 0D, -0.6D, 0D, 2.5D, 3D));
        assertFalse(HerdSpacing.stepCrowds(0D, 0D, 0D, 0.6D, 0D, 10D, 3D));
    }

    @Test
    void awayFromOneNeighbourIsStraightBack() {
        Vec3 away = HerdSpacing.away(List.of(new HerdSpacing.Neighbour(1D, 0D, 3D)));
        assertEquals(-1D, away.x, EPSILON);
        assertEquals(0D, away.z, EPSILON);
    }

    @Test
    void awayFromBalancedNeighboursIsSideways() {
        Vec3 away = HerdSpacing.away(List.of(new HerdSpacing.Neighbour(1D, 0D, 3D), new HerdSpacing.Neighbour(-1D, 0D, 3D)));
        assertEquals(0D, away.x, EPSILON);
        assertEquals(1D, Math.abs(away.z), EPSILON);
    }

    @Test
    void awayFromNobodyIsZero() {
        assertEquals(Vec3.ZERO, HerdSpacing.away(List.of()));
    }

    @Test
    void awayFromANeighbourOnTheSameSpotIsAUnitStep() {
        Vec3 away = HerdSpacing.away(List.of(new HerdSpacing.Neighbour(0D, 0D, 3D)));
        assertEquals(1D, away.length(), EPSILON);
    }

    @Test
    void memberOnTheLeftStepsFurtherLeft() {
        Vec3 side = HerdSpacing.sideStep(1D, 0D, 2D, 1D);
        assertEquals(0D, side.x, EPSILON);
        assertEquals(1D, side.z, EPSILON);
    }

    @Test
    void memberOnTheRightStepsFurtherRight() {
        Vec3 side = HerdSpacing.sideStep(1D, 0D, 2D, -1D);
        assertEquals(0D, side.x, EPSILON);
        assertEquals(-1D, side.z, EPSILON);
    }

    @Test
    void memberDeadAheadStepsToAFixedSide() {
        Vec3 side = HerdSpacing.sideStep(1D, 0D, 2D, 0D);
        assertEquals(1D, side.length(), EPSILON);
        assertEquals(0D, side.x, EPSILON);
    }

    @Test
    void sideStepIsNeverAlongTheHeading() {
        Vec3 side = HerdSpacing.sideStep(3D, 4D, 5D, 1D);
        assertEquals(0D, side.x * 3D + side.z * 4D, EPSILON);
        assertEquals(1D, side.length(), EPSILON);
    }

    @Test
    void aStandingBodyCanBePassedWhereOneSideHasTheTightHardMinimumOfRoom() {
        // Playtest 2 fix P8a, with P8b's tight minimum: a walker passes a standing body only where its own box
        // can stand the hard minimum beside it, and a place with less than the open threshold of room is tight,
        // so the tight minimum (1.7) is the one that counts. Side room comes in half-block steps: 1.5 is short
        // of 1.7 and 2.0 is not.
        double minimum = HerdSpacing.hardMinimum(1.2D, 1.2D, false, true);
        assertTrue(HerdSpacing.roomToPass(2F, 0F, minimum));
        assertTrue(HerdSpacing.roomToPass(0F, 2F, minimum));
        assertTrue(HerdSpacing.roomToPass(5.5F, 5.5F, minimum));
        assertFalse(HerdSpacing.roomToPass(1.5F, 1.5F, minimum));
        assertFalse(HerdSpacing.roomToPass(0F, 0F, minimum));
    }
}
