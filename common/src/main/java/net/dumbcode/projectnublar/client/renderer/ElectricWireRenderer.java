package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.api.geometry.RenderUtils;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class ElectricWireRenderer implements BlockEntityRenderer<BlockEntityElectricFence> {

    @Override
    public void render(BlockEntityElectricFence pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        List<Connection> connections = pBlockEntity.getConnections().stream().toList();
        VertexConsumer consumer = pBuffer.getBuffer(RenderType.leash());
        connections.forEach(connection -> {
//            if (connection.isPowered(pBlockEntity.getLevel())) {
                RenderUtils.drawSpacedCube(pPoseStack,consumer, 1,1,1,1,0x00F000F0, OverlayTexture.NO_OVERLAY,
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

    private void renderLeash(Vec3 in, Vec3 out, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer) {
        pPoseStack.pushPose();
        double d0 = 0;//(double)(Mth.lerp(pPartialTicks, pEntityLiving.yBodyRotO, pEntityLiving.yBodyRot) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
        double d1 = Math.cos(d0) * out.z + Math.sin(d0) * out.x;
        double d2 = Math.sin(d0) * out.z - Math.cos(d0) * out.x;
        double d3 = Mth.lerp((double) pPartialTicks, in.x(), in.x()) + d1;
        double d4 = Mth.lerp((double) pPartialTicks, in.y(), in.y()) + out.y;
        double d5 = Mth.lerp((double) pPartialTicks, in.z(), in.z()) + d2;
        pPoseStack.translate(d1, out.y, d2);
        float f = (float) (in.x - d3);
        float f1 = (float) (in.y - d4);
        float f2 = (float) (in.z - d5);
        float f3 = 0.025F;
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.leash());
        Matrix4f matrix4f = pPoseStack.last().pose();
        float f4 = Mth.invSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
//        BlockPos blockpos = BlockPos.containing(pEntityLiving.getEyePosition(pPartialTicks));
//        BlockPos blockpos1 = BlockPos.containing(pLeashHolder.getEyePosition(pPartialTicks));
        int i = 15;
        int j = 15;
        int k = 15;//pEntityLiving.getLevel().getBrightness(LightLayer.SKY, blockpos);
        int l = 15;//pEntityLiving.getLevel().getBrightness(LightLayer.SKY, blockpos1);

        addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, 1, false);
//        vertexconsumer.vertex(matrix4f, 0, 0, 0).color(1, 1, 1, 1.0F).uv2(k).endVertex();
//        vertexconsumer.vertex(matrix4f, 0, 0, 1).color(1, 1, 1, 1.0F).uv2(k).endVertex();

        addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, 24, true);
//        vertexconsumer.vertex(matrix4f, 0, 1, 1).color(1, 1, 1, 1.0F).uv2(k).endVertex();
//        vertexconsumer.vertex(matrix4f, 1, 1, 0).color(1, 1, 1, 1.0F).uv2(k).endVertex();

        pPoseStack.popPose();
    }

    private static void addVertexPair(VertexConsumer pConsumer, Matrix4f pMatrix, float p_174310_, float p_174311_, float p_174312_, int pEntityBlockLightLevel, int pLeashHolderBlockLightLevel, int pEntitySkyLightLevel, int pLeashHolderSkyLightLevel, float p_174317_, float p_174318_, float p_174319_, float p_174320_, int pIndex, boolean p_174322_) {
        float f = (float) pIndex/24f;
        int i = (int) Mth.lerp(f, (float) pEntityBlockLightLevel, (float) pLeashHolderBlockLightLevel);
        int j = (int) Mth.lerp(f, (float) pEntitySkyLightLevel, (float) pLeashHolderSkyLightLevel);
        int k = LightTexture.pack(i, j);
        float f5 = p_174310_ * f;
        float f6 = p_174311_ > 0.0F ? p_174311_ * f * f : p_174311_ - p_174311_ * (1.0F - f) * (1.0F - f);
        float f7 = p_174312_ * f;
        pConsumer.vertex(pMatrix, f5 - p_174319_, f6 + p_174318_, f7 + p_174320_).color(1, 1, 1, 1.0F).uv2(k).endVertex();
        pConsumer.vertex(pMatrix, f5 + p_174319_, f6 + p_174317_ - p_174318_, f7 - p_174320_).color(1, 1, 1, 1.0F).uv2(k).endVertex();
    }
}
