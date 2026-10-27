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
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * Draws an electric fence post. Only the base block of a post draws the GeckoLib model, turned about the block's
 * vertical axis by the post's cached rotation. Every post block draws its wires ({@link ElectricWireRenderer}).
 *
 * <p>Render box: NeoForge reads {@code getRenderBoundingBox} (exact); there the method overrides
 * {@code IBlockEntityRendererExtension.getRenderBoundingBox} and javac emits the {@code (BlockEntity)AABB} bridge.
 * Fabric always draws ({@code shouldRenderOffScreen}); EntityCulling's reflective
 * {@code getRenderBoundingBox(BlockEntity)} lookup finds nothing there, which does not matter, because EntityCulling
 * never reads the box while {@code shouldRenderOffScreen()} is true. EntityCulling exact box not reachable on Fabric
 * without a mixin; no mixin added (D-j). Client only. D-j, 2026-10-25.
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

    /**
     * The post's wire box ({@link ElectricWireRenderer#wireBox}) and, on the base block, the post model: the post
     * column ({@code getHeight()} blocks, 3 low security, 8 high security) plus one block on each side and on top,
     * because the model reaches up to 1.12 blocks from the post's centre line at any rotation and up to 0.44 blocks
     * above the column.
     */
    public AABB getRenderBoundingBox(BlockEntityElectricFencePole blockEntity) {
        AABB box = ElectricWireRenderer.wireBox(blockEntity);
        if (isBasePole(blockEntity)) {
            int height = ((ElectricFencePostBlock) blockEntity.getBlockState().getBlock()).getType().getHeight();
            BlockPos base = blockEntity.getBlockPos();
            box = box.minmax(AABB.encapsulatingFullBlocks(base.offset(-1, 0, -1), base.offset(1, height, 1)));
        }
        return box;
    }

    /** {@code true}: Fabric always draws; NeoForge still tests the render box against the view (D-j). */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    private static boolean isBasePole(BlockEntityElectricFencePole pole) {
        if (!(pole.getBlockState().getBlock() instanceof ElectricFencePostBlock post)) {
            return false;
        }
        return pole.getBlockState().getValue(post.getIndexProperty()) == 0;
    }
}
