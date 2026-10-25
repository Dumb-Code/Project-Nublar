package net.dumbcode.projectnublar.entity.dinosaur.animation;

import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.RunGait;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovementGaitTest {

    private static final double ATTRIBUTE = Dinosaur.BASE_MOVEMENT_SPEED;
    /** Gallimimus cycles in ticks, rounded up: move.walk 1.875 s, run 0.7917 s. */
    private static final int WALK_CYCLE = 38;
    private static final int RUN_CYCLE = 16;
    /** Ground speed the walk is made for (roaming journey 0.8: 0.086 blocks per tick). */
    private static final double WALK = RunGait.authoredSpeed(false, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
    /** Ground speed the run is made for: the walk's stride per cycle, 38 / 16 x the walk (0.205 blocks per tick). */
    private static final double RUN = RunGait.authoredSpeed(true, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);

    private static MovementGait settled(double groundSpeed) {
        return settled(groundSpeed, WALK_CYCLE, RUN_CYCLE);
    }

    private static MovementGait settled(double groundSpeed, int walkCycle, int runCycle) {
        MovementGait gait = new MovementGait();
        for (int i = 0; i < 200; i++) {
            gait.tick(groundSpeed, ATTRIBUTE, walkCycle, runCycle);
        }
        return gait;
    }

    /** Leg cycles per tick: GeckoLib plays {@code speed} animation ticks per tick. */
    private static double stepRate(MovementGait gait, int walkCycle, int runCycle) {
        return gait.animationSpeed() / (gait.gait() == MovementGait.Gait.RUN ? runCycle : walkCycle);
    }

    @Test
    void standsWalksAndRunsAtSteadySpeeds() {
        assertSame(MovementGait.Gait.IDLE, settled(0D).gait());
        assertFalse(settled(0D).moving());
        assertSame(MovementGait.Gait.WALK, settled(WALK).gait());
        assertTrue(settled(WALK).moving());
        assertSame(MovementGait.Gait.RUN, settled(RUN).gait());
    }

    @Test
    void slowestHerdWalkStillWalks() {
        // The steering floor (modifier 0.45) moves 0.027 blocks per tick.
        assertSame(MovementGait.Gait.WALK, settled(0.027D).gait());
    }

    @Test
    void startsToWalkOnTheFirstTickFromALongStand() {
        MovementGait gait = settled(0D);
        gait.tick(WALK, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        assertSame(MovementGait.Gait.WALK, gait.gait());
    }

    @Test
    void oneStillTickDoesNotChangeTheGait() {
        // A late position packet or one guard tick gives one tick with no movement.
        MovementGait runner = settled(RUN);
        runner.tick(0D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        assertSame(MovementGait.Gait.RUN, runner.gait(), "a runner keeps running");
        MovementGait walker = settled(WALK);
        walker.tick(0D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        assertSame(MovementGait.Gait.WALK, walker.gait(), "a walker keeps walking");
    }

    @Test
    void oneBurstTickDoesNotMakeAWalkerRun() {
        // Two packets close together move the body 4/3 as fast for a tick.
        double inBand = 0.13D;
        MovementGait walker = settled(inBand);
        assertSame(MovementGait.Gait.WALK, walker.gait());
        walker.tick(inBand * 4D / 3D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        assertSame(MovementGait.Gait.WALK, walker.gait());
    }

    @Test
    void aGaitIsHeldForTheMinimumTimeAfterASwitch() {
        MovementGait gait = new MovementGait();
        gait.tick(WALK, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        assertSame(MovementGait.Gait.WALK, gait.gait(), "the first switch is free");
        for (int i = 1; i < MovementGait.HOLD_TICKS; i++) {
            gait.tick(0D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
            assertSame(MovementGait.Gait.WALK, gait.gait(), "held at tick " + i);
        }
        gait.tick(0D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        assertSame(MovementGait.Gait.IDLE, gait.gait(), "the hold is over");
    }

    @Test
    void stopsSoonAfterTheBodyStops() {
        MovementGait gait = settled(RUN);
        int ticks = 0;
        while (gait.moving() && ticks < 100) {
            gait.tick(0D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
            ticks++;
        }
        assertTrue(ticks > 1, "not on the first still tick");
        assertTrue(ticks <= MovementGait.HOLD_TICKS + 4, "stopped after " + ticks + " ticks");
    }

    @Test
    void animationSpeedIsGroundSpeedOverTheAuthoredSpeed() {
        assertEquals(1D, settled(WALK).animationSpeed(), 1.0E-6D);
        assertEquals(1D, settled(RUN).animationSpeed(), 1.0E-6D);
        assertEquals(1.5D, settled(WALK * 1.5D).animationSpeed(), 1.0E-6D, "a fast walk");
        assertEquals(MovementGait.MIN_ANIMATION_SPEED, settled(0.027D).animationSpeed(), 1.0E-6D, "clamped low");
        // The Gallimimus run reaches the high clamp only above the per-tick cap; a species with a short walk does.
        double dilophosaurusRun = RunGait.authoredSpeed(true, ATTRIBUTE, 27, 20);
        assertEquals(MovementGait.MAX_ANIMATION_SPEED, settled(dilophosaurusRun * 3D, 27, 20).animationSpeed(), 1.0E-6D,
                "clamped high");
    }

    @Test
    void stepRateIsNearlyContinuousAtTheRunSwitch() {
        // Gallimimus file: move.walk 1.875 s (37.5 ticks), run 0.7917 s (15.83 ticks). Cycles per tick at the same
        // ground speed: walk (speed / WALK) / 37.5, run (speed / RUN) / 15.83. Without the scale the rate jumped 2.4x.
        double walkCycle = 37.5D;
        double runCycle = 15.83D;
        double ratio = (RUN * runCycle) / (WALK * walkCycle);
        assertEquals(1D, ratio, 0.1D);
    }

    @Test
    void animationSpeedChangesSlowlyInASettledGait() {
        // GeckoLib scales the time since the cycle start by the speed; the change per tick keeps that phase error
        // under MAX_RATE_ERROR of the rate even at the end of a cycle.
        MovementGait gait = settled(WALK);
        gait.tick(WALK * 1.5D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        assertEquals(1D + MovementGait.MAX_RATE_ERROR / WALK_CYCLE, gait.animationSpeed(), 1.0E-9D, "one bounded step");
        for (int i = 0; i < 300; i++) {
            gait.tick(WALK * 1.5D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        }
        assertEquals(1.5D, gait.animationSpeed(), 1.0E-6D, "reaches the target");
    }

    @Test
    void unknownCycleLengthUsesTheFallback() {
        MovementGait gait = settled(WALK);
        gait.tick(WALK * 1.5D, ATTRIBUTE, AnimationChoice.LENGTH_UNKNOWN, AnimationChoice.LENGTH_NOT_DEFINED);
        assertEquals(1D + MovementGait.MAX_RATE_ERROR / MovementGait.FALLBACK_CYCLE_TICKS, gait.animationSpeed(), 1.0E-9D);
    }

    @Test
    void legRateFollowsAStartUpQuickly() {
        // Right after a gait starts, little time has passed in the cycle, so bigger changes are safe.
        MovementGait gait = settled(0D);
        for (int i = 0; i < 10; i++) {
            gait.tick(WALK, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        }
        assertTrue(gait.animationSpeed() >= 0.9D, "leg rate after 10 ticks: " + gait.animationSpeed());
    }

    @Test
    void legsNeverRunBackward() {
        // A sudden stop from a fast walk: the phase jump of each tick's change stays under MAX_RATE_ERROR of the rate.
        MovementGait gait = settled(WALK * 1.7D);
        double previous = gait.animationSpeed();
        for (int i = 0; i < 40 && gait.moving(); i++) {
            gait.tick(0D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
            if (!gait.moving()) {
                break; // the movement controller stops; the speed resets for the next start
            }
            double change = previous - gait.animationSpeed();
            // Phase moved by the change: change x (at most one cycle, WALK_CYCLE / previous ticks).
            assertTrue(change * WALK_CYCLE / previous <= MovementGait.MAX_RATE_ERROR * previous + 1.0E-9D,
                    "change " + change + " at speed " + previous);
            previous = gait.animationSpeed();
        }
    }

    @Test
    void aWalkFromAStandTakesItsAnimationSpeedAtOnce() {
        MovementGait gait = settled(0D);
        gait.tick(WALK * 4D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        assertSame(MovementGait.Gait.WALK, gait.gait());
        assertEquals(gait.groundSpeed() / WALK, gait.animationSpeed(), 1.0E-9D,
                "a new gait starts its animation from frame 0, so the speed jumps to the target");
    }

    @Test
    void aTeleportCountsAsOneFastStep() {
        // A /tp or a position correction moves the body far in one tick. It must not spike the smoothed speed.
        MovementGait teleported = settled(0D);
        teleported.tick(40D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
        assertEquals(MovementGait.SMOOTHING * MovementGait.MAX_GROUND_SPEED, teleported.groundSpeed(), 1.0E-9D,
                "the tick counts as a move of the cap");
        assertSame(MovementGait.Gait.WALK, teleported.gait(), "a standing animal walks for the hold, it does not run");
        int ticks = 0;
        while (teleported.moving() && ticks < 100) {
            teleported.tick(0D, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
            ticks++;
        }
        assertEquals(MovementGait.HOLD_TICKS, ticks, "it stands again when the hold ends");
    }

    @Test
    void capIsAboveEveryRealStep() {
        // The fastest setting the AI uses is a chase, modifier 1.5 (0.30 blocks per tick); a burst tick moves 4/3 of it.
        // The run fallback is the ground speed of modifier 1.2, and ground speed is quadratic in the modifier.
        double chase = RunGait.authoredSpeed(true, ATTRIBUTE, 0, 0) * (1.5D * 1.5D) / (1.2D * 1.2D);
        assertTrue(MovementGait.MAX_GROUND_SPEED > chase * 4D / 3D, "a chase step, also a burst tick, is not cut");
        assertFalse(RunGait.running(false, MovementGait.MAX_GROUND_SPEED * MovementGait.SMOOTHING, ATTRIBUTE),
                "one capped tick from a stand stays under the run threshold");
    }

    @Test
    void aRampThroughTheRunSwitchChangesTheGaitHoldsItAndKeepsTheStepRate() {
        // An urgent trip starts: over RAMP ticks the body speeds up from the walk to the run (the client sees the
        // acceleration), then it drops back to the walk at once. The run starts on the first tick over the threshold,
        // lasts the hold although the speed falls back, and the legs keep their step rate at both switches.
        int ramp = 10;
        MovementGait gait = settled(WALK);
        MovementGait.Gait previous = gait.gait();
        double previousRate = stepRate(gait, WALK_CYCLE, RUN_CYCLE);
        boolean overThreshold = false;
        int changes = 0;
        int runTicks = 0;
        for (int i = 0; i < ramp + 40; i++) {
            double speed = i < ramp ? WALK + (RUN - WALK) * (i + 1) / ramp : WALK;
            gait.tick(speed, ATTRIBUTE, WALK_CYCLE, RUN_CYCLE);
            double rate = stepRate(gait, WALK_CYCLE, RUN_CYCLE);
            if (gait.gait() != previous) {
                changes++;
                assertEquals(1D, rate / previousRate, 0.01D, "step rate kept at the switch on tick " + i
                        + " (" + previous + " " + previousRate + " to " + gait.gait() + " " + rate + ")");
                if (changes == 1) {
                    assertSame(MovementGait.Gait.RUN, gait.gait(), "the first change is to the run");
                    assertTrue(!overThreshold && RunGait.running(false, gait.groundSpeed(), ATTRIBUTE),
                            "the run starts on the first tick over the threshold");
                } else {
                    assertSame(MovementGait.Gait.WALK, gait.gait(), "then back to the walk");
                    assertEquals(MovementGait.HOLD_TICKS, runTicks, "the run lasts the hold, not less and not more");
                }
            } else if (gait.gait() == MovementGait.Gait.WALK && changes == 0) {
                overThreshold = RunGait.running(false, gait.groundSpeed(), ATTRIBUTE);
            }
            if (gait.gait() == MovementGait.Gait.RUN) {
                runTicks++;
            }
            previous = gait.gait();
            previousRate = rate;
        }
        assertEquals(2, changes, "walk, run, walk");
    }
}
