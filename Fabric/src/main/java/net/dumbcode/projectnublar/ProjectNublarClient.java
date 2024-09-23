package net.dumbcode.projectnublar;

import net.dumbcode.projectnublar.client.ClientRegistrationHolder;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.network.NetworkInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import static net.dumbcode.projectnublar.client.renderer.entity.layer.IKDebugRenderLayer.getArgb;

public class ProjectNublarClient implements ClientModInitializer {

    private static final int ORANGE = getArgb(255, 255, 165, 0);
    @Override
    public void onInitializeClient() {
        BlockInit.BLOCKS.getEntries().forEach(block -> {
            BlockRenderLayerMap.INSTANCE.putBlock(block.get(), RenderType.cutout());
        });

        ClientRegistrationHolder.entityRenderers().forEach( (supplier, entityRendererProvider) -> {
            EntityRendererRegistry.register(supplier.get(), entityRendererProvider);
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(
            client -> {
                ClientRegistrationHolder.menuScreens();
            }
        );

        ClientRegistrationHolder.getBlockEntityRenderers().forEach( (supplier, blockEntityRendererProvider) -> {
            BlockEntityRenderers.register(supplier.get(), blockEntityRendererProvider);
        });
        ClientRegistrationHolder.registerItemProperties();
        NetworkInit.registerPackets();
    }
}
