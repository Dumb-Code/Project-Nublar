package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.GeneInit;
import net.dumbcode.projectnublar.util.DebugFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
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
    public DinosaurRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel model) {
        super(renderManager, model);
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
