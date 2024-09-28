package net.dumbcode.projectnublar.entity.ik.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public interface IKGeoModelComponent<E extends GeoAnimatable> {
    void tickServer(E animatable);

    <M extends DefaultedEntityGeoModel<E>>void tickClient(E animatable, long instanceId, AnimationState<E> animationState, M model);

    void renderDebug(PoseStack poseStack, E animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay);
}
