package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.minecraft.util.Mth;

import java.util.UUID;

/**
 * Dominance decides who leads a group and who wins a rank contest (plan B18, decision of 2026-10-06).
 * <p>
 * The value comes from three inputs, each normalised to 0..1:
 * <ul>
 *   <li>age: growth from hatchling (0) to adult (1),</li>
 *   <li>size: the size gene, -100% .. +100%,</li>
 *   <li>aggression: the aggression gene, -100% .. +100%.</li>
 * </ul>
 * The result is in 0..1. Age carries the most weight: an animal at half growth or less, even with both
 * genes at +100%, at most ties an unedited adult. A nearly grown animal with strong genes can lead.
 * The weights are constants here; B00 moves them to the species config.
 */
public final class Dominance {

    static final float AGE_WEIGHT = 0.5F;
    static final float SIZE_WEIGHT = 0.3F;
    static final float AGGRESSION_WEIGHT = 0.2F;

    /** Age in ticks of a fresh hatchling (vanilla {@code AgeableMob.BABY_START_AGE}). It must agree with B16. */
    private static final float HATCHLING_AGE = -24000F;

    private Dominance() {
    }

    /** The dominance of a living animal. Server side only: the client does not know the real age. */
    public static float of(Dinosaur dinosaur) {
        float growth = 1F - Mth.clamp(dinosaur.getAge() / HATCHLING_AGE, 0F, 1F);
        return compute(growth,
                dinosaur.getDinoData().getGeneValue(GeneInit.SIZE.get()),
                dinosaur.getDinoData().getGeneValue(GeneInit.AGGRESSION.get()));
    }

    /**
     * The pure rule, kept apart from the entity so a unit test can check it.
     *
     * @param growth         0 (hatchling) to 1 (adult); values outside are clamped
     * @param sizeGene       size gene in percent, -100 to 100; values outside are clamped
     * @param aggressionGene aggression gene in percent, -100 to 100; values outside are clamped
     */
    static float compute(float growth, double sizeGene, double aggressionGene) {
        return AGE_WEIGHT * Mth.clamp(growth, 0F, 1F)
                + SIZE_WEIGHT * percentToUnit(sizeGene)
                + AGGRESSION_WEIGHT * percentToUnit(aggressionGene);
    }

    /**
     * A total order on animals: higher dominance first, and on a tie the lower UUID.
     * Unedited adults all tie, so without the UUID the winner would depend on the order of joining.
     */
    static boolean outranks(float dominance, UUID id, float otherDominance, UUID otherId) {
        return dominance > otherDominance || (dominance == otherDominance && id.compareTo(otherId) < 0);
    }

    private static float percentToUnit(double percent) {
        return (float) Mth.clamp((percent + 100D) / 200D, 0D, 1D);
    }
}
