package net.dumbcode.projectnublar;

import net.dumbcode.projectnublar.client.ClientRegistrationHolder;
import net.dumbcode.projectnublar.init.BlockInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;
import net.minecraft.client.renderer.RenderType;

public class ProjectNublarClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        BlockInit.BLOCKS.getEntries().forEach(block -> {
            BlockRenderLayerMap.INSTANCE.putBlock(block.get(), RenderType.cutout());
        });
        ClientRegistrationHolder.entityRenderers().forEach( (supplier, entityRendererProvider) -> {
            EntityRendererRegistryImpl.register(supplier.get(), entityRendererProvider);
        });
        ClientLifecycleEvents.CLIENT_STARTED.register(
            client -> {
                ClientRegistrationHolder.menuScreens();
            }
        );
        ClientRegistrationHolder.getBlockEntityRenderers().forEach( (supplier, blockEntityRendererProvider) -> {
            BlockEntityRendererRegistryImpl.register(supplier.get(), blockEntityRendererProvider);
        });
        ClientRegistrationHolder.registerItemProperties();
    }
}
