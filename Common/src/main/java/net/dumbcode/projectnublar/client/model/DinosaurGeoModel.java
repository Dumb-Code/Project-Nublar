package net.dumbcode.projectnublar.client.model;

import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class DinosaurGeoModel extends DefaultedEntityGeoModel<Dinosaur> {

    public DinosaurGeoModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    public DinosaurGeoModel(ResourceLocation assetSubpath, boolean turnsHead) {
        super(assetSubpath, turnsHead);
    }

    @Override
    public ResourceLocation getTextureResource(Dinosaur animatable) {
        return super.getTextureResource(animatable);
    }
}
