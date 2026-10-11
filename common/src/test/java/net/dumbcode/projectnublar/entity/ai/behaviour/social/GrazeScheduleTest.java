package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class GrazeScheduleTest {

    /** Returns the given values in order, then repeats the last one. */
    private static DoubleSupplier fixed(double... values) {
        int[] next = new int[1];
        return () -> values[Math.min(next[0]++, values.length - 1)];
    }

    @Test
    void enteringStaggersTheFirstPick() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.enter(100L, 0.5D);
        assertSame(GrazeSchedule.Action.HOLD, schedule.decide(129L, false, false, fixed(0D)));
        assertSame(GrazeSchedule.Action.PICK_ANY, schedule.decide(130L, false, false, fixed(0D)));
    }

    @Test
    void aSpotIsKeptThirtyToNinetySeconds() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.spotTaken(0L, fixed(0D));
        assertEquals(600L, schedule.nextDecision());
        schedule.spotTaken(0L, fixed(0.999D));
        assertEquals(1798L, schedule.nextDecision());
    }

    @Test
    void afterTheStayHalfTheMembersMoveNearby() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.spotTaken(0L, fixed(0D));
        assertSame(GrazeSchedule.Action.HOLD, schedule.decide(599L, true, true, fixed(0.2D)));
        assertSame(GrazeSchedule.Action.PICK_NEAR, schedule.decide(600L, true, true, fixed(0.2D)));
    }

    @Test
    void theOtherHalfStayAnotherPeriod() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.spotTaken(0L, fixed(0D));
        assertSame(GrazeSchedule.Action.HOLD, schedule.decide(600L, true, true, fixed(0.7D, 0D)));
        assertEquals(1200L, schedule.nextDecision());
    }

    @Test
    void anInvalidSpotIsReplacedAtOnce() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.spotTaken(0L, fixed(0D));
        assertSame(GrazeSchedule.Action.PICK_ANY, schedule.decide(10L, true, false, fixed(0.9D)));
    }

    @Test
    void noSpotTriesAgainAfter100Ticks() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.noSpot(50L);
        assertSame(GrazeSchedule.Action.HOLD, schedule.decide(149L, false, false, fixed(0D)));
        assertSame(GrazeSchedule.Action.PICK_ANY, schedule.decide(150L, false, false, fixed(0D)));
    }

    @Test
    void repickNowPicksOnTheNextDecision() {
        GrazeSchedule schedule = new GrazeSchedule();
        schedule.spotTaken(0L, fixed(0D));
        schedule.repickNow(10L);
        assertSame(GrazeSchedule.Action.PICK_ANY, schedule.decide(10L, false, false, fixed(0.9D)));
    }
}
