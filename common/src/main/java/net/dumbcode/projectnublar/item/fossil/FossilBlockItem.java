package net.dumbcode.projectnublar.item.fossil;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.Dinosaur;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilPieces;
import net.dumbcode.projectnublar.api.fossil.FossilBase;
import net.dumbcode.projectnublar.api.fossil.FossilBlockStates;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.DinosaurInit;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Properties;

public class FossilBlockItem extends BlockItem {


    private final Quality quality;


    public FossilBlockItem(Block block,Properties properties, Quality quality) {
        super(block, properties);
        this.quality = quality;
    }
///TODO clean up this class not all of this is needed im pretty sure.
    public Quality getQuality() {return this.quality;}
    private static <T extends Comparable<T>> BlockState updateState(BlockState state, Property<T> property, String valueIdentifier) {
        return (BlockState)property.getValue(valueIdentifier).map((p_40592_) -> (BlockState)state.setValue(property, p_40592_)).orElse(state);
    }
    private BlockState updateBlockStateFromTag(BlockPos pos, Level level, ItemStack stack, BlockState state) {
        BlockState blockstate = state;
        CompoundTag compoundtag = stack.getTag();
        if (compoundtag != null) {
            CompoundTag compoundtag1 = compoundtag.getCompound("BlockStateTag");
            StateDefinition<Block, BlockState> statedefinition = state.getBlock().getStateDefinition();

            for(String s : compoundtag1.getAllKeys()) {
                Property<?> property = statedefinition.getProperty(s);
                if (property != null) {
                    String s1 = compoundtag1.get(s).getAsString();
                    blockstate = updateState(blockstate, property, s1);
                    blockstate = blockstate.setValue(FossilBlock.QUALITY_PROPERTY,this.quality);
                }
            }
        }

        if (blockstate != state) {
            level.setBlock(pos, blockstate, 2);
        }

        blockstate.setValue(FossilBlock.QUALITY_PROPERTY, this.quality);

        return blockstate;
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
            FossilBlock fossilBlock = (FossilBlock) this.getBlock();
            BlockState blockstate = fossilBlock.defaultBlockState().setValue(FossilBlock.QUALITY_PROPERTY, this.quality);
            return blockstate != null && this.canPlace(context, blockstate) ? blockstate : null;

    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (!this.getBlock().isEnabled(context.getLevel().enabledFeatures())) {
            return InteractionResult.FAIL;
        } else if (!context.canPlace()) {
            return InteractionResult.FAIL;
        } else {
            BlockPlaceContext blockplacecontext = this.updatePlacementContext(context);
            if (blockplacecontext == null) {
                return InteractionResult.FAIL;
            } else {
                BlockState blockstate = this.getPlacementState(blockplacecontext);
                if (blockstate == null) {
                    return InteractionResult.FAIL;
                } else if (!this.placeBlock(blockplacecontext, blockstate)) {
                    return InteractionResult.FAIL;
                } else {
                    BlockPos blockpos = blockplacecontext.getClickedPos();
                    Level level = blockplacecontext.getLevel();
                    Player player = blockplacecontext.getPlayer();
                    ItemStack itemstack = blockplacecontext.getItemInHand();
                    BlockState blockstate1 = level.getBlockState(blockpos);
                    if (blockstate1.is(blockstate.getBlock())) {
                        blockstate1 = this.updateBlockStateFromTag(blockpos, level, itemstack, blockstate1);
                        blockstate1.setValue(FossilBlock.QUALITY_PROPERTY, this.quality);
                        this.updateCustomBlockEntityTag(blockpos, level, player, itemstack, blockstate1);
                        blockstate1.getBlock().setPlacedBy(level, blockpos, blockstate1, player, itemstack);
                        if (player instanceof ServerPlayer) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
                        }
                    }

                    SoundType soundtype = blockstate1.getSoundType();
                    level.playSound(player, blockpos, this.getPlaceSound(blockstate1), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockstate1));
                    if (player == null || !player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }

                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
    }
}
