package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextSteeringTest {

    private static final double EPSILON = 1.0E-9D;

    private static boolean[] open() {
        return new boolean[ContextSteering.HEADINGS];
    }

    private static double[] calm() {
        return new double[ContextSteering.HEADINGS];
    }

    private static double dx(double yaw) {
        return -Math.sin(Math.toRadians(yaw));
    }

    private static double dz(double yaw) {
        return Math.cos(Math.toRadians(yaw));
    }

    @Test
    void headingsFollowMinecraftYaw() {
        assertEquals(0D, ContextSteering.dirX(0), EPSILON);
        assertEquals(1D, ContextSteering.dirZ(0), EPSILON);
        assertEquals(-1D, ContextSteering.dirX(3), EPSILON);
        assertEquals(0D, ContextSteering.dirZ(3), EPSILON);
        assertEquals(90F, ContextSteering.yawOf(3), 0F);
    }

    @Test
    void freeWayPicksTheHeadingTowardTheTarget() {
        assertEquals(0, ContextSteering.pick(0D, 1D, 0F, ContextSteering.STOP, open(), calm()));
    }

    @Test
    void maskedHeadingIsNeverPicked() {
        boolean[] masked = open();
        masked[0] = true;
        int heading = ContextSteering.pick(0D, 1D, 0F, ContextSteering.STOP, masked, calm());
        assertTrue(heading == 1 || heading == 11, "should take a neighbouring heading, was " + heading);
    }

    @Test
    void wallAheadTurnsAlongTheWall() {
        // A wall across the way: every heading within 60 degrees of the target is blocked.
        boolean[] masked = open();
        for (int heading : new int[]{10, 11, 0, 1, 2}) {
            masked[heading] = true;
        }
        int heading = ContextSteering.pick(0D, 1D, 0F, ContextSteering.STOP, masked, calm());
        assertTrue(heading == 3 || heading == 9, "should walk along the wall, was " + heading);
    }

    @Test
    void allMaskedStops() {
        boolean[] masked = open();
        Arrays.fill(masked, true);
        assertEquals(ContextSteering.STOP, ContextSteering.pick(0D, 1D, 0F, 0, masked, calm()));
    }

    @Test
    void deadEndStops() {
        // Only the headings back the way it came are open: standing beats turning round.
        boolean[] masked = open();
        Arrays.fill(masked, true);
        masked[5] = false;
        masked[6] = false;
        masked[7] = false;
        assertEquals(ContextSteering.STOP, ContextSteering.pick(0D, 1D, 0F, ContextSteering.STOP, masked, calm()));
    }

    @Test
    void hysteresisKeepsThePreviousHeading() {
        // The target is at yaw 20: heading 1 (yaw 30) scores a little better than heading 0, but not by HYSTERESIS.
        assertEquals(1, ContextSteering.pick(dx(20D), dz(20D), 0F, ContextSteering.STOP, open(), calm()));
        assertEquals(0, ContextSteering.pick(dx(20D), dz(20D), 0F, 0, open(), calm()));
    }

    @Test
    void clearlyBetterHeadingWins() {
        assertEquals(3, ContextSteering.pick(dx(90D), dz(90D), 0F, 0, open(), calm()));
    }

    @Test
    void clearlyBetterAdjacentHeadingWinsOnceTheBodyHasTurned() {
        // Playtest 2 fix P8a: the body walks heading 1 (yaw 30) and the target lies straight along heading 0.
        // Heading 0 scores 1 - 0.04 (turn cost for 30 degrees), heading 1 scores cos 30 = 0.866: a gap of
        // 0.094, the most an adjacent heading can ever gain. HYSTERESIS must lie under it, or a walker can
        // never straighten up on a heading next to its own (it hugged walls until the mask took over).
        assertTrue(ContextSteering.HYSTERESIS < 1D - Math.cos(Math.toRadians(ContextSteering.STEP_DEGREES))
                - ContextSteering.TURN_COST_PER_DEGREE * (ContextSteering.STEP_DEGREES - ContextSteering.MAX_TURN_DEGREES));
        assertEquals(0, ContextSteering.pick(0D, 1D, 30F, 1, open(), calm()));
    }

    @Test
    void dangerSteersToTheSafeSide() {
        double[] danger = calm();
        danger[0] = ContextSteering.DANGER_BLOCKED_FAR;
        assertEquals(1, ContextSteering.pick(0D, 1D, 0F, ContextSteering.STOP, open(), danger));
    }

    @Test
    void speedSlowsOnArrivalAndNearBlocks() {
        assertEquals(1D, ContextSteering.speedFactor(10D, 10D), EPSILON);
        assertEquals(0.5D, ContextSteering.speedFactor(1.5D, 10D), EPSILON);
        assertEquals(ContextSteering.MIN_ARRIVAL_FACTOR, ContextSteering.speedFactor(0D, 10D), EPSILON);
        // The free-distance slow-down is the last FREE_SLOW_DISTANCE before the hard minimum (1 block).
        assertEquals(0.5D, ContextSteering.speedFactor(10D, 0.5D), EPSILON);
        assertEquals(ContextSteering.MIN_FREE_FACTOR, ContextSteering.speedFactor(10D, 0D), EPSILON);
    }

    @Test
    void tightTravelSlotLiesOutsideTheSlowDownZone() {
        // A follower in a tight slot stands TIGHT_MARGIN past the member ahead's hard minimum (free
        // distance 1). It must not be slowed there, or it cannot hold the slot at the leader's pace.
        assertEquals(1D, ContextSteering.speedFactor(10D, TravelOrder.TIGHT_MARGIN), EPSILON);
    }

    /** Every heading masked: by blocks, or (those also in {@code crowded}) by a member. */
    private static boolean[] allMasked() {
        boolean[] masked = open();
        Arrays.fill(masked, true);
        return masked;
    }

    private static boolean[] crowdedAt(int... headings) {
        boolean[] crowded = open();
        for (int heading : headings) {
            crowded[heading] = true;
        }
        return crowded;
    }

    @Test
    void stoppedByAMemberOnTheHeadingItWouldWalkIsYielding() {
        // Target straight ahead; heading 0 is open to blocks but a member masks it, every other heading is
        // blocked: the member alone stopped the walker (lead decision 14: waiting, not stuck).
        boolean[] crowded = crowdedAt(0);
        assertTrue(ContextSteering.yielding(0D, 1D, 0F, crowded, allMasked(), calm()));
    }

    @Test
    void stoppedByBlocksWithAMemberBehindIsNotYielding() {
        // Dead end: only the heading back the way it came (6) is open to blocks, and a member masks it.
        // The walker would not have turned round anyway (its interest is under STOP_SCORE): blocks stopped it.
        boolean[] crowded = crowdedAt(6);
        assertFalse(ContextSteering.yielding(0D, 1D, 0F, crowded, allMasked(), calm()));
    }

    @Test
    void stoppedWithNoMemberMaskIsNotYielding() {
        boolean[] crowded = open();
        assertFalse(ContextSteering.yielding(0D, 1D, 0F, crowded, allMasked(), calm()));
    }

    @Test
    void yieldingOnlyWhenTheMemberMaskedHeadingBeatsEveryOpenOne() {
        // Heading 0 (interest 1) is crowded, heading 1 (0.866) is open: the walker would have taken 0.
        boolean[] crowded = crowdedAt(0);
        boolean[] masked = allMasked();
        masked[1] = false;
        assertTrue(ContextSteering.yielding(0D, 1D, 0F, crowded, masked, calm()));
        // Heading 11 (0.866) is crowded, heading 0 (1) is open: the member cost the walker nothing.
        crowded = crowdedAt(11);
        masked = allMasked();
        masked[0] = false;
        assertFalse(ContextSteering.yielding(0D, 1D, 0F, crowded, masked, calm()));
    }

    @Test
    void yieldHeadingIsTheCrowdedHeadingThatWouldHaveWon() {
        // Playtest 2 fix P8b: the make-way ask needs the member that masked the winning heading, so the yield
        // test names the heading. Heading 0 crowded, all else blocked: 0. Headings 0 and 1 crowded: 0 (the
        // better one). Heading 11 crowded with 0 open: no yield, STOP.
        assertEquals(0, ContextSteering.yieldHeading(0D, 1D, 0F, crowdedAt(0), allMasked(), calm()));
        boolean[] crowded = crowdedAt(0);
        crowded[1] = true;
        assertEquals(0, ContextSteering.yieldHeading(0D, 1D, 0F, crowded, allMasked(), calm()));
        boolean[] masked = allMasked();
        masked[0] = false;
        assertEquals(ContextSteering.STOP, ContextSteering.yieldHeading(0D, 1D, 0F, crowdedAt(11), masked, calm()));
        assertEquals(ContextSteering.STOP, ContextSteering.yieldHeading(0D, 1D, 0F, open(), allMasked(), calm()));
    }

    @Test
    void dangerCountsAgainstTheOpenHeading() {
        // Heading 0 open but blocked 3 ahead (danger 0.5: score 0.5); heading 1 crowded (0.866) beats it.
        boolean[] crowded = crowdedAt(1);
        boolean[] masked = allMasked();
        masked[0] = false;
        double[] danger = calm();
        danger[0] = ContextSteering.DANGER_BLOCKED_FAR;
        assertTrue(ContextSteering.yielding(0D, 1D, 0F, crowded, masked, danger));
    }

    @Test
    void aYieldEndsAfterMaxWaitTicks() {
        assertTrue(ContextSteering.waiting(true, 1));
        assertTrue(ContextSteering.waiting(true, ContextSteering.MAX_WAIT_TICKS));
        assertFalse(ContextSteering.waiting(true, ContextSteering.MAX_WAIT_TICKS + 1));
        assertFalse(ContextSteering.waiting(false, 1));
    }

    @Test
    void maxWaitOutlastsAGrazingHerdMateCrossingTheWalkersPath() {
        // The longest legitimate yield, from the constants: a Gallimimus (width 1.2) crossing at the graze
        // speed must carry its body through the walker's hard minimum zone (2 x personal space) and clear it
        // (plus its own width): 5.6 blocks at 0.049 blocks per tick, about 115 ticks. The time-out must leave
        // a margin above that (a crosser must never time the walker out just as it clears), yet not sit on a
        // deadlock for more than twice as long.
        double width = 1.2D;
        double crossing = 2D * HerdSpacing.personalSpace(width, width) + width;
        double graze = GroundSpeed.groundSpeed(FollowFormation.GRAZE_SPEED, Dinosaur.BASE_MOVEMENT_SPEED);
        double crossingTicks = crossing / graze;
        assertTrue(crossingTicks > 100D && crossingTicks < 130D, "crossing ticks " + crossingTicks);
        assertTrue(ContextSteering.MAX_WAIT_TICKS >= crossingTicks + ProgressMonitor.WINDOW_TICKS,
                "MAX_WAIT_TICKS " + ContextSteering.MAX_WAIT_TICKS + " leaves no window above " + crossingTicks);
        assertTrue(ContextSteering.MAX_WAIT_TICKS <= 2D * crossingTicks);
    }

    @Test
    void walksHeadingOnlyWithinHalfAStepOfTheYaw() {
        // Playtest 2 fix P8a: the guard tests along the body yaw, the probes along the heading. A guard stop
        // while the yaw still lags the heading by more than half a step was not on the heading walked, so it
        // must not mask that heading at the next evaluation.
        assertTrue(ContextSteering.walksHeading(0F, 0));
        assertTrue(ContextSteering.walksHeading(15F, 0));
        assertTrue(ContextSteering.walksHeading(-15F, 0));
        assertTrue(ContextSteering.walksHeading(345F, 0));
        assertFalse(ContextSteering.walksHeading(16F, 0));
        assertFalse(ContextSteering.walksHeading(30F, 0));
        assertTrue(ContextSteering.walksHeading(-90F, 9));
        assertFalse(ContextSteering.walksHeading(0F, ContextSteering.STOP));
    }

    @Test
    void facesDirectionWithinHalfAStep() {
        // P8a fix round 1: a path hold counts only while the yaw points at the next node (within 15 degrees),
        // so the ticks of a turn toward it (up to 19 for a half turn) are not held ticks.
        assertTrue(ContextSteering.facesDirection(0F, 0D, 1D));
        assertTrue(ContextSteering.facesDirection(-90F, 1D, 0D));
        assertTrue(ContextSteering.facesDirection(14F, 0D, 1D));
        assertTrue(ContextSteering.facesDirection(-15F, 0D, 1D));
        assertFalse(ContextSteering.facesDirection(16F, 0D, 1D));
        assertFalse(ContextSteering.facesDirection(180F, 0D, 1D));
        assertTrue(ContextSteering.facesDirection(45F, -1D, 1D));
        // On the node itself (no direction) the body faces it by definition.
        assertTrue(ContextSteering.facesDirection(123F, 0D, 0D));
    }

    @Test
    void facesDirectionWithinAGivenLimit() {
        // P12b: the guard's member test looks along the move direction while the yaw is within 45 degrees of it.
        assertTrue(ContextSteering.facesDirection(40F, 0D, 1D, 45D));
        assertTrue(ContextSteering.facesDirection(-44F, 0D, 1D, 45D));
        assertFalse(ContextSteering.facesDirection(50F, 0D, 1D, 45D));
        assertFalse(ContextSteering.facesDirection(20F, 0D, 1D, ContextSteering.HALF_STEP_DEGREES));
        assertTrue(ContextSteering.facesDirection(170F, 0D, 0D, 45D), "no direction counts as faced");
    }

    @Test
    void nearestHeadingRoundsYaw() {
        assertEquals(9, ContextSteering.nearestHeading(-90F));
        assertEquals(3, ContextSteering.nearestHeading(95F));
        assertEquals(0, ContextSteering.nearestHeading(359F));
    }

    private static final double ATTRIBUTE = Dinosaur.BASE_MOVEMENT_SPEED;
    private static final double FLOOR = GroundSpeed.groundSpeed(GroundSpeed.MIN_WALK_MODIFIER, ATTRIBUTE);

    /** The ground speed the walker really walks for a matched speed: under the stop threshold it stands, else the floor lifts it. */
    private static double walked(double matched) {
        return GroundSpeed.groundSpeed(GroundSpeed.walkModifier(GroundSpeed.modifierFor(matched, ATTRIBUTE)), ATTRIBUTE);
    }

    @Test
    void farBehindTheMateAheadKeepsTheWantedSpeed() {
        // P12b: at the matching range the cap lies above the catch-up speed, so speed matching starts without a jump.
        double catchUp = GroundSpeed.groundSpeed(TravelOrder.CATCH_UP_SPEED, ATTRIBUTE);
        double journey = GroundSpeed.groundSpeed(JourneyPolicy.JOURNEY_SPEED, ATTRIBUTE);
        assertEquals(catchUp, ContextSteering.matchedSpeed(catchUp, journey, ContextSteering.MATCH_RANGE, FLOOR), EPSILON);
        assertEquals(catchUp, ContextSteering.matchedSpeed(catchUp, 0D, ContextSteering.MATCH_RANGE, FLOOR), EPSILON,
                "even behind a mate that hardly moves");
        assertTrue(ContextSteering.MATCH_SETTLE_FREE + catchUp / ContextSteering.MATCH_CLOSE_RATE <= ContextSteering.MATCH_RANGE);
    }

    @Test
    void catchingUpFollowerSettlesBehindTheMateWithoutMaskOrGuard() {
        // P12b, motion report cause 2: catch-up 1.2 behind a mate at the journey pace 0.8. Before, the follower
        // ran at full speed into the member mask (1.08) and the guard (0.6) and stopped dead or zigzagged.
        double catchUp = GroundSpeed.groundSpeed(TravelOrder.CATCH_UP_SPEED, ATTRIBUTE);
        double ahead = GroundSpeed.groundSpeed(JourneyPolicy.JOURNEY_SPEED, ATTRIBUTE);
        double minimum = HerdSpacing.hardMinimum(1.2D, 1.2D, false, false);
        double free = ContextSteering.MATCH_RANGE;
        double previous = catchUp;
        double least = free;
        double speed = catchUp;
        for (int tick = 0; tick < 400; tick++) {
            speed = walked(ContextSteering.matchedSpeed(catchUp, ahead, free, FLOOR));
            assertFalse(HerdSpacing.wouldCrowd(0D, 0D, 0D, speed, 0D, minimum + free, 0D, ahead, minimum, HerdBodies.PREDICT_TICKS),
                    "the member mask fires at free " + free);
            assertTrue(Math.abs(speed - previous) < 0.01D, "speed jumps at free " + free);
            previous = speed;
            free -= speed - ahead;
            least = Math.min(least, free);
        }
        assertTrue(least >= ContextSteering.MATCH_SETTLE_FREE - EPSILON, "closer than the settle distance: " + least);
        assertTrue(least > HerdMoveControl.GUARD_AHEAD, "inside the guard distance");
        assertEquals(ahead, speed, 1.0E-3D, "matches the mate's speed");
    }

    @Test
    void matchedFollowIsNeverMaskedByConstruction() {
        // The closing speed allowed over the mask's prediction window is less than the free distance itself.
        assertTrue(ContextSteering.MATCH_CLOSE_RATE * HerdBodies.PREDICT_TICKS < 1D);
        assertTrue(ContextSteering.MATCH_SETTLE_FREE > HerdMoveControl.GUARD_AHEAD);
    }

    @Test
    void mateSlowerThanTheWalkFloorMakesTheWalkerStandAtTheSettleDistance() {
        // The floor lifts a slow matched speed; at the settle distance the walker stands instead of creeping into the guard.
        double slow = 0.015D;
        assertEquals(0D, ContextSteering.matchedSpeed(0.194D, slow, ContextSteering.MATCH_SETTLE_FREE - 0.05D, FLOOR), EPSILON);
        double beyond = ContextSteering.matchedSpeed(0.194D, slow, ContextSteering.MATCH_SETTLE_FREE + 0.2D, FLOOR);
        assertEquals(slow + ContextSteering.MATCH_CLOSE_RATE * 0.2D, beyond, EPSILON);
        // A mate faster than the floor: inside the settle distance the walker falls back a little, it does not stop.
        assertEquals(0.05D - ContextSteering.MATCH_CLOSE_RATE * 0.05D,
                ContextSteering.matchedSpeed(0.194D, 0.05D, ContextSteering.MATCH_SETTLE_FREE - 0.05D, FLOOR), EPSILON);
        // Never negative, never above the wanted speed.
        assertEquals(0D, ContextSteering.matchedSpeed(0.194D, 0D, 0D, FLOOR), EPSILON);
        assertEquals(0.05D, ContextSteering.matchedSpeed(0.05D, 0.086D, 2D, FLOOR), EPSILON);
    }
    @Test
    void catchUpMatchingStartsEarlierAndSlowsGentler() {
        // P13b item 4 (ruling 8): behind a mate at the journey pace, a catch-up follower starts to slow 4.3 blocks
        // of free distance out (P12b: 3.2) and loses at most 0.0036 blocks per tick per tick (P12b: 0.0054).
        double catchUp = GroundSpeed.groundSpeed(TravelOrder.CATCH_UP_SPEED, ATTRIBUTE);
        double ahead = GroundSpeed.groundSpeed(JourneyPolicy.JOURNEY_SPEED, ATTRIBUTE);
        assertTrue(ContextSteering.matchedSpeed(catchUp, ahead, 4.2D, FLOOR) < catchUp, "slows from 4.2 blocks out");
        double free = ContextSteering.MATCH_RANGE;
        double previous = catchUp;
        double largestDrop = 0D;
        for (int tick = 0; tick < 400; tick++) {
            double speed = walked(ContextSteering.matchedSpeed(catchUp, ahead, free, FLOOR));
            largestDrop = Math.max(largestDrop, previous - speed);
            previous = speed;
            free -= speed - ahead;
        }
        assertTrue(largestDrop < 0.004D, "slows too hard: " + largestDrop + " per tick");
    }
}
