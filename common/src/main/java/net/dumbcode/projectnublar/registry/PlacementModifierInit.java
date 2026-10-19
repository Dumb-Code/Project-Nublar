package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.worldgen.placement.FossilHeightPlacement;
import net.dumbcode.projectnublar.worldgen.placement.FossilRarityFilter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/** Registers the fossil placement modifiers. The id strings are frozen contracts (worldgen data names them). */
public class PlacementModifierInit {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Constants.MODID, Registries.PLACEMENT_MODIFIER_TYPE);
    public static final DeferredSupplier<PlacementModifierType<FossilRarityFilter>> FOSSIL_FILTER = PLACEMENT_MODIFIERS.register("fossil_filter", () -> () -> FossilRarityFilter.CODEC);
    public static final DeferredSupplier<PlacementModifierType<FossilHeightPlacement>> FOSSIL_HEIGHT = PLACEMENT_MODIFIERS.register("fossil_height", () -> () -> FossilHeightPlacement.CODEC);

    public static void loadClass() {
        PLACEMENT_MODIFIERS.register();
    }
}
