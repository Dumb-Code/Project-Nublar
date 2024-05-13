package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.worldgen.feature.AmberFeature;
import net.dumbcode.projectnublar.worldgen.feature.FossilConfiguration;
import net.dumbcode.projectnublar.worldgen.feature.FossilFeature;
import net.dumbcode.projectnublar.registration.RegistrationProvider;
import net.dumbcode.projectnublar.registration.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class FeatureInit {
    public static RegistrationProvider<Feature<?>> FEATURES = RegistrationProvider.get(BuiltInRegistries.FEATURE, Constants.MODID);
    public static RegistryObject<Feature<FossilConfiguration>> FOSSIL_FEATURE = FEATURES.register("fossil_feature", () -> new FossilFeature(FossilConfiguration.CODEC));
    public static RegistryObject<Feature<FossilConfiguration>> AMBER_FEATURE = FEATURES.register("amber_feature", () -> new AmberFeature(FossilConfiguration.CODEC));

    public static void loadClass() {
    }
}