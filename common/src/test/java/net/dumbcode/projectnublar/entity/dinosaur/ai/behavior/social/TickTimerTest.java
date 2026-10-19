package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TickTimerTest {

    private static final double EPSILON = 1.0E-9D;
    private static final long MS = 1_000_000L;

    @Test
    void emptyTimerReportsZero() {
        TickTimer timer = new TickTimer();
        assertEquals(0D, timer.averageMillis(), EPSILON);
        assertEquals(0D, timer.worstMillis(), EPSILON);
    }

    @Test
    void timesInOneTickAddUpAndCountWhenTheTickEnds() {
        TickTimer timer = new TickTimer();
        timer.add(1, MS);
        timer.add(1, 2 * MS);
        assertEquals(0D, timer.averageMillis(), EPSILON, "tick 1 is still open");
        timer.add(2, 0);
        assertEquals(3D, timer.averageMillis(), EPSILON);
        assertEquals(3D, timer.worstMillis(), EPSILON);
    }

    @Test
    void averageAndWorstCoverFinishedTicks() {
        TickTimer timer = new TickTimer();
        timer.add(1, MS);
        timer.add(2, 2 * MS);
        timer.add(3, 3 * MS);
        timer.add(4, 0);
        assertEquals(2D, timer.averageMillis(), EPSILON);
        assertEquals(3D, timer.worstMillis(), EPSILON);
    }

    @Test
    void windowKeepsOnlyTheLatestTicks() {
        TickTimer timer = new TickTimer();
        timer.add(0, 100 * MS);
        for (long tick = 1; tick <= TickTimer.WINDOW + 1; tick++) {
            timer.add(tick, MS);
        }
        assertEquals(1D, timer.worstMillis(), EPSILON, "the 100 ms tick has left the window");
        assertEquals(1D, timer.averageMillis(), EPSILON);
    }
}
