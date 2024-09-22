package net.dumbcode.projectnublar.client.model;

import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class DinosaurGeoModel<E extends Dinosaur> extends DefaultedEntityGeoModel<E> {


    public DinosaurGeoModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public ResourceLocation getTextureResource(E animatable) {
        return super.getTextureResource(animatable);
    }

    @Override
    public void setCustomAnimations(E animatable, long instanceId, AnimationState<E> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        animatable.tickComponentsClient(animatable, instanceId, animationState, this);
    }
}
