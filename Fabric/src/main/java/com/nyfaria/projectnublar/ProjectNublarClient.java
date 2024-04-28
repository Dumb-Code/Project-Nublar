package com.nyfaria.projectnublar;

import com.nyfaria.projectnublar.init.BlockInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class ProjectNublarClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        BlockInit.BLOCKS.getEntries().forEach(block -> {
            BlockRenderLayerMap.INSTANCE.putBlock(block.get(), RenderType.cutout());
        });
    }
}
