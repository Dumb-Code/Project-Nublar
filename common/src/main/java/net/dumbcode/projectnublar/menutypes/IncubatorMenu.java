package net.dumbcode.projectnublar.menutypes;

import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.registry.MenuTypeInit;
import net.dumbcode.projectnublar.registry.TagInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Menu for the incubator. Slot 0 is the plant-matter input (container index 9), followed by the
 * egg slots at their stored positions, then the player inventory - hence the menu/container index
 * offset that {@code IncubatorBlockEntity.updateSlot} compensates for (frozen handshake).
 *
 * <p>The egg slots need the incubator's slot count and egg positions. The server menu reads them
 * from the incubator it is opened on. The client menu has no block entity: {@code IncubatorScreen}
 * reads the incubator from the client level in {@code init()} and calls
 * {@link #addEggAndInventorySlots}, before the server sends the slot contents. Both menus then
 * hold the same slots in the same order.
 */
public class IncubatorMenu extends AbstractContainerMenu {
    /** The egg bed's left and top offset inside the incubator screen; the server checks egg positions against the bed. */
    public static final int BED_OFFSET = 9;
    /** The egg bed's width in screen pixels. */
    public static final int BED_WIDTH = 158;
    /** The egg bed's height in screen pixels. */
    public static final int BED_HEIGHT = 115;

    private final BlockPos pos;
    private final ContainerData data;
    private final Container container;
    private final Inventory playerInventory;
    private boolean eggAndInventorySlotsAdded;

    /**
     * The client-side menu; {@code pos} is the incubator that the server opened. It holds only the
     * plant-matter slot until the screen calls {@link #addEggAndInventorySlots}.
     */
    public IncubatorMenu(int pContainerId, Inventory playerInventory, BlockPos pos) {
        this(pContainerId, playerInventory ,new SimpleContainer(10), new SimpleContainerData(11), pos);
    }

    /**
     * The menu over {@code container}. On the server {@code container} is the incubator itself,
     * so the egg and inventory slots are added here.
     */
    public IncubatorMenu (int containerId, Inventory playerInventory,  Container container, ContainerData data,BlockPos pos) {
        super(MenuTypeInit.INCUBATOR.get(), containerId);
        this.pos = pos;
        this.data = data;
        this.container = container;
        this.playerInventory = playerInventory;
        checkContainerSize(container, 10);
        checkContainerDataCount(data, 11);
        this.addDataSlots(data);
        this.addSlot(new Slot(container, 9, 7, 110){
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return pStack.is(TagInit.PLANT_MATTER);
            }
        });
        if (container instanceof IncubatorBlockEntity incubator) {
            addEggAndInventorySlots(incubator);
        }
    }

    /**
     * Adds one egg slot per incubator slot at its stored position, then the player inventory.
     * Does nothing after the first call: the screen's {@code init()} runs again when the window
     * is resized.
     */
    public void addEggAndInventorySlots(IncubatorBlockEntity incubator) {
        if (eggAndInventorySlotsAdded) {
            return;
        }
        eggAndInventorySlotsAdded = true;
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

    /** The incubator block's position. */
    public BlockPos pos() {
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
