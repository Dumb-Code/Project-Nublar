package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class TestTubeItem extends DNADataItem {
    public TestTubeItem(Properties properties) {
        super(properties);
    }


    @Override
    public Component getName(ItemStack stack) {
        CompoundTag customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if(customData.contains("DNAData")) {
            DNAData data = DNAData.fromStack(stack);
            return Component.translatable("item.projectnublar.test_tube2", data.getFormattedType());
        }
        if(customData.contains("DinoData")) {
            DinoData data = DinoData.fromStack(stack);
            return Component.translatable("item.projectnublar.test_tube2", data.getFormattedType());
        }
        return super.getName(stack);
    }
}
