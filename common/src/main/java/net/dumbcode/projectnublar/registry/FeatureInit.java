package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.worldgen.FossilConfiguration;
import net.dumbcode.projectnublar.worldgen.FossilFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Registers the worldgen features. The id strings are frozen contracts (worldgen data names them). */
public class FeatureInit {
    public static DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Constants.MODID, Registries.FEATURE);
    public static final DeferredSupplier<Feature<FossilConfiguration>> FOSSIL_FEATURE = FEATURES.register("fossil_feature", () -> new FossilFeature(FossilConfiguration.CODEC));

    public static void loadClass() {
        FEATURES.register();
    }
}
