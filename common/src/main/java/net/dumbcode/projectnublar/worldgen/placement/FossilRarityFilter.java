package net.dumbcode.projectnublar.worldgen.placement;

import com.mojang.serialization.Codec;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.registry.PlacementModifierInit;
import net.dumbcode.projectnublar.worldgen.FossilRolls;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Lets a fossil vein through with the rarity chance of its period. The chance is read from the
 * config at each placement, so an edited config applies without new worldgen data.
 */
public class FossilRarityFilter extends PlacementFilter {
    public static final Codec<FossilRarityFilter> CODEC =
            ExtraCodecs.NON_EMPTY_STRING.fieldOf("period").xmap(FossilRarityFilter::new, filter -> filter.period).codec();
    private final String period;

    private FossilRarityFilter(String period) {
        this.period = period;
    }

    public static FossilRarityFilter of(String period) {
        return new FossilRarityFilter(period);
    }

    @Override
    protected boolean shouldPlace(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
        return FossilRolls.passesRarity(FossilsConfig.rarityModifier(period), randomSource.nextDouble());
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierInit.FOSSIL_FILTER.get();
    }
}
