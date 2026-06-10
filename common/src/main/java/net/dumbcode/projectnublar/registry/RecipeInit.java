package net.dumbcode.projectnublar.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.recipe.UnincubatedEggRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Registers the mod's recipe serializers. The id strings are frozen contracts. */
public class RecipeInit {
    public static DeferredRegister<RecipeSerializer<?>> RECIPE_TYPE = DeferredRegister.create(Constants.MODID, Registries.RECIPE_SERIALIZER);
    public static DeferredSupplier<RecipeSerializer<UnincubatedEggRecipe>> UNINCUBATED_EGG = RECIPE_TYPE.register("unincubated_egg", UnincubatedEggRecipe.Serializer::new);

    public static void loadClass() {
        RECIPE_TYPE.register();
    }
}
