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

    /** Steady ground speed of a speed setting: the law in {@code GroundSpeed}, 2.16 x (modifier x attribute)^2. */
    private static double at(double modifier) {
        double speed = modifier * ATTRIBUTE;
        return 0.98D / (1D - 0.6D * 0.91D) * speed * speed;
    }

    private static MovementGait settled(double groundSpeed) {
        return settled(groundSpeed, WALK, RUN);
    }

    private static MovementGait settled(double groundSpeed, double walkSpeed, double runSpeed) {
        MovementGait gait = new MovementGait();
        for (int i = 0; i < 200; i++) {
            gait.tick(groundSpeed, ATTRIBUTE, walkSpeed, runSpeed);
        }
        return gait;
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
        gait.tick(WALK, ATTRIBUTE, WALK, RUN);
        assertSame(MovementGait.Gait.WALK, gait.gait());
    }

    @Test
    void oneStillTickDoesNotChangeTheGait() {
        // A late position packet or one guard tick gives one tick with no movement.
        MovementGait runner = settled(RUN);
        runner.tick(0D, ATTRIBUTE, WALK, RUN);
        assertSame(MovementGait.Gait.RUN, runner.gait(), "a runner keeps running");
        MovementGait walker = settled(WALK);
        walker.tick(0D, ATTRIBUTE, WALK, RUN);
        assertSame(MovementGait.Gait.WALK, walker.gait(), "a walker keeps walking");
    }

    @Test
    void oneBurstTickDoesNotMakeAWalkerRun() {
        // Two packets close together move the body 4/3 as fast for a tick.
        double inBand = 0.13D;
        MovementGait walker = settled(inBand);
        assertSame(MovementGait.Gait.WALK, walker.gait());
        walker.tick(inBand * 4D / 3D, ATTRIBUTE, WALK, RUN);
        assertSame(MovementGait.Gait.WALK, walker.gait());
    }

    @Test
    void aGaitIsHeldForTheMinimumTimeAfterASwitch() {
        MovementGait gait = new MovementGait();
        gait.tick(WALK, ATTRIBUTE, WALK, RUN);
        assertSame(MovementGait.Gait.WALK, gait.gait(), "the first switch is free");
        for (int i = 1; i < MovementGait.HOLD_TICKS; i++) {
            gait.tick(0D, ATTRIBUTE, WALK, RUN);
            assertSame(MovementGait.Gait.WALK, gait.gait(), "held at tick " + i);
        }
        gait.tick(0D, ATTRIBUTE, WALK, RUN);
        assertSame(MovementGait.Gait.IDLE, gait.gait(), "the hold is over");
    }

    @Test
    void stopsSoonAfterTheBodyStops() {
        MovementGait gait = settled(RUN);
        int ticks = 0;
        while (gait.moving() && ticks < 100) {
            gait.tick(0D, ATTRIBUTE, WALK, RUN);
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
        assertEquals(MovementGait.MIN_ANIMATION_SPEED, settled(0.027D, RAPTOR_WALK, RUN).animationSpeed(), 1.0E-6D,
                "clamped low");
        // An adult Dilophosaurus run (6.0 blocks x scale 0.5 per 20 ticks) at the per-tick cap would play at 2.8.
        double dilophosaurusRun = MovementGait.authoredSpeed(6D, 0.5D, 20);
        assertEquals(MovementGait.MAX_ANIMATION_SPEED, settled(dilophosaurusRun * 3D, WALK, dilophosaurusRun).animationSpeed(),
                1.0E-6D, "clamped high");
    }

    @Test
    void theRaptorWalkPlaysSlowerThanTheOldFloorOnItsUsualWalks() {
        // The Velociraptor's walk clip covers 3.9 blocks per cycle at its adult size, so its graze (0.6) and its
        // journey (0.8) play it at about 0.25 and 0.44. A floor of 0.5 made its feet faster than the ground on both.
        double graze = at(0.6D) / RAPTOR_WALK;
        double journey = at(0.8D) / RAPTOR_WALK;
        assertTrue(graze > MovementGait.MIN_ANIMATION_SPEED && graze < 0.5D, "graze rate " + graze);
        assertTrue(journey > MovementGait.MIN_ANIMATION_SPEED && journey < 0.5D, "journey rate " + journey);
        assertEquals(graze, settled(at(0.6D), RAPTOR_WALK, RUN).animationSpeed(), 1.0E-6D, "not clamped");
    }

    @Test
    void theLegRateFollowsTheGroundSpeedOnEveryTick() {
        // GeckoLib 5 adds each frame's time x the speed to the clip, so a change of the speed moves no leg phase and
        // needs no limit: the rate is the smoothed ground speed over the authored speed on the very next tick.
        MovementGait gait = settled(WALK);
        gait.tick(WALK * 1.5D, ATTRIBUTE, WALK, RUN);
        assertEquals(gait.groundSpeed() / WALK, gait.animationSpeed(), 1.0E-9D, "one tick after the change");
        assertEquals(1D + MovementGait.SMOOTHING * 0.5D, gait.animationSpeed(), 1.0E-9D, "the smoothing alone lags");
    }

    @Test
    void aWalkFromAStandTakesItsAnimationSpeedAtOnce() {
        MovementGait gait = settled(0D);
        gait.tick(WALK * 4D, ATTRIBUTE, WALK, RUN);
        assertSame(MovementGait.Gait.WALK, gait.gait());
        assertEquals(gait.groundSpeed() / WALK, gait.animationSpeed(), 1.0E-9D);
    }

    @Test
    void aClipWithNoKnownSpeedPlaysAsMade() {
        // No stride measured, or no clip length in the file: the clip plays at rate 1, as before P12a.
        assertEquals(MovementGait.Strides.NOT_MEASURED, MovementGait.strides("not_a_species"));
        assertEquals(0D, MovementGait.authoredSpeed(0D, 1D, WALK_CYCLE), "no stride");
        assertEquals(0D, MovementGait.authoredSpeed(GALLIMIMUS.walk(), 1D, AnimationChoice.LENGTH_UNKNOWN), "no length");
        assertEquals(1D, settled(WALK * 1.5D, 0D, 0D).animationSpeed(), 1.0E-9D);
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
        MovementGait gait = settled(ground, RAPTOR_WALK, MovementGait.authoredSpeed(10.4D, 0.6D, cycle));
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
        teleported.tick(40D, ATTRIBUTE, WALK, RUN);
        assertEquals(MovementGait.SMOOTHING * MovementGait.MAX_GROUND_SPEED, teleported.groundSpeed(), 1.0E-9D,
                "the tick counts as a move of the cap");
        assertSame(MovementGait.Gait.WALK, teleported.gait(), "a standing animal walks for the hold, it does not run");
        int ticks = 0;
        while (teleported.moving() && ticks < 100) {
            teleported.tick(0D, ATTRIBUTE, WALK, RUN);
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
    void aRampThroughTheRunSwitchChangesTheGaitHoldsItAndKeepsPace() {
        // An urgent trip starts: over RAMP ticks the body speeds up from the walk to the trip's speed (modifier 1.2; the
        // client sees the acceleration), then it drops back to the walk at once. The run starts on the first tick over the threshold
        // and lasts the hold although the speed falls back; on every tick the legs play at the smoothed ground speed
        // over the authored speed of the gait they are in.
        int ramp = 10;
        double trip = at(1.2D);
        MovementGait gait = settled(WALK);
        MovementGait.Gait previous = gait.gait();
        boolean overThreshold = false;
        int changes = 0;
        int runTicks = 0;
        for (int i = 0; i < ramp + 40; i++) {
            double speed = i < ramp ? WALK + (trip - WALK) * (i + 1) / ramp : WALK;
            gait.tick(speed, ATTRIBUTE, WALK, RUN);
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
            double authored = gait.gait() == MovementGait.Gait.RUN ? RUN : WALK;
            double wanted = Math.max(MovementGait.MIN_ANIMATION_SPEED,
                    Math.min(MovementGait.MAX_ANIMATION_SPEED, gait.groundSpeed() / authored));
            assertEquals(wanted, gait.animationSpeed(), 1.0E-9D, "pace on tick " + i);
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
