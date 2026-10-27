package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.api.multiblock.MultiEntityBlock;
import net.dumbcode.projectnublar.block.entity.EggPrinterBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

/**
 * Draws the Egg Printer with its {@code egg_printer} GeckoLib model.
 *
 * <p>Render box: NeoForge reads {@code getRenderBoundingBox} (exact); there the method overrides
 * {@code IBlockEntityRendererExtension.getRenderBoundingBox} and javac emits the {@code (BlockEntity)AABB} bridge.
 * Fabric always draws ({@code shouldRenderOffScreen}); EntityCulling's reflective
 * {@code getRenderBoundingBox(BlockEntity)} lookup finds nothing there, which does not matter, because EntityCulling
 * never reads the box while {@code shouldRenderOffScreen()} is true. EntityCulling exact box not reachable on Fabric
 * without a mixin; no mixin added (D-j). Client only. D-j, 2026-10-25.
 */
public class EggPrinterRenderer extends GeoBlockRenderer<EggPrinterBlockEntity, BlockEntityRenderState> {

    public EggPrinterRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(Identifier.fromNamespaceAndPath(Constants.MODID,"egg_printer")));
    }

    /**
     * The Egg Printer's footprint ({@link MultiEntityBlock#getFootprint}) plus half a block on every side, so the box
     * also holds the parts of the model that overhang the block grid.
     */
    public AABB getRenderBoundingBox(EggPrinterBlockEntity blockEntity) {
        MultiEntityBlock block = (MultiEntityBlock) blockEntity.getBlockState().getBlock();
        return block.getFootprint(blockEntity.getBlockPos(), blockEntity.getBlockState()).inflate(0.5);
    }

    /** {@code true}: Fabric always draws; NeoForge still tests the render box against the view (D-j). */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
