package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.recipe.UnincubatedEggRecipe;
import net.dumbcode.projectnublar.registration.RegistrationProvider;
import net.dumbcode.projectnublar.registration.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipeInit {
    public static RegistrationProvider<RecipeSerializer<?>> RECIPE_TYPE = RegistrationProvider.get(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MODID);
    public static RegistryObject<RecipeSerializer<UnincubatedEggRecipe>> UNINCUBATED_EGG = RECIPE_TYPE.register("unincubated_egg", UnincubatedEggRecipe.Serializer::new);

    public static void loadClass() {


    }
}
