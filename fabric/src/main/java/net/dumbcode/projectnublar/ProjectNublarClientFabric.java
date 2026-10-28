package net.dumbcode.projectnublar;

import java.util.List;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.client.ClientRegistrationHolder;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.model.FossilTextureProperty;
import net.dumbcode.projectnublar.client.model.TestTubeOverlayProperty;
import net.dumbcode.projectnublar.client.recipe.ClientRecipeStore;
import net.dumbcode.projectnublar.client.screen.GuiEntityGroupRenderer;
import net.dumbcode.projectnublar.registry.RecipeInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * The Fabric client entry point: the {@code client} entrypoint in {@code fabric.mod.json}, run on
 * the physical client only, after {@code ProjectNublarFabric}. It registers what NeoForge's
 * {@code NeoForgeClientEvents} and {@code NeoForgeClientGameEvents} register: the entity and block
 * entity renderers, the menu screens and dinosaur textures, the colour layers and gene widgets,
 * the fossil block tint, the Sequencer's picture-in-picture renderer, the item model properties of
 * the fossil and the test tube, and the hooks that fill and clear the client recipe store. It is
 * the one class of the Fabric module that names client classes.
 */
public class ProjectNublarClientFabric implements ClientModInitializer {

    /**
     * Registers the client side. The menu screens wait for {@code CLIENT_STARTED}, the moment
     * NeoForge registers them too (its first client tick).
     */
    @Override
    public void onInitializeClient() {
        ClientRegistrationHolder.registerEntityRenderers();
        ClientRegistrationHolder.registerBlockEntityRenderers();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> ClientRegistrationHolder.menuScreens());
        CommonClientClass.initClient();
        registerFossilTint();
        PictureInPictureRendererRegistry.register(
                context -> new GuiEntityGroupRenderer(context.minecraft().getEntityRenderDispatcher()));
        registerSelectItemModelProperties();
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> storeRecipes(recipes));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientRecipeStore.clear());
    }

    /**
     * Stores the synced recipes of each type in {@link RecipeInit#SYNCED_TYPES}, then logs at debug
     * level whether the unincubated egg recipe arrived. Fabric API sends only the recipes of the
     * serializers that {@code ProjectNublarFabric} registered for sync, and fires this on the
     * client thread.
     */
    private static void storeRecipes(SynchronizedRecipes recipes) {
        RecipeInit.SYNCED_TYPES.forEach(type -> store(recipes, type.get()));
        Constants.LOG.debug("recipes received: {} types, unincubated egg present: {}", RecipeInit.SYNCED_TYPES.size(),
                ClientRecipeStore.has(ResourceKey.create(Registries.RECIPE, Constants.modLoc("unincubated_egg"))));
    }

    /**
     * Stores the synced recipes of one type. {@code getAllOfType} needs the exact recipe type, so
     * the wildcard type is cast; Fabric API files each recipe under its own type.
     */
    @SuppressWarnings("unchecked")
    private static <I extends RecipeInput, T extends Recipe<I>> void store(SynchronizedRecipes recipes, RecipeType<?> type) {
        RecipeType<T> typed = (RecipeType<T>) type;
        ClientRecipeStore.put(typed, List.copyOf(recipes.getAllOfType(typed)));
    }

    /**
     * Registers the {@code select} properties that {@code items/fossil.json} and
     * {@code items/test_tube.json} name: they pick the texture of each fossil and test tube stack.
     * Fabric API has no registry for them; its transitive access widener makes the vanilla
     * {@code ID_MAPPER} public, the same map that NeoForge's registration event fills. This runs
     * before the first resource load, so the item definitions find both properties.
     */
    private static void registerSelectItemModelProperties() {
        SelectItemModelProperties.ID_MAPPER.put(FossilTextureProperty.ID, FossilTextureProperty.TYPE);
        SelectItemModelProperties.ID_MAPPER.put(TestTubeOverlayProperty.ID, TestTubeOverlayProperty.TYPE);
    }

    /**
     * Tints the fossil overlay (tint index 0 in {@code block/fossil_base}) to match the host stone,
     * so the bone reads as part of the rock. Same colours as on NeoForge: 1.20.1's with an opaque
     * alpha byte, because 26.2 multiplies the alpha channel of a tint as well.
     */
    private static void registerFossilTint() {
        BlockTintSource byBase = state -> switch (((FossilBlock) state.getBlock()).getBase()) {
            case SANDSTONE -> 0xFFE5D38C;
            case DEEPSLATE -> 0xFF4F4F56;
            case STONE -> 0xFFFFFFFF;
        };
        List<BlockTintSource> tints = List.of(byBase);
        FossilCollection.COLLECTIONS.values().forEach(collection ->
                collection.blocks().forEach(block -> BlockColorRegistry.register(tints, block.get())));
    }
}
