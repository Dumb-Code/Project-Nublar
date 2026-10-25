package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilBase;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.registry.FeatureInit;
import net.dumbcode.projectnublar.registry.TagInit;
import net.dumbcode.projectnublar.worldgen.FossilConfiguration;
import net.dumbcode.projectnublar.worldgen.placement.FossilHeightPlacement;
import net.dumbcode.projectnublar.worldgen.placement.FossilRarityFilter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the fossil vein worldgen data: per host stone, one configured feature, one placed
 * feature and one biome modifier that adds the vein to every overworld biome.
 *
 * <p>Only the Cretaceous veins exist for now. The rarity and the depth band of a period are read
 * from the fossil config at run time, so they are not in the generated data.
 */
public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGenProvider::biomeModifiers)
            .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::configuredFeature)
            .add(Registries.PLACED_FEATURE, ModWorldGenProvider::placedFeatures);

    private static final String CRETACEOUS = "cretaceous";

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Constants.MODID));
    }

    public static void biomeModifiers(BootstapContext<BiomeModifier> context) {
        for (FossilBase base : FossilBase.values()) {
            createFossilDepositBiomeModifier(base, CRETACEOUS, context);
        }
    }

    public static void configuredFeature(BootstapContext<ConfiguredFeature<?, ?>> context) {
        createFossilDepositConfiguredFeature(FossilBase.STONE, CRETACEOUS, new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), context);
        createFossilDepositConfiguredFeature(FossilBase.SANDSTONE, CRETACEOUS, new TagMatchTest(TagInit.SANDSTONE_ORE_REPLACEABLES), context);
        createFossilDepositConfiguredFeature(FossilBase.DEEPSLATE, CRETACEOUS, new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), context);
    }

    public static void placedFeatures(BootstapContext<PlacedFeature> context) {
        for (FossilBase base : FossilBase.values()) {
            createFossilDepositPlacedFeature(base, CRETACEOUS, context);
        }
    }

    /** A vein of size 15 whose targets are every fossil block of this stone from every collection of this period. */
    public static void createFossilDepositConfiguredFeature(FossilBase base, String period, RuleTest ruleTest, BootstapContext<ConfiguredFeature<?, ?>> context) {
        List<FossilConfiguration.TargetBlockState> targets = FossilCollection.COLLECTIONS.values().stream()
                .filter(collection -> collection.period().equals(period))
                .flatMap(collection -> collection.fossilBlocks().get(base).values().stream())
                .map(block -> FossilConfiguration.target(ruleTest, block.get().defaultBlockState()))
                .toList();
        context.register(configuredKey(base, period),
                new ConfiguredFeature<>(FeatureInit.FOSSIL_FEATURE.get(), new FossilConfiguration(targets, 15)));
    }

    public static void createFossilDepositPlacedFeature(FossilBase base, String period, BootstapContext<PlacedFeature> context) {
        context.register(placedKey(base, period), new PlacedFeature(
                context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(configuredKey(base, period)),
                List.of(FossilRarityFilter.of(period), InSquarePlacement.spread(), FossilHeightPlacement.of(period), BiomeFilter.biome())));
    }

    public static void createFossilDepositBiomeModifier(FossilBase base, String period, BootstapContext<BiomeModifier> context) {
        context.register(biomeModifierKey(base, period), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(context.lookup(Registries.PLACED_FEATURE).getOrThrow(placedKey(base, period))),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }

    /** For example {@code stone_cretaceous_fossils}. */
    private static String name(FossilBase base, String period) {
        return base.getName() + "_" + period + "_fossils";
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configuredKey(FossilBase base, String period) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Constants.modLoc(name(base, period)));
    }

    private static ResourceKey<PlacedFeature> placedKey(FossilBase base, String period) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Constants.modLoc(name(base, period) + "_placed"));
    }

    private static ResourceKey<BiomeModifier> biomeModifierKey(FossilBase base, String period) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, Constants.modLoc(name(base, period)));
    }
}
