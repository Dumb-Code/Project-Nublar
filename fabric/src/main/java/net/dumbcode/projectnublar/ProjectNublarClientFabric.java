package net.dumbcode.projectnublar;

import java.util.List;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.client.ClientRegistrationHolder;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.screen.GuiEntityGroupRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.color.block.BlockTintSource;

/**
 * The Fabric client entry point: the {@code client} entrypoint in {@code fabric.mod.json}, run on
 * the physical client only, after {@code ProjectNublarFabric}. It registers what NeoForge's
 * {@code NeoForgeClientEvents} registers: the entity and block entity renderers, the menu screens
 * and dinosaur textures, the colour layers and gene widgets, the fossil block tint and the
 * Sequencer's picture-in-picture renderer. It is the one class of the Fabric module that names
 * client classes.
 */
public class ProjectNublarClientFabric implements ClientModInitializer {

    /**
     * Registers the client side. The menu screens wait for {@code CLIENT_STARTED}: they also
     * register the dinosaur textures, and Fabric runs this method inside the {@code Minecraft}
     * constructor, before the texture manager exists.
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
