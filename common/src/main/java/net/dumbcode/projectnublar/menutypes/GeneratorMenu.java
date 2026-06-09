package net.dumbcode.projectnublar.menutypes;

import net.dumbcode.projectnublar.block.entity.GeneratorBlockEntity;
import net.dumbcode.projectnublar.registry.MenuTypeInit;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GeneratorMenu extends AbstractContainerMenu {

    private ContainerData data;
    public GeneratorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1), new SimpleContainerData(2));
    }

    public GeneratorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(MenuTypeInit.GENERATOR_MENU.get(), containerId);
        checkContainerSize(container, 1);
        checkContainerDataCount(data, 2);
        this.addSlot(new Slot(container, 0, 79, 34) {
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return pStack.is(Items.COAL);
            }
        });
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }
        this.data = data;
        this.addDataSlots(data);
    }
    public int getData(int slot){
        return this.data.get(slot);
    }
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (pIndex > 0) {
                if (this.slots.get(0).mayPlace(itemstack1)) {
                    if (this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                if (pIndex < 28 && this.moveItemStackTo(itemstack1, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
                if (this.moveItemStackTo(itemstack1, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (this.moveItemStackTo(itemstack1, 28, 37, false)) {
                return ItemStack.EMPTY;
            } else if (this.moveItemStackTo(itemstack1, 1, 28, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
