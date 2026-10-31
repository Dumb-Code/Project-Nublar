package net.dumbcode.projectnublar.entity.dinosaur.animation;

import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.RunGait;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovementGaitTest {

    private static final double ATTRIBUTE = Dinosaur.BASE_MOVEMENT_SPEED;
    /** Gallimimus clip lengths in ticks, rounded up as the client reads them: move.walk 1.875 s, run 0.7917 s. */
    private static final int WALK_CYCLE = 38;
    private static final int RUN_CYCLE = 16;
    private static final MovementGait.Strides GALLIMIMUS = MovementGait.strides("gallimimus");
    /** Ground speed the adult Gallimimus walk is made for (render scale 1): 2.7 blocks per 38 ticks, 0.071. */
    private static final double WALK = MovementGait.authoredSpeed(GALLIMIMUS.walk(), 1D, WALK_CYCLE);
    /** Ground speed the adult Gallimimus run is made for: 6.0 blocks per 16 ticks, 0.375. */
    private static final double RUN = MovementGait.authoredSpeed(GALLIMIMUS.run(), 1D, RUN_CYCLE);
    /** Ground speed the adult Velociraptor walk is made for (render scale 0.6): 6.5 x 0.6 blocks per 20 ticks, 0.195. */
    private static final double RAPTOR_WALK = MovementGait.authoredSpeed(MovementGait.strides("velociraptor").walk(), 0.6D, 20);
    private static final MovementGait.LegClip WALK_CLIP = new MovementGait.LegClip(WALK, WALK_CYCLE);
    private static final MovementGait.LegClip RUN_CLIP = new MovementGait.LegClip(RUN, RUN_CYCLE);
    private static final MovementGait.LegClip RAPTOR_WALK_CLIP = new MovementGait.LegClip(RAPTOR_WALK, 20);
    /** Adult Brachiosaurus clips (render scale 2.5): move.walk 4.04 s (81 ticks), run 2.79 s (56 ticks). */
    private static final MovementGait.Strides BRACHIOSAURUS = MovementGait.strides("brachiosaurus");
    private static final MovementGait.LegClip BRACHIOSAURUS_WALK =
            MovementGait.LegClip.of(BRACHIOSAURUS.walk(), 2.5D, 81);
    private static final MovementGait.LegClip BRACHIOSAURUS_RUN =
            MovementGait.LegClip.of(BRACHIOSAURUS.run(), 2.5D, 56);

    /** Steady ground speed of a speed setting: the law in {@code GroundSpeed}, 2.16 x (modifier x attribute)^2. */
    private static double at(double modifier) {
        double speed = modifier * ATTRIBUTE;
        return 0.98D / (1D - 0.6D * 0.91D) * speed * speed;
    }

    private static MovementGait settled(double groundSpeed) {
        return settled(groundSpeed, WALK_CLIP, RUN_CLIP);
    }

    private static MovementGait settled(double groundSpeed, MovementGait.LegClip walk, MovementGait.LegClip run) {
        MovementGait gait = new MovementGait();
        for (int i = 0; i < 200; i++) {
            gait.tick(groundSpeed, ATTRIBUTE, walk, run);
        }
        return gait;
    }

    /** The rate the gait moves toward in its current gait: the smoothed speed over the clip's speed, clamped. */
    private static double target(MovementGait gait, MovementGait.LegClip walk, MovementGait.LegClip run) {
        double authored = gait.gait() == MovementGait.Gait.RUN ? run.authoredSpeed() : walk.authoredSpeed();
        return Math.max(MovementGait.MIN_ANIMATION_SPEED,
                Math.min(MovementGait.MAX_ANIMATION_SPEED, gait.groundSpeed() / authored));
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
        gait.tick(WALK, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
        assertSame(MovementGait.Gait.WALK, gait.gait());
    }

    @Test
    void oneStillTickDoesNotChangeTheGait() {
        // A late position packet or one guard tick gives one tick with no movement.
        MovementGait runner = settled(RUN);
        runner.tick(0D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
        assertSame(MovementGait.Gait.RUN, runner.gait(), "a runner keeps running");
        MovementGait walker = settled(WALK);
        walker.tick(0D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
        assertSame(MovementGait.Gait.WALK, walker.gait(), "a walker keeps walking");
    }

    @Test
    void oneBurstTickDoesNotMakeAWalkerRun() {
        // Two packets close together move the body 4/3 as fast for a tick.
        double inBand = 0.13D;
        MovementGait walker = settled(inBand);
        assertSame(MovementGait.Gait.WALK, walker.gait());
        walker.tick(inBand * 4D / 3D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
        assertSame(MovementGait.Gait.WALK, walker.gait());
    }

    @Test
    void aGaitIsHeldForTheMinimumTimeAfterASwitch() {
        MovementGait gait = new MovementGait();
        gait.tick(WALK, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
        assertSame(MovementGait.Gait.WALK, gait.gait(), "the first switch is free");
        for (int i = 1; i < MovementGait.HOLD_TICKS; i++) {
            gait.tick(0D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
            assertSame(MovementGait.Gait.WALK, gait.gait(), "held at tick " + i);
        }
        gait.tick(0D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
        assertSame(MovementGait.Gait.IDLE, gait.gait(), "the hold is over");
    }

    @Test
    void stopsSoonAfterTheBodyStops() {
        MovementGait gait = settled(RUN);
        int ticks = 0;
        while (gait.moving() && ticks < 100) {
            gait.tick(0D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
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
        // A Velociraptor at the steering floor would play its long-stride walk at 0.14.
        assertEquals(MovementGait.MIN_ANIMATION_SPEED, settled(0.027D, RAPTOR_WALK_CLIP, RUN_CLIP).animationSpeed(),
                1.0E-6D, "clamped low");
        // An adult Dilophosaurus run (6.0 blocks x scale 0.5 per 20 ticks) at the per-tick cap would play at 2.8.
        MovementGait.LegClip dilophosaurusRun = new MovementGait.LegClip(MovementGait.authoredSpeed(6D, 0.5D, 20), 20);
        assertEquals(MovementGait.MAX_ANIMATION_SPEED,
                settled(dilophosaurusRun.authoredSpeed() * 3D, WALK_CLIP, dilophosaurusRun).animationSpeed(), 1.0E-6D,
                "clamped high");
    }

    @Test
    void theRaptorWalkPlaysSlowerThanTheOldFloorOnItsUsualWalks() {
        // The Velociraptor's walk clip covers 3.9 blocks per cycle at its adult size, so its graze (0.6) and its
        // journey (0.8) play it at about 0.25 and 0.44. A floor of 0.5 made its feet faster than the ground on both.
        double graze = at(0.6D) / RAPTOR_WALK;
        double journey = at(0.8D) / RAPTOR_WALK;
        assertTrue(graze > MovementGait.MIN_ANIMATION_SPEED && graze < 0.5D, "graze rate " + graze);
        assertTrue(journey > MovementGait.MIN_ANIMATION_SPEED && journey < 0.5D, "journey rate " + journey);
        assertEquals(graze, settled(at(0.6D), RAPTOR_WALK_CLIP, RUN_CLIP).animationSpeed(), 1.0E-6D, "not clamped");
    }

    @Test
    void theLegRateMovesToItsTargetByAtMostTheStepPerTick() {
        // Task 52: a walk that speeds up by half moves its target rate by 0.15 on the first tick (the smoothing). The
        // rate follows by MAX_RATE_STEP per tick, so the step rate eases up, and it reaches the target in the end.
        MovementGait gait = settled(WALK);
        gait.tick(WALK * 1.5D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
        assertEquals(1D + MovementGait.SMOOTHING * 0.5D, gait.groundSpeed() / WALK, 1.0E-9D, "the target is 1.15");
        assertEquals(1D + MovementGait.MAX_RATE_STEP, gait.animationSpeed(), 1.0E-9D, "the rate moved by one step");
        for (int i = 0; i < 100; i++) {
            double before = gait.animationSpeed();
            gait.tick(WALK * 1.5D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
            assertTrue(gait.animationSpeed() - before <= MovementGait.MAX_RATE_STEP + 1.0E-9D, "limited on tick " + i);
        }
        assertEquals(1.5D, gait.animationSpeed(), 1.0E-6D, "at the target in the end");
        // Slowing down is limited the same way.
        gait.tick(WALK, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
        assertEquals(1.5D - MovementGait.MAX_RATE_STEP, gait.animationSpeed(), 1.0E-9D, "the rate fell by one step");
    }

    @Test
    void theStepRateDoesNotDropAtTheSwitchToTheRun() {
        // Task 52, the Brachiosaurus of the round 6 playtest: at the run switch its walk plays near the cap (2.0, 81
        // ticks a cycle) and its run's target is about 1.2 (56 ticks a cycle), 14 % fewer steps per tick. On the switch
        // tick the run keeps the walk's steps per tick: rate x 56 / 81.
        MovementGait gait = settled(0.14D, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN);
        assertSame(MovementGait.Gait.WALK, gait.gait());
        double walkRate = gait.animationSpeed();
        int ticks = 0;
        while (gait.gait() == MovementGait.Gait.WALK && ticks < 20) {
            walkRate = gait.animationSpeed();
            gait.tick(0.16D, ATTRIBUTE, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN);
            ticks++;
        }
        assertSame(MovementGait.Gait.RUN, gait.gait(), "it runs");
        assertEquals(walkRate / 81D, gait.animationSpeed() / 56D, 1.0E-9D, "the same steps per tick");
        assertTrue(Math.abs(gait.animationSpeed() - target(gait, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN)) > 0.1D,
                "not the run's target, which would change the step rate");
    }

    @Test
    void theStepRateDoesNotRiseAtTheSwitchBackToTheWalk() {
        // The other way: a run that slows below the exit speed. The Brachiosaurus walk's target is about 22 % more
        // steps per tick than the run; on the switch tick the walk keeps the run's: rate x 81 / 56.
        MovementGait gait = settled(0.2D, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN);
        assertSame(MovementGait.Gait.RUN, gait.gait());
        double runRate = gait.animationSpeed();
        int ticks = 0;
        while (gait.gait() == MovementGait.Gait.RUN && ticks < 40) {
            runRate = gait.animationSpeed();
            gait.tick(0.1D, ATTRIBUTE, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN);
            ticks++;
        }
        assertSame(MovementGait.Gait.WALK, gait.gait(), "it walks");
        assertEquals(runRate / 56D, gait.animationSpeed() / 81D, 1.0E-9D, "the same steps per tick");
        assertTrue(Math.abs(gait.animationSpeed() - target(gait, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN)) > 0.1D,
                "not the walk's target, which would change the step rate");
    }

    @Test
    void afterTheSwitchTheRateEasesToTheNewTarget() {
        // The carried rate is not the run's target; from the next tick on it moves there by the step per tick.
        MovementGait gait = settled(0.14D, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN);
        gait.tick(0.25D, ATTRIBUTE, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN);
        assertSame(MovementGait.Gait.RUN, gait.gait(), "a fast tick switches at once");
        for (int i = 0; i < 100; i++) {
            double before = gait.animationSpeed();
            gait.tick(0.25D, ATTRIBUTE, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN);
            double wanted = target(gait, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN);
            double step = Math.max(-MovementGait.MAX_RATE_STEP, Math.min(MovementGait.MAX_RATE_STEP, wanted - before));
            assertEquals(before + step, gait.animationSpeed(), 1.0E-9D, "tick " + i);
        }
        assertSame(MovementGait.Gait.RUN, gait.gait());
        assertEquals(target(gait, BRACHIOSAURUS_WALK, BRACHIOSAURUS_RUN), gait.animationSpeed(), 1.0E-9D,
                "at the run's target in the end");
    }

    @Test
    void aWalkFromAStandTakesItsAnimationSpeedAtOnce() {
        MovementGait gait = settled(0D);
        gait.tick(WALK * 4D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
        assertSame(MovementGait.Gait.WALK, gait.gait());
        assertEquals(gait.groundSpeed() / WALK, gait.animationSpeed(), 1.0E-9D);
    }

    @Test
    void aClipWithNoKnownSpeedPlaysAsMade() {
        // No stride measured, or no clip length in the file: the clip plays at rate 1, as before P12a.
        assertEquals(MovementGait.Strides.NOT_MEASURED, MovementGait.strides("not_a_species"));
        assertEquals(0D, MovementGait.authoredSpeed(0D, 1D, WALK_CYCLE), "no stride");
        assertEquals(0D, MovementGait.authoredSpeed(GALLIMIMUS.walk(), 1D, AnimationChoice.LENGTH_UNKNOWN), "no length");
        MovementGait.LegClip unknown = MovementGait.LegClip.NOT_MEASURED;
        assertEquals(1D, settled(WALK * 1.5D, unknown, unknown).animationSpeed(), 1.0E-9D);
    }

    @Test
    void theAuthoredSpeedFollowsTheAnimalsSize() {
        // Stride x render scale: a growth stage 1 Velociraptor (scale 0.6 x 0.25) covers a quarter of the adult's ground
        // per cycle, so at one ground speed its legs cycle four times as fast.
        double adult = MovementGait.authoredSpeed(6.5D, 0.6D, 20);
        double hatchling = MovementGait.authoredSpeed(6.5D, 0.15D, 20);
        assertEquals(0.195D, adult, 1.0E-9D, "6.5 blocks x 0.6 per 20 ticks");
        assertEquals(adult / 4D, hatchling, 1.0E-9D);
    }

    @Test
    void thePlantedFeetKeepPaceWithTheGround() {
        // At rate a the clip covers stride x scale blocks in cycle / a ticks; a = ground / authored makes that the
        // ground speed, for the walk and for the run.
        int cycle = 20;
        double ground = at(1.5D);
        MovementGait gait = settled(ground, RAPTOR_WALK_CLIP,
                new MovementGait.LegClip(MovementGait.authoredSpeed(10.4D, 0.6D, cycle), cycle));
        assertSame(MovementGait.Gait.RUN, gait.gait(), "a chase runs");
        double feet = 10.4D * 0.6D / (cycle / gait.animationSpeed());
        assertEquals(ground, feet, 1.0E-9D);
    }

    @Test
    void everySpeciesWithAnAnimationFileHasMeasuredStrides() throws IOException {
        // A new species needs its walk and run strides measured, or its legs play at rate 1 at every speed.
        List<String> missing = new ArrayList<>();
        for (String species : speciesWithAnimationFiles()) {
            MovementGait.Strides strides = MovementGait.strides(species);
            if (!(strides.walk() > 0D && strides.run() > 0D)) {
                missing.add(species);
            }
        }
        assertEquals(List.of(), missing);
    }

    @Test
    void aTeleportCountsAsOneFastStep() {
        // A /tp or a position correction moves the body far in one tick. It must not spike the smoothed speed.
        MovementGait teleported = settled(0D);
        teleported.tick(40D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
        assertEquals(MovementGait.SMOOTHING * MovementGait.MAX_GROUND_SPEED, teleported.groundSpeed(), 1.0E-9D,
                "the tick counts as a move of the cap");
        assertSame(MovementGait.Gait.WALK, teleported.gait(), "a standing animal walks for the hold, it does not run");
        int ticks = 0;
        while (teleported.moving() && ticks < 100) {
            teleported.tick(0D, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
            ticks++;
        }
        assertEquals(MovementGait.HOLD_TICKS, ticks, "it stands again when the hold ends");
    }

    @Test
    void capIsAboveEveryRealStep() {
        // The fastest setting the AI uses is a chase, modifier 1.5 (0.30 blocks per tick); a burst tick moves 4/3 of it.
        assertTrue(MovementGait.MAX_GROUND_SPEED > at(1.5D) * 4D / 3D, "a chase step, also a burst tick, is not cut");
        assertFalse(RunGait.running(false, MovementGait.MAX_GROUND_SPEED * MovementGait.SMOOTHING, ATTRIBUTE),
                "one capped tick from a stand stays under the run threshold");
    }

    @Test
    void aRampThroughTheRunSwitchChangesTheGaitHoldsItAndCarriesTheStepRate() {
        // An urgent trip starts: over RAMP ticks the body speeds up from the walk to the trip's speed (modifier 1.2; the
        // client sees the acceleration), then it drops back to the walk at once. The run starts on the first tick over
        // the threshold and lasts the hold although the speed falls back. On each switch tick the legs keep the steps
        // per tick of the gait before; on every other tick the rate moves toward the smoothed ground speed over the
        // authored speed of the gait they are in, by at most the step.
        int ramp = 10;
        double trip = at(1.2D);
        MovementGait gait = settled(WALK);
        MovementGait.Gait previous = gait.gait();
        boolean overThreshold = false;
        int changes = 0;
        int runTicks = 0;
        for (int i = 0; i < ramp + 40; i++) {
            double speed = i < ramp ? WALK + (trip - WALK) * (i + 1) / ramp : WALK;
            double before = gait.animationSpeed();
            gait.tick(speed, ATTRIBUTE, WALK_CLIP, RUN_CLIP);
            if (gait.gait() != previous) {
                changes++;
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
            double wanted;
            if (gait.gait() == previous) {
                double change = target(gait, WALK_CLIP, RUN_CLIP) - before;
                wanted = before + Math.max(-MovementGait.MAX_RATE_STEP, Math.min(MovementGait.MAX_RATE_STEP, change));
            } else {
                wanted = gait.gait() == MovementGait.Gait.RUN
                        ? before * RUN_CYCLE / WALK_CYCLE
                        : before * WALK_CYCLE / RUN_CYCLE;
            }
            assertEquals(wanted, gait.animationSpeed(), 1.0E-9D, "rate on tick " + i);
            if (gait.gait() == MovementGait.Gait.RUN) {
                runTicks++;
            }
            previous = gait.gait();
        }
        assertEquals(2, changes, "walk, run, walk");
    }

    /** The registry ids of the species with an animation file: the file names of the mod's entity animations. */
    private static List<String> speciesWithAnimationFiles() throws IOException {
        String folder = "/assets/projectnublar/geckolib/animations/entity";
        URL url = MovementGaitTest.class.getResource(folder);
        assertNotNull(url, folder + " is not on the test class path");
        Path root;
        try {
            root = Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        String suffix = ".animation.json";
        try (Stream<Path> files = Files.list(root)) {
            List<String> species = files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(suffix))
                    .map(name -> name.substring(0, name.length() - suffix.length()))
                    .sorted()
                    .toList();
            assertFalse(species.isEmpty(), "no animation file in " + folder);
            return species;
        }
    }
}
