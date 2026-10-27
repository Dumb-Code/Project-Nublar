package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.api.geometry.RenderUtils;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the wires of an electric fence block with the leash render type, at full brightness. The wire geometry is
 * read from the fence's connections at extraction ({@link #WIRES}) and drawn at submit.
 * {@link ElectricFenceRenderer} draws the wires of a post the same way.
 *
 * <p>Render box: NeoForge reads {@code getRenderBoundingBox} (exact); there the method overrides
 * {@code IBlockEntityRendererExtension.getRenderBoundingBox} and javac emits the {@code (BlockEntity)AABB} bridge.
 * Fabric always draws ({@code shouldRenderOffScreen}); EntityCulling's reflective
 * {@code getRenderBoundingBox(BlockEntity)} lookup finds nothing there, which does not matter, because EntityCulling
 * never reads the box while {@code shouldRenderOffScreen()} is true. EntityCulling exact box not reachable on Fabric
 * without a mixin; no mixin added (D-j). Client only. D-j, 2026-10-25.
 */
public class ElectricWireRenderer implements BlockEntityRenderer<BlockEntityElectricFence, BlockEntityRenderState> {
    /** One {@code float[]} per wire segment, as {@link Connection.CompiledRenderData#connectionData()} gives it. */
    static final DataTicket<float[][]> WIRES = DataTicket.create("projectnublar_fence_wires", float[][].class);
    private static final float[][] NO_WIRES = new float[0][];

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityElectricFence blockEntity, BlockEntityRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
        state.addGeckolibData(WIRES, wireData(blockEntity));
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        submitWires(state, poseStack, submitNodeCollector);
    }

    /** The wire box of this fence block ({@link #wireBox}). */
    public AABB getRenderBoundingBox(BlockEntityElectricFence blockEntity) {
        return wireBox(blockEntity);
    }

    /** {@code true}: Fabric always draws; NeoForge still tests the render box against the view (D-j). */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    /**
     * The block of {@code fence} and the end blocks ({@code from}, {@code to}) of each of its connections. Each wire
     * runs between its end blocks at a height inside them and is clipped to this block, so it lies inside this box.
     */
    static AABB wireBox(BlockEntityElectricFence fence) {
        AABB box = new AABB(fence.getBlockPos());
        for (Connection connection : fence.getConnections()) {
            box = box.minmax(AABB.encapsulatingFullBlocks(connection.getFrom(), connection.getTo()));
        }
        return box;
    }

    /** The wire segments of every connection of {@code fence}; none while it has no level. */
    static float[][] wireData(BlockEntityElectricFence fence) {
        if (fence.getLevel() == null) {
            return NO_WIRES;
        }
        List<float[]> wires = new ArrayList<>();
        for (Connection connection : fence.getConnections()) {
            wires.addAll(connection.compileRenderData(fence.getLevel()).connectionData());
        }
        return wires.toArray(NO_WIRES);
    }

    /** Submits the wires stored in {@code state} ({@link #WIRES}) at the current pose. */
    static void submitWires(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        float[][] wires = state.getOrDefaultGeckolibData(WIRES, NO_WIRES);
        if (wires.length == 0) {
            return;
        }
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.leash(), (pose, consumer) -> {
            PoseStack wireStack = new PoseStack();
            wireStack.last().set(pose);
            for (float[] data : wires) {
                drawWire(wireStack, consumer, data);
            }
        });
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
