package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.ProcessorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * Draws the Processor and shows its tank upgrades by the tank capacity: 8000 shows the iron, gold and diamond
 * tanks, 4000 the iron and gold tanks, 3000 the iron tanks, any other capacity none.
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
}
