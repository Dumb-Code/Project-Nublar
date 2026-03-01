package net.dumbcode.projectnublar.item.fossil;

import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilPieces;
import net.dumbcode.projectnublar.api.fossil.FossilBase;
import net.dumbcode.projectnublar.api.fossil.FossilBlockStates;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.DinosaurInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Properties;

public class FossilBlockItem extends BlockItem {


    private @Nullable Quality data;

    public FossilBlockItem(Block block,Properties properties) {
        super(block, properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltips, flag);
        if (!stack.hasTag()||data==null) {
            stack.getOrCreateTag().putString("fossil_blockstate",Quality.COMMON.getName().toLowerCase());
        } else {
         data = Quality.byName(stack.getTag().getString("fossil_blockstate"));
        }
    }


    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        Quality quality;
        if(!context.getItemInHand().hasTag()||data==null){
            context.getItemInHand().getOrCreateTag().putString("fossil_blockstate", Quality.COMMON.getName().toLowerCase());
            quality = Quality.COMMON;
        } else {
            quality = data;
        }
        return this.getBlock().defaultBlockState().setValue(FossilBlock.QUALITY_PROPERTY, quality);
    }





}
