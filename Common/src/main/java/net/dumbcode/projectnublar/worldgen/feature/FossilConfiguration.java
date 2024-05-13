package net.dumbcode.projectnublar.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FossilConfiguration implements FeatureConfiguration {
    public static final Codec<FossilConfiguration> CODEC = RecordCodecBuilder.create((p_67849_) -> {
        return p_67849_.group(
                Codec.intRange(0, 64).fieldOf("size").forGetter((p_161025_) -> {
                    return p_161025_.size;
                }), Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter((p_161020_) -> {
                    return p_161020_.discardChanceOnAirExposure;
                })).apply(p_67849_, FossilConfiguration::new);
    });
    public final int size;
    public final float discardChanceOnAirExposure;

    public FossilConfiguration(int size, float discardChanceOnAirExposure) {
        this.size = size;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
    }

    public FossilConfiguration(int pSize) {
        this(pSize, 0.0F);
    }

}
