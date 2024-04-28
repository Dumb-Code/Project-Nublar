package com.nyfaria.projectnublar.init;

import com.nyfaria.projectnublar.Constants;
import com.nyfaria.projectnublar.registration.RegistrationProvider;
import com.nyfaria.projectnublar.registration.RegistryObject;
import com.nyfaria.projectnublar.worldgen.feature.AmberFeature;
import com.nyfaria.projectnublar.worldgen.feature.FossilConfiguration;
import com.nyfaria.projectnublar.worldgen.feature.FossilFeature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class FeatureInit {
    public static RegistrationProvider<Feature<?>> FEATURES = RegistrationProvider.get(BuiltInRegistries.FEATURE, Constants.MODID);
    public static RegistryObject<Feature<FossilConfiguration>> FOSSIL_FEATURE = FEATURES.register("fossil_feature", () -> new FossilFeature(FossilConfiguration.CODEC));
    public static RegistryObject<Feature<FossilConfiguration>> AMBER_FEATURE = FEATURES.register("amber_feature", () -> new AmberFeature(FossilConfiguration.CODEC));

    public static void loadClass() {
    }
}