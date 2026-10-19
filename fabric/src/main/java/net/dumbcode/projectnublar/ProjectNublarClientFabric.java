package net.dumbcode.projectnublar;

import net.dumbcode.projectnublar.client.ClientRegistrationHolder;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.network.NetworkInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;
import net.minecraft.client.renderer.RenderType;

/**
 * TODO(DEAD): the Fabric client entry point is intentionally disabled - the whole body of
 * {@link #onInitializeClient()} was commented out in the original (render layers, entity/BE
 * renderers, menu screens, item properties, packet registration, client init) and this class is
 * not declared as a {@code client} entry point in {@code fabric.mod.json}. Do not wire it up;
 * we know Fabric won't load.
 */
public class ProjectNublarClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
     /*
        BlockInit.BLOCKS.getEntries().forEach(block -> {
            BlockRenderLayerMap.INSTANCE.putBlock(block.get(), RenderType.cutout());
        });
        ClientRegistrationHolder.registerEntityRenderers();
        ClientLifecycleEvents.CLIENT_STARTED.register(
            client -> {
                ClientRegistrationHolder.menuScreens();
            }
        );
        ClientRegistrationHolder.registerBlockEntityRenderers();
        ClientRegistrationHolder.registerItemProperties();
        NetworkInit.registerPackets();
        CommonClientClass.initClient();


      */
    }
}
