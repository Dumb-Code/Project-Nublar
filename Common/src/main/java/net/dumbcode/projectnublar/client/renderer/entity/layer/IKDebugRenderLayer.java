package net.dumbcode.projectnublar.client.renderer.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class IKDebugRenderLayer extends GeoRenderLayer<Dinosaur> {
    ///summon projectnublar:tyrannosaurus_rex ~ ~ ~ {NoAI:1b}

    public static int getArgb(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public IKDebugRenderLayer(GeoRenderer<Dinosaur> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, Dinosaur animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!(animatable instanceof Dinosaur) || !Constants.shouldRenderDebugLegs) {
            return;
        }

        animatable.renderDebug(poseStack, animatable, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}
