package net.dumbcode.projectnublar.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.TagInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DinosaurFeederBlock extends BaseEntityBlock {
    /** Codec of this block type: the properties plus the texture path. */
    public static final MapCodec<DinosaurFeederBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    propertiesCodec(),
                    Codec.STRING.fieldOf("path").forGetter(block -> block.path))
            .apply(i, DinosaurFeederBlock::new));

    public final String path;
    public DinosaurFeederBlock(Properties properties, String path) {
        super(properties);
        this.path = path;
    }

    @Override
    protected MapCodec<DinosaurFeederBlock> codec() {
        return CODEC;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!level.isClientSide()){
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if(blockEntity instanceof DinosaurFeederBlockEntity fbe) {
                if ((player.getItemInHand(hand).is(TagInit.FEEDER_MEAT) && fbe.getItem(0).isEmpty()) || (!fbe.getItem(0).isEmpty() && player.getItemInHand(hand).is(fbe.getItem(0).getItem()))) {
                    fbe.setItem(0, player.getItemInHand(hand));
                    player.getItemInHand(hand).shrink(1);
                    return InteractionResult.SUCCESS_SERVER;
                }
                if ((player.getItemInHand(hand).is(TagInit.FEEDER_MEAT) && fbe.getItem(1).isEmpty()) || (!fbe.getItem(1).isEmpty() && player.getItemInHand(hand).is(fbe.getItem(1).getItem()))) {
                    fbe.setItem(1, player.getItemInHand(hand));
                    player.getItemInHand(hand).shrink(1);
                    return InteractionResult.SUCCESS_SERVER;
                }
                if ((player.getItemInHand(hand).is(TagInit.FEEDER_MEAT) && fbe.getItem(2).isEmpty()) || (!fbe.getItem(2).isEmpty() && player.getItemInHand(hand).is(fbe.getItem(2).getItem()))) {
                    fbe.setItem(2, player.getItemInHand(hand));
                    player.getItemInHand(hand).shrink(1);
                    return InteractionResult.SUCCESS_SERVER;
                }
                if (player.getItemInHand(hand).isEmpty() && !fbe.coolDown && !player.isCrouching()) {
                    fbe.dispenseFood();
                    return InteractionResult.SUCCESS_SERVER;
                }
                if (player.getItemInHand(hand).isEmpty() && player.isCrouching()) {
                    if(!fbe.getItem(0).isEmpty()){
                        ItemStack stack = new ItemStack(fbe.getItem(0).getItem());
                        ItemStack copy = stack.copy();
                        copy.setCount(stack.getCount());
                        player.setItemInHand(hand, copy);
                        fbe.setItem(0, ItemStack.EMPTY);
                        return InteractionResult.SUCCESS_SERVER;
                    }
                    if(!fbe.getItem(1).isEmpty()){
                        ItemStack stack = new ItemStack(fbe.getItem(1).getItem());
                        ItemStack copy = stack.copy();
                        copy.setCount(stack.getCount());
                        player.setItemInHand(hand, copy);
                        fbe.setItem(1, ItemStack.EMPTY);
                        return InteractionResult.SUCCESS_SERVER;
                    }
                    if(!fbe.getItem(2).isEmpty()){
                        ItemStack stack = new ItemStack(fbe.getItem(2).getItem());
                        ItemStack copy = stack.copy();
                        copy.setCount(stack.getCount());
                        player.setItemInHand(hand, copy);
                        fbe.setItem(2, ItemStack.EMPTY);
                        return InteractionResult.SUCCESS_SERVER;
                    }
                }
            }

        }
        if(level.isClientSide()){
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof DinosaurFeederBlockEntity fbe) {
                if (player.getItemInHand(hand).is(Items.PORKCHOP)|| player.getItemInHand(hand).is(Items.BEEF)|| player.getItemInHand(hand).is(Items.CHICKEN)) {
                    if(!fbe.shouldDisplayFood){
                        fbe.setTexture(true);
                    }
                }
            }
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hit);
    }

    /**
     * The block model has only a particle texture, so the chunk mesh draws nothing and the block
     * entity renderer draws the feeder. 26.2 dropped {@code ENTITYBLOCK_ANIMATED}; vanilla chests
     * moved to {@code MODEL} with the same kind of particle-only model.
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DinosaurFeederBlockEntity(blockPos,blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockInit.FEEDER_BLOCK_ENTITY.get(),(world,pos,pState,be)-> be.tick(world,pos,pState,be));
    }
}
