package net.dumbcode.projectnublar.menutypes;

import net.dumbcode.projectnublar.container.CloneDisplaySlot;
import net.dumbcode.projectnublar.container.ToggleSlot;
import net.dumbcode.projectnublar.init.MenuTypeInit;
import net.dumbcode.projectnublar.item.DiskStorageItem;
import net.dumbcode.projectnublar.item.TestTubeItem;
import net.dumbcode.projectnublar.item.SyringeItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SequencerMenu extends AbstractContainerMenu {
    ContainerData data;
    public ToggleSlot storageSlot;
    public ToggleSlot dnaInputSlot;
    public ToggleSlot emptyVialOutputSlot;
    public CloneDisplaySlot storageDisplaySlot;
    public CloneDisplaySlot dnaInputDisplaySlot;
    public CloneDisplaySlot emptyVialOutputDisplaySlot;
    public List<ToggleSlot> inventorySlots = new ArrayList<>();
    public SequencerMenu(int containerId, Inventory playerInventory) {
        this(containerId,playerInventory, ContainerLevelAccess.NULL, new SimpleContainer(3), new SimpleContainerData(2));
    }

    public SequencerMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, Container container, ContainerData data) {
        super(MenuTypeInit.SEQUENCER.get(), containerId);
        checkContainerSize(container, 3);
        checkContainerDataCount(data, 0);
        this.addDataSlots(data);
        this.data = data;
        this.addSlot(this.storageSlot = new ToggleSlot(container, 0, 167, 61, (stack)->stack.getItem() instanceof DiskStorageItem));
        this.addSlot(this.dnaInputSlot = new ToggleSlot(container, 1, 167, 61, (stack)->stack.getItem() instanceof TestTubeItem || stack.getItem() instanceof SyringeItem));
        this.addSlot(this.emptyVialOutputSlot = new ToggleSlot(container, 2, 167, 61){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

        });
        this.addSlot(this.storageDisplaySlot = new CloneDisplaySlot(storageSlot, 11, 45));
        this.addSlot(this.dnaInputDisplaySlot = new CloneDisplaySlot(dnaInputSlot, 27, 153));
        this.addSlot(this.emptyVialOutputDisplaySlot = new CloneDisplaySlot(emptyVialOutputSlot, 308, 153));

        int i;
        for(i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                inventorySlots.add((ToggleSlot)this.addSlot(new ToggleSlot(playerInventory, j + i * 9 + 9, 8 + j * 18 + 87, 84 + i * 18 + 1)));
            }
        }
        for(i = 0; i < 9; ++i) {
            inventorySlots.add((ToggleSlot)this.addSlot(new ToggleSlot(playerInventory, i, 8 + i * 18 + 87, 143)));
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
