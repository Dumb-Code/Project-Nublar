package net.dumbcode.projectnublar.client.model;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.minecraft.resources.Identifier;

/**
 * GeckoLib model of the feeder: the {@code carnivore_feeder_one} geometry and animations. The renderer
 * ({@code CarnivoreFeederRenderer}) picks the texture by the feeder's state.
 */
public class CarnivoreFeederModel extends DefaultedBlockGeoModel<DinosaurFeederBlockEntity> {
    private final Identifier FEEDER_MODEL = buildFormattedModelPath(Constants.modLoc("carnivore_feeder_one"));
    private final Identifier FEEDER_TEXTURE = buildFormattedTexturePath(Constants.modLoc("carnivore_feeder_one_empty"));
    private final Identifier FEEDER_ANIMATIONS = buildFormattedAnimationPath(Constants.modLoc( "carnivore_feeder_one"));

    public CarnivoreFeederModel() {
        super(Constants.modLoc("carnivore_feeder_one"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return FEEDER_MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return FEEDER_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(DinosaurFeederBlockEntity animatable) {
        return FEEDER_ANIMATIONS;
    }

}
