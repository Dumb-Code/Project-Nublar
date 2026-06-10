package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Empty worldgen feature register. No features are registered yet, but the register is still
 * initialized by {@code ProjectNublar.init()} and referenced by Forge datagen.
 */
public class FeatureInit {
    public static DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Constants.MODID, Registries.FEATURE);

    public static void loadClass() {
        FEATURES.register();
    }
}