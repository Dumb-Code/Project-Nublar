package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.api.multiblock.MultiEntityBlock;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

/**
 * Draws the Incubator: translucent, and each part shows only while its item is in the incubator.
 *
 * <p>Render box: NeoForge reads {@code getRenderBoundingBox} (exact); there the method overrides
 * {@code IBlockEntityRendererExtension.getRenderBoundingBox} and javac emits the {@code (BlockEntity)AABB} bridge.
 * Fabric always draws ({@code shouldRenderOffScreen}); EntityCulling's reflective
 * {@code getRenderBoundingBox(BlockEntity)} lookup finds nothing there, which does not matter, because EntityCulling
 * never reads the box while {@code shouldRenderOffScreen()} is true. EntityCulling exact box not reachable on Fabric
 * without a mixin; no mixin added (D-j). Client only. D-j, 2026-10-25.
 */
public class IncubatorRenderer extends GeoBlockRenderer<IncubatorBlockEntity, BlockEntityRenderState> {
    private static final DataTicket<Boolean> HAS_NEST = DataTicket.create("projectnublar_incubator_has_nest", Boolean.class);
    private static final DataTicket<Boolean> HAS_LID = DataTicket.create("projectnublar_incubator_has_lid", Boolean.class);
    private static final DataTicket<Boolean> HAS_BASE = DataTicket.create("projectnublar_incubator_has_base", Boolean.class);
    private static final DataTicket<Boolean> HAS_ARM = DataTicket.create("projectnublar_incubator_has_arm", Boolean.class);

    public IncubatorRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(Identifier.fromNamespaceAndPath(Constants.MODID,"incubator")));
    }

    @Override
    public void addRenderData(IncubatorBlockEntity animatable, Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
        renderState.addGeckolibData(HAS_NEST, !animatable.getNestStack().isEmpty());
        renderState.addGeckolibData(HAS_LID, !animatable.getLidStack().isEmpty());
        renderState.addGeckolibData(HAS_BASE, !animatable.getBaseStack().isEmpty());
        renderState.addGeckolibData(HAS_ARM, !animatable.getArmStack().isEmpty());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<BlockEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        setHidden(snapshots, "nest", !renderPassInfo.getGeckolibData(HAS_NEST));
        setHidden(snapshots, "cover", !renderPassInfo.getGeckolibData(HAS_LID));
        setHidden(snapshots, "arm_base", !renderPassInfo.getGeckolibData(HAS_BASE));
        setHidden(snapshots, "RoboticHand1", !renderPassInfo.getGeckolibData(HAS_ARM));
    }

    /** Hides or shows a bone and its children, as GeckoLib 4's {@code GeoBone.setHidden} did. */
    private static void setHidden(BoneSnapshots snapshots, String bone, boolean hidden) {
        snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(hidden).skipChildrenRender(hidden));
    }

    @Override
    public RenderType getRenderType(BlockEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    /**
     * The Incubator's footprint ({@link MultiEntityBlock#getFootprint}) plus half a block on every side, so the box
     * also holds the parts of the model that overhang the block grid.
     */
    public AABB getRenderBoundingBox(IncubatorBlockEntity blockEntity) {
        MultiEntityBlock block = (MultiEntityBlock) blockEntity.getBlockState().getBlock();
        return block.getFootprint(blockEntity.getBlockPos(), blockEntity.getBlockState()).inflate(0.5);
    }

    /** {@code true}: Fabric always draws; NeoForge still tests the render box against the view (D-j). */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
