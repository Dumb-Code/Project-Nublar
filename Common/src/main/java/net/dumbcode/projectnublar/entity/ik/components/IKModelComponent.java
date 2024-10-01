package net.dumbcode.projectnublar.entity.ik.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.entity.ik.model.ModelAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.core.animation.AnimationState;

public interface IKModelComponent {
    void tickServer(IKAnimatable animatable);

    void tickClient(IKAnimatable animatable, ModelAccessor model);

    void renderDebug(PoseStack poseStack, IKAnimatable animatable, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay);
}
