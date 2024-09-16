package net.dumbcode.projectnublar.block;

import com.google.common.collect.ImmutableMap;
import net.dumbcode.projectnublar.block.api.MultiBlock;
import net.dumbcode.projectnublar.block.api.MultiEntityBlock;
import net.dumbcode.projectnublar.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.client.ModShapes;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class EggPrinterBlock extends MultiEntityBlock {

    public EggPrinterBlock(Properties properties, int rows, int columns, int depth) {
        super(properties, rows, columns, depth);
    }


    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(MultiBlock.getCorePos(pState, pPos));
            if (blockEntity instanceof EggPrinterBlockEntity sbe) {
                if (pPlayer.getItemInHand(pHand).is(ItemInit.LEVELING_SENSOR.get())) {
                    sbe.setSensor(pPlayer.getMainHandItem().copy());
                    pPlayer.getItemInHand(pHand).shrink(1);
                    return InteractionResult.CONSUME;
                }
                if (pPlayer.getItemInHand(pHand).getItem() instanceof ComputerChipItem) {
                    sbe.setChip(pPlayer.getMainHandItem().copy());
                    pPlayer.getItemInHand(pHand).shrink(1);
                    return InteractionResult.CONSUME;
                }
            }
            return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Override
    public VoxelShape getShapeForDirection(Direction direction) {
        return switch (direction) {
            case SOUTH -> ModShapes.EGG_PRINTER_SOUTH;
            case EAST -> ModShapes.EGG_PRINTER_EAST;
            case WEST -> ModShapes.EGG_PRINTER_WEST;
            default -> ModShapes.EGG_PRINTER_NORTH;
        };
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new EggPrinterBlockEntity(blockPos, blockState);
    }

    @Override
    protected ImmutableMap<BlockState, VoxelShape> getShapeForEachState(Function<BlockState, VoxelShape> pShapeGetter) {
        return super.getShapeForEachState(pShapeGetter);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), (world, pos, pState, be) -> be.tick(world, pos, pState, be));
    }
}
