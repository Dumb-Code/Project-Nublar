package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TestTubeItem extends DNADataItem {
    public TestTubeItem(Properties properties) {
        super(properties);
    }


    @Override
    public Component getName(ItemStack stack) {
        if(stack.hasTag() && stack.getTag().contains("DNAData")){
            DNAData data = DNAData.loadFromNBT(stack.getTag().getCompound("DNAData"));
            return Component.translatable("item.projectnublar.test_tube2", data.getFormattedType());
        }
        return super.getName(stack);
    }
}
