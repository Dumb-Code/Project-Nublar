package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunGaitTest {

    private static final double ATTRIBUTE = Dinosaur.BASE_MOVEMENT_SPEED;

    private static double at(double modifier) {
        return GroundSpeed.groundSpeed(modifier, ATTRIBUTE);
    }

    @Test
    void grazeAndRoamWalksNeverRun() {
        assertFalse(RunGait.running(false, at(0.6D), ATTRIBUTE));
        assertFalse(RunGait.running(false, at(JourneyPolicy.JOURNEY_SPEED), ATTRIBUTE), "roaming journey 0.8");
        assertFalse(RunGait.running(true, at(0.8D), ATTRIBUTE), "a runner that slows to 0.8 walks");
    }

    @Test
    void urgentTripsAndChasesRun() {
        assertTrue(RunGait.running(false, at(1.2D), ATTRIBUTE), "urgent trip 1.2");
        assertTrue(RunGait.running(false, at(1.5D), ATTRIBUTE), "chase 1.5");
    }

    @Test
    void thresholdHasHysteresis() {
        double middle = at(1.0D);
        assertFalse(RunGait.running(false, middle, ATTRIBUTE), "a walker at 1.0 keeps walking");
        assertTrue(RunGait.running(true, middle, ATTRIBUTE), "a runner at 1.0 keeps running");
        assertTrue(RunGait.running(false, at(RunGait.ENTER_MODIFIER) * 1.01D, ATTRIBUTE));
        assertFalse(RunGait.running(true, at(RunGait.EXIT_MODIFIER) * 0.99D, ATTRIBUTE));
    }

    @Test
    void thresholdSitsBetweenTheGroundSpeedsOf08And12() {
        assertTrue(at(RunGait.EXIT_MODIFIER) > at(0.8D));
        assertTrue(at(RunGait.ENTER_MODIFIER) < at(1.2D));
        assertTrue(RunGait.EXIT_MODIFIER < RunGait.ENTER_MODIFIER);
    }

    @Test
    void walkIsAuthoredForTheRoamingJourneyAndTheRunFallbackForTheCatchUp() {
        // Both Gallimimus cycles cover about 3 blocks at these speeds (move.walk 1.875 s, run 0.79 s).
        assertTrue(Math.abs(RunGait.WALK_AUTHORED_MODIFIER - JourneyPolicy.JOURNEY_SPEED) < 1.0E-6D, "the roaming journey speed");
        assertTrue(Math.abs(RunGait.authoredSpeed(false, ATTRIBUTE, 38, 16) - at(RunGait.WALK_AUTHORED_MODIFIER)) < 1.0E-12D);
        assertTrue(Math.abs(RunGait.authoredSpeed(false, ATTRIBUTE, 0, 0) - at(RunGait.WALK_AUTHORED_MODIFIER)) < 1.0E-12D,
                "the walk does not depend on the lengths");
        assertTrue(RunGait.authoredSpeed(false, ATTRIBUTE, 38, 16) < at(RunGait.EXIT_MODIFIER), "the walk is made for a walk speed");
        assertTrue(RunGait.RUN_AUTHORED_MODIFIER > RunGait.ENTER_MODIFIER, "the run fallback is made for a run speed");
    }

    @Test
    void runFallbackWhenALengthIsNotKnown() {
        double fallback = at(RunGait.RUN_AUTHORED_MODIFIER);
        assertTrue(Math.abs(RunGait.authoredSpeed(true, ATTRIBUTE, 0, 16) - fallback) < 1.0E-12D, "walk length unknown");
        assertTrue(Math.abs(RunGait.authoredSpeed(true, ATTRIBUTE, 38, 0) - fallback) < 1.0E-12D, "run length unknown");
        assertTrue(Math.abs(RunGait.authoredSpeed(true, ATTRIBUTE, 38, -1) - fallback) < 1.0E-12D, "no run animation");
    }

    @Test
    void runIsAuthoredForTheWalksStrideSoTheStepRateIsContinuousForEverySpecies() {
        // Step rate at one ground speed v: walk (v / walk speed) / walk length, run (v / run speed) / run length. It is the
        // same for both when the run speed is the walk speed x walk length / run length. File lengths in seconds, and the
        // ticks (rounded up) the client reads: the rounding costs up to 3.5 % (the triceratops Sprint, 18.3 ticks read as 19).
        double[][] species = {
                {1.875D, 0.7917D},  // gallimimus: walk 2.4 x the run
                {1.3333D, 1D},      // dilophosaurus: 1.33 x
                {0.9583D, 0.7083D}, // velociraptor: 1.35 x
                {4.0417D, 2.7917D}, // brachiosaurus: 1.45 x
                {3D, 0.9167D},      // triceratops: 3.3 x
        };
        for (double[] lengths : species) {
            double walkCycle = lengths[0] * 20D;
            double runCycle = lengths[1] * 20D;
            int walkTicks = (int) Math.ceil(walkCycle);
            int runTicks = (int) Math.ceil(runCycle);
            double walk = RunGait.authoredSpeed(false, ATTRIBUTE, walkTicks, runTicks);
            double run = RunGait.authoredSpeed(true, ATTRIBUTE, walkTicks, runTicks);
            double ratio = (walk * walkCycle) / (run * runCycle); // run step rate over walk step rate
            assertTrue(Math.abs(ratio - 1D) < 0.05D, "walk " + lengths[0] + " s, run " + lengths[1] + " s: step rate x" + ratio);
        }
    }
    @Test
    void aWalkDrainsAQuarterLessStaminaAndARunAsBefore() {
        // P13b item 3 (ruling Q3): walking x0.75 of the species' drain, running unchanged (x2).
        assertEquals(1.5D, RunGait.staminaDrain(2D, false), 1.0E-9D, "Gallimimus walk: 2.0 per second before");
        assertEquals(4D, RunGait.staminaDrain(2D, true), 1.0E-9D, "Gallimimus run: unchanged");
    }

    @Test
    void aGallimimusWalksAboutEightyBlocksOfJourneyBeforeItIsTired() {
        // Gallimimus config: max 200, drain 2.0 per second, tired below 0.4 x 200 = 80; a journey starts at 150 or more.
        double perBlock = RunGait.staminaDrain(2D, false) / (at(JourneyPolicy.JOURNEY_SPEED) * 20D);
        double fromStart = (150D - 80D) / perBlock;
        double fromFull = (200D - 80D) / perBlock;
        assertTrue(fromStart > 79D && fromStart < 82D, "from the start minimum: " + fromStart + " blocks (60 before)");
        assertTrue(fromFull > 136D && fromFull < 140D, "from full: " + fromFull + " blocks (103 before)");
    }
}
