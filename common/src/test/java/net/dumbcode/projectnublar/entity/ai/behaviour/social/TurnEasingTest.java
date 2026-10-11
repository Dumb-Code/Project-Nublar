package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnEasingTest {

    private static final float EPSILON = 1.0E-4F;

    /** Turns toward a fixed target yaw change from rest; returns the steps, one per tick. */
    private static float[] turn(float total) {
        float[] steps = new float[100];
        float done = 0F;
        float rate = 0F;
        int tick = 0;
        while (Math.abs(total - done) > 0F && tick < steps.length) {
            rate = TurnEasing.yawStep(total - done, rate);
            steps[tick++] = rate;
            done += rate;
        }
        return java.util.Arrays.copyOf(steps, tick);
    }

    @Test
    void aTurnStartsSlowly() {
        // P12b: the turn rate ramps in instead of jumping to the full 10 degrees in the first tick.
        assertEquals(TurnEasing.TURN_ACCELERATION, TurnEasing.yawStep(30F, 0F), EPSILON);
        assertEquals(-TurnEasing.TURN_ACCELERATION, TurnEasing.yawStep(-30F, 0F), EPSILON);
        assertTrue(TurnEasing.TURN_ACCELERATION < Dinosaur.MAX_TURN_PER_TICK);
    }

    @Test
    void aTurnLandsExactlyWithoutOvershootAndNeverPassesTheTopRate() {
        for (float total : new float[] {2F, 5F, 30F, -30F, 60F, 90F, -179F}) {
            float[] steps = turn(total);
            float done = 0F;
            for (float step : steps) {
                assertTrue(Math.signum(step) == Math.signum(total), "a step against the turn for " + total);
                assertTrue(Math.abs(step) <= Dinosaur.MAX_TURN_PER_TICK + EPSILON, "above the top rate for " + total);
                done += step;
                assertTrue(Math.abs(done) <= Math.abs(total) + EPSILON, "overshoot for " + total);
            }
            assertEquals(total, done, EPSILON, "did not land for " + total);
            assertTrue(steps.length <= 30, "too slow for " + total + ": " + steps.length + " ticks");
        }
    }

    @Test
    void aLongTurnReachesTheTopRateThenEasesOut() {
        float[] steps = turn(90F);
        float peak = 0F;
        int peakAt = 0;
        for (int i = 0; i < steps.length; i++) {
            if (steps[i] > peak) {
                peak = steps[i];
                peakAt = i;
            }
        }
        assertEquals(Dinosaur.MAX_TURN_PER_TICK, peak, EPSILON);
        for (int i = 1; i <= peakAt; i++) {
            assertTrue(steps[i] - steps[i - 1] <= TurnEasing.TURN_ACCELERATION + EPSILON, "ramp-in faster than the acceleration");
        }
        for (int i = peakAt + 1; i < steps.length; i++) {
            assertTrue(steps[i] <= steps[i - 1] + EPSILON, "the rate rises again after the peak");
        }
        assertTrue(steps[steps.length - 1] < peak / 2F, "no easing out: last step " + steps[steps.length - 1]);
    }

    @Test
    void aTurnTheOtherWayStartsFromRest() {
        assertEquals(TurnEasing.TURN_ACCELERATION, TurnEasing.yawStep(30F, -Dinosaur.MAX_TURN_PER_TICK), EPSILON);
        assertEquals(0F, TurnEasing.yawStep(0F, 7F), EPSILON);
    }

    @Test
    void theBodyEasesTowardTheYaw() {
        // P12b: the body yaw moves a fraction of the way each tick (at most BODY_MAX_STEP), instead of copying the yaw.
        assertEquals(10F * TurnEasing.BODY_TURN_FRACTION, TurnEasing.bodyStep(10F), EPSILON);
        assertEquals(-10F * TurnEasing.BODY_TURN_FRACTION, TurnEasing.bodyStep(-10F), EPSILON);
        assertEquals(TurnEasing.BODY_MAX_STEP, TurnEasing.bodyStep(170F), EPSILON);
        assertEquals(0.5F, TurnEasing.bodyStep(0.5F), EPSILON, "a small gap closes at once");
        float gap = 90F;
        int ticks = 0;
        while (gap != 0F && ticks < 50) {
            gap -= TurnEasing.bodyStep(gap);
            ticks++;
        }
        assertEquals(0F, gap, EPSILON);
        assertTrue(ticks < 20, "the body takes too long: " + ticks);
    }
    @Test
    void aThirtyDegreeTurnIsSofterAndNeverOvershoots() {
        // P13b item 4 (ruling 8): a slower ramp, 2, 4, 6, 8, 6.3, 3.7 degrees (P12b: 3, 6, 9, 8.5, 3.5). The gap to
        // the new heading starts at 30, inside the guard's turn limit, and only shrinks while the turn runs.
        assertTrue(30F <= HerdMoveControl.GUARD_TURN_LIMIT_DEGREES, "a 30-degree change starts inside the guard's turn limit");
        float[] steps = turn(30F);
        float[] expected = {2F, 4F, 6F, 8F, 6.325F, 3.675F};
        assertEquals(expected.length, steps.length, "ticks for a 30-degree turn");
        float left = 30F;
        for (int i = 0; i < steps.length; i++) {
            assertEquals(expected[i], steps[i], 1.0E-3F, "step " + i);
            left -= steps[i];
            assertTrue(left >= -1.0E-3F, "overshoots the heading at tick " + i);
        }
    }

    @Test
    void theBodyEasesGentlerButStillCatchesUpWithAFullRateTurn() {
        // P13b item 4: the body closes a third of its gap per tick (P12b: half). The step cap stays above the top
        // turn rate, or a body that lags a full-rate turn could never catch up.
        assertEquals(1F / 3F, TurnEasing.BODY_TURN_FRACTION, EPSILON);
        assertTrue(TurnEasing.BODY_MAX_STEP > Dinosaur.MAX_TURN_PER_TICK);
        float yaw = 0F;
        float body = -60F;
        for (int tick = 0; tick < 40; tick++) {
            yaw += Dinosaur.MAX_TURN_PER_TICK;
            body += TurnEasing.bodyStep(yaw - body);
        }
        // Steady lag after the step: 10 x (1 - f) / f = 20 degrees.
        assertEquals(20F, yaw - body, 0.1F, "the body lag behind a full-rate turn");
    }
}
