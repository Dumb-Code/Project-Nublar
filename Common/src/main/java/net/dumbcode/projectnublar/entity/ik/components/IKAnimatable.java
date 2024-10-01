package net.dumbcode.projectnublar.entity.ik.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.entity.ik.model.ModelAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.List;

public interface IKAnimatable {
    List<IKModelComponent> getComponents();

    default boolean containsComponent(Class type) {
        return !this.getComponents().stream().filter(eikModelComponent -> eikModelComponent.getClass() == type).toList().isEmpty();
    }

    default List<? extends IKModelComponent> getComponentOfType(Class type) {
        return this.getComponents().stream().filter(eikModelComponent -> eikModelComponent.getClass() == type).toList();
    }

    default void addComponent(IKModelComponent component) {
        this.getComponents().add(component);
    }

    default<M extends DefaultedEntityGeoModel> void tickComponentsClient(IKAnimatable animatable, ModelAccessor model) {
        this.getComponents().forEach(ikModelComponent -> ikModelComponent.tickClient(animatable, model));
    }

    default void tickComponentsServer(IKAnimatable animatable) {
        this.getComponents().forEach(ikModelComponent -> ikModelComponent.tickServer(animatable));
    }

    default void renderDebug(PoseStack poseStack, IKAnimatable animatable, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.getComponents().forEach(eikModelComponent -> eikModelComponent.renderDebug(poseStack, animatable, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay));
    }
}
