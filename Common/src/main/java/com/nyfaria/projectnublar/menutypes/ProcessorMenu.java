package com.nyfaria.projectnublar.menutypes;

import com.nyfaria.projectnublar.init.ItemInit;
import com.nyfaria.projectnublar.init.MenuTypeInit;
import com.nyfaria.projectnublar.item.ComputerChipItem;
import com.nyfaria.projectnublar.item.FilterItem;
import com.nyfaria.projectnublar.item.TankItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
