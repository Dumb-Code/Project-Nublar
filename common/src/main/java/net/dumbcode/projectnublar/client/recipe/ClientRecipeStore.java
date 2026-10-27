package net.dumbcode.projectnublar.client.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * The recipes that the server sent to this client, by recipe type. On 26.2 the server does not
 * send recipes to clients by default, so recipe viewers read them from here. Each loader's
 * recipe-sync hook fills the store once per login and once per {@code /reload}: NeoForge's
 * {@code RecipesReceivedEvent} and Fabric API's {@code ClientRecipeSynchronizedEvent}. Each loader's
 * disconnect hook clears it.
 *
 * <p>Client only. The loader hooks fill it on the client thread and only the client reads it, so it
 * has no locks.
 */
public final class ClientRecipeStore {

    private static final Map<RecipeType<?>, List<? extends RecipeHolder<?>>> RECIPES = new HashMap<>();

    /** Replaces the stored recipes of {@code type} with {@code recipes}. */
    public static <T extends Recipe<?>> void put(RecipeType<T> type, List<RecipeHolder<T>> recipes) {
        RECIPES.put(type, List.copyOf(recipes));
    }

    /** The stored recipes of {@code type}; an empty list when the server sent none of that type. */
    @SuppressWarnings("unchecked") // put files each list under the type of its recipes
    public static <T extends Recipe<?>> List<RecipeHolder<T>> get(RecipeType<T> type) {
        return (List<RecipeHolder<T>>) RECIPES.getOrDefault(type, List.of());
    }

    /** Whether the store holds the recipe with this key, of any type. */
    public static boolean has(ResourceKey<Recipe<?>> key) {
        return RECIPES.values().stream().flatMap(List::stream).anyMatch(recipe -> recipe.id().equals(key));
    }

    /** Removes every stored recipe. The loaders call it when the client disconnects. */
    public static void clear() {
        RECIPES.clear();
    }

    private ClientRecipeStore() {
    }
}
