package net.dumbcode.projectnublar.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class FeatureInit {
    public static DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Constants.MODID, Registries.FEATURE);

    public static void loadClass() {
        FEATURES.register();
    }
}