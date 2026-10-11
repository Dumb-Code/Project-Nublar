package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressMonitorTest {

    @Test
    void firstUpdateOnlySetsTheBaseline() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        assertFalse(monitor.update(0D, false, 7L));
        assertEquals(7L, monitor.lastProgressTick());
    }

    @Test
    void steadyProgressNeverStalls() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        for (long tick = 0L; tick <= 200L; tick++) {
            assertFalse(monitor.update(tick * 0.1D, false, tick), "stalled at tick " + tick);
        }
    }

    @Test
    void lessThanHalfABlockIn40TicksIsStuck() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        monitor.update(0D, false, 0L);
        for (long tick = 1L; tick < ProgressMonitor.WINDOW_TICKS; tick++) {
            assertFalse(monitor.update(0.4D, false, tick));
        }
        assertTrue(monitor.update(0.4D, false, ProgressMonitor.WINDOW_TICKS));
    }

    @Test
    void waitingIsNotStuck() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        monitor.update(0D, false, 0L);
        for (long tick = 1L; tick <= 100L; tick++) {
            assertFalse(monitor.update(0D, true, tick));
        }
    }

    @Test
    void stuckFiresOnceThenStartsANewWindow() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        monitor.update(0D, false, 0L);
        assertTrue(monitor.update(0D, false, 40L));
        assertFalse(monitor.update(0D, false, 41L));
        assertTrue(monitor.update(0D, false, 80L));
    }

    @Test
    void aMemberStuckForSeveralWindowsKeepsItsLastProgressTick() {
        ProgressMonitor monitor = new ProgressMonitor();
        monitor.reset(Double.NaN, 0L);
        monitor.update(0D, false, 5L);
        assertTrue(monitor.update(0D, false, 45L));
        assertTrue(monitor.update(0D, false, 85L));
        assertTrue(monitor.update(0D, false, 125L));
        assertEquals(5L, monitor.lastProgressTick());
    }
}
