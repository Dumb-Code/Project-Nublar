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
 * The box is this block inflated by {@link #WIRE_MARGIN}: a fence block draws only the wire through its own block,
 * and {@link #WIRE_MARGIN} says how far that wire can reach past it.
 * {@code shouldRenderOffScreen()} is false on both loaders, so the wire is section-culled as on 1.20.1 and a long
 * fence does not put every wire block in the always-drawn list (P138). EntityCulling, if installed on Fabric, finds
 * no {@code getRenderBoundingBox(BlockEntity)} by reflection here, so it culls this block entity with its own default
 * box, the block itself. Client only. D-j, 2026-10-25.
 */
public class ElectricWireRenderer implements BlockEntityRenderer<BlockEntityElectricFence, BlockEntityRenderState> {
    /** One {@code float[]} per wire segment, as {@link Connection.CompiledRenderData#connectionData()} gives it. */
    static final DataTicket<float[][]> WIRES = DataTicket.create("projectnublar_fence_wires", float[][].class);
    private static final float[][] NO_WIRES = new float[0][];
    /**
     * How far past its own block the wires of a fence block can reach: half a block. A straight wire stays inside
     * the block plus the cable thickness (at most 0.044), but a broken wire's bent end is not clipped to the block:
     * {@code Connection.clipBrokenPointToBlock} clips from inside the block, so it never clips. The bent end reaches
     * at most 0.43 block past the block (P145).
     */
    static final double WIRE_MARGIN = 0.5;

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

    /** This fence block inflated by {@link #WIRE_MARGIN}, which holds every wire it draws. */
    public AABB getRenderBoundingBox(BlockEntityElectricFence blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(WIRE_MARGIN);
    }

    /** {@code false}: section-culled as on 1.20.1 (P138); NeoForge also tests the render box against the view. */
    @Override
    public boolean shouldRenderOffScreen() {
        return false;
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
