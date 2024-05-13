package net.dumbcode.projectnublar.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CloneDisplaySlot extends Slot {
    Slot originSlot;
    public CloneDisplaySlot(Slot originSlot,  int pX, int pY) {
        super(originSlot.container, originSlot.getContainerSlot(), pX, pY);
        this.originSlot = originSlot;
    }

    @Override
    public ItemStack getItem() {
        return originSlot.getItem();
    }

    @Override
    public boolean hasItem() {
        return originSlot.hasItem();
    }


    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return false;
    }
}
