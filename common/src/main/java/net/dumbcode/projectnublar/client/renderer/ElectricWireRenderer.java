package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.api.geometry.RenderUtils;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class ElectricWireRenderer implements BlockEntityRenderer<BlockEntityElectricFence> {

    @Override
    public void render(BlockEntityElectricFence pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if (pBlockEntity.getLevel() == null) {
            return;
        }
        VertexConsumer consumer = pBuffer.getBuffer(RenderType.leash());
        for (Connection connection : pBlockEntity.getConnections()) {
            Connection.CompiledRenderData compiled = connection.compileRenderData(pBlockEntity.getLevel());
            for (float[] data : compiled.connectionData()) {
                drawWire(pPoseStack, consumer, data);
            }
        }
    }

    static void drawWire(PoseStack poseStack, VertexConsumer consumer, float[] data) {
        int light = 0x00F000F0;
        RenderUtils.drawSpacedCube(poseStack, consumer, 1, 1, 1, 1, light, OverlayTexture.NO_OVERLAY,
            data[0], data[1], data[2],
            data[3], data[4], data[5], data[6],
            data[7], data[8], data[9], data[10],
            data[11], data[12], data[13], data[14],
            data[15], data[16], data[17], data[18],
            data[19], data[20], data[21], data[22],
            data[23], data[24], data[25], data[26],
            data[27], data[28], data[29], data[30],
            data[31], data[32], data[33], data[34],
            data[35], data[36], data[37], data[38]);
    }
}
