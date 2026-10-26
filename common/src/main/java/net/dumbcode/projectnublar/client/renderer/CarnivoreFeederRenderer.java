package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.DinosaurFeederBlock;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.client.model.CarnivoreFeederModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Draws the feeder: {@code textures/block/<feeder path>/full.png} while it shows and dispenses food, else
 * {@code empty.png}.
 */
public class CarnivoreFeederRenderer extends GeoBlockRenderer<DinosaurFeederBlockEntity, BlockEntityRenderState> {
    /** The texture picked from the feeder's state at extraction. */
    private static final DataTicket<Identifier> TEXTURE = DataTicket.create("projectnublar_feeder_texture", Identifier.class);

    public CarnivoreFeederRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new CarnivoreFeederModel());
    }

    @Override
    public void addRenderData(DinosaurFeederBlockEntity animatable, Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
        DinosaurFeederBlock block = (DinosaurFeederBlock) animatable.getBlockState().getBlock();
        String path = block.path;
        if(animatable.shouldDisplayFood && animatable.shouldDispenseFood) {
            renderState.addGeckolibData(TEXTURE, Constants.modLoc("textures/block/"+ path +"/full.png"));
        } else {
            renderState.addGeckolibData(TEXTURE, Constants.modLoc("textures/block/"+ path +"/empty.png"));
        }
    }

    @Override
    public Identifier getTextureLocation(BlockEntityRenderState renderState) {
        return renderState.getGeckolibData(TEXTURE);
    }
}
