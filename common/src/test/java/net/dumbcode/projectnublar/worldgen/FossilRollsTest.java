package net.dumbcode.projectnublar.worldgen;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The quality and piece rolls of fossil worldgen, with the config defaults of fossils-1.20.1 (40, 20, 15, 4). */
class FossilRollsTest {

    private static final List<String> QUALITIES = List.of("fragmented", "poor", "common", "pristine");
    private static final Map<String, Integer> WEIGHTS = Map.of("fragmented", 40, "poor", 20, "common", 15, "pristine", 4);

    private static String pick(int roll) {
        return FossilRolls.pickWeighted(roll, QUALITIES, WEIGHTS::get);
    }

    @Test
    void totalWeightAddsTheWeights() {
        assertEquals(79, FossilRolls.totalWeight(QUALITIES, WEIGHTS::get));
    }

    @Test
    void eachEntryWinsExactlyItsWeightInRolls() {
        assertEquals("fragmented", pick(0));
        assertEquals("fragmented", pick(39));
        assertEquals("poor", pick(40));
        assertEquals("poor", pick(59));
        assertEquals("common", pick(60));
        assertEquals("common", pick(74));
        assertEquals("pristine", pick(75));
        assertEquals("pristine", pick(78));
    }

    @Test
    void everyEntryCanWin() {
        Set<String> seen = new HashSet<>();
        for (int roll = 0; roll < 79; roll++) {
            seen.add(pick(roll));
        }
        assertEquals(Set.copyOf(QUALITIES), seen);
    }

    @Test
    void rollOutsideTheTotalIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> pick(79));
        assertThrows(IllegalArgumentException.class, () -> pick(-1));
    }

    @Test
    void zeroTotalOrEmptyListIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> FossilRolls.pickWeighted(0, QUALITIES, q -> 0));
        assertThrows(IllegalArgumentException.class, () -> FossilRolls.pickWeighted(0, List.<String>of(), q -> 1));
    }

    @Test
    void rarityPassesOnlyBelowTheChance() {
        assertTrue(FossilRolls.passesRarity(0.4D, 0.0D));
        assertTrue(FossilRolls.passesRarity(0.4D, 0.39D));
        assertFalse(FossilRolls.passesRarity(0.4D, 0.4D));
        assertFalse(FossilRolls.passesRarity(0.4D, 0.95D));
        assertFalse(FossilRolls.passesRarity(0.0D, 0.0D));
        assertTrue(FossilRolls.passesRarity(1.0D, 0.999D));
    }
}
