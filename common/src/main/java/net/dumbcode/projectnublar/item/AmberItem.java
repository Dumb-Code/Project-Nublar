package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class AmberItem extends DNADataItem {
    public AmberItem(Properties $$0) {
        super($$0);
    }

    @Override
    public Component getName(ItemStack stack) {
        if(stack.has(DataComponents.CUSTOM_DATA)){
            DNAData data = DNAData.fromStack(stack);
            return Component.translatable("item." + Constants.MODID + ".amber", data.getFormattedType());
        }
        return super.getName(stack);
    }

}
