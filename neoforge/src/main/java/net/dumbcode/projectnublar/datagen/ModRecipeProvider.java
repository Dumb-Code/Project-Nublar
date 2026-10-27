package net.dumbcode.projectnublar.datagen;

import net.dumbcode.projectnublar.recipe.UnincubatedEggRecipeBuilder;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

/** The mod's recipes: the unincubated egg (artificial egg + test tube) and its unlock advancement. */
public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        UnincubatedEggRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, ItemInit.UNINCUBATED_EGG.get())
                .requires(ItemInit.ARTIFICIAL_EGG.get())
                .requires(ItemInit.TEST_TUBE_ITEM.get())
                .unlockedBy("has_artificial_egg", this.has(ItemInit.ARTIFICIAL_EGG.get()))
                .save(this.output);
    }

    /** The data provider that runs {@link ModRecipeProvider} once the registries are ready. */
    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Project Nublar recipes";
        }
    }
}
