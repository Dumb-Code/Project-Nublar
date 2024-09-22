package net.dumbcode.projectnublar.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SequencerRenderer extends GeoBlockRenderer<SequencerBlockEntity> {
    public SequencerRenderer() {
        super(new DefaultedBlockGeoModel<>(Constants.modLoc( "sequencer")) {
        });
    }

    @Override
    public void renderRecursively(PoseStack poseStack, SequencerBlockEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if(bone.getName().contains("computer")){
            bone.setHidden(!animatable.isHasComputer());
        }
        if(bone.getName().contains("Door")){
            bone.setHidden(!animatable.isHasDoor());
        }
        if(bone.getName().contains("monitor")){
            bone.setHidden(!animatable.isHasScreen());
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public boolean shouldRenderOffScreen(SequencerBlockEntity pBlockEntity) {

        return true;
    }

    @Override
    public int getViewDistance() {
        return super.getViewDistance();
    }

    @Override
    public boolean shouldRender(SequencerBlockEntity pBlockEntity, Vec3 pCameraPos) {
        return Vec3.atCenterOf(pBlockEntity.getBlockPos()).multiply(1.0D, 0.0D, 1.0D).closerThan(pCameraPos.multiply(1.0D, 0.0D, 1.0D), this.getViewDistance());
    }

}
