package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TestTubeItem extends DNADataItem {
    public TestTubeItem(Properties properties) {
        super(properties);
    }


    @Override
    public Component getName(ItemStack stack) {
        // read only: component values are shared between stack copies
        DNAData dnaData = stack.get(DataComponentInit.DNA_DATA.get());
        if (dnaData != null) {
            return Component.translatable("item.projectnublar.test_tube2", dnaData.getFormattedType());
        }
        DinoData dinoData = stack.get(DataComponentInit.DINO_DATA.get());
        if (dinoData != null) {
            return Component.translatable("item.projectnublar.test_tube2", dinoData.getFormattedType());
        }
        return super.getName(stack);
    }
}
