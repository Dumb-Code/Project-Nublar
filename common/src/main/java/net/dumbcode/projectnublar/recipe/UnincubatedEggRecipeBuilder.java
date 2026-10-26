package net.dumbcode.projectnublar.recipe;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

/**
 * Datagen builder for {@link UnincubatedEggRecipe}. A copy of vanilla's {@code ShapelessRecipeBuilder}
 * that saves the egg recipe type instead of the vanilla shapeless type.
 */
public class UnincubatedEggRecipeBuilder implements RecipeBuilder {
   private final HolderGetter<Item> items;
   private final RecipeCategory category;
   private final ItemStackTemplate result;
   private final List<Ingredient> ingredients = Lists.newArrayList();
   private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
   @Nullable
   private String group;

   /**
    * @param pItems the item lookup of the data provider, used to resolve item tags
    * @param pCategory the recipe category, which also sets the recipe book category
    * @param pResult the item the recipe makes
    * @param pCount how many items the recipe makes
    */
   public UnincubatedEggRecipeBuilder(HolderGetter<Item> pItems, RecipeCategory pCategory, ItemLike pResult, int pCount) {
      this.items = pItems;
      this.category = pCategory;
      this.result = new ItemStackTemplate(pResult.asItem(), pCount);
   }

   /**
    * Creates a new builder for a shapeless recipe.
    */
   public static UnincubatedEggRecipeBuilder shapeless(HolderGetter<Item> pItems, RecipeCategory pCategory, ItemLike pResult) {
      return new UnincubatedEggRecipeBuilder(pItems, pCategory, pResult, 1);
   }

   /**
    * Creates a new builder for a shapeless recipe.
    */
   public static UnincubatedEggRecipeBuilder shapeless(HolderGetter<Item> pItems, RecipeCategory pCategory, ItemLike pResult, int pCount) {
      return new UnincubatedEggRecipeBuilder(pItems, pCategory, pResult, pCount);
   }

   /**
    * Adds an ingredient that can be any item in the given tag.
    */
   public UnincubatedEggRecipeBuilder requires(TagKey<Item> pTag) {
      return this.requires(Ingredient.of(this.items.getOrThrow(pTag)));
   }

   /**
    * Adds an ingredient of the given item.
    */
   public UnincubatedEggRecipeBuilder requires(ItemLike pItem) {
      return this.requires(pItem, 1);
   }

   /**
    * Adds the given ingredient multiple times.
    */
   public UnincubatedEggRecipeBuilder requires(ItemLike pItem, int pQuantity) {
      for(int i = 0; i < pQuantity; ++i) {
         this.requires(Ingredient.of(pItem));
      }

      return this;
   }

   /**
    * Adds an ingredient.
    */
   public UnincubatedEggRecipeBuilder requires(Ingredient pIngredient) {
      return this.requires(pIngredient, 1);
   }

   /**
    * Adds an ingredient multiple times.
    */
   public UnincubatedEggRecipeBuilder requires(Ingredient pIngredient, int pQuantity) {
      for(int i = 0; i < pQuantity; ++i) {
         this.ingredients.add(pIngredient);
      }

      return this;
   }

   @Override
   public UnincubatedEggRecipeBuilder unlockedBy(String pCriterionName, Criterion<?> pCriterion) {
      this.advancementBuilder.unlockedBy(pCriterionName, pCriterion);
      return this;
   }

   @Override
   public UnincubatedEggRecipeBuilder group(@Nullable String pGroupName) {
      this.group = pGroupName;
      return this;
   }

   @Override
   public ResourceKey<Recipe<?>> defaultId() {
      return RecipeBuilder.getDefaultRecipeId(this.result);
   }

   /**
    * Writes the recipe and its unlock advancement. Throws if no {@link #unlockedBy} criterion was
    * added, because nothing could unlock the recipe then.
    */
   @Override
   public void save(RecipeOutput pOutput, ResourceKey<Recipe<?>> pRecipeId) {
      UnincubatedEggRecipe recipe = new UnincubatedEggRecipe(
              RecipeBuilder.createCraftingBookInfo(this.category, this.group), this.result, this.ingredients);
      pOutput.accept(pRecipeId, recipe, this.advancementBuilder.build(pOutput, pRecipeId, this.category));
   }
}