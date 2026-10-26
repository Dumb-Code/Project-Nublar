package net.dumbcode.projectnublar.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.registry.RecipeInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shapeless crafting recipe for the unincubated egg: it copies the dinosaur data of the test tube
 * in the grid onto the egg and resets its incubation progress.
 *
 * <p>Matching, placement and the recipe book display follow vanilla's {@code ShapelessRecipe}. The
 * JSON fields are the 1.20.1 ones: {@code group}, {@code category}, {@code ingredients} (1 to 9) and
 * {@code result}.
 */
public class UnincubatedEggRecipe implements CraftingRecipe {
    /** JSON form of the recipe, read by the serializer registered in {@link RecipeInit}. */
    public static final MapCodec<UnincubatedEggRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                            Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients))
                    .apply(i, UnincubatedEggRecipe::new));
    /** Network form of the recipe, sent to clients with the recipe sync. */
    public static final StreamCodec<RegistryFriendlyByteBuf, UnincubatedEggRecipe> STREAM_CODEC = StreamCodec.composite(
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC,
            recipe -> recipe.bookInfo,
            ItemStackTemplate.STREAM_CODEC,
            recipe -> recipe.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
            recipe -> recipe.ingredients,
            UnincubatedEggRecipe::new);

    private final CraftingRecipe.CraftingBookInfo bookInfo;
    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;
    @Nullable
    private PlacementInfo placementInfo;

    /**
     * @param bookInfo the recipe book group and category
     * @param result the egg the recipe makes, before the dinosaur data is added
     * @param ingredients the items the grid must hold, in any order
     */
    public UnincubatedEggRecipe(CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, List<Ingredient> ingredients) {
        this.bookInfo = bookInfo;
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    public RecipeSerializer<UnincubatedEggRecipe> getSerializer() {
        return RecipeInit.UNINCUBATED_EGG.get();
    }

    /**
     * Recipes with equal group are combined into one button in the recipe book
     */
    @Override
    public String group() {
        return this.bookInfo.group();
    }

    @Override
    public CraftingBookCategory category() {
        return this.bookInfo.category();
    }

    /** Always true, as the 1.20.1 recipe had it (the old {@code Recipe} default). */
    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.ingredients);
        }

        return this.placementInfo;
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        } else {
            return input.size() == 1 && this.ingredients.size() == 1
                    ? this.ingredients.getFirst().test(input.getItem(0))
                    : input.stackedContents().canCraft(this, null);
        }
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input) {
        ItemStack result = this.result.create();
        ItemStack testTubeItem = input.items().stream().filter(itemStack -> itemStack.is(ItemInit.TEST_TUBE_ITEM.get())).findFirst().orElse(ItemStack.EMPTY);
        if (testTubeItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!testTubeItem.has(DataComponents.CUSTOM_DATA))
            return ItemStack.EMPTY;

        DinoData dinoData = DinoData.fromStack(testTubeItem);
        dinoData.setIncubationProgress(0);
        dinoData.saveTo(result);
        return result;
    }

    /** The recipe book entry: the ingredients, the plain egg and the crafting table. */
    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new ShapelessCraftingRecipeDisplay(
                        this.ingredients.stream().map(Ingredient::display).toList(),
                        new SlotDisplay.ItemStackSlotDisplay(this.result),
                        new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }
}