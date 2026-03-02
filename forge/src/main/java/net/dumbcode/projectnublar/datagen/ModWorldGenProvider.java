package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;

import net.dumbcode.projectnublar.block.FossilBlock;

import net.dumbcode.projectnublar.init.DinosaurInit;
import net.dumbcode.projectnublar.init.FeatureInit;
import net.dumbcode.projectnublar.init.TagInit;
import net.dumbcode.projectnublar.worldgen.FossilConfiguration;
import net.dumbcode.projectnublar.worldgen.placement.FossilHeightPlacement;
import net.dumbcode.projectnublar.worldgen.placement.FossilRarityFilter;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::configuredFeature)
            .add(Registries.PLACED_FEATURE, ModWorldGenProvider::placedFeatures)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGenProvider::biomeModifiers);
    private static final ResourceKey<BiomeModifier> FOSSIL = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, Constants.modLoc( "fossil"));

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Constants.MODID));
    }
    private static final Map<String, ResourceKey<PlacedFeature>> placedFeatureKeys = new HashMap<>();
    private static final Map<String, ResourceKey<ConfiguredFeature<?,?>>> configuredFeaturesKeys = new HashMap<>();
    private static final Map<String, ResourceKey<BiomeModifier>> biomeKeys = new HashMap<>();



    public static void biomeModifiers(BootstapContext<BiomeModifier> context) {
        createFossilDepositBiomeModifier("sandstone_cretaceous_fossils_feature",context);
        createFossilDepositBiomeModifier("stone_cretaceous_fossils_feature",context);
        createFossilDepositBiomeModifier("deepslate_cretaceous_fossils_feature",context);
    }

    public static void dimension(BootstapContext<DimensionType> context) {

    }
    public static void configuredFeature(BootstapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceabeles = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepSlateReplaceabeles = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest sandStoneReplaceabeles = new TagMatchTest(TagInit.SANDSTONE_REPLACEABLES);
        createFossilDepositConfiguredFeature("sandstone_cretaceous_fossils_feature",sandStoneReplaceabeles,"cretaceous",Blocks.SANDSTONE,context);
        createFossilDepositConfiguredFeature("stone_cretaceous_fossils_feature",stoneReplaceabeles,"cretaceous",Blocks.STONE,context);
        createFossilDepositConfiguredFeature("deepslate_cretaceous_fossils_feature",deepSlateReplaceabeles,"cretaceous",Blocks.DEEPSLATE, context);

    }


    public static void placedFeatures(BootstapContext<PlacedFeature> context) {
        createFossilDepositPlacedFeature("stone_cretaceous_fossils_feature","cretaceous",context);
        createFossilDepositPlacedFeature("sandstone_cretaceous_fossils_feature","cretaceous",context);
        createFossilDepositPlacedFeature("deepslate_cretaceous_fossils_feature","cretaceous",context);

    }
    public static void createFossilDepositConfiguredFeature(String key, RuleTest ruleTest, String period, Block base, BootstapContext<ConfiguredFeature<?, ?>> context) {


        ResourceKey<ConfiguredFeature<?, ?>> fossil_key =  ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Constants.MODID,key));
        configuredFeaturesKeys.put(key,fossil_key);

        List<FossilConfiguration.TargetBlockState> fossilList = new ArrayList<>();
        FossilCollection.COLLECTIONS.forEach((entity, fossilCollection) -> {
            fossilCollection.fossilblocks().forEach((block, piecemap) -> {
                piecemap.forEach((piece, fossil) -> {
                        FossilBlock fossilBlock = (FossilBlock) fossil.get();
                        if(fossilBlock.getDinosaur().period().equals(period) && fossilBlock.getBase().getBlock().equals(base)) {
                            fossilList.add(FossilConfiguration.target(ruleTest, fossil.get().defaultBlockState()));
                        }
                });
            });
        });


        context.register(fossil_key, new ConfiguredFeature<>(
                        FeatureInit.FOSSIL_FEATURE.get(),
                        new FossilConfiguration(fossilList, 15)
                )
        );

    }
    public static void createFossilDepositPlacedFeature(String key,String period, BootstapContext<PlacedFeature> context) {

        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        ResourceKey<PlacedFeature> fossil_key =  ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Constants.MODID,key + "_placed_key"));
        placedFeatureKeys.put(key + "_placed_key",fossil_key);
        ResourceKey<ConfiguredFeature<?,?>> configuredKey = configuredFeaturesKeys.get(key);

        context.register(fossil_key, new PlacedFeature(configuredFeatures.getOrThrow(configuredKey),
                List.of(FossilRarityFilter.useConfigBasedChanceAndHeight(period), InSquarePlacement.spread(), FossilHeightPlacement.of(period), BiomeFilter.biome())));


    }

    public static void createFossilDepositBiomeModifier(String key, BootstapContext<BiomeModifier> context) {
        ResourceKey<BiomeModifier> fossil_key =  ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(Constants.MODID,key));
        biomeKeys.put(key,fossil_key);
        ResourceKey<PlacedFeature> placedKey = placedFeatureKeys.get(key + "_placed_key");

        context.register(fossil_key,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(
                        context.lookup(Registries.PLACED_FEATURE).getOrThrow(placedKey)
                ), GenerationStep.Decoration.UNDERGROUND_ORES)
        );

    }
}
