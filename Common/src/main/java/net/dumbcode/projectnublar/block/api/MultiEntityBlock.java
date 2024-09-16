package net.dumbcode.projectnublar.block.api;

import net.dumbcode.projectnublar.client.ModShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class MultiEntityBlock extends BaseEntityBlock implements MultiBlock {
    int rows = 0;
    int columns = 0;
    int depth = 0;
    public static Map<BlockState,VoxelShape> SHAPES = new HashMap<>();

    public MultiEntityBlock(Properties properties, int rows, int columns, int depth) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ROWS, 0)
                .setValue(COLUMNS, 0)
                .setValue(FACING, Direction.NORTH)
                .setValue(DEPTH, 0));
        this.rows = rows;
        this.columns = columns;
        this.depth = depth;
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockPos corePos = MultiBlock.getCorePos(pState, pPos);
            this.openContainer(pLevel, corePos, pPlayer);
            return InteractionResult.CONSUME;
        }

    }

    protected void openContainer(Level pLevel, BlockPos pPos, Player pPlayer) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof BaseContainerBlockEntity) {
            pPlayer.openMenu((MenuProvider) blockentity);
            //todo: add stat
//            pPlayer.awardStat(getOpenState());
        }
    }

//    abstract Stat getOpenStat();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
        $$0.add(ROWS, COLUMNS, DEPTH, FACING);
    }

    public abstract VoxelShape getShapeForDirection(Direction direction);
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        int columns = pState.getValue(COLUMNS);
        int rows = pState.getValue(ROWS);
        int depth = pState.getValue(DEPTH);
        Direction direction = pState.getValue(FACING);
        return SHAPES.computeIfAbsent(pState,(state)->switch (pState.getValue(FACING)) {
            case NORTH ->getShapeForDirection(direction).move(columns, -rows, -depth);
            case SOUTH -> getShapeForDirection(direction).move(-columns, -rows, depth);
            case EAST -> getShapeForDirection(direction).move(depth, -rows, columns);
            case WEST -> getShapeForDirection(direction).move(-depth, -rows, -columns);
            default -> getShapeForDirection(direction);
        });
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getValue(ROWS) == 0 && pState.getValue(COLUMNS) == 0 && pState.getValue(DEPTH) == 0) {
            if (!pState.is(pNewState.getBlock())) {
                BlockEntity blockentity = pLevel.getBlockEntity(pPos);
                if (blockentity instanceof Container) {
                    Containers.dropContents(pLevel, pPos, (Container) blockentity);
                    pLevel.updateNeighbourForOutputSignal(pPos, this);
                }
                if(blockentity instanceof IMachineParts){
                    Containers.dropContents(pLevel, pPos, ((IMachineParts) blockentity).getMachineParts());
                }

            }
        }
        //break the other blocks
        Direction direction = pState.getValue(FACING);
        BlockPos corePos = pPos.relative(direction, pState.getValue(DEPTH)).relative(direction.getClockWise(), pState.getValue(COLUMNS)).relative(Direction.UP, -pState.getValue(ROWS));
        //use r c d for loops
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                for (int d = 0; d < depth; d++) {
                    BlockPos blockPos = corePos.relative(direction.getOpposite(), d).relative(direction.getCounterClockWise(), c).relative(Direction.UP, r);
                    if (!pLevel.getBlockState(blockPos).isAir() && pLevel.getBlockState(blockPos).is(this)) {
                        pLevel.destroyBlock(blockPos, false);
                    }
                }
            }
        }


        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(ROWS) == 0 && blockState.getValue(COLUMNS) == 0 && blockState.getValue(DEPTH) == 0)
            return createBlockEntity(blockPos,blockState);
        return null;
    }
    public abstract BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState);



}
