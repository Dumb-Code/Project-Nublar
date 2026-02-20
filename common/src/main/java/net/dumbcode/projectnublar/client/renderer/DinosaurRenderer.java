package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayersContainer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DinosaurRenderer extends GeoEntityRenderer<Dinosaur> {
    protected final SimpleGeoLayerContainer<Dinosaur> simpleRenderLayers = new SimpleGeoLayerContainer<>(this);
    public DinosaurRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel model) {
        super(renderManager, model);
    }

    public void createLayers(Dinosaur entity) {
        List<DinoLayer> layers = entity.getLayers();
        for(DinoLayer layer : layers) {
            if (layer.getRenderRequirement().apply(entity)) {
                this.addSimpleRenderLayer(new SimpleGeoLayerRenderer<>(this, layer.getLayerName()) {

                    @Override
                    public void render(PoseStack poseStack, Dinosaur animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                        Color color = animatable.layerColor(layers.indexOf(layer) + 1, layer);
                        buffer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureResource(animatable)));
                        reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, packedOverlay, color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), 1f);
                        reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, packedOverlay, 1, 1, 1, 1);
                    }

                    @Override
                    public GeoModel<Dinosaur> getGeoModel() {
                        return DinosaurRenderer.this.getGeoModel();
                    }

                    @Override
                    public @Nullable ResourceLocation getTextureResource(Dinosaur animatable) {
                        if (layer.getTextureLocation(animatable).isPresent()) {
                            return layer.getTextureLocation(animatable).get();
                        } else {
                            return null;
                        }
                    }
                });
            }
        }
    }

    @Override
    public void render(Dinosaur entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if(getSimpleRenderLayers().isEmpty()) {
            createLayers(entity);
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private Color color = null;

    @Override
    public Color getRenderColor(Dinosaur animatable, float partialTick, int packedLight) {
        return animatable.layerColor(0, null);
    }
    @Override
    public float getMotionAnimThreshold(Dinosaur animatable) {
        return 0.005f;
    }
    @Override
    public void applyRenderLayers(PoseStack poseStack, Dinosaur animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if(getSimpleRenderLayers().isEmpty()) {
            createLayers(animatable);
        }

        for (SimpleGeoLayerRenderer<Dinosaur> renderLayer : getSimpleRenderLayers()) {
            renderLayer.render(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
    }
    public List<SimpleGeoLayerRenderer<Dinosaur>> getSimpleRenderLayers() {
        return this.simpleRenderLayers.getRenderLayers();
    }
    private GeoEntityRenderer<Dinosaur> addSimpleRenderLayer(SimpleGeoLayerRenderer<Dinosaur> renderLayer) {
        this.simpleRenderLayers.addLayer(renderLayer);
        return this;
    }
}
