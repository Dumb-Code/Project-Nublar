package net.dumbcode.projectnublar.api.fossil;

/** Per-config weights and DNA yields for each fossil quality tier (datapack model). */
public record FossilQuality(
        String configId,
        int fragmentedWeight,
        double fragmentedYield,
        int poorWeight,
        double poorYield,
        int commonWeight,
        double commonYield,
        int pristineWeight,
        double pristineYield
) {
}
