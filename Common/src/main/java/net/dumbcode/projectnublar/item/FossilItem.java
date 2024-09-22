package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FossilItem extends DNADataItem {
    public FossilItem(Properties properties) {
        super(properties);
    }


    @Override
    public Component getName(ItemStack stack) {
        if(stack.hasTag()){
            DNAData data = DNAData.loadFromNBT(stack.getTag().getCompound("DNAData"));
            return Component.translatable("item." + Constants.MODID + ".fossil", data.getFormattedType(), Component.translatable("piece.projectnublar." + data.getFossilPiece().name())).withStyle(data.getQuality().getColor());
        }
        return super.getName(stack);
    }

}
