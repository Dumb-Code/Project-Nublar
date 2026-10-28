package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

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
