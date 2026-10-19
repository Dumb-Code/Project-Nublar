package net.dumbcode.projectnublar.menutypes;

import net.dumbcode.projectnublar.registry.MenuTypeInit;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GeneratorMenu extends AbstractContainerMenu {
    private static final int FUEL_SLOT = 0;
    private static final int MACHINE_SLOT_COUNT = 1;
    private static final int DATA_SLOT_COUNT = 2;
    private static final int FUEL_SLOT_X = 79;
    private static final int FUEL_SLOT_Y = 34;
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLUMNS = 9;
    private static final int PLAYER_INVENTORY_START = 1;
    private static final int PLAYER_INVENTORY_END = 28;
    private static final int HOTBAR_START = 28;
    private static final int HOTBAR_END = 37;
    private static final int PLAYER_INVENTORY_X = 8;
    private static final int PLAYER_INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int SLOT_SPACING = 18;

    private ContainerData data;

    public GeneratorMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_SLOT_COUNT));
    }

    public GeneratorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(MenuTypeInit.GENERATOR_MENU.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_SLOT_COUNT);
        addFuelSlot(container);
        addPlayerInventorySlots(playerInventory);
        addPlayerHotbarSlots(playerInventory);
        this.data = data;
        this.addDataSlots(data);
    }

    public int getData(int slot) {
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
                if (this.slots.get(FUEL_SLOT).mayPlace(itemstack1)) {
                    if (this.moveItemStackTo(itemstack1, FUEL_SLOT, MACHINE_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                if (pIndex < PLAYER_INVENTORY_END
                        && this.moveItemStackTo(itemstack1, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
                if (this.moveItemStackTo(
                        itemstack1, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (this.moveItemStackTo(itemstack1, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            } else if (this.moveItemStackTo(
                    itemstack1, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
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

    private void addFuelSlot(Container container) {
        this.addSlot(new Slot(container, FUEL_SLOT, FUEL_SLOT_X, FUEL_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.COAL);
            }
        });
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; ++row) {
            for (int column = 0; column < PLAYER_INVENTORY_COLUMNS; ++column) {
                this.addSlot(
                        new Slot(
                                playerInventory,
                                column + (row + 1) * PLAYER_INVENTORY_COLUMNS,
                                PLAYER_INVENTORY_X + column * SLOT_SPACING,
                                PLAYER_INVENTORY_Y + row * SLOT_SPACING));
            }
        }
    }

    private void addPlayerHotbarSlots(Inventory playerInventory) {
        for (int column = 0; column < PLAYER_INVENTORY_COLUMNS; ++column) {
            this.addSlot(
                    new Slot(
                            playerInventory,
                            column,
                            PLAYER_INVENTORY_X + column * SLOT_SPACING,
                            HOTBAR_Y));
        }
    }
}
