package net.dumbcode.projectnublar.client;

import net.minecraft.world.item.ItemStack;

public interface IStackSensitive {
    void setStack(ItemStack stack);
    ItemStack getStack();
}
