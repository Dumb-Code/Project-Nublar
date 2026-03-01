package net.dumbcode.projectnublar.worldgen.placement;

import com.mojang.serialization.Codec;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class FossilRarityFilter extends PlacementFilter {
    public static final Codec<FossilRarityFilter> CODEC;
    private double chance;
    private final String period;

    private FossilRarityFilter(String period) {
        this.period = period;
    }

    public static FossilRarityFilter useConfigBasedChanceAndHeight(String period) {
        return new FossilRarityFilter(period);
    }

    @Override
    protected boolean shouldPlace(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
        switch (period){
            case "carboniferous" ->  this.chance = FossilsConfig.INSTANCE.carboniferous.rarityModifier().get();
            case "jurassic" -> this.chance = FossilsConfig.INSTANCE.jurassic.rarityModifier().get();
            case "cretaceous" -> this.chance = FossilsConfig.INSTANCE.cretaceous.rarityModifier().get();
            default -> this.chance = 1.0D;
        }

        return randomSource.nextDouble() < 1.0D / this.chance;
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModifierTypes.FOSSIL_FILTER.get();
    }
    static {
        CODEC = ExtraCodecs.NON_EMPTY_STRING.fieldOf("period").xmap(FossilRarityFilter::new,(filter) -> filter.period).codec();
    }
}
