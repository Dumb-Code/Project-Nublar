package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dumbcode.projectnublar.block.ElectricFencePostBlock;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.client.model.FencePostModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ElectricFenceRenderer extends GeoBlockRenderer<BlockEntityElectricFencePole> {
    public ElectricFenceRenderer() {
        super(new FencePostModel());
    }

    @Override
    public void renderFinal(PoseStack poseStack, BlockEntityElectricFencePole animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (animatable.getLevel() == null) {
            return;
        }
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.leash());
        for (Connection connection : animatable.getConnections()) {
            Connection.CompiledRenderData compiled = connection.compileRenderData(animatable.getLevel());
            for (float[] data : compiled.connectionData()) {
                ElectricWireRenderer.drawWire(poseStack, consumer, data);
            }
        }
    }

    @Override
    public void actuallyRender(PoseStack poseStack, BlockEntityElectricFencePole animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (!isBasePole(animatable)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) animatable.getCachedRotation()));
        poseStack.translate(-0.5, -0.5, -0.5);
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }

    private static boolean isBasePole(BlockEntityElectricFencePole pole) {
        if (!(pole.getBlockState().getBlock() instanceof ElectricFencePostBlock post)) {
            return false;
        }
        return pole.getBlockState().getValue(post.getIndexProperty()) == 0;
    }
}
