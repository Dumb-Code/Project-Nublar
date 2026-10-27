package net.dumbcode.projectnublar.client;

import java.util.List;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.recipe.ClientRecipeStore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

/**
 * NeoForge client events of the <b>game bus</b> ({@code NeoForge.EVENT_BUS}): the recipes that the
 * server sends fill {@link ClientRecipeStore}, and the logout clears it. FML loads this class on
 * the physical client only ({@code value = Dist.CLIENT}). The annotation has no bus element: FML
 * picks the bus from the event type, and both events here are game-bus events.
 *
 * <p>Only client game-bus events go here; client mod-bus events go in {@code NeoForgeClientEvents}.
 */
@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class NeoForgeClientGameEvents {

    /**
     * Stores the recipes of each type that the server sent, then logs at debug level whether the
     * unincubated egg recipe arrived. NeoForge fires this on the client thread.
     */
    @SubscribeEvent
    public static void storeRecipes(RecipesReceivedEvent event) {
        event.getRecipeTypes().forEach(type -> store(event.getRecipeMap(), type));
        Constants.LOG.debug("recipes received: {} types, unincubated egg present: {}", event.getRecipeTypes().size(),
                ClientRecipeStore.has(ResourceKey.create(Registries.RECIPE, Constants.modLoc("unincubated_egg"))));
    }

    /** Clears the stored recipes when the client leaves the world or server. */
    @SubscribeEvent
    public static void clearRecipes(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientRecipeStore.clear();
    }

    /**
     * Stores the recipes of one type. {@code RecipeMap.byType} needs the exact recipe type, so the
     * wildcard type is cast; the map files each recipe under its own type.
     */
    @SuppressWarnings("unchecked")
    private static <I extends RecipeInput, T extends Recipe<I>> void store(RecipeMap recipes, RecipeType<?> type) {
        RecipeType<T> typed = (RecipeType<T>) type;
        ClientRecipeStore.put(typed, List.copyOf(recipes.byType(typed)));
    }

    private NeoForgeClientGameEvents() {
    }
}
