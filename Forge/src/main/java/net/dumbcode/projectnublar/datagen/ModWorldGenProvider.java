package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.init.FeatureInit;
import net.dumbcode.projectnublar.worldgen.feature.FossilConfiguration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGenProvider::biomeModifiers)
            .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::configuredFeature)
            .add(Registries.PLACED_FEATURE, ModWorldGenProvider::placedFeatures);
    private static final ResourceKey<BiomeModifier> FOSSIL = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(Constants.MODID, "fossil"));

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Constants.MODID));
    }

    public static void biomeModifiers(BootstapContext<BiomeModifier> context) {
        context.register(FOSSIL,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(
                        context.lookup(Registries.PLACED_FEATURE).getOrThrow(Constants.FOSSIL_PLACED)
                ), GenerationStep.Decoration.UNDERGROUND_ORES)
        );
    }

    public static void configuredFeature(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(Constants.FOSSIL, new ConfiguredFeature<>(
                        FeatureInit.FOSSIL_FEATURE.get(),
                        new FossilConfiguration(
                                15,
                                0
                        )
                )
        );
        context.register(Constants.AMBER, new ConfiguredFeature<>(
                        FeatureInit.AMBER_FEATURE.get(),
                        new FossilConfiguration(
                                15,
                                1
                        )
                )
        );

    }

    public static void placedFeatures(BootstapContext<PlacedFeature> context) {
        context.register(Constants.FOSSIL_PLACED, new PlacedFeature(context.lookup(Registries.CONFIGURED_FEATURE).get(Constants.FOSSIL).get(),
                        List.of(
                                CountPlacement.of(UniformInt.of(4,10)),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64),VerticalAnchor.absolute(256)),
                                InSquarePlacement.spread(),
                                BiomeFilter.biome()
                        )
                )
        );
        context.register(Constants.AMBER_PLACED, new PlacedFeature(context.lookup(Registries.CONFIGURED_FEATURE).get(Constants.AMBER).get(),
                        List.of(
                                CountPlacement.of(ConstantInt.of(1)),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64),VerticalAnchor.absolute(256)),
                                InSquarePlacement.spread(),
                                BiomeFilter.biome()
                        )
                )
        );
    }


}
