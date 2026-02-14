package net.dumbcode.projectnublar.api.fossil;

import net.minecraft.resources.ResourceLocation;

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
