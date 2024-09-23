package net.dumbcode.projectnublar.client.renderer;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.ProcessorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.util.Objects;

public class ProcessorRenderer extends GeoBlockRenderer<ProcessorBlockEntity> {
    public ProcessorRenderer() {
        super(new DefaultedBlockGeoModel<>(Constants.modLoc( "processor")) {
        });
    }

    @Override
    public void renderRecursively(PoseStack poseStack, ProcessorBlockEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        switch (animatable.getMaxFluidLevel()) {
            case 8000:
                if(Objects.equals(bone.getName(), "diamond_tanks") || Objects.equals(bone.getName(), "gold_tanks") || Objects.equals(bone.getName(), "iron_tanks")){
                    bone.setHidden(false);
                }
                break;
            case 4000:
                if(Objects.equals(bone.getName(), "gold_tanks") || Objects.equals(bone.getName(), "iron_tanks")){
                    bone.setHidden(false);
                }
                if(Objects.equals(bone.getName(), "diamond_tanks")){
                    bone.setHidden(true);
                }
                break;
            case 3000:
                if(Objects.equals(bone.getName(), "iron_tanks")){
                    bone.setHidden(false);
                }
                if(Objects.equals(bone.getName(), "diamond_tanks") || Objects.equals(bone.getName(), "gold_tanks")){
                    bone.setHidden(true);
                }
                break;
            default:
                if(Objects.equals(bone.getName(), "diamond_tanks") || Objects.equals(bone.getName(), "gold_tanks") || Objects.equals(bone.getName(), "iron_tanks")){
                    bone.setHidden(true);
                }
                break;
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public boolean shouldRenderOffScreen(ProcessorBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return super.getViewDistance();
    }

    @Override
    public boolean shouldRender(ProcessorBlockEntity pBlockEntity, Vec3 pCameraPos) {
        return Vec3.atCenterOf(pBlockEntity.getBlockPos()).multiply(1.0D, 0.0D, 1.0D).closerThan(pCameraPos.multiply(1.0D, 0.0D, 1.0D), this.getViewDistance());
    }

}
