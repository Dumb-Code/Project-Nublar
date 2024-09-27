package net.dumbcode.projectnublar.client.model;

import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class DinosaurGeoModel extends DefaultedEntityGeoModel<net.dumbcode.projectnublar.entity.Dinosaur> {


    public DinosaurGeoModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public ResourceLocation getTextureResource(Dinosaur animatable) {
        return super.getTextureResource(animatable);
    }

    @Override
    public void setCustomAnimations(Dinosaur animatable, long instanceId, AnimationState<Dinosaur> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        animatable.tickComponentsClient(animatable, instanceId, animationState, this);
    }
}
