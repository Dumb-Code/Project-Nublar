package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nullable;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Shared renderer for all dinosaur species. The per-species subclasses differ only by their
 * adult-scale offset, passed through the three-argument constructor (: T-Rex +1.0,
 * Velociraptor +0.6, Dilophosaurus +0.5, Gallimimus +1.0, Triceratops +2.0, Brachiosaurus +2.5).
 */
public class DinosaurRenderer extends GeoEntityRenderer<Dinosaur> {

    // Growth-stage render scale multipliers .
    private static final float BABY_SCALE_MULTIPLIER = 0.25F;
    private static final float JUVENILE_SCALE_MULTIPLIER = 0.5F;
    private static final float SUB_ADULT_SCALE_MULTIPLIER = 0.75F;

    protected final SimpleGeoLayerContainer<Dinosaur> simpleRenderLayers = new SimpleGeoLayerContainer<>(this);

    /** Null means "no custom scaling" (the plain GeoEntityRenderer path). */
    @Nullable
    private final Float adultScaleOffset;

    public DinosaurRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel model) {
        this(renderManager, model, null);
    }

    public DinosaurRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel model,
            @Nullable Float adultScaleOffset) {
        super(renderManager, model);
        this.adultScaleOffset = adultScaleOffset;
    }

    /**
     * Applies the growth-stage scale and publishes the head bone position. Consolidated from the
     * six byte-identical per-species overrides (only the adult-scale offset differed).
     *
     * <p>TODO(BUG): writes the {@code DINOSAUR_HEAD_POS} synched entity data from the client
     * render thread.
     */
    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, Dinosaur animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        if (adultScaleOffset == null) {
            super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
            return;
        }
        float adultScale = ((float) animatable.getDinoData().getGeneValue(GeneInit.SIZE.get()) / 100) + adultScaleOffset;
        float babyScale = adultScale * BABY_SCALE_MULTIPLIER;
        float juvenileScale = adultScale * JUVENILE_SCALE_MULTIPLIER;
        float subAdultScale = adultScale * SUB_ADULT_SCALE_MULTIPLIER;

        float renderScale = switch (animatable.getGrowthStage()) {
            case 1 -> babyScale;
            case 2 -> juvenileScale;
            case 3 -> subAdultScale;
            default -> adultScale;
        };

        if (model.getBone("head").isPresent()) {
            CoreGeoBone head = model.getBone("head").get();
            Vector3f local = new Vector3f(head.getPivotX(), head.getPivotY(), head.getPivotZ());
            Vec3 worldpos = animatable.position().add(local.x, local.y, local.z);
            animatable.getEntityData().set(Dinosaur.DINOSAUR_HEAD_POS, worldpos.toVector3f());
        }

        super.scaleModelForRender(renderScale, renderScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    /**
     * Builds the render layers lazily from the first entity rendered.
     *
     * <p>TODO(BUG): layers are created once per renderer instance (i.e. per entity type) from the
     * first entity encountered, so per-entity render requirements (e.g. male-only layers) stick
     * for every later entity of the type.
     */
    public void createLayers(Dinosaur entity) {
        List<DinoLayer> layers = entity.getLayers();
        for(DinoLayer layer : layers) {
            if (layer.getRenderRequirement().apply(entity)) {
                this.addSimpleRenderLayer(new SimpleGeoLayerRenderer<>(this, layer.getLayerName()) {

                    // TODO(BUG): each layer is rendered twice - once tinted, then once
                    // full-white over it; the current visuals depend on this.
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
