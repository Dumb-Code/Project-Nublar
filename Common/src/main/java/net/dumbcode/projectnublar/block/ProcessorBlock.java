package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.block.api.MultiEntityBlock;
import net.dumbcode.projectnublar.block.entity.ProcessorBlockEntity;
import net.dumbcode.projectnublar.client.ModShapes;
import net.dumbcode.projectnublar.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ProcessorBlock extends MultiEntityBlock {

    public ProcessorBlock(Properties properties, int rows, int columns, int depth) {
        super(properties, rows, columns, depth);
    }
    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ProcessorBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockInit.PROCESSOR_BLOCK_ENTITY.get(), (world, pos, pState, be) -> be.tick(world, pos, pState, be));
    }

    @Override
    public VoxelShape getShapeForDirection(Direction direction) {
        return switch (direction) {
            case SOUTH -> ModShapes.PROCESSOR_SOUTH;
            case EAST -> ModShapes.PROCESSOR_EAST;
            case WEST -> ModShapes.PROCESSOR_WEST;
            default -> ModShapes.PROCESSOR_NORTH;
        };
    }
}
