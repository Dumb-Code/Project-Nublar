package net.dumbcode.projectnublar.client.renderer.dinosaurs;

import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/** T-Rex renderer; only the adult-scale offset differs from the base. */
public class TyrannosaurusRenderer extends DinosaurRenderer {

    private static final float ADULT_SCALE_OFFSET = 1F;

    public TyrannosaurusRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel model) {
        super(renderManager, model, ADULT_SCALE_OFFSET);
    }
}
