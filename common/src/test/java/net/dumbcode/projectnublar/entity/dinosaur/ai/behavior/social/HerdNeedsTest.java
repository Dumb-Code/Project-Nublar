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
}
