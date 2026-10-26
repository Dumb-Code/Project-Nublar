package net.dumbcode.projectnublar.client.renderer.dinosaurs;

import com.geckolib.model.GeoModel;
import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Dilophosaurus renderer; the adult-scale offset now lives on the entity ({@code adultScaleOffset}). */
public class DilophosaurusRenderer extends DinosaurRenderer {

    /** The Dilophosaurus renderer, drawing {@code model}. */
    public DilophosaurusRenderer(EntityRendererProvider.Context renderManager, GeoModel<Dinosaur> model) {
        super(renderManager, model);
    }
}
