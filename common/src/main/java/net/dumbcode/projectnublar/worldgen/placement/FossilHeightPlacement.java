package net.dumbcode.projectnublar.worldgen.placement;

import com.mojang.serialization.Codec;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.registry.PlacementModifierInit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

/**
 * Moves a fossil vein to a uniform random y inside the depth band of its period. The band is read
 * from the config at each placement, so an edited config applies without new worldgen data.
 */
public class FossilHeightPlacement extends PlacementModifier {
    public static final Codec<FossilHeightPlacement> CODEC =
            ExtraCodecs.NON_EMPTY_STRING.fieldOf("period").xmap(FossilHeightPlacement::new, placement -> placement.period).codec();
    private final String period;

    private FossilHeightPlacement(String period) {
        this.period = period;
    }

    public static FossilHeightPlacement of(String period) {
        return new FossilHeightPlacement(period);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        UniformHeight height = UniformHeight.of(
                VerticalAnchor.absolute(FossilsConfig.minY(period)), VerticalAnchor.absolute(FossilsConfig.maxY(period)));
        return Stream.of(pos.atY(height.sample(random, context)));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierInit.FOSSIL_HEIGHT.get();
    }
}
