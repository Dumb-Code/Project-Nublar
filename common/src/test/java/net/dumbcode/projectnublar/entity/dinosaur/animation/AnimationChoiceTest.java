package net.dumbcode.projectnublar.entity.dinosaur.animation;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimationChoiceTest {

    /** The Gallimimus file (assets/projectnublar/animations/entity/gallimimus.animation.json). */
    private static final Set<String> GALLIMIMUS = Set.of("move.walk", "run", "swim", "attack1", "attack2", "calling",
            "misc.idle", "rest", "restidle", "lookright", "lookleft", "sentry", "sniffair", "flinch", "startled");

    @Test
    void availableIndicesKeepCandidateOrderAndSkipMissingNames() {
        int[] run = AnimationChoice.RUN.availableIndices(Set.of("Sprint")::contains);
        assertEquals(1, run.length);
        assertEquals("Sprint", AnimationChoice.RUN.name(run[0]));
        int[] both = AnimationChoice.RUN.availableIndices(Set.of("Sprint", "run")::contains);
        assertEquals(2, both.length);
        assertEquals("run", AnimationChoice.RUN.name(both[0]), "the first candidate the species has comes first");
    }

    @Test
    void triceratopsSprintWithATrailingSpaceIsACandidate() {
        // triceratops.animation.json names its run "Sprint " (trailing space).
        int[] run = AnimationChoice.RUN.availableIndices(Set.of("Sprint ")::contains);
        assertEquals(1, run.length);
    }

    @Test
    void roarAndCallUseTheGallimimusCallingAnimation() {
        int[] roar = AnimationChoice.ROAR.availableIndices(GALLIMIMUS::contains);
        assertEquals("calling", AnimationChoice.ROAR.name(roar[0]));
        int[] call = AnimationChoice.CALL.availableIndices(GALLIMIMUS::contains);
        assertEquals("calling", AnimationChoice.CALL.name(call[0]));
        int[] other = AnimationChoice.CALL.availableIndices(Set.of("call", "call2")::contains);
        assertEquals("call", AnimationChoice.CALL.name(other[0]));
    }

    @Test
    void noneAvailableFallsBackToIdleForEatDrinkAndGetUp() {
        for (AnimationChoice choice : new AnimationChoice[]{AnimationChoice.EAT, AnimationChoice.DRINK, AnimationChoice.GET_UP}) {
            assertEquals(0, choice.availableIndices(GALLIMIMUS::contains).length, choice + " is not in the Gallimimus file");
            assertSame(AnimationChoice.IDLE, AnimationChoice.resolve(choice, false), choice + " falls back to idle");
            assertSame(choice, AnimationChoice.resolve(choice, true), choice + " plays itself when present");
        }
    }

    @Test
    void deadWithoutADeadAnimationPlaysNothing() {
        // Controller ruling (P10 addendum): a corpse does not play idles; the controller stops (base pose).
        assertEquals(0, AnimationChoice.DEAD.availableIndices(GALLIMIMUS::contains).length);
        assertNull(AnimationChoice.resolve(AnimationChoice.DEAD, false));
        assertSame(AnimationChoice.DEAD, AnimationChoice.resolve(AnimationChoice.DEAD, true));
    }

    @Test
    void lengthIsTheFirstAvailableCandidateInTicksRoundedUp() {
        // Seconds from the files: gallimimus calling 2.625 s, dilophosaurus call 2.5 s.
        Map<String, Double> gallimimus = Map.of("calling", 2.625D, "sentry", 4.74D, "flinch", 1.2083D);
        assertEquals(53, AnimationChoice.CALL.lengthTicks(gallimimus));
        assertEquals(95, AnimationChoice.SENTRY.lengthTicks(gallimimus));
        assertEquals(25, AnimationChoice.FLINCH.lengthTicks(gallimimus));
        assertEquals(50, AnimationChoice.CALL.lengthTicks(Map.of("call", 2.5D, "call2", 1.75D)));
        assertEquals(AnimationChoice.LENGTH_NOT_DEFINED, AnimationChoice.STARTLED.lengthTicks(Map.of("calling", 2.625D)));
        assertEquals(AnimationChoice.LENGTH_UNKNOWN, AnimationChoice.CALL.lengthTicks(Map.of("calling", Double.NaN)),
                "present without animation_length: unknown");
    }

    @Test
    void flinchLastsTheConfiguredValueOrTheAnimationLength() {
        assertEquals(39, AnimationChoice.flinchTicks(39, 18), "a positive species value is kept");
        assertEquals(25, AnimationChoice.flinchTicks(0, 25), "0: the flinch animation's length");
        assertEquals(AnimationChoice.FLINCH_FALLBACK_TICKS, AnimationChoice.flinchTicks(0, AnimationChoice.LENGTH_UNKNOWN),
                "file not readable: the fallback");
        assertEquals(0, AnimationChoice.flinchTicks(0, AnimationChoice.LENGTH_NOT_DEFINED), "no flinch animation: as before");
    }

    @Test
    void runFallsBackToWalkAndOtherStatesStop() {
        assertSame(AnimationChoice.WALK, AnimationChoice.resolve(AnimationChoice.RUN, false));
        assertNull(AnimationChoice.resolve(AnimationChoice.WALK, false));
        assertNull(AnimationChoice.resolve(AnimationChoice.ATTACK, false));
        assertNull(AnimationChoice.resolve(AnimationChoice.SENTRY, false));
        assertNull(AnimationChoice.resolve(AnimationChoice.IDLE, false), "idle has no fallback of its own");
    }

    @Test
    void idlePickComesOnlyFromAvailableNames() {
        int[] idle = AnimationChoice.IDLE.availableIndices(GALLIMIMUS::contains);
        assertEquals(4, idle.length, "misc.idle, sniffair, lookleft, lookright");
        for (int i = 0; i < 1000; i++) {
            int pick = AnimationChoice.IDLE.pick(idle, i / 1000D);
            assertTrue(GALLIMIMUS.contains(AnimationChoice.IDLE.name(pick)), "picked " + AnimationChoice.IDLE.name(pick));
        }
        assertEquals(-1, AnimationChoice.IDLE.pick(new int[0], 0.5D), "nothing available: no pick");
    }

    @Test
    void miscIdleIsPickedMostSoVariantsStayOccasional() {
        // Lead, playtest 5: sniffair and look left/right played too often, so misc.idle is picked most.
        int[] idle = AnimationChoice.IDLE.availableIndices(GALLIMIMUS::contains);
        int[] counts = new int[AnimationChoice.IDLE.size()];
        int samples = 10_000;
        for (int i = 0; i < samples; i++) {
            counts[AnimationChoice.IDLE.pick(idle, (i + 0.5D) / samples)]++;
        }
        int main = counts[idle[0]];
        assertEquals("misc.idle", AnimationChoice.IDLE.name(idle[0]));
        assertTrue(AnimationChoice.MAIN_IDLE_WEIGHT > AnimationChoice.IDLE_VARIANT_WEIGHT);
        for (int k = 1; k < idle.length; k++) {
            assertTrue(main > counts[idle[k]], "misc.idle " + main + " vs " + AnimationChoice.IDLE.name(idle[k]) + " " + counts[idle[k]]);
        }
        assertTrue(main > samples / 2, "variants together are picked less than half the time: misc.idle " + main);
        int total = AnimationChoice.MAIN_IDLE_WEIGHT + AnimationChoice.IDLE_VARIANT_WEIGHT * (idle.length - 1);
        assertEquals(samples * AnimationChoice.MAIN_IDLE_WEIGHT / (double) total, main, 2D);
    }

    @Test
    void variantPickIsUniformOverAvailableAttacks() {
        int[] attacks = AnimationChoice.ATTACK.availableIndices(GALLIMIMUS::contains);
        assertEquals(2, attacks.length);
        assertEquals("attack1", AnimationChoice.ATTACK.name(AnimationChoice.ATTACK.pick(attacks, 0.25D)));
        assertEquals("attack2", AnimationChoice.ATTACK.name(AnimationChoice.ATTACK.pick(attacks, 0.75D)));
        assertEquals("attack2", AnimationChoice.ATTACK.name(AnimationChoice.ATTACK.pick(attacks, 0.999999D)));
    }

    @Test
    void idleChangesAfterFourToEightSeconds() {
        assertEquals(80L, AnimationChoice.idleTicks(0D));
        assertEquals(160L, AnimationChoice.idleTicks(0.999999D));
    }

    @Test
    void idleRepicksWhenTheCycleFinishesOrTheTimeIsUp() {
        long picked = 100L;
        long change = 200L;
        assertFalse(AnimationChoice.shouldRepickIdle(false, 150L, picked, change), "still looping, time not up");
        assertTrue(AnimationChoice.shouldRepickIdle(false, 200L, picked, change), "time is up");
        assertTrue(AnimationChoice.shouldRepickIdle(true, 150L, picked, change), "the cycle finished");
        assertFalse(AnimationChoice.shouldRepickIdle(true, picked + AnimationChoice.IDLE_MIN_TICKS - 1, picked, change),
                "a finished flag left over from the previous animation does not repick at once");
    }

    @Test
    void candidateLengthIsThatNameOnly() {
        Map<String, Double> gallimimus = Map.of("misc.idle", Double.NaN, "sniffair", 7.88D, "lookleft", 2.375D);
        int[] idle = AnimationChoice.IDLE.availableIndices(Map.of("misc.idle", 0, "sniffair", 0, "lookleft", 0)::containsKey);
        assertEquals(AnimationChoice.LENGTH_UNKNOWN, AnimationChoice.IDLE.lengthTicks(gallimimus, idle[0]), "misc.idle: no length");
        assertEquals(158, AnimationChoice.IDLE.lengthTicks(gallimimus, idle[1]), "sniffair 7.88 s");
        assertEquals(48, AnimationChoice.IDLE.lengthTicks(gallimimus, idle[2]), "lookleft 2.375 s");
        assertEquals(AnimationChoice.LENGTH_NOT_DEFINED, AnimationChoice.IDLE.lengthTicks(Map.of(), idle[1]), "not in the file");
    }

    @Test
    void anIdlePickPlaysAtLeastOneFullCycleBeforeTheTimerChangesIt() {
        int transition = 5;
        assertEquals(1000L + 158 + transition, AnimationChoice.idleChangeAt(1000L, 80L, 158, transition),
                "sniffair (7.88 s) is no longer cut by an 80-tick timer");
        assertEquals(1120L, AnimationChoice.idleChangeAt(1000L, 120L, 48, transition), "a short look keeps the timer");
        assertEquals(1080L, AnimationChoice.idleChangeAt(1000L, 80L, AnimationChoice.LENGTH_UNKNOWN, transition),
                "no length (misc.idle): the timer");
        assertEquals(1080L, AnimationChoice.idleChangeAt(1000L, 80L, AnimationChoice.LENGTH_NOT_DEFINED, transition));
    }

    @Test
    void anInterruptedIdlePickStartsItsTimerAgainWhenItPlaysAgain() {
        // sniffair (158 ticks) is picked at tick 0 with an 80-tick timer. A walk stops it at tick 100; at tick 120 the
        // animal stands again and the same pick plays again from its first frame.
        int transition = 5;
        long changeAt = AnimationChoice.idleChangeAt(0L, 80L, 158, transition);
        assertTrue(AnimationChoice.idleTimerStarts(false, false), "the kept pick plays again from frame 0: its timer starts again");
        long restartedAt = 120L;
        long restartedChangeAt = AnimationChoice.idleChangeAt(restartedAt, 80L, 158, transition);
        assertTrue(AnimationChoice.shouldRepickIdle(false, changeAt, 0L, changeAt),
                "with the old timer the pick is cut 43 ticks after the restart");
        assertFalse(AnimationChoice.shouldRepickIdle(false, restartedAt + 158 + transition - 1, restartedAt, restartedChangeAt),
                "with the new timer one full cycle plays from the restart");
        assertTrue(AnimationChoice.shouldRepickIdle(false, restartedAt + 158 + transition, restartedAt, restartedChangeAt));
        assertFalse(AnimationChoice.idleTimerStarts(false, true), "a pick that plays on keeps its timer");
        assertTrue(AnimationChoice.idleTimerStarts(true, false), "a new pick starts its timer");
        assertTrue(AnimationChoice.idleTimerStarts(true, true), "a new pick of the same idle after a finished cycle too");
    }

    @Test
    void flinchPlaysOnceAndHolds() {
        // The flinch state outlasts the animation by a tick or two; a loop restarted the flinch just before it cleared.
        assertSame(AnimationChoice.Loop.HOLD, AnimationChoice.FLINCH.loop());
    }
    @Test
    void theIdlePlaysOnlyTheMainIdleWhileTheHerdTravels() {
        // P13b item 1 (ruling 3b): no idle variants while the herd is on a journey, pauses included.
        assertTrue(AnimationChoice.mainIdleOnly(AnimationChoice.IDLE, true));
        assertFalse(AnimationChoice.mainIdleOnly(AnimationChoice.IDLE, false), "grazing: the variants play as before");
        assertTrue(AnimationChoice.mainIdleOnly(AnimationChoice.EAT, true), "an eat that falls back to the idle, on a journey");
        assertFalse(AnimationChoice.mainIdleOnly(AnimationChoice.DRINK, false), "a drink fallback while grazing keeps the variants");
    }

    @Test
    void aGetUpWithNoGetUpAnimationPlaysTheMainIdle() {
        // P13b item 2 (ruling 3c): the Gallimimus has no getup; it plays the main idle, not a look left/right.
        int[] getUp = AnimationChoice.GET_UP.availableIndices(GALLIMIMUS::contains);
        assertEquals(0, getUp.length);
        assertSame(AnimationChoice.IDLE, AnimationChoice.resolve(AnimationChoice.GET_UP, false));
        assertTrue(AnimationChoice.mainIdleOnly(AnimationChoice.GET_UP, false));
        assertTrue(AnimationChoice.mainIdleOnly(AnimationChoice.GET_UP, true));
        // The main idle is the first idle candidate, and the Gallimimus has it.
        int[] idles = AnimationChoice.IDLE.availableIndices(GALLIMIMUS::contains);
        assertEquals("misc.idle", AnimationChoice.IDLE.name(idles[0]));
    }
}
