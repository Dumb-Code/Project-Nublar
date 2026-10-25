package net.dumbcode.projectnublar.entity.dinosaur.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OneShotStateTest {

    @Test
    void countdownEndsAfterTheGivenTicks() {
        OneShotState state = new OneShotState();
        assertTrue(state.start(OneShotAction.SENTRY, 3));
        assertSame(OneShotAction.SENTRY, state.current());
        assertFalse(state.tick());
        assertFalse(state.tick());
        assertTrue(state.tick(), "ends on the third tick");
        assertSame(OneShotAction.NONE, state.current());
        assertFalse(state.tick(), "nothing left to end");
    }

    @Test
    void zeroTicksOrNoneDoNotStart() {
        OneShotState state = new OneShotState();
        assertFalse(state.start(OneShotAction.CALLING, 0));
        assertFalse(state.start(OneShotAction.NONE, 10));
        assertSame(OneShotAction.NONE, state.current());
    }

    @Test
    void onlyAHigherPriorityActionReplacesAPlayingOne() {
        OneShotState state = new OneShotState();
        assertTrue(state.start(OneShotAction.SENTRY, 50));
        assertTrue(state.start(OneShotAction.CALLING, 40), "the journey call replaces the watch");
        assertFalse(state.start(OneShotAction.SENTRY, 50), "the watch does not replace the call");
        assertFalse(state.start(OneShotAction.CALLING, 40), "the same action does not restart");
        assertTrue(state.start(OneShotAction.STARTLED, 30), "startled replaces everything");
        assertFalse(state.start(OneShotAction.CALLING, 40));
        assertSame(OneShotAction.STARTLED, state.current());
    }

    @Test
    void clearEndsAtOnce() {
        OneShotState state = new OneShotState();
        state.start(OneShotAction.SENTRY, 50);
        state.clear();
        assertSame(OneShotAction.NONE, state.current());
        assertTrue(state.start(OneShotAction.SENTRY, 50));
    }

    @Test
    void lengthComesFromTheFileOrTheFallback() {
        assertEquals(53, OneShotAction.CALLING.lengthTicks(53));
        assertEquals(0, OneShotAction.CALLING.lengthTicks(AnimationChoice.LENGTH_NOT_DEFINED), "species without a call: no call");
        assertEquals(0, OneShotAction.CALLING.lengthTicks(AnimationChoice.LENGTH_UNKNOWN), "unknown file: set off at once");
        assertEquals(OneShotAction.SENTRY_FALLBACK_TICKS, OneShotAction.SENTRY.lengthTicks(AnimationChoice.LENGTH_UNKNOWN));
        assertEquals(OneShotAction.STARTLED_FALLBACK_TICKS, OneShotAction.STARTLED.lengthTicks(AnimationChoice.LENGTH_UNKNOWN));
        assertEquals(0, OneShotAction.SENTRY.lengthTicks(AnimationChoice.LENGTH_NOT_DEFINED));
    }

    @Test
    void journeyWaitIsTheCallCappedAtSixtyTicks() {
        assertEquals(53, OneShotAction.journeyWaitTicks(53));
        assertEquals(60, OneShotAction.journeyWaitTicks(81));
        assertEquals(0, OneShotAction.journeyWaitTicks(0));
    }

    @Test
    void sentryIsCheckedEvery200To400TicksWithA30PercentChance() {
        assertEquals(1200L, OneShotAction.nextSentryCheck(1000L, 0D));
        assertEquals(1400L, OneShotAction.nextSentryCheck(1000L, 0.999999D));
        assertTrue(OneShotAction.sentryRollPasses(0.29D));
        assertFalse(OneShotAction.sentryRollPasses(0.3D));
    }

    @Test
    void idsRoundTrip() {
        for (OneShotAction action : OneShotAction.values()) {
            assertSame(action, OneShotAction.byId(action.id()));
        }
        assertSame(OneShotAction.NONE, OneShotAction.byId((byte) 99), "an unknown id is no action");
    }
}
