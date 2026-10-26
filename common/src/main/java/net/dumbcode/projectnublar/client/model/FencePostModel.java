package net.dumbcode.projectnublar.client.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * GeckoLib model of an electric fence post: each post block uses {@code geckolib/models/block/<block id>.geo.json}
 * and {@code textures/block/<block id>.png}. No animations.
 */
public class FencePostModel extends GeoModel<BlockEntityElectricFencePole> {
    /** The post's block id, read at extraction: the render pass has no block entity. */
    private static final DataTicket<Identifier> BLOCK_ID = DataTicket.create("projectnublar_fence_post_block", Identifier.class);
    private static final Map<Identifier, Identifier> modelCache = new HashMap<>();
    private static final Map<Identifier, Identifier> textureCache = new HashMap<>();

    @Override
    public void addAdditionalStateData(BlockEntityElectricFencePole animatable, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(BLOCK_ID, BuiltInRegistries.BLOCK.getKey(animatable.getBlockState().getBlock()));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return modelCache.computeIfAbsent(renderState.getGeckolibData(BLOCK_ID), k -> Identifier.fromNamespaceAndPath(k.getNamespace(), "block/" + k.getPath()));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return textureCache.computeIfAbsent(renderState.getGeckolibData(BLOCK_ID), k -> Identifier.fromNamespaceAndPath(k.getNamespace(),"textures/block/" + k.getPath() + ".png"));
    }

    @Override
    public Identifier getAnimationResource(BlockEntityElectricFencePole animatable) {
        return null;
    }
}
