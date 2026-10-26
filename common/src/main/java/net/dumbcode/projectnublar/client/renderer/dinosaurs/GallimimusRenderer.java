package net.dumbcode.projectnublar.client.renderer.dinosaurs;

import com.geckolib.model.GeoModel;
import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Gallimimus renderer; the adult-scale offset now lives on the entity ({@code adultScaleOffset}). */
public class GallimimusRenderer extends DinosaurRenderer {

    /** The Gallimimus renderer, drawing {@code model}. */
    public GallimimusRenderer(EntityRendererProvider.Context renderManager, GeoModel<Dinosaur> model) {
        super(renderManager, model);
    }
}
