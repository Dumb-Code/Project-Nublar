package net.dumbcode.projectnublar.item.api;

import net.dumbcode.projectnublar.block.api.MultiBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MultiBlockItem extends BlockItem {
    private final int rows;
    private final int columns;
    private final int depth;

    public MultiBlockItem(Block block, Properties properties, int rows, int columns, int depth) {
        super(block, properties);
        this.rows = rows;
        this.columns = columns;
        this.depth = depth;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        return super.getBarWidth(pStack);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if(context.getClickedFace()!= Direction.UP)
            return false;
        Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                for (int k = 0; k < depth; k++) {
                    if(!context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace(), i).relative(direction.getCounterClockWise(), j).relative(direction.getOpposite(), k)).canBeReplaced()) {
                        return false;
                    }

                }
            }
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                for (int k = 0; k < depth; k++) {
                    if (!context.getLevel().setBlock(context.getClickedPos().relative(context.getClickedFace(), i).relative(direction.getCounterClockWise(), j).relative(direction.getOpposite(), k),
                            state.setValue(MultiBlock.DEPTH, k)
                                    .setValue(MultiBlock.COLUMNS, j)
                                    .setValue(MultiBlock.ROWS, i)
                            ,
                            Block.UPDATE_ALL_IMMEDIATE)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
