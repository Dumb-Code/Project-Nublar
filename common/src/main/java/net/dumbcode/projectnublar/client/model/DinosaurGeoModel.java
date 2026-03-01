package net.dumbcode.projectnublar.client.model;

import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class DinosaurGeoModel extends DefaultedEntityGeoModel<AbstractDinosaur> {

    public DinosaurGeoModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    public DinosaurGeoModel(ResourceLocation assetSubpath, boolean turnsHead) {
        super(assetSubpath, turnsHead);
    }

    @Override
    public ResourceLocation getTextureResource(AbstractDinosaur animatable) {
        return super.getTextureResource(animatable);
    }

    @Override
    public void setCustomAnimations(AbstractDinosaur animatable, long instanceId, AnimationState<AbstractDinosaur> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);


    }
}
