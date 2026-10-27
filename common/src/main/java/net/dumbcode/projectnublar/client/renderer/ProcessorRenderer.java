package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.api.multiblock.MultiEntityBlock;
import net.dumbcode.projectnublar.block.entity.ProcessorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the Processor and shows its tank upgrades by the tank capacity: 8000 shows the iron, gold and diamond
 * tanks, 4000 the iron and gold tanks, 3000 the iron tanks, any other capacity none.
 *
 * <p>Render box: NeoForge reads {@code getRenderBoundingBox} (exact); there the method overrides
 * {@code IBlockEntityRendererExtension.getRenderBoundingBox} and javac emits the {@code (BlockEntity)AABB} bridge.
 * Fabric always draws ({@code shouldRenderOffScreen}); EntityCulling's reflective
 * {@code getRenderBoundingBox(BlockEntity)} lookup finds nothing there, which does not matter, because EntityCulling
 * never reads the box while {@code shouldRenderOffScreen()} is true. EntityCulling exact box not reachable on Fabric
 * without a mixin; no mixin added (D-j). Client only. D-j, 2026-10-25.
 */
public class ProcessorRenderer extends GeoBlockRenderer<ProcessorBlockEntity, BlockEntityRenderState> {
    /** The Processor's tank capacity, read at extraction. */
    private static final DataTicket<Integer> MAX_FLUID_LEVEL = DataTicket.create("projectnublar_processor_max_fluid_level", Integer.class);

    public ProcessorRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(Constants.modLoc( "processor")));
    }

    @Override
    public void addRenderData(ProcessorBlockEntity animatable, Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
        renderState.addGeckolibData(MAX_FLUID_LEVEL, animatable.getMaxFluidLevel());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<BlockEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        int maxFluidLevel = renderPassInfo.getGeckolibData(MAX_FLUID_LEVEL);
        setHidden(snapshots, "iron_tanks", maxFluidLevel != 8000 && maxFluidLevel != 4000 && maxFluidLevel != 3000);
        setHidden(snapshots, "gold_tanks", maxFluidLevel != 8000 && maxFluidLevel != 4000);
        setHidden(snapshots, "diamond_tanks", maxFluidLevel != 8000);
    }

    /** Hides or shows a bone and its children, as GeckoLib 4's {@code GeoBone.setHidden} did. */
    private static void setHidden(BoneSnapshots snapshots, String bone, boolean hidden) {
        snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(hidden).skipChildrenRender(hidden));
    }

    /**
     * The Processor's footprint ({@link MultiEntityBlock#getFootprint}) plus half a block on every side, so the box
     * also holds the parts of the model that overhang the block grid.
     */
    public AABB getRenderBoundingBox(ProcessorBlockEntity blockEntity) {
        MultiEntityBlock block = (MultiEntityBlock) blockEntity.getBlockState().getBlock();
        return block.getFootprint(blockEntity.getBlockPos(), blockEntity.getBlockState()).inflate(0.5);
    }

    /** {@code true}: Fabric always draws; NeoForge still tests the render box against the view (D-j). */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    /** Draws within {@link #getViewDistance()} blocks measured on the horizontal plane only, as on 1.20.1. */
    @Override
    public boolean shouldRender(ProcessorBlockEntity blockEntity, Vec3 cameraPosition) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0D, 0.0D, 1.0D)
                .closerThan(cameraPosition.multiply(1.0D, 0.0D, 1.0D), this.getViewDistance());
    }
}
