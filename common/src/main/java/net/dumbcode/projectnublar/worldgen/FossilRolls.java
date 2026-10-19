package net.dumbcode.projectnublar.worldgen;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * The two dice rolls of fossil worldgen: a weighted pick (quality, piece) and a rarity test.
 *
 * <p>The class is pure on purpose. It takes the random number as an argument and has no Minecraft import, so unit
 * tests can pin the exact boundaries. {@code FossilFeature} cannot load outside the game, so its rolls live here.
 */
public final class FossilRolls {

    private FossilRolls() {
    }

    /**
     * Adds the weights of all candidates.
     *
     * @param candidates the entries to weigh
     * @param weight     the weight of one entry
     * @return the sum of all weights; a caller rolls {@code random.nextInt(total)} for {@link #pickWeighted}
     */
    public static <T> int totalWeight(List<T> candidates, ToIntFunction<? super T> weight) {
        int total = 0;
        for (T candidate : candidates) {
            total += weight.applyAsInt(candidate);
        }
        return total;
    }

    /**
     * Picks one candidate by weight. The candidates keep their order: entry i wins for every roll in
     * {@code [sum(w0..w(i-1)), sum(w0..wi))}, so each entry wins exactly its weight in rolls.
     *
     * @param roll       a whole number in {@code [0, total)}
     * @param candidates the entries to pick from
     * @param weight     the weight of one entry
     * @return the winning entry
     * @throws IllegalArgumentException when the list is empty, the total weight is 0 or less, or the roll is outside
     *                                  {@code [0, total)}
     */
    public static <T> T pickWeighted(int roll, List<T> candidates, ToIntFunction<? super T> weight) {
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidates to pick from");
        }
        int total = totalWeight(candidates, weight);
        if (total <= 0) {
            throw new IllegalArgumentException("Total weight must be above 0, got " + total);
        }
        if (roll < 0 || roll >= total) {
            throw new IllegalArgumentException("Roll " + roll + " is outside [0, " + total + ")");
        }
        int upTo = 0;
        for (T candidate : candidates) {
            upTo += weight.applyAsInt(candidate);
            if (roll < upTo) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unreachable: roll " + roll + " is below the total " + total);
    }

    /**
     * Tests a rarity chance against a roll.
     *
     * @param chance the chance in {@code [0, 1]}; 0 never passes and 1 always passes for a roll below 1
     * @param roll   a roll in {@code [0, 1)}, such as {@code random.nextDouble()}
     * @return true when {@code roll < chance}
     */
    public static boolean passesRarity(double chance, double roll) {
        return roll < chance;
    }
}
