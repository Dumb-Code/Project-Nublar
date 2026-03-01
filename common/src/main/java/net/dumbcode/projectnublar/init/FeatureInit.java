package net.dumbcode.projectnublar.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;

import net.dumbcode.projectnublar.worldgen.FossilConfiguration;
import net.dumbcode.projectnublar.worldgen.FossilFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureInit {
    //Configured Features
    public static DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Constants.MODID, Registries.FEATURE);
    public static DeferredSupplier<Feature<FossilConfiguration>> FOSSIL_FEATURE = FEATURES.register("fossil_feature", () -> new FossilFeature(FossilConfiguration.CODEC));

    public static void loadClass() {
        FEATURES.register();
    }
}