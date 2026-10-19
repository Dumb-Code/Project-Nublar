package net.dumbcode.projectnublar.menutypes;

import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.registry.MenuTypeInit;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.item.FilterItem;
import net.dumbcode.projectnublar.item.TankItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Menu for the processor. Machine slots are added in the (frozen) order 0, 1, 12, 13, 14, 2,
 * then the nine output container slots (3–11), then the player inventory; the menu slot order
 * therefore does not match the container indices.
 */
public class ProcessorMenu extends AbstractContainerMenu {
    ContainerData data;

    public ProcessorMenu(int containerId, Inventory playerInventory) {
        this(containerId,playerInventory, ContainerLevelAccess.NULL, new SimpleContainer(15), new SimpleContainerData(4));
    }
    public ProcessorMenu (int containerId, Inventory playerInventory, ContainerLevelAccess access, Container container, ContainerData data) {
        super(MenuTypeInit.PROCESSOR.get(), containerId);
        checkContainerSize(container, 15);
        checkContainerDataCount(data, 4);
        this.addDataSlots(data);
        this.data = data;
        this.addSlot(new Slot(container, 0, 22, 40){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.WATER_BUCKET);
            }
        });

        this.addSlot(new Slot(container, 1, 80, 18){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemInit.FOSSIL_ITEM.get()) || (stack.is(ItemInit.AMBER_ITEM.get()) && stack.hasTag() && stack.getTag().contains("dna_percentage"));
            }
        });


        this.addSlot(new Slot(container, 12, 22, 19){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof FilterItem;
            }
        });
        this.addSlot(new Slot(container, 13, 136, 87){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof TankItem;
            }
        });
        this.addSlot(new Slot(container, 14, 136, 108){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ComputerChipItem;
            }
        });
        this.addSlot(new Slot(container, 2, 136, 40){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemInit.TEST_TUBE_ITEM.get()) && !stack.hasTag();
            }
        });
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(container, (j + i * 3) + 3, 59 + j * 21, 66 + i * 21));
            }
        }



        for(int l = 0; l < 3; ++l) {
            for(int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18 + 56));
            }
        }

        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 198));
        }
    }


    public int getDataSlot(int slot) {
        return data.get(slot);
    }

    /**
     * TODO(BUG): broken control flow. After the {@code if (i < 15)}/else pair,
     * a separate water/filter/tank/chip/test-tube chain runs for every slot index
     * (machine slots included), {@code slot.onTake} fires even when nothing was moved, and the
     * method always returns EMPTY so vanilla's shift-click loop never repeats.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int i) {
            Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            if (i < 15) {
                if (!this.moveItemStackTo(itemstack1, 16, 51, false)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if (itemstack1.is(ItemInit.FOSSIL_ITEM.get()) || (itemstack1.is(ItemInit.AMBER_ITEM.get()) && itemstack1.hasTag() && itemstack1.getTag().contains("dna_percentage"))) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }

                }
            } if (itemstack1.is(Items.WATER_BUCKET)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.getItem() instanceof FilterItem) {
                    if (!this.moveItemStackTo(itemstack1, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.getItem() instanceof TankItem) {
                    if (!this.moveItemStackTo(itemstack1, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.getItem() instanceof ComputerChipItem) {
                    if (!this.moveItemStackTo(itemstack1, 4, 5, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.is(ItemInit.TEST_TUBE_ITEM.get()) && !itemstack1.hasTag()) {
                    if (!this.moveItemStackTo(itemstack1, 5, 6, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                slot.onTake(player, itemstack1);
            }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
