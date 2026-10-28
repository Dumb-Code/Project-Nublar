package net.dumbcode.projectnublar.client;

import java.util.List;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.client.model.FossilTextureProperty;
import net.dumbcode.projectnublar.client.model.TestTubeOverlayProperty;
import net.dumbcode.projectnublar.client.screen.GuiEntityGroupRenderState;
import net.dumbcode.projectnublar.client.screen.GuiEntityGroupRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;

/**
 * NeoForge client events of the <b>mod bus</b>: renderers, menu screens, the fossil block tint,
 * the Sequencer's picture-in-picture renderer and the item model properties of the fossil and the
 * test tube. FML loads this class on the physical client only
 * ({@code value = Dist.CLIENT}), so it is the one place on NeoForge that names client classes.
 *
 * <p>Only mod-bus events go here; client game-bus events need a class of their own.
 */
@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {

    /**
     * Tints the fossil overlay (tint index 0 in {@code block/fossil_base}) to match the host stone,
     * so the bone reads as part of the rock. The colours are 1.20.1's with an opaque alpha byte:
     * 26.2 multiplies the alpha channel of a tint as well, 1.20.1 ignored it.
     */
    @SubscribeEvent
    public static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        BlockTintSource byBase = state -> switch (((FossilBlock) state.getBlock()).getBase()) {
            case SANDSTONE -> 0xFFE5D38C;
            case DEEPSLATE -> 0xFF4F4F56;
            case STONE -> 0xFFFFFFFF;
        };
        List<BlockTintSource> tints = List.of(byBase);
        FossilCollection.COLLECTIONS.values().forEach(collection ->
                collection.blocks().forEach(block -> event.register(tints, block.get())));
    }

    /** Registers the entity and block entity renderers. */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ClientRegistrationHolder.registerEntityRenderers();
        ClientRegistrationHolder.registerBlockEntityRenderers();
    }

    /**
     * Registers the renderer that draws the Sequencer edit tab's dinosaur and player into one
     * picture (see {@link GuiEntityGroupRenderer}).
     */
    @SubscribeEvent
    public static void registerPictureInPictureRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(GuiEntityGroupRenderState.class,
                () -> new GuiEntityGroupRenderer(Minecraft.getInstance().getEntityRenderDispatcher()));
    }

    /**
     * Registers the {@code select} properties that {@code items/fossil.json} and
     * {@code items/test_tube.json} name: they pick the texture of each fossil and test tube stack.
     */
    @SubscribeEvent
    public static void registerSelectItemModelProperties(RegisterSelectItemModelPropertyEvent event) {
        event.register(FossilTextureProperty.ID, FossilTextureProperty.TYPE);
        event.register(TestTubeOverlayProperty.ID, TestTubeOverlayProperty.TYPE);
    }

    /**
     * Queues the menu screens for the first client tick, before any menu can open (Fabric registers
     * them on {@code CLIENT_STARTED}, the same moment), then registers the colour layers and gene
     * widgets. Each dinosaur's model picks its own gender's base texture, so no texture loads here.
     */
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        Minecraft.getInstance().execute(ClientRegistrationHolder::menuScreens);
        CommonClientClass.initClient();
    }

    private NeoForgeClientEvents() {
    }
}
