package net.dumbcode.projectnublar.worldgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class FossilHeightPlacement extends PlacementModifier {
    public static final Codec<FossilHeightPlacement> CODEC = RecordCodecBuilder.create((p_191679_) -> p_191679_.group(ExtraCodecs.NON_EMPTY_STRING.fieldOf("period").forGetter((p_191686_) -> p_191686_.period)).apply(p_191679_, FossilHeightPlacement::new));
    private HeightProvider height;
    private final String period;

    private FossilHeightPlacement(String pPeriod) {
    this.period = pPeriod;
    }

    public static FossilHeightPlacement of(String period) {
        return new FossilHeightPlacement(period);
    }

    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        int minHeight;
        int maxHeight;
        switch (period){
            case "carboniferous" -> {
                minHeight = FossilsConfig.INSTANCE.carboniferous.minY().get();
                maxHeight = FossilsConfig.INSTANCE.carboniferous.maxY().get();
            }
            case "jurassic" -> {
                minHeight = FossilsConfig.INSTANCE.jurassic.minY().get();
                maxHeight = FossilsConfig.INSTANCE.jurassic.maxY().get();
            }
            case "cretaceous" -> {
                minHeight = FossilsConfig.INSTANCE.cretaceous.minY().get();
                maxHeight = FossilsConfig.INSTANCE.cretaceous.maxY().get();
            }
            default -> {
                minHeight = -64;
                maxHeight = 64;
            }
        }
        this.height =  UniformHeight.of(VerticalAnchor.absolute(minHeight),VerticalAnchor.absolute(maxHeight));



        return Stream.of(pos.atY(this.height.sample(random, context)));
    }

    public PlacementModifierType<?> type() {
        return ModifierTypes.FOSSIL_HEIGHT.get();
    }
}