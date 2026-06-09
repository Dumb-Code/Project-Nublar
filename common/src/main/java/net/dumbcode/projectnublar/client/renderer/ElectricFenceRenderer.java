package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dumbcode.projectnublar.block.ElectricFencePostBlock;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.api.geometry.RenderUtils;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.client.model.FencePostModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.util.List;

public class ElectricFenceRenderer extends GeoBlockRenderer<BlockEntityElectricFencePole> {
    public ElectricFenceRenderer() {
        super(new FencePostModel());
    }

    @Override
    public void renderFinal(PoseStack poseStack, BlockEntityElectricFencePole animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        List<Connection> connections = animatable.getConnections().stream().toList();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.leash());
        connections.forEach(connection -> {
//            if (connection.isPowered(animatable.getLevel())) {
                RenderUtils.drawSpacedCube(poseStack, consumer, 1,1,1,1,0x00F000F0, OverlayTexture.NO_OVERLAY,
                        connection.getRenderData().data()[0],
                        connection.getRenderData().data()[1], connection.getRenderData().data()[2],
                        connection.getRenderData().data()[3], connection.getRenderData().data()[4],
                        connection.getRenderData().data()[5], connection.getRenderData().data()[6],
                        connection.getRenderData().data()[7], connection.getRenderData().data()[8],
                        connection.getRenderData().data()[9], connection.getRenderData().data()[10],
                        connection.getRenderData().data()[11], connection.getRenderData().data()[12],
                        connection.getRenderData().data()[13], connection.getRenderData().data()[14],
                        connection.getRenderData().data()[15], connection.getRenderData().data()[16],
                        connection.getRenderData().data()[17], connection.getRenderData().data()[18],
                        connection.getRenderData().data()[19], connection.getRenderData().data()[20],
                        connection.getRenderData().data()[21], connection.getRenderData().data()[22],
                        connection.getRenderData().data()[23], connection.getRenderData().data()[24],
                        connection.getRenderData().data()[25], connection.getRenderData().data()[26],
                        connection.getRenderData().data()[27], connection.getRenderData().data()[28],
                        connection.getRenderData().data()[29], connection.getRenderData().data()[30],
                        connection.getRenderData().data()[31], connection.getRenderData().data()[32],
                        connection.getRenderData().data()[33], connection.getRenderData().data()[34],
                        connection.getRenderData().data()[35], connection.getRenderData().data()[36],
                        connection.getRenderData().data()[37], connection.getRenderData().data()[38]
                );
//            }
        });
    }

    @Override
    public void actuallyRender(PoseStack poseStack, BlockEntityElectricFencePole animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if(animatable.getBlockState().getValue(((ElectricFencePostBlock)animatable.getBlockState().getBlock()).getIndexProperty()) == 0) {
            poseStack.pushPose();
            double rotation = animatable.getCachedRotation();
            poseStack.translate(0.5, 0.5, 0.5);
            if(animatable.isFlippedAround()) {
                poseStack.mulPose(Axis.YP.rotationDegrees((float) rotation));
            } else {
                poseStack.mulPose(Axis.YP.rotationDegrees((float) rotation ));
            }
            poseStack.translate(-0.5, -0.5, -0.5);
            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            poseStack.popPose();
        }
    }
}
