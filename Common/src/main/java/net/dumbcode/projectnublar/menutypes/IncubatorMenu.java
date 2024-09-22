package net.dumbcode.projectnublar.menutypes;

import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.init.MenuTypeInit;
import net.dumbcode.projectnublar.init.TagInit;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class IncubatorMenu extends AbstractContainerMenu {
    private BlockPos pos;
    private ContainerData data;

    public IncubatorMenu(int pContainerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(pContainerId, playerInventory, buf.readBlockPos());
    }

    public IncubatorMenu(int pContainerId, Inventory playerInventory,BlockPos pos) {
        this(pContainerId, playerInventory ,new SimpleContainer(10), new SimpleContainerData(11), pos);
    }
    public IncubatorMenu (int containerId, Inventory playerInventory,  Container container, ContainerData data,BlockPos pos) {
        super(MenuTypeInit.INCUBATOR.get(), containerId);
        this.pos = pos;
        this.data = data;
        checkContainerSize(container, 10);
        checkContainerDataCount(data, 11);
        this.addDataSlots(data);
        this.addSlot(new Slot(container, 9, 7, 110){
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return pStack.is(TagInit.PLANT_MATTER);
            }
        });
        IncubatorBlockEntity incubator = (IncubatorBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos);
        for(int i = 0; i < incubator.getSlotCount(); i++){
            this.addSlot(new IncubatorSlot(container, i, incubator.getX(i), incubator.getY(i)));
        }
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18 + 56));
            }
        }
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 198));
        }

    }

    public ContainerData getData() {
        return data;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
    public static class IncubatorSlot extends Slot{
        public IncubatorSlot(Container pContainer, int pIndex, int pX, int pY) {
            super(pContainer, pIndex, pX, pY);
        }

    }
}
