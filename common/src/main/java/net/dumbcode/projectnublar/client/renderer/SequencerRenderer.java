package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.api.multiblock.MultiEntityBlock;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the Sequencer and shows its installed parts: bones named with {@code computer}, {@code Door} or
 * {@code monitor} show only while the computer, the door or the screen is installed.
 *
 * <p>Render box: NeoForge reads {@code getRenderBoundingBox} (exact); there the method overrides
 * {@code IBlockEntityRendererExtension.getRenderBoundingBox} and javac emits the {@code (BlockEntity)AABB} bridge.
 * Fabric always draws ({@code shouldRenderOffScreen}); EntityCulling's reflective
 * {@code getRenderBoundingBox(BlockEntity)} lookup finds nothing there, which does not matter, because EntityCulling
 * never applies the box while {@code shouldRenderOffScreen()} is true. EntityCulling exact box not reachable on Fabric
 * without a mixin; no mixin added (D-j). Client only. D-j, 2026-10-25.
 */
public class SequencerRenderer extends GeoBlockRenderer<SequencerBlockEntity, BlockEntityRenderState> {
    private static final DataTicket<Boolean> HAS_COMPUTER = DataTicket.create("projectnublar_sequencer_has_computer", Boolean.class);
    private static final DataTicket<Boolean> HAS_DOOR = DataTicket.create("projectnublar_sequencer_has_door", Boolean.class);
    private static final DataTicket<Boolean> HAS_SCREEN = DataTicket.create("projectnublar_sequencer_has_screen", Boolean.class);

    public SequencerRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(Constants.modLoc( "sequencer")));
    }

    @Override
    public void addRenderData(SequencerBlockEntity animatable, Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
        renderState.addGeckolibData(HAS_COMPUTER, animatable.isHasComputer());
        renderState.addGeckolibData(HAS_DOOR, animatable.isHasDoor());
        renderState.addGeckolibData(HAS_SCREEN, animatable.isHasScreen());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<BlockEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        boolean hasComputer = renderPassInfo.getGeckolibData(HAS_COMPUTER);
        boolean hasDoor = renderPassInfo.getGeckolibData(HAS_DOOR);
        boolean hasScreen = renderPassInfo.getGeckolibData(HAS_SCREEN);
        for (GeoBone bone : renderPassInfo.model().boneLookup().get().values()) {
            String name = bone.name();
            if (name.contains("computer")) {
                setHidden(snapshots, bone, !hasComputer);
            }
            if (name.contains("Door")) {
                setHidden(snapshots, bone, !hasDoor);
            }
            if (name.contains("monitor")) {
                setHidden(snapshots, bone, !hasScreen);
            }
        }
    }

    /** Hides or shows a bone and its children, as GeckoLib 4's {@code GeoBone.setHidden} did. */
    private static void setHidden(BoneSnapshots snapshots, GeoBone bone, boolean hidden) {
        snapshots.get(bone).skipRender(hidden).skipChildrenRender(hidden);
    }

    /**
     * The Sequencer's footprint ({@link MultiEntityBlock#getFootprint}) plus half a block on every side, so the box
     * also holds the parts of the model that overhang the block grid.
     */
    public AABB getRenderBoundingBox(SequencerBlockEntity blockEntity) {
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
    public boolean shouldRender(SequencerBlockEntity blockEntity, Vec3 cameraPosition) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0D, 0.0D, 1.0D)
                .closerThan(cameraPosition.multiply(1.0D, 0.0D, 1.0D), this.getViewDistance());
    }
}
