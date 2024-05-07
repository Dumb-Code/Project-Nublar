package com.nyfaria.projectnublar.block;

import com.nyfaria.projectnublar.block.api.MultiBlock;
import com.nyfaria.projectnublar.block.entity.ProcessorBlockEntity;
import com.nyfaria.projectnublar.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProcessorBlock extends BaseEntityBlock implements MultiBlock {

    public static DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ProcessorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MultiBlock.ROWS, 0).setValue(MultiBlock.COLUMNS, 0).setValue(FACING, Direction.NORTH).setValue(MultiBlock.DEPTH, 0));
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            Direction direction = pState.getValue(FACING);
            BlockPos corePos = pPos.relative(direction, pState.getValue(MultiBlock.DEPTH)).relative(direction.getClockWise(), pState.getValue(MultiBlock.COLUMNS)).relative(Direction.UP, -pState.getValue(MultiBlock.ROWS));
            this.openContainer(pLevel, corePos, pPlayer);
            return InteractionResult.CONSUME;
        }

    }

    protected void openContainer(Level pLevel, BlockPos pPos, Player pPlayer) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof ProcessorBlockEntity) {
            pPlayer.openMenu((MenuProvider) blockentity);
            //todo: add stat
//            pPlayer.awardStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
        $$0.add(MultiBlock.ROWS, MultiBlock.COLUMNS, MultiBlock.DEPTH, FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getValue(MultiBlock.ROWS) == 0 && pState.getValue(MultiBlock.COLUMNS) == 0 && pState.getValue(MultiBlock.DEPTH) == 0) {
            if (!pState.is(pNewState.getBlock())) {
                BlockEntity blockentity = pLevel.getBlockEntity(pPos);
                if (blockentity instanceof Container) {
                    Containers.dropContents(pLevel, pPos, (Container) blockentity);
                    pLevel.updateNeighbourForOutputSignal(pPos, this);
                }

            }
        }
        //break the other blocks
        Direction direction = pState.getValue(FACING);
        BlockPos corePos = pPos.relative(direction, pState.getValue(MultiBlock.DEPTH)).relative(direction.getClockWise(), pState.getValue(MultiBlock.COLUMNS)).relative(Direction.UP, -pState.getValue(MultiBlock.ROWS));
        //use r c d for loops
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 2; c++) {
                for (int d = 0; d < 2; d++) {
                    BlockPos blockPos = corePos.relative(direction.getOpposite(), d).relative(direction.getCounterClockWise(), c).relative(Direction.UP, r);
                    if (!pLevel.getBlockState(blockPos).isAir()) {
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
        if (blockState.getValue(MultiBlock.ROWS) == 0 && blockState.getValue(MultiBlock.COLUMNS) == 0 && blockState.getValue(MultiBlock.DEPTH) == 0)
            return new ProcessorBlockEntity(blockPos, blockState);
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockInit.PROCESSOR_BLOCK_ENTITY.get(), (world, pos, pState, be) -> be.tick(world, pos, pState, be));
    }
    public static BlockPos getCorePos(BlockState pState, BlockPos pPos){
        Direction direction = pState.getValue(FACING);
        BlockPos corePos = pPos.relative(direction, pState.getValue(MultiBlock.DEPTH)).relative(direction.getClockWise(), pState.getValue(MultiBlock.COLUMNS)).relative(Direction.UP, -pState.getValue(MultiBlock.ROWS));
        return corePos;
    }
    record Blah<T,S,R>(T a, S b, R c){
        static List<Blah<?,?,?>> blahs(){
            Blah<?,?,?> blah2 = new Blah(1,2,4);
            Blah<?,?,?> blah1 = new Blah("",1, ItemStack.EMPTY);
            List<Blah<?,?,?>> blah = new ArrayList<>();
            blah.add(blah1);
            blah.add(blah2);
            return blah;
        }
    }
}
