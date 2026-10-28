package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdNeedsTest {

    @Test
    void criticalBelowTheThreshold() {
        assertTrue(HerdNeeds.isCritical(39F, 100F, 0.4D));
    }

    @Test
    void notCriticalAtOrAboveTheThreshold() {
        assertFalse(HerdNeeds.isCritical(40F, 100F, 0.4D));
        assertFalse(HerdNeeds.isCritical(70F, 100F, 0.4D));
    }

    @Test
    void staminaIsCriticalBelowHalfTheRageThreshold() {
        double threshold = 0.4D * HerdNeeds.CRITICAL_STAMINA_FACTOR;
        assertTrue(HerdNeeds.isCritical(19F, 100F, threshold));
        assertFalse(HerdNeeds.isCritical(20F, 100F, threshold));
        assertFalse(HerdNeeds.isCritical(39F, 100F, threshold), "tired but not critical: keeps walking on a roaming journey");
    }

    @Test
    void aMemberThatStepsAsideDoesNotStartARest() {
        // P12b fix round 2: a make-way side step in progress (its get-up wait included) blocks the start of a rest,
        // so a still-tired member steps out of the leader's line before it lies down again.
        assertFalse(HerdNeeds.mayStartRest(true, true));
        assertTrue(HerdNeeds.mayStartRest(false, true), "after the side step it may rest where it stands");
        assertFalse(HerdNeeds.mayStartRest(false, false), "the needs rule still applies");
        assertFalse(HerdNeeds.mayStartRest(true, false));
    }

    @Test
    void anAnimalIsDownWhileItSitsRestsOrGetsUp() {
        // P13a item 1: the shared "down" rule of HerdLeading (pause) and FollowFormation (leader stands).
        assertFalse(HerdNeeds.isDown(false, false, false, false, false));
        assertTrue(HerdNeeds.isDown(true, false, false, false, false), "sitting down");
        assertTrue(HerdNeeds.isDown(false, true, false, false, false), "resting");
        assertTrue(HerdNeeds.isDown(false, false, true, false, false), "getting up");
        assertTrue(HerdNeeds.isDown(false, false, false, true, false), "the resting memory (before the sit state shows)");
        assertTrue(HerdNeeds.isDown(false, false, false, false, true), "the getting-up memory (the tick between rest and get-up)");
    }

    @Test
    void anAnimalLiesDownWhenItsSleepWindowStarts() {
        // Task 42: the window itself starts a rest, tired or not, for a solo animal, a leader and a member alike.
        // SleepWindowsTest pins each species' window; here only the other conditions count.
        assertTrue(HerdNeeds.sleepsNow(true, false, true, false, false), "in its window, on dry ground, fed and watered");
        assertFalse(HerdNeeds.sleepsNow(false, false, true, false, false), "out of its window it lies down only when tired");
    }

    @Test
    void theWindowStartsNoSleepWhileItGetsUpIsInWaterStarvesOrItsHerdTravels() {
        assertFalse(HerdNeeds.sleepsNow(true, true, true, false, false), "it gets up from a rest: no sit-down mid get-up");
        assertFalse(HerdNeeds.sleepsNow(true, false, false, false, false), "in water, or not on the ground");
        assertFalse(HerdNeeds.sleepsNow(true, false, true, true, false), "starving or dehydrated: it stays awake to eat or drink");
        assertFalse(HerdNeeds.sleepsNow(true, false, true, false, true),
                "its herd travels: a roaming leader lies down through the journey pause, an urgent trip ends first");
    }
}
