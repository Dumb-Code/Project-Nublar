package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.block.api.MultiEntityBlock;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.item.BulbItem;
import net.dumbcode.projectnublar.item.ContainerUpgradeItem;
import net.dumbcode.projectnublar.item.PlantTankItem;
import net.dumbcode.projectnublar.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class IncubatorBlock extends MultiEntityBlock {

    public IncubatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide && pHand == InteractionHand.MAIN_HAND) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            ItemStack stack = pPlayer.getMainHandItem();
            if (blockEntity instanceof IncubatorBlockEntity incubator) {
                if (stack.getItem() instanceof BulbItem) {
                    if(!incubator.getBulbStack().isEmpty()){
                        popResource(pLevel, pPos, incubator.getBulbStack());
                    }
                    incubator.setBulbStack(pPlayer.getMainHandItem().copyWithCount(1));
                    pPlayer.getMainHandItem().shrink(1);
                    return InteractionResult.SUCCESS;
                }
                if (stack.getItem() instanceof ContainerUpgradeItem) {
                    if(!incubator.getContainerStack().isEmpty()){
                        popResource(pLevel, pPos, incubator.getContainerStack());
                    }
                    incubator.setContainerStack(pPlayer.getMainHandItem().copyWithCount(1));
                    pPlayer.getMainHandItem().shrink(1);
                    return InteractionResult.SUCCESS;
                }
                if (stack.getItem() instanceof PlantTankItem) {
                    if(!incubator.getTankStack().isEmpty()){
                        popResource(pLevel, pPos, incubator.getTankStack());
                    }
                    incubator.setTankStack(pPlayer.getMainHandItem().copyWithCount(1));
                    pPlayer.getMainHandItem().shrink(1);
                    return InteractionResult.SUCCESS;
                }
                if(stack.is(ItemInit.INCUBATOR_NEST.get())){
                    if(!incubator.getNestStack().isEmpty()){
                        return InteractionResult.FAIL;
                    }
                    incubator.setNestStack(pPlayer.getMainHandItem().copyWithCount(1));
                    pPlayer.getMainHandItem().shrink(1);
                    return InteractionResult.SUCCESS;
                }
                if(stack.is(ItemInit.INCUBATOR_LID.get())) {
                    if (!incubator.getLidStack().isEmpty() || incubator.getNestStack().isEmpty()) {
                        return InteractionResult.FAIL;
                    }
                    incubator.setLidStack(pPlayer.getMainHandItem().copyWithCount(1));
                    pPlayer.getMainHandItem().shrink(1);
                    return InteractionResult.SUCCESS;
                }
                if (stack.is(ItemInit.INCUBATOR_ARM_BASE.get())){
                    if(!incubator.getBaseStack().isEmpty()){
                        return InteractionResult.FAIL;
                    }
                    incubator.setBaseStack(pPlayer.getMainHandItem().copyWithCount(1));
                    pPlayer.getMainHandItem().shrink(1);
                    return InteractionResult.SUCCESS;
                }
                if (stack.is(ItemInit.INCUBATOR_ARM.get())) {
                    if (!incubator.getArmStack().isEmpty() || incubator.getBaseStack().isEmpty()) {
                        return InteractionResult.FAIL;
                    }
                    incubator.setArmStack(pPlayer.getMainHandItem().copyWithCount(1));
                    pPlayer.getMainHandItem().shrink(1);
                    return InteractionResult.SUCCESS;
                }
                if(incubator.getNestStack().isEmpty()||incubator.getLidStack().isEmpty()||incubator.getBaseStack().isEmpty()||incubator.getArmStack().isEmpty()){
                    return InteractionResult.FAIL;
                }
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    protected void openContainer(Level pLevel, BlockPos pPos, Player pPlayer) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof IncubatorBlockEntity) {
            Services.PLATFORM.openMenu((ServerPlayer) pPlayer, (MenuProvider) blockentity, buf -> {
                ((FriendlyByteBuf) buf).writeBlockPos(pPos);
            });

            //todo: add stat
//            pPlayer.awardStat(getOpenState());
        }
    }

//    @Override
//    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
//        if (!pLevel.isClientSide) {
//            BlockEntity blockEntity = pLevel.getBlockEntity(MultiBlock.getCorePos(pState, pPos));
//            if (blockEntity instanceof SequencerBlockEntity sbe) {
//                if (pPlayer.getItemInHand(pHand).is(ItemInit.SEQUENCER_COMPUTER.get())) {
//                    sbe.setHasComputer(true);
//                    pPlayer.getItemInHand(pHand).shrink(1);
//                    return InteractionResult.CONSUME;
//                }
//                if (pPlayer.getItemInHand(pHand).is(ItemInit.SEQUENCER_DOOR.get())) {
//                    sbe.setHasDoor(true);
//                    pPlayer.getItemInHand(pHand).shrink(1);
//                    return InteractionResult.CONSUME;
//                }
//                if (pPlayer.getItemInHand(pHand).is(ItemInit.SEQUENCER_SCREEN.get())) {
//                    if (!sbe.isHasComputer()) return InteractionResult.FAIL;
//                    sbe.setHasScreen(true);
//                    pPlayer.getItemInHand(pHand).shrink(1);
//                    return InteractionResult.CONSUME;
//
//                }
//            }
//            return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
//        }
//        return InteractionResult.sidedSuccess(pLevel.isClientSide);
//    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new IncubatorBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockInit.INCUBATOR_BLOCK_ENTITY.get(), (world, pos, pState, be) -> be.tick(world, pos, pState, be));
    }
}
