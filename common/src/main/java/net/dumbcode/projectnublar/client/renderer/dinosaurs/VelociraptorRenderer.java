package net.dumbcode.projectnublar.client.renderer.dinosaurs;

import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/** Velociraptor renderer; only the adult-scale offset differs from the base. */
public class VelociraptorRenderer extends DinosaurRenderer {

    private static final float ADULT_SCALE_OFFSET = 0.6F;

    public VelociraptorRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel model) {
        super(renderManager, model, ADULT_SCALE_OFFSET);
    }
}
