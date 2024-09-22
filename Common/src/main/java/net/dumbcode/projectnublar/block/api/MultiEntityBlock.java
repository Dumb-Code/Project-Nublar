package net.dumbcode.projectnublar.block.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class MultiEntityBlock extends BaseEntityBlock implements MultiBlock {


    public MultiEntityBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ROWS, 0)
                .setValue(COLUMNS, 0)
                .setValue(FACING, Direction.NORTH)
                .setValue(DEPTH, 0));
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
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 2; c++) {
                for (int d = 0; d < 2; d++) {
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
