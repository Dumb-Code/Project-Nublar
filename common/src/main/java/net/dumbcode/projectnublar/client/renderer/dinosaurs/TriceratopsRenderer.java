package net.dumbcode.projectnublar.client.renderer.dinosaurs;

import com.geckolib.model.GeoModel;
import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Triceratops renderer; the adult-scale offset now lives on the entity ({@code adultScaleOffset}). */
public class TriceratopsRenderer extends DinosaurRenderer {

    /** The Triceratops renderer, drawing {@code model}. */
    public TriceratopsRenderer(EntityRendererProvider.Context renderManager, GeoModel<Dinosaur> model) {
        super(renderManager, model);
    }
}
