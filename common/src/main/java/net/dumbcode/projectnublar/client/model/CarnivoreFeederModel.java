package net.dumbcode.projectnublar.client.model;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.DinosaurFeederBlock;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.model.GeoModel;

public class CarnivoreFeederModel extends DefaultedBlockGeoModel<DinosaurFeederBlockEntity> {
    private final Identifier FEEDER_MODEL = buildFormattedModelPath(Constants.modLoc("carnivore_feeder_one"));
    private final Identifier FEEDER_TEXTURE = buildFormattedTexturePath(Constants.modLoc("carnivore_feeder_one_empty"));
    private final Identifier FEEDER_ANIMATIONS = buildFormattedAnimationPath(Constants.modLoc( "carnivore_feeder_one"));

    public CarnivoreFeederModel() {
        super(Constants.modLoc("carnivore_feeder_one"));
    }

    @Override
    public Identifier getModelResource(DinosaurFeederBlockEntity animatable) {
        return FEEDER_MODEL;
    }

    @Override
    public Identifier getTextureResource(DinosaurFeederBlockEntity animatable) {
        return FEEDER_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(DinosaurFeederBlockEntity animatable) {
        return FEEDER_ANIMATIONS;
    }

}
