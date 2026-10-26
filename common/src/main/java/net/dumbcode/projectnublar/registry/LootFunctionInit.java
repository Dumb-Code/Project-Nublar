package net.dumbcode.projectnublar.registry;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.loot.functions.AmberItemFunction;
import net.dumbcode.projectnublar.api.loot.functions.FossilItemFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

/**
 * Registers the fossil and amber loot item functions. On 26.2 a loot function type is the
 * function's {@code MapCodec}. The id strings are frozen contracts.
 */
public class LootFunctionInit {
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> FUNCTIONS = DeferredRegister.create(Constants.MODID, Registries.LOOT_FUNCTION_TYPE);
    public static final DeferredSupplier<MapCodec<FossilItemFunction>> FOSSIL_PART_FUNCTION = FUNCTIONS.register("fossil_part", () -> FossilItemFunction.MAP_CODEC);
    public static final DeferredSupplier<MapCodec<AmberItemFunction>> AMBER_FUNCTION = FUNCTIONS.register("amber", () -> AmberItemFunction.MAP_CODEC);

    public static void loadClass() {
        FUNCTIONS.register();
    }
}
