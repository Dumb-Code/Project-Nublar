package net.dumbcode.projectnublar.worldgen.placement;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.rmi.registry.Registry;

public class ModifierTypes{
    public static DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Constants.MODID, Registries.PLACEMENT_MODIFIER_TYPE);
    public static DeferredSupplier<PlacementModifierType<FossilRarityFilter>> FOSSIL_FILTER = PLACEMENT_MODIFIERS.register("fossil_filter",() -> () -> FossilRarityFilter.CODEC);
    public static DeferredSupplier<PlacementModifierType<FossilHeightPlacement>> FOSSIL_HEIGHT = PLACEMENT_MODIFIERS.register("fossil_height",() -> () -> FossilHeightPlacement.CODEC);

    public static void loadClass() {
        PLACEMENT_MODIFIERS.register();
    }
}
