package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.recipe.UnincubatedEggRecipe;
import net.dumbcode.projectnublar.recipe.UnincubatedEggRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput generator) {
        super(generator);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> recipeSaver) {
        UnincubatedEggRecipeBuilder.shapeless(RecipeCategory.MISC, ItemInit.UNINCUBATED_EGG.get())
                .requires(ItemInit.ARTIFICIAL_EGG.get())
                .requires(ItemInit.TEST_TUBE_ITEM.get())
                .unlockedBy("has_artificial_egg", has(ItemInit.ARTIFICIAL_EGG.get()))
                .save(recipeSaver);
    }
}
