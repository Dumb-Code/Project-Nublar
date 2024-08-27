package net.dumbcode.projectnublar.block.api;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IMachineParts {
    NonNullList<ItemStack> getMachineParts();
}
