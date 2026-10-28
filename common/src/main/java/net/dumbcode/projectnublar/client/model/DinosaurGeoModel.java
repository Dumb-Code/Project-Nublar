package net.dumbcode.projectnublar.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.resources.Identifier;

/**
 * The model of every dinosaur species: model and animations from the species id, and a base texture per gender
 * ({@link DinosaurRenderer#MALE}) from the gender's texture folder,
 * {@code textures/entity/<species>/<male|female>/<base>.png}. The always-on breathing sway (fix P12a) is added by
 * {@code DinosaurRenderer} in the bone pass.
 */
public class DinosaurGeoModel extends DefaultedEntityGeoModel<Dinosaur> {
    private final Identifier maleBase;
    private final Identifier femaleBase;

    /**
     * The model of the species {@code assetSubpath} ({@code projectnublar:<species id>}); the base pass draws the
     * gender's composite, {@code <species id>.png}.
     */
    public DinosaurGeoModel(Identifier assetSubpath) {
        this(assetSubpath, assetSubpath.getPath());
    }

    /** The model of the species {@code assetSubpath}; the base pass draws the gender's {@code <base>.png}. */
    public DinosaurGeoModel(Identifier assetSubpath, String base) {
        super(assetSubpath);
        this.maleBase = baseTexture(assetSubpath, "male", base);
        this.femaleBase = baseTexture(assetSubpath, "female", base);
    }

    private static Identifier baseTexture(Identifier species, String gender, String base) {
        return Identifier.fromNamespaceAndPath(species.getNamespace(),
                "textures/entity/" + species.getPath() + "/" + gender + "/" + base + ".png");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getGeckolibData(DinosaurRenderer.MALE) ? maleBase : femaleBase;
    }
}
