package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.block.api.MultiBlock;
import net.dumbcode.projectnublar.block.api.MultiEntityBlock;
import net.dumbcode.projectnublar.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EggPrinterBlock extends MultiEntityBlock {

    public EggPrinterBlock(Properties properties) {
        super(properties);
    }

//    @Override
//    protected void openContainer(Level pLevel, BlockPos pPos, Player pPlayer) {
//        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
//        if (blockentity instanceof EggPrinterBlockEntity) {
//            Services.PLATFORM.openMenu((ServerPlayer) pPlayer, (MenuProvider) blockentity, buf->{
//                ((FriendlyByteBuf)buf).writeBlockPos(pPos);
//            });
//
//            //todo: add stat
////            pPlayer.awardStat(getOpenState());
//        }
//    }

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
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new EggPrinterBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), (world, pos, pState, be) -> be.tick(world, pos, pState, be));
    }
}
