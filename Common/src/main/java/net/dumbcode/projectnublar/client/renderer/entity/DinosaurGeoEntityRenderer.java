package net.dumbcode.projectnublar.client.renderer.entity;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.model.DinosaurGeoModel;
import net.dumbcode.projectnublar.client.renderer.entity.layer.IKDebugRenderLayer;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DinosaurGeoEntityRenderer extends GeoEntityRenderer<Dinosaur> {
    public DinosaurGeoEntityRenderer(EntityRendererProvider.Context renderManager, DinosaurGeoModel<Dinosaur> model) {
        super(renderManager, model);
        this.addRenderLayer(new IKDebugRenderLayer<>(this));
    }
}
