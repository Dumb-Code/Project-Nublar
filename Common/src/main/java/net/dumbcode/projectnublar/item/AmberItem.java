package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class AmberItem extends DNADataItem {
    public AmberItem(Properties $$0) {
        super($$0);
    }

    @Override
    public Component getName(ItemStack stack) {
        if(stack.hasTag()){
            DNAData data = DNAData.loadFromNBT(stack.getTag().getCompound("DNAData"));
            return Component.translatable("item." + Constants.MODID + ".amber", data.getFormattedType());
        }
        return super.getName(stack);
    }

}
