package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the pure dominance rule. Needs JUnit 5 in common/build.gradle; the project has no tests yet. */
class DominanceTest {

    private static final float EPSILON = 1.0E-6F;
    private static final UUID LOW_ID = new UUID(0L, 1L);
    private static final UUID HIGH_ID = new UUID(0L, 2L);

    @Test
    void neutralAdultScoresAgeWeightPlusHalfOfEachGene() {
        float expected = Dominance.AGE_WEIGHT + Dominance.SIZE_WEIGHT / 2F + Dominance.AGGRESSION_WEIGHT / 2F;
        assertEquals(expected, Dominance.compute(1F, 0D, 0D), EPSILON);
    }

    @Test
    void weightsSumToOneSoTheRangeIsZeroToOne() {
        assertEquals(0F, Dominance.compute(0F, -100D, -100D), EPSILON);
        assertEquals(1F, Dominance.compute(1F, 100D, 100D), EPSILON);
    }

    @Test
    void outOfRangeInputsAreClamped() {
        assertEquals(Dominance.compute(1F, 100D, 100D), Dominance.compute(2F, 500D, 500D), EPSILON);
        assertEquals(Dominance.compute(0F, -100D, -100D), Dominance.compute(-1F, -500D, -500D), EPSILON);
    }

    @Test
    void halfGrownAnimalAtMostTiesAnUneditedAdult() {
        // Largest and most aggressive possible at half growth: 0.25 + 0.3 + 0.2 = 0.75 = 0.5 + 0.15 + 0.1.
        assertTrue(Dominance.compute(0.5F, 100D, 100D) <= Dominance.compute(1F, 0D, 0D) + EPSILON);
    }

    @Test
    void largerAndMoreAggressiveWinsBetweenAdults() {
        assertTrue(Dominance.compute(1F, 20D, 0D) > Dominance.compute(1F, 0D, 0D));
        assertTrue(Dominance.compute(1F, 0D, 20D) > Dominance.compute(1F, 0D, 0D));
    }

    @Test
    void higherDominanceOutranksWhateverTheIds() {
        assertTrue(Dominance.outranks(0.8F, HIGH_ID, 0.7F, LOW_ID));
        assertFalse(Dominance.outranks(0.7F, LOW_ID, 0.8F, HIGH_ID));
    }

    @Test
    void tieGoesToTheLowerIdInBothDirections() {
        assertTrue(Dominance.outranks(0.75F, LOW_ID, 0.75F, HIGH_ID));
        assertFalse(Dominance.outranks(0.75F, HIGH_ID, 0.75F, LOW_ID));
    }

    @Test
    void noAnimalOutranksItself() {
        assertFalse(Dominance.outranks(0.75F, LOW_ID, 0.75F, LOW_ID));
    }
}
