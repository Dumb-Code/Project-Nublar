package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.loot.functions.AmberItemFunction;
import net.dumbcode.projectnublar.api.loot.functions.FossilItemFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

/** Registers the fossil and amber loot item functions. The id strings are frozen contracts. */
public class LootFunctionInit {
    public static final DeferredRegister<LootItemFunctionType> FUNCTIONS = DeferredRegister.create(Constants.MODID, Registries.LOOT_FUNCTION_TYPE);
    public static final DeferredSupplier<LootItemFunctionType> FOSSIL_PART_FUNCTION = FUNCTIONS.register("fossil_part", () -> new LootItemFunctionType(new FossilItemFunction.Serializer()));
    public static final DeferredSupplier<LootItemFunctionType> AMBER_FUNCTION = FUNCTIONS.register("amber", () -> new LootItemFunctionType(new AmberItemFunction.Serializer()));

    public static void loadClass() {
        FUNCTIONS.register();
    }
}
