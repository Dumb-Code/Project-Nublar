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
import net.minecraft.world.phys.AABB;

/**
 * Draws the feeder: {@code textures/block/<feeder path>/full.png} while it shows and dispenses food, else
 * {@code empty.png}.
 *
 * <p>Render box: NeoForge reads {@code getRenderBoundingBox} (exact); there the method overrides
 * {@code IBlockEntityRendererExtension.getRenderBoundingBox} and javac emits the {@code (BlockEntity)AABB} bridge.
 * Fabric always draws ({@code shouldRenderOffScreen}); EntityCulling's reflective
 * {@code getRenderBoundingBox(BlockEntity)} lookup finds nothing there, which does not matter, because EntityCulling
 * never reads the box while {@code shouldRenderOffScreen()} is true. EntityCulling exact box not reachable on Fabric
 * without a mixin; no mixin added (D-j). Client only. D-j, 2026-10-25.
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

    /**
     * The model's space, centred on the feeder block: 4 blocks along x, because the open doors slide out to 2 blocks
     * from the centre; 3 blocks along z (the frame); from 1 block below the block (the food panel hangs 0.875 below)
     * to its top.
     */
    public AABB getRenderBoundingBox(DinosaurFeederBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1.5, 0, 1).expandTowards(0, -1, 0);
    }

    /** {@code true}: Fabric always draws; NeoForge still tests the render box against the view (D-j). */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
