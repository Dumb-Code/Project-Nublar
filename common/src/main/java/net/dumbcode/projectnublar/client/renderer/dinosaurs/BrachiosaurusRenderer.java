package net.dumbcode.projectnublar.client.renderer.dinosaurs;

import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/** Brachiosaurus renderer; only the adult-scale offset differs from the base. */
public class BrachiosaurusRenderer extends DinosaurRenderer {

    private static final float ADULT_SCALE_OFFSET = 2.5F;

    public BrachiosaurusRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel model) {
        super(renderManager, model, ADULT_SCALE_OFFSET);
    }
}
