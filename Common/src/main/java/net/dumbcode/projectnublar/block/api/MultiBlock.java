package net.dumbcode.projectnublar.block.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public interface MultiBlock {
    DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    IntegerProperty ROWS = IntegerProperty.create("rows", 0, 2);
    IntegerProperty COLUMNS = IntegerProperty.create("columns", 0, 1);
    IntegerProperty DEPTH = IntegerProperty.create("depth", 0, 1);


    static BlockPos getCorePos(BlockState pState, BlockPos pPos){
        Direction direction = pState.getValue(FACING);
        return pPos.relative(direction, pState.getValue(MultiBlock.DEPTH)).relative(direction.getClockWise(), pState.getValue(MultiBlock.COLUMNS)).relative(Direction.UP, -pState.getValue(MultiBlock.ROWS));
    }
}
