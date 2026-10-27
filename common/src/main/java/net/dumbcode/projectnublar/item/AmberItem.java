package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class AmberItem extends DNADataItem {
    public AmberItem(Properties $$0) {
        super($$0);
    }

    @Override
    public Component getName(ItemStack stack) {
        // read only: component values are shared between stack copies
        DNAData data = stack.get(DataComponentInit.DNA_DATA.get());
        if (data != null) {
            return Component.translatable("item." + Constants.MODID + ".amber", data.getFormattedType());
        }
        return super.getName(stack);
    }

}
