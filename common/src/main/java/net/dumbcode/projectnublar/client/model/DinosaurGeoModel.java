package net.dumbcode.projectnublar.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.resources.Identifier;

/**
 * The model of every dinosaur species: model and animations from the species id, and the texture of the base pass per
 * gender ({@link DinosaurRenderer#MALE}): the gender's plain base layer,
 * {@code textures/entity/<species>/<male|female>/base.png}. The colour layers draw on top of it, so with the layers off
 * an animal shows its base layer alone. The Velociraptor's {@code base.png} is still a whole painted skin: the art
 * team's raptor layers sit on another UV layout and wait to be fitted to the model. The always-on breathing sway (fix
 * P12a) is added by {@code DinosaurRenderer} in the bone pass.
 */
public class DinosaurGeoModel extends DefaultedEntityGeoModel<Dinosaur> {
    private final Identifier maleBase;
    private final Identifier femaleBase;

    /** The model of the species {@code assetSubpath} ({@code projectnublar:<species id>}). */
    public DinosaurGeoModel(Identifier assetSubpath) {
        super(assetSubpath);
        this.maleBase = baseTexture(assetSubpath, "male");
        this.femaleBase = baseTexture(assetSubpath, "female");
    }

    private static Identifier baseTexture(Identifier species, String gender) {
        return Identifier.fromNamespaceAndPath(species.getNamespace(),
                "textures/entity/" + species.getPath() + "/" + gender + "/base.png");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getGeckolibData(DinosaurRenderer.MALE) ? maleBase : femaleBase;
    }
}
