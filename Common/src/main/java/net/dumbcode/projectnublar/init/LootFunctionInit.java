package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.loot.functions.AmberItemFunction;
import net.dumbcode.projectnublar.api.loot.functions.FossilItemFunction;
import net.dumbcode.projectnublar.registration.RegistrationProvider;
import net.dumbcode.projectnublar.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class LootFunctionInit {
    public static final RegistrationProvider<LootItemFunctionType> FUNCTIONS = RegistrationProvider.get(Registries.LOOT_FUNCTION_TYPE, Constants.MODID);
    public static final RegistryObject<LootItemFunctionType> FOSSIL_PART_FUNCTION = FUNCTIONS.register("fossil_part", () -> new LootItemFunctionType(new FossilItemFunction.Serializer()));
    public static final RegistryObject<LootItemFunctionType> AMBER_FUNCTION = FUNCTIONS.register("amber", () -> new LootItemFunctionType(new AmberItemFunction.Serializer()));

    public static void loadClass() {
    }
}
