package net.dumbcode.projectnublar.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The hunger lines and the days-without-water count. The lead's lines (2026-10-27): an animal wants food below 75 %
 * hunger and stops eating above 90 %, on a 100-point bar.
 */
class DinoNeedsUtilsTest {

    private static final float MAX_HUNGER = 100F;

    @Test
    void anAnimalWantsFoodBelowTheLowLine() {
        assertTrue(DinoNeedsUtils.wantsFood(false, 74.9F, MAX_HUNGER), "74.9 % sets the flag");
        assertFalse(DinoNeedsUtils.wantsFood(false, 75F, MAX_HUNGER), "75 % is not below the line");
    }

    @Test
    void anEaterGoesOnEatingUpToTheHighLine() {
        // Between the lines the flag keeps its state: an eater that started below 75 % eats the food in reach.
        assertTrue(DinoNeedsUtils.wantsFood(true, 80F, MAX_HUNGER));
        assertTrue(DinoNeedsUtils.wantsFood(true, 90F, MAX_HUNGER), "90 % keeps the flag");
        assertFalse(DinoNeedsUtils.wantsFood(true, 90.1F, MAX_HUNGER), "90.1 % clears it");
    }

    @Test
    void aFedAnimalDoesNotWantFoodAgainBeforeTheLowLine() {
        // Without the two lines, the first hunger tick after a meal (90.1 -> 87.2) would send it back to food at once.
        assertFalse(DinoNeedsUtils.wantsFood(false, 87.2F, MAX_HUNGER));
        assertFalse(DinoNeedsUtils.wantsFood(false, 97F, MAX_HUNGER), "a full animal never wants food");
    }

    @Test
    void aDrinkStartsTheDaysWithoutWaterAgain() {
        assertEquals(0, DinoNeedsUtils.daysWithoutWater(2, true));
        assertEquals(0, DinoNeedsUtils.daysWithoutWater(null, true));
        assertEquals(1, DinoNeedsUtils.daysWithoutWater(0, false), "the rollover after a drink counts one day");
        assertEquals(1, DinoNeedsUtils.daysWithoutWater(null, false), "the first rollover counts one day");
        assertEquals(3, DinoNeedsUtils.daysWithoutWater(2, false));
    }

    @Test
    void anAnimalThatDrinksEveryDayLivesPastItsThirdDay() {
        int maxDaysWithoutWater = 3; // every species' max_days_without_water
        Integer days = null;
        for (int day = 1; day <= 10; day++) {
            days = DinoNeedsUtils.daysWithoutWater(days, false);
            assertTrue(days < maxDaysWithoutWater, "day " + day + ": below the limit at the rollover");
            days = DinoNeedsUtils.daysWithoutWater(days, true);
        }
    }
}
