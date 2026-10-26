package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dumbcode.projectnublar.block.ElectricFencePostBlock;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.client.model.FencePostModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Draws an electric fence post. Only the base block of a post draws the GeckoLib model, turned about the block's
 * vertical axis by the post's cached rotation. Every post block draws its wires ({@link ElectricWireRenderer}).
 */
public class ElectricFenceRenderer extends GeoBlockRenderer<BlockEntityElectricFencePole, BlockEntityRenderState> {
    /** Whether this block is the base (index 0) of its post. */
    private static final DataTicket<Boolean> BASE_POLE = DataTicket.create("projectnublar_fence_base_pole", Boolean.class);
    /** The post's cached rotation in degrees. */
    private static final DataTicket<Float> ROTATION = DataTicket.create("projectnublar_fence_rotation", Float.class);

    public ElectricFenceRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new FencePostModel());
    }

    @Override
    public void addRenderData(BlockEntityElectricFencePole animatable, Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
        renderState.addGeckolibData(BASE_POLE, isBasePole(animatable));
        renderState.addGeckolibData(ROTATION, (float) animatable.getCachedRotation());
        renderState.addGeckolibData(ElectricWireRenderer.WIRES, ElectricWireRenderer.wireData(animatable));
    }

    @Override
    public void submit(BlockEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (renderState.getGeckolibData(BASE_POLE)) {
            super.submit(renderState, poseStack, submitNodeCollector, camera);
        }
        ElectricWireRenderer.submitWires(renderState, poseStack, submitNodeCollector);
    }

    /**
     * Turns the model by the post's rotation. GeckoLib has already moved the pose to the block's bottom centre,
     * so a Y rotation here turns the model about the block's vertical centre line, as before.
     */
    @Override
    public void adjustRenderPose(RenderPassInfo<BlockEntityRenderState> renderPassInfo) {
        renderPassInfo.poseStack().mulPose(Axis.YP.rotationDegrees(renderPassInfo.getGeckolibData(ROTATION)));
        super.adjustRenderPose(renderPassInfo);
    }

    private static boolean isBasePole(BlockEntityElectricFencePole pole) {
        if (!(pole.getBlockState().getBlock() instanceof ElectricFencePostBlock post)) {
            return false;
        }
        return pole.getBlockState().getValue(post.getIndexProperty()) == 0;
    }
}
