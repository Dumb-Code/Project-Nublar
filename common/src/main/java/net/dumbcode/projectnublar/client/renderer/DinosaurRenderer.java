package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;
import java.util.List;

public class DinosaurRenderer extends GeoEntityRenderer<AbstractDinosaur> {
    protected final SimpleGeoLayerContainer<AbstractDinosaur> simpleRenderLayers = new SimpleGeoLayerContainer<>(this);
    public DinosaurRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel model) {
        super(renderManager, model);
    }

    public void createLayers(AbstractDinosaur entity) {
        List<DinoLayer> layers = entity.getLayers();
        for(DinoLayer layer : layers) {
            if (layer.getRenderRequirement().apply(entity)) {
                this.addSimpleRenderLayer(new SimpleGeoLayerRenderer<>(this, layer.getLayerName()) {

                    @Override
                    public void render(PoseStack poseStack, AbstractDinosaur animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                        Color color = animatable.layerColor(layers.indexOf(layer) + 1, layer);
                        buffer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureResource(animatable)));
                        reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, packedOverlay, color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), 1f);
                        reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, packedOverlay, 1, 1, 1, 1);
                    }

                    @Override
                    public GeoModel<AbstractDinosaur> getGeoModel() {
                        return DinosaurRenderer.this.getGeoModel();
                    }

                    @Override
                    public @Nullable ResourceLocation getTextureResource(AbstractDinosaur animatable) {
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
    public void render(AbstractDinosaur entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if(getSimpleRenderLayers().isEmpty()) {
            createLayers(entity);
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private Color color = null;

    @Override
    public Color getRenderColor(AbstractDinosaur animatable, float partialTick, int packedLight) {
        return animatable.layerColor(0, null);
    }
    @Override
    public float getMotionAnimThreshold(AbstractDinosaur animatable) {
        return 0.005f;
    }
    @Override
    public void applyRenderLayers(PoseStack poseStack, AbstractDinosaur animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if(getSimpleRenderLayers().isEmpty()) {
            createLayers(animatable);
        }

        for (SimpleGeoLayerRenderer<AbstractDinosaur> renderLayer : getSimpleRenderLayers()) {
            renderLayer.render(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
    }
    public List<SimpleGeoLayerRenderer<AbstractDinosaur>> getSimpleRenderLayers() {
        return this.simpleRenderLayers.getRenderLayers();
    }
    private GeoEntityRenderer<AbstractDinosaur> addSimpleRenderLayer(SimpleGeoLayerRenderer<AbstractDinosaur> renderLayer) {
        this.simpleRenderLayers.addLayer(renderLayer);
        return this;
    }
}
