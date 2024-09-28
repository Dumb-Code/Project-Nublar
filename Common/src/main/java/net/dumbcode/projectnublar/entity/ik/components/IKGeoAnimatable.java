package net.dumbcode.projectnublar.entity.ik.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.List;

public interface IKGeoAnimatable<E extends GeoAnimatable> {
    List<IKGeoModelComponent<E>> getComponents();

    default boolean containsComponent(Class type) {
        return !this.getComponents().stream().filter(eikModelComponent -> eikModelComponent.getClass() == type).toList().isEmpty();
    }

    default List<? extends IKGeoModelComponent<E>> getComponentOfType(Class type) {
        return this.getComponents().stream().filter(eikModelComponent -> eikModelComponent.getClass() == type).toList();
    }

    default void addComponent(IKGeoModelComponent<E> component) {
        this.getComponents().add(component);
    }

    default<M extends DefaultedEntityGeoModel<E>> void tickComponentsClient(E animatable, long instanceId, AnimationState<E> animationState, M model) {
        this.getComponents().forEach(ikModelComponent -> ikModelComponent.tickClient(animatable, instanceId, animationState, model));
    }

    default void tickComponentsServer(E animatable) {
        this.getComponents().forEach(ikModelComponent -> ikModelComponent.tickServer(animatable));
    }

    default void renderDebug(PoseStack poseStack, E animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.getComponents().forEach(eikModelComponent -> eikModelComponent.renderDebug(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay));
    }
}
