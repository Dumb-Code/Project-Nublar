package net.dumbcode.projectnublar.menutypes;

import net.dumbcode.projectnublar.init.MenuTypeInit;
import net.dumbcode.projectnublar.init.TagInit;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EggPrinterMenu extends AbstractContainerMenu {
    private ContainerData data;
    public EggPrinterMenu(int containerId, Inventory playerInventory) {
        this(containerId,playerInventory, new SimpleContainer(4), new SimpleContainerData(4));
    }
    public EggPrinterMenu (int containerId, Inventory playerInventory,  Container container, ContainerData data) {
        super(MenuTypeInit.EGG_PRINTER.get(), containerId);
        checkContainerSize(container, 4);
        checkContainerDataCount(data, 4);
        this.data = data;
        this.addDataSlots(data);
        this.addSlot(new Slot(container, 0, 15, 110){
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return pStack.hasTag() && pStack.getTag().contains("Embryo");
            }
        });
        this.addSlot(new Slot(container, 1, 15, 80){
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return pStack.is(TagInit.BONE_MATTER);
            }
        });
        this.addSlot(new Slot(container, 2, 148, 60){
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return false;
            }
        });
        this.addSlot(new Slot(container, 3, 148, 110){
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return false;
            }
        });
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18 + 64));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 206));
        }
    }
    public int getDataSlot(int index) {
        return this.data.get(index);
    }
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
