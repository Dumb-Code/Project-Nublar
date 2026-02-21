package net.dumbcode.projectnublar.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.fossil.Dinosaurs;
import net.dumbcode.projectnublar.api.fossil.FossilSets;
import net.dumbcode.projectnublar.api.fossil.Fossils;
import net.dumbcode.projectnublar.data.FossilConfigReloadListener;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.EntityInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.List;

public class FossilConfiguration implements FeatureConfiguration {
    public static final Codec<FossilConfiguration> CODEC = RecordCodecBuilder.create((p_67849_) -> p_67849_.group(Codec.list(FossilConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter((p_161027_) -> p_161027_.targetStates),Codec.list(TargetEntityType.CODEC).fieldOf("entityType").forGetter((fossilConfiguration)-> fossilConfiguration.targetEntityTypes), Codec.intRange(0, 64).fieldOf("size").forGetter((p_161025_) -> p_161025_.size), Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter((p_161020_) -> p_161020_.discardChanceOnAirExposure)).apply(p_67849_, FossilConfiguration::new));
    public final List<FossilConfiguration.TargetBlockState> targetStates;
    public final List<FossilConfiguration.TargetEntityType> targetEntityTypes;
    public final int size;
    public final float discardChanceOnAirExposure;

    public final Fossils fossils;
    public final FossilSets fossilSets;

    public FossilConfiguration(List<FossilConfiguration.TargetBlockState> targetStates,List<FossilConfiguration.TargetEntityType> entityType, int size, float discardChanceOnAirExposure) {
        this.size = size;
        this.targetStates = targetStates;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.targetEntityTypes = entityType;
        this.fossils = FossilConfigReloadListener.getFossils(targetEntityTypes.get(0).entityType);
        this.fossilSets = FossilConfigReloadListener.getFossilSets("fossil_sets");

    }

    public FossilConfiguration(List<FossilConfiguration.TargetBlockState> targetStates,List<FossilConfiguration.TargetEntityType> entityType, int size) {
        this(targetStates,entityType, size, 0.0F);
    }

    public FossilConfiguration(RuleTest target, List<FossilConfiguration.TargetEntityType> entityType, BlockState state, int size, float discardChanceOnAirExposure) {
        this(ImmutableList.of(new FossilConfiguration.TargetBlockState(target, state)),entityType, size, discardChanceOnAirExposure);
    }

    public FossilConfiguration(RuleTest target,List<FossilConfiguration.TargetEntityType> entityType, BlockState state, int size) {
        this(ImmutableList.of(new FossilConfiguration.TargetBlockState(target, state)),entityType, size, 0.0F);
    }

    public static TargetBlockState target(RuleTest target, BlockState state) {
        return new TargetBlockState(target, state);
    }

    public static TargetEntityType targetEntityType(EntityType<?> target) {
        return new TargetEntityType(target);
    }

    public static class TargetBlockState {
        public static final Codec<FossilConfiguration.TargetBlockState> CODEC = RecordCodecBuilder.create((p_161039_) -> p_161039_.group(RuleTest.CODEC.fieldOf("target").forGetter((p_161043_) -> p_161043_.target), BlockState.CODEC.fieldOf("state").forGetter((p_161041_) -> p_161041_.state)).apply(p_161039_, FossilConfiguration.TargetBlockState::new));
        public final RuleTest target;
        public final BlockState state;

        TargetBlockState(RuleTest target, BlockState state) {
            this.target = target;
            this.state = state;
        }
    }
    public static class TargetEntityType{
        public static final Codec<TargetEntityType> CODEC =
                RecordCodecBuilder.create(instance ->
                        instance.group(
                                BuiltInRegistries.ENTITY_TYPE.byNameCodec()
                                        .fieldOf("entity")
                                        .forGetter(t -> t.entityType)
                        ).apply(instance, TargetEntityType::new)
                );

        public final EntityType<?> entityType;

        public TargetEntityType(EntityType<?> entityType) {
            this.entityType = entityType;
        }

    }

}
