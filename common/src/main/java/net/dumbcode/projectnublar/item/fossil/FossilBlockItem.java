package net.dumbcode.projectnublar.item.fossil;

import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** The item of a fossil ore at one quality; it places the block with that quality. */
public class FossilBlockItem extends BlockItem {
    private final Quality quality;

    public FossilBlockItem(Block block, Properties properties, Quality quality) {
        super(block, properties);
        this.quality = quality;
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = getBlock().defaultBlockState().setValue(FossilBlock.QUALITY, quality);
        return canPlace(context, state) ? state : null;
    }

    /** The block name with the quality in front, through the {@code fossil_ore} lang key. */
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.projectnublar.fossil_ore",
                Component.translatable("quality.projectnublar." + quality.getName()), super.getName(stack));
    }
}
