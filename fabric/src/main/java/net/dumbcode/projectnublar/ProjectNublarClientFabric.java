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
