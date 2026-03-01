package net.dumbcode.projectnublar.client.renderer.dinosaurs;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class VelociraptorRenderer extends DinosaurRenderer {

    public VelociraptorRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel model) {
        super(renderManager, model);
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, AbstractDinosaur animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        float adultScale = ((float) animatable.getDinoData().getGeneValue(GeneInit.SIZE.get()) / 100) + 0.6F;
        float babyScale = adultScale * 0.25F;
        float juvenileScale = adultScale * 0.5F;
        float subAdultScale = adultScale * 0.75F;

        float renderScale = switch (animatable.getGrowthStage()) {
            case 1 -> babyScale;
            case 2 -> juvenileScale;
            case 3 -> subAdultScale;
            default -> adultScale;
        };

        if(model.getBone("head").isPresent()) {
            CoreGeoBone head = model.getBone("head").get();
            Vector3f local = new Vector3f(head.getPivotX(),head.getPivotY(),head.getPivotZ());
            Vec3 worldpos =  animatable.position().add(local.x,local.y,local.z);
            animatable.getEntityData().set(AbstractDinosaur.DINOSAUR_HEAD_POS, worldpos.toVector3f());
        }

        super.scaleModelForRender(renderScale, renderScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
