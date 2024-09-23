package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dumbcode.projectnublar.api.Genes;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DinosaurRenderer extends GeoEntityRenderer<Dinosaur> {
    public DinosaurRenderer(EntityRendererProvider.Context renderManager, GeoModel<Dinosaur> model) {
        super(renderManager, model);
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, Dinosaur animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        float scale = ((float)animatable.getDinoData().getGeneValue(Genes.SIZE)/100) + 1.0f;
        super.scaleModelForRender(scale, scale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
