package net.dumbcode.projectnublar.entity.dinosaur.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StartleWatchTest {

    /** One check: the ids of the threats inside the radius now. */
    private static boolean check(StartleWatch watch, long now, boolean allowed, int... inside) {
        watch.beginCheck();
        for (int id : inside) {
            watch.inside(id);
        }
        return watch.endCheck(now, allowed);
    }

    @Test
    void firstCheckOnlyRecords() {
        StartleWatch watch = new StartleWatch();
        assertFalse(check(watch, 0L, true, 7), "no earlier check: a threat already inside is not an approach");
    }

    @Test
    void outsideThenInsideTriggers() {
        StartleWatch watch = new StartleWatch();
        check(watch, 0L, true);
        assertTrue(check(watch, 10L, true, 7));
    }

    @Test
    void standingInsideDoesNotTrigger() {
        StartleWatch watch = new StartleWatch();
        check(watch, 0L, true, 7);
        assertFalse(check(watch, 10L, true, 7));
        assertFalse(check(watch, 20L, true, 7));
    }

    @Test
    void cooldownBlocksASecondStartle() {
        StartleWatch watch = new StartleWatch();
        check(watch, 0L, true);
        assertTrue(check(watch, 10L, true, 7));
        check(watch, 20L, true);
        assertFalse(check(watch, 30L, true, 7), "inside the cooldown");
        check(watch, 10L + StartleWatch.COOLDOWN_TICKS - 1, true);
        assertFalse(check(watch, 10L + StartleWatch.COOLDOWN_TICKS - 1, true, 7), "one tick before the cooldown ends");
        check(watch, 10L + StartleWatch.COOLDOWN_TICKS, true);
        assertTrue(check(watch, 10L + StartleWatch.COOLDOWN_TICKS, true, 7), "after the cooldown");
    }

    @Test
    void anApproachWhileNotAllowedIsRecordedButDoesNotTrigger() {
        StartleWatch watch = new StartleWatch();
        check(watch, 0L, true);
        assertFalse(check(watch, 10L, false, 7), "resting, attacking or dead");
        assertFalse(check(watch, 20L, true, 7), "it now stands inside: not an approach");
    }

    @Test
    void aSecondThreatApproachingTriggersWhileTheFirstStandsInside() {
        StartleWatch watch = new StartleWatch();
        check(watch, 0L, true, 7);
        assertTrue(check(watch, 10L, true, 7, 9));
    }

    @Test
    void moreThreatsThanTrackedDoNotBreakTheWatch() {
        StartleWatch watch = new StartleWatch();
        int[] many = new int[StartleWatch.MAX_TRACKED + 5];
        for (int i = 0; i < many.length; i++) {
            many[i] = i;
        }
        check(watch, 0L, true);
        assertTrue(check(watch, 10L, true, many));
    }
}
