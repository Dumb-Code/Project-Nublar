package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import javax.annotation.Nullable;
import java.util.Optional;

public class SimpleGeoLayerRenderer<T extends GeoAnimatable> {
    private final GeoRenderer<T> renderer;
    private final String layerName;


    public SimpleGeoLayerRenderer(GeoRenderer<T> pRenderer,String pLayerName) {
        this.renderer = pRenderer;
        this.layerName = pLayerName;
    }

    public GeoModel<T> getGeoModel() {
        return this.renderer.getGeoModel();
    }

    public BakedGeoModel getDefaultBakedModel(T animatable) {
        return getGeoModel().getBakedModel(getGeoModel().getModelResource(animatable, getRenderer()));
    }

    public GeoRenderer<T> getRenderer() {
        return this.renderer;
    }

    public @Nullable ResourceLocation getTextureResource(T animatable) {
        return this.getRenderer().getTextureLocation(animatable);
    }

    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                          MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                          int packedLight, int packedOverlay) {

    }

    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {

    }

    public void renderForBone(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType,
                              MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {}

    public String getLayerName() {return layerName;}
}
