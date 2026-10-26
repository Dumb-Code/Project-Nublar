package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * Draws the Sequencer and shows its installed parts: bones named with {@code computer}, {@code Door} or
 * {@code monitor} show only while the computer, the door or the screen is installed.
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
}
