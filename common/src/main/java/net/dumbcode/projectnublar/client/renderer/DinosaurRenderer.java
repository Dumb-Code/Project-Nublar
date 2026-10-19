package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.dumbcode.projectnublar.util.DebugFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
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
    private static final ResourceLocation LEADER_CROWN = new ResourceLocation(Constants.MODID, "textures/misc/leader_crown.png");
    private static final float CROWN_SIZE = 0.6F;
    private static final float CROWN_GAP = 0.5F;

    /**
     * Colour layers, built once per gender on first use. A species may register a separate male layer set
     * ({@link net.dumbcode.projectnublar.client.CommonClientClass#getDinoLayers(net.minecraft.world.entity.EntityType, boolean)}),
     * and one renderer serves every animal of its species, so a single list built from the first animal seen would give
     * every animal that animal's layers. Each frame picks one of the two prebuilt lists by gender: no per-frame allocation.
     * Null until built.
     */
    private @Nullable List<SimpleGeoLayerRenderer<Dinosaur>> maleRenderLayers;
    private @Nullable List<SimpleGeoLayerRenderer<Dinosaur>> femaleRenderLayers;

    // Growth-stage render scale multipliers .
    private static final float BABY_SCALE_MULTIPLIER = 0.25F;
    private static final float JUVENILE_SCALE_MULTIPLIER = 0.5F;
    private static final float SUB_ADULT_SCALE_MULTIPLIER = 0.75F;

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
     * Builds the colour layers for the gender of {@code entity}. {@link DinoLayer#getRenderRequirement()} is read here,
     * once, with this first animal of that gender.
     */
    private List<SimpleGeoLayerRenderer<Dinosaur>> createLayers(Dinosaur entity) {
        List<SimpleGeoLayerRenderer<Dinosaur>> built = new ArrayList<>();
        List<DinoLayer> layers = entity.getLayers();
        for(DinoLayer layer : layers) {
            if (layer.getRenderRequirement().apply(entity)) {
                built.add(new SimpleGeoLayerRenderer<>(this, layer.getLayerName()) {

                    // TODO(BUG): each layer is rendered twice - once tinted, then once
                    // full-white over it; the current visuals depend on this.
                    @Override
                    public void render(PoseStack poseStack, Dinosaur animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                        if (!layer.isVisibleFor(animatable)) {
                            return;
                        }
                        Color color = animatable.layerColor(layers.indexOf(layer) + 1, layer);
                        buffer = bufferSource.getBuffer(RenderType.entityTranslucent(layer.getTextureLocation(animatable)));
                        float red = color.getRedFloat();
                        float green = color.getGreenFloat();
                        float blue = color.getBlueFloat();
                        reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, packedOverlay, red, green, blue, 1f);
                        // Both passes blend into the same translucent buffer, so the second pass doubles the coverage
                        // of partial-alpha texels. Skip it only for layers that are never tinted (basicLayer == -1):
                        // their texture is drawn as is. Other layers keep both passes even when their colour is white,
                        // because skipping would change how they look. Texture baking is the real fix
                        // (docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md).
                        if (layer.getBasicLayer() != -1) {
                            reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, packedOverlay, 1, 1, 1, 1);
                        }
                    }

                    @Override
                    public GeoModel<Dinosaur> getGeoModel() {
                        return DinosaurRenderer.this.getGeoModel();
                    }

                    @Override
                    public @Nullable ResourceLocation getTextureResource(Dinosaur animatable) {
                        return layer.getTextureLocation(animatable);
                    }
                });
            }
        }
        return built;
    }

    @Override
    public void render(Dinosaur entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        if (entity.isGroupLeader()) {
            renderLeaderCrown(entity, poseStack, buffer);
        }
    }

    /** A crown facing the camera just above the hitbox, at full brightness so it reads at night too. */
    private void renderLeaderCrown(Dinosaur entity, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.translate(0.0F, entity.getBbHeight() + CROWN_GAP + CROWN_SIZE / 2F, 0.0F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        // After the camera rotation, +x points left on screen; flip it so u runs left to right.
        poseStack.scale(-CROWN_SIZE, CROWN_SIZE, CROWN_SIZE);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.text(LEADER_CROWN));
        crownVertex(consumer, matrix, -0.5F, -0.5F, 0F, 1F);
        crownVertex(consumer, matrix, 0.5F, -0.5F, 1F, 1F);
        crownVertex(consumer, matrix, 0.5F, 0.5F, 1F, 0F);
        crownVertex(consumer, matrix, -0.5F, 0.5F, 0F, 0F);
        poseStack.popPose();
    }

    private static void crownVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float u, float v) {
        consumer.vertex(matrix, x, y, 0F).color(255, 255, 255, 255).uv(u, v).uv2(LightTexture.FULL_BRIGHT).endVertex();
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
        if (DebugFlags.skipDinoColourLayers) {
            return;
        }
        for (SimpleGeoLayerRenderer<Dinosaur> renderLayer : getSimpleRenderLayers(animatable)) {
            renderLayer.render(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
    }
    /** The colour layers for the gender of {@code animatable}, built on first use. */
    public List<SimpleGeoLayerRenderer<Dinosaur>> getSimpleRenderLayers(Dinosaur animatable) {
        if (animatable.isMale()) {
            if (maleRenderLayers == null) {
                maleRenderLayers = createLayers(animatable);
            }
            return maleRenderLayers;
        }
        if (femaleRenderLayers == null) {
            femaleRenderLayers = createLayers(animatable);
        }
        return femaleRenderLayers;
    }
}
