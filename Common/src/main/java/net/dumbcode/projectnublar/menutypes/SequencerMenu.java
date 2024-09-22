package net.dumbcode.projectnublar.menutypes;

import commonnetwork.api.Network;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.container.CloneDisplaySlot;
import net.dumbcode.projectnublar.container.ToggleSlot;
import net.dumbcode.projectnublar.init.MenuTypeInit;
import net.dumbcode.projectnublar.item.DiskStorageItem;
import net.dumbcode.projectnublar.item.SyringeItem;
import net.dumbcode.projectnublar.item.TestTubeItem;
import net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class SequencerMenu extends AbstractContainerMenu {
    ContainerData data;
    public ToggleSlot storageSlot;
    public ToggleSlot dnaInputSlot;
    public ToggleSlot emptyVialOutputSlot;
    public ToggleSlot waterInputSlot;
    public ToggleSlot boneMatterInputSlot;
    public ToggleSlot sugarInputSlot;
    public ToggleSlot plantMatterInputSlot;
    public ToggleSlot emptyVialInputSlot;
    public ToggleSlot dnaTestTubeOutputSlot;
    public CloneDisplaySlot storageDisplaySlot;
    public CloneDisplaySlot dnaInputDisplaySlot;
    public CloneDisplaySlot emptyVialOutputDisplaySlot;
    public CloneDisplaySlot waterInputDisplaySlot;
    public CloneDisplaySlot boneMatterInputDisplaySlot;
    public CloneDisplaySlot sugarInputDisplaySlot;
    public CloneDisplaySlot plantMatterInputDisplaySlot;
    public CloneDisplaySlot emptyVialInputDisplaySlot;
    public CloneDisplaySlot dnaTestTubeOutputDisplaySlot;
    public Container container;


    public List<ToggleSlot> inventorySlots = new ArrayList<>();
    public BlockPos pos;


    public SequencerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL, new SimpleContainer(9), new SimpleContainerData(10));
    }

    public SequencerMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, Container container, ContainerData data) {
        super(MenuTypeInit.SEQUENCER.get(), containerId);
        checkContainerSize(container, 3);
        checkContainerDataCount(data, 0);
        this.container = container;
        this.addDataSlots(data);
        this.data = data;
        this.addSlot(this.storageSlot = new ToggleSlot(container, 0, 167, 61, (stack) -> stack.getItem() instanceof DiskStorageItem));
        this.addSlot(this.dnaInputSlot = new ToggleSlot(container, 1, 167, 61, (stack) -> (stack.getItem() instanceof TestTubeItem || stack.getItem() instanceof SyringeItem) && stack.hasTag()));
        this.addSlot(this.emptyVialOutputSlot = new ToggleSlot(container, 2, 167, 61) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

        });
        this.addSlot(this.waterInputSlot = new ToggleSlot(container, 3, 167, 61, (stack) -> stack.is(Items.WATER_BUCKET)));
        this.addSlot(this.boneMatterInputSlot = new ToggleSlot(container, 4, 167, 61, (stack) -> stack.is(Items.BONE_MEAL)));
        this.addSlot(this.sugarInputSlot = new ToggleSlot(container, 5, 167, 61, (stack) -> stack.is(Items.SUGAR)));
        this.addSlot(this.plantMatterInputSlot = new ToggleSlot(container, 6, 167, 61, (stack) -> stack.is(ItemTags.LEAVES)));
        this.addSlot(this.emptyVialInputSlot = new ToggleSlot(container, 7, 167, 61, (stack) -> stack.getItem() instanceof TestTubeItem));
        this.addSlot(this.dnaTestTubeOutputSlot = new ToggleSlot(container, 8, 167, 61, (stack) -> stack.getItem() instanceof TestTubeItem));


        this.addSlot(this.storageDisplaySlot = new CloneDisplaySlot(storageSlot, 11, 45));
        this.addSlot(this.dnaInputDisplaySlot = new CloneDisplaySlot(dnaInputSlot, 27, 153));
        this.addSlot(this.emptyVialOutputDisplaySlot = new CloneDisplaySlot(emptyVialOutputSlot, 308, 153));

        this.addSlot(this.waterInputDisplaySlot = new CloneDisplaySlot(waterInputSlot, 49, 37));
        this.addSlot(this.boneMatterInputDisplaySlot = new CloneDisplaySlot(boneMatterInputSlot, 49 + 236, 37));
        this.addSlot(this.sugarInputDisplaySlot = new CloneDisplaySlot(sugarInputSlot, 49, 37 + 70));
        this.addSlot(this.plantMatterInputDisplaySlot = new CloneDisplaySlot(plantMatterInputSlot, 49 + 236, 37 + 70));

        this.addSlot(this.emptyVialInputDisplaySlot = new CloneDisplaySlot(emptyVialInputSlot, 129, 169));
        this.addSlot(this.dnaTestTubeOutputDisplaySlot = new CloneDisplaySlot(dnaTestTubeOutputSlot, 129 + 76, 169));

        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                inventorySlots.add((ToggleSlot) this.addSlot(new ToggleSlot(playerInventory, j + i * 9 + 9, 8 + j * 18 + 87, 84 + i * 18 + 1)));
            }
        }
        for (i = 0; i < 9; ++i) {
            inventorySlots.add((ToggleSlot) this.addSlot(new ToggleSlot(playerInventory, i, 8 + i * 18 + 87, 143)));
        }


    }

    public BlockPos getPos() {
        return pos;
    }

    public SequencerMenu(int i, Inventory inventory, FriendlyByteBuf buf) {
        this(i, inventory);
        this.pos = buf.readBlockPos();
    }

    public int getDataSlot(int slot) {
        return data.get(slot);
    }

    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        if(pId == 99){
            ((SequencerBlockEntity)container).toggleSynth();
            return true;
        }
        if(pId == 100){
            enableSequencerScreen();
            disableSynthScreen();
            return true;
        }
        if(pId == 101){
            disableSynthScreen();
            disableSequencerScreen();
            return true;
        }
        if (pId == 102) {
            enableSynthScreen();
            disableSequencerScreen();
            return true;
        }
        return false;
    }
    public void enableSequencerScreen(){
        dnaInputSlot.setActive(true);
        emptyVialOutputSlot.setActive(true);
        storageSlot.setActive(true);
    }
    public void disableSequencerScreen(){
        dnaInputSlot.setActive(false);
        emptyVialOutputSlot.setActive(false);
        storageSlot.setActive(false);
    }
    public void enableSynthScreen(){
        waterInputSlot.setActive(true);
        boneMatterInputSlot.setActive(true);
        sugarInputSlot.setActive(true);
        plantMatterInputSlot.setActive(true);
        emptyVialInputSlot.setActive(true);
        dnaTestTubeOutputSlot.setActive(true);
    }
    public void disableSynthScreen(){
        waterInputSlot.setActive(false);
        boneMatterInputSlot.setActive(false);
        sugarInputSlot.setActive(false);
        plantMatterInputSlot.setActive(false);
        emptyVialInputSlot.setActive(false);
        dnaTestTubeOutputSlot.setActive(false);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (pIndex >= 9) {
                for(int i = 0; i < 9; i++){
                    if(this.inventorySlots.get(i).mayPlace(itemstack1)){
                        if (this.moveItemStackTo(itemstack1, i, i + 1, false)) {
                            break;
                        }
                    }
                }
            } else if (!this.moveItemStackTo(itemstack1, 18, 54, false)) {
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
    public boolean stillValid(Player player) {
        return true;
    }

    public void sendUpdate(DinoData data){
        Network.getNetworkHandler().sendToServer(new UpdateEditInfoPacket(data, pos),true);
    }
}
