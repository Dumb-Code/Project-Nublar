package net.dumbcode.projectnublar.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.resources.Identifier;

/**
 * The model of every dinosaur species: model, animations and base texture from the species id. The always-on
 * breathing sway (fix P12a) is added by {@code DinosaurRenderer} in the bone pass.
 */
public class DinosaurGeoModel extends DefaultedEntityGeoModel<Dinosaur> {

    /** The model of the species {@code assetSubpath} ({@code projectnublar:<species id>}). */
    public DinosaurGeoModel(Identifier assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return super.getTextureResource(renderState);
    }
}
