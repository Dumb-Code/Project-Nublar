package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilPieces;
import net.dumbcode.projectnublar.api.Quality;
import net.dumbcode.projectnublar.init.EntityInit;

import net.dumbcode.projectnublar.init.FeatureInit;
import net.dumbcode.projectnublar.worldgen.FossilConfiguration;
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
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
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
        createFossilDepositBiomeModifier("common_tyrannosaur_stone",context);
        //Registers each fossil block as individual ore
        /*
        FossilCollection.COLLECTIONS.forEach((s,fossilCollection) -> {
            fossilCollection.fossilblocks().forEach((block, qualityMap) -> {
                qualityMap.forEach((quality, fossilPieceRegistryObjectMap) -> {
                    fossilPieceRegistryObjectMap.forEach((fossilPiece, blockRegistryObject) -> {
                        String blockd = blockRegistryObject.get().getDescriptionId().replaceAll("block.projectnublar.","");
                        System.out.println(blockd);
                        ResourceKey<BiomeModifier> FOSSIL_ORE_KEY = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, Constants.modLoc( blockd));
                        ResourceKey<PlacedFeature> FOSSIL_ORE_KEY_PLACED = placedFeatureKeys.get(blockd);
                        biomeKeys.put(blockd, FOSSIL_ORE_KEY);
                        context.register(FOSSIL_ORE_KEY,
                                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(
                                        context.lookup(Registries.PLACED_FEATURE).getOrThrow(FOSSIL_ORE_KEY_PLACED)
                                ), GenerationStep.Decoration.UNDERGROUND_ORES)
                        );
                    });
                });
            });
        });

         */

    }

    public static void dimension(BootstapContext<DimensionType> context) {

    }
    public static void configuredFeature(BootstapContext<ConfiguredFeature<?, ?>> context) {

        createFossilDepositConfiguredFeature(EntityInit.TYRANNOSAURUS_REX.getId(),Quality.COMMON, Blocks.STONE,"common_tyrannosaur_stone",context);

    }


    public static void placedFeatures(BootstapContext<PlacedFeature> context) {
        createFossilDepositPlacedFeature("common_tyrannosaur_stone",context);
        //Registers each fossil block as individual ore
        /*
        FossilCollection.COLLECTIONS.forEach((s,fossilCollection) -> {
            fossilCollection.fossilblocks().forEach((block, qualityMap) -> {
                qualityMap.forEach((quality, fossilPieceRegistryObjectMap) -> {
                    fossilPieceRegistryObjectMap.forEach((fossilPiece, blockRegistryObject) -> {
                        String blockd = blockRegistryObject.get().getDescriptionId().replaceAll("block.projectnublar.","");
                        System.out.println(blockd);
                        ResourceKey<PlacedFeature> FOSSIL_ORE_KEY = registerPlacedKey(blockd);
                        ResourceKey<ConfiguredFeature<?,?>> FOSSIL_ORE_KEY_CONFIGURED = configuredFeaturesKeys.get(blockd);

                        placedFeatureKeys.put(blockd, FOSSIL_ORE_KEY);

                        context.register(FOSSIL_ORE_KEY, new PlacedFeature(configuredFeatures.getOrThrow(FOSSIL_ORE_KEY_CONFIGURED),
                                ModOrePlacement.commonOrePlacement(12,
                                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(80)))));
                    });
                });
            });
        });

        //Legacy Attempt
        context.register(Constants.FOSSIL_PLACED, new PlacedFeature(context.lookup(Registries.CONFIGURED_FEATURE).get(Constants.FOSSIL).get(),
                        List.of(
                                CountPlacement.of(ConstantInt.of(100)),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64),VerticalAnchor.absolute(256)),
                                InSquarePlacement.spread(),
                                BiomeFilter.biome()
                        )
                )
        );

         */
    }
    public static void createFossilDepositConfiguredFeature(ResourceLocation dinosaur, Quality pQuality, Block stoneType, String key, BootstapContext<ConfiguredFeature<?, ?>> context) {
        List<FossilPiece> fossilPieces = new ArrayList<>();
        fossilPieces.addAll(FossilPieces.getPiecesByEntityType(dinosaur));

        RuleTest stoneReplaceabeles = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        ResourceKey<ConfiguredFeature<?, ?>> fossil_key =  ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Constants.MODID,key));
        configuredFeaturesKeys.put(key,fossil_key);
        List<FossilConfiguration.TargetBlockState> fossilList = new ArrayList<>();

        FossilCollection.COLLECTIONS.forEach((entity, fossilCollection) -> {
            fossilCollection.fossilblocks().forEach((block, qualityMap) -> {
                qualityMap.forEach((quality, stoneMap) -> {
                    stoneMap.forEach((piece, blockDeferredSupplier) -> {
                        if(entity.equals(dinosaur.toString()) && quality == pQuality && block == stoneType && fossilPieces.contains(piece)) {
                            fossilList.add(FossilConfiguration.target(stoneReplaceabeles,blockDeferredSupplier.get().defaultBlockState()));
                        }
                    });
                });
            });
        });

        List<FossilConfiguration.TargetEntityType> entityTypeList = new ArrayList<>();
        entityTypeList.add(FossilConfiguration.targetEntityType(EntityInit.TYRANNOSAURUS_REX.get()));

        context.register(fossil_key, new ConfiguredFeature<>(
                        FeatureInit.FOSSIL_FEATURE.get(),
                        new FossilConfiguration(fossilList,entityTypeList, 9)
                )
        );


    }
    public static void createFossilDepositPlacedFeature(String key, BootstapContext<PlacedFeature> context) {

        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        ResourceKey<PlacedFeature> fossil_key =  ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Constants.MODID,key + "_placed"));
        placedFeatureKeys.put(key + "_placed",fossil_key);
        ResourceKey<ConfiguredFeature<?,?>> configuredKey = configuredFeaturesKeys.get(key);

        context.register(fossil_key, new PlacedFeature(configuredFeatures.getOrThrow(configuredKey),
                ModOrePlacement.commonOrePlacement(20,
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(80)))));


    }

    public static void createFossilDepositBiomeModifier(String key, BootstapContext<BiomeModifier> context) {
        ResourceKey<BiomeModifier> fossil_key =  ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(Constants.MODID,key));
        biomeKeys.put(key,fossil_key);
        ResourceKey<PlacedFeature> placedKey = placedFeatureKeys.get(key + "_placed");

        context.register(fossil_key,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(
                        context.lookup(Registries.PLACED_FEATURE).getOrThrow(placedKey)
                ), GenerationStep.Decoration.UNDERGROUND_ORES)
        );

    }

}
