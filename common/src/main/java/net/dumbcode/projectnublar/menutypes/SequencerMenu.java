package net.dumbcode.projectnublar.menutypes;

import commonnetwork.api.Network;
import java.util.ArrayList;
import java.util.List;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.container.CloneDisplaySlot;
import net.dumbcode.projectnublar.container.ToggleSlot;
import net.dumbcode.projectnublar.item.DiskStorageItem;
import net.dumbcode.projectnublar.item.SyringeItem;
import net.dumbcode.projectnublar.item.TestTubeItem;
import net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket;
import net.dumbcode.projectnublar.registry.MenuTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.ItemTags;
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
 * Menu for the sequencer. Each machine slot exists twice: a {@link ToggleSlot} (the real,
 * activatable slot, all parked at the same off-screen-ish position) and a {@link CloneDisplaySlot}
 * mirroring it at the tab-specific display position. Tab switching toggles which real slots are
 * active. Slot order, positions, and indices are frozen contracts with {@code SequencerScreen}.
 */
public class SequencerMenu extends AbstractContainerMenu {

    /**
     * The {@code clickMenuButton} ids sent by {@code SequencerScreen}. The numeric wire values
     * (99–102) are a frozen network contract.
     */
    public enum MenuButton {
        TOGGLE_SYNTH(99),
        SEQUENCE_TAB(100),
        EDIT_TAB(101),
        SYNTH_TAB(102);

        private final int id;

        MenuButton(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

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
        this(containerId, playerInventory, ContainerLevelAccess.NULL,
                new SimpleContainer(9), new SimpleContainerData(10));
    }

    public SequencerMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access,
            Container container, ContainerData data) {
        super(MenuTypeInit.SEQUENCER.get(), containerId);
        checkContainerSize(container, 3);
        // TODO(BUG): validates against a data count of 0 even though 10 data slots are synced;
        // the check is effectively disabled.
        checkContainerDataCount(data, 0);
        this.container = container;
        this.addDataSlots(data);
        this.data = data;
        addMachineSlots(container);
        addDisplaySlots();
        addPlayerInventorySlots(playerInventory);
    }

    public SequencerMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory);
        this.pos = buf.readBlockPos();
    }

    private void addMachineSlots(Container container) {
        this.addSlot(this.storageSlot = new ToggleSlot(container, SequencerBlockEntity.SLOT_STORAGE,
                167, 61, (stack) -> stack.getItem() instanceof DiskStorageItem));
        this.addSlot(this.dnaInputSlot = new ToggleSlot(container, SequencerBlockEntity.SLOT_DNA_INPUT,
                167, 61, (stack) -> (stack.getItem() instanceof TestTubeItem
                        || stack.getItem() instanceof SyringeItem) && stack.hasTag()));
        this.addSlot(this.emptyVialOutputSlot =
                new ToggleSlot(container, SequencerBlockEntity.SLOT_EMPTY_VIAL_OUTPUT, 167, 61) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
        this.addSlot(this.waterInputSlot = new ToggleSlot(container, SequencerBlockEntity.SLOT_WATER,
                167, 61, (stack) -> stack.is(Items.WATER_BUCKET)));
        // TODO(BUG): this filter only accepts BONE_MEAL while SequencerBlockEntity's tick accepts
        // the whole projectnublar:bone_matter tag.
        this.addSlot(this.boneMatterInputSlot =
                new ToggleSlot(container, SequencerBlockEntity.SLOT_BONE_MATTER,
                        167, 61, (stack) -> stack.is(Items.BONE_MEAL)));
        this.addSlot(this.sugarInputSlot = new ToggleSlot(container, SequencerBlockEntity.SLOT_SUGAR,
                167, 61, (stack) -> stack.is(Items.SUGAR)));
        this.addSlot(this.plantMatterInputSlot =
                new ToggleSlot(container, SequencerBlockEntity.SLOT_PLANT_MATTER,
                        167, 61, (stack) -> stack.is(ItemTags.LEAVES)));
        this.addSlot(this.emptyVialInputSlot =
                new ToggleSlot(container, SequencerBlockEntity.SLOT_EMPTY_TUBE_INPUT,
                        167, 61, (stack) -> stack.getItem() instanceof TestTubeItem));
        this.addSlot(this.dnaTestTubeOutputSlot =
                new ToggleSlot(container, SequencerBlockEntity.SLOT_DNA_TEST_TUBE_OUTPUT,
                        167, 61, (stack) -> stack.getItem() instanceof TestTubeItem));
    }

    private void addDisplaySlots() {
        this.addSlot(this.storageDisplaySlot = new CloneDisplaySlot(storageSlot, 11, 45));
        this.addSlot(this.dnaInputDisplaySlot = new CloneDisplaySlot(dnaInputSlot, 27, 153));
        this.addSlot(this.emptyVialOutputDisplaySlot =
                new CloneDisplaySlot(emptyVialOutputSlot, 308, 153));

        this.addSlot(this.waterInputDisplaySlot = new CloneDisplaySlot(waterInputSlot, 49, 37));
        this.addSlot(this.boneMatterInputDisplaySlot =
                new CloneDisplaySlot(boneMatterInputSlot, 49 + 236, 37));
        this.addSlot(this.sugarInputDisplaySlot = new CloneDisplaySlot(sugarInputSlot, 49, 37 + 70));
        this.addSlot(this.plantMatterInputDisplaySlot =
                new CloneDisplaySlot(plantMatterInputSlot, 49 + 236, 37 + 70));

        this.addSlot(this.emptyVialInputDisplaySlot =
                new CloneDisplaySlot(emptyVialInputSlot, 129, 169));
        this.addSlot(this.dnaTestTubeOutputDisplaySlot =
                new CloneDisplaySlot(dnaTestTubeOutputSlot, 129 + 76, 169));
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                inventorySlots.add((ToggleSlot) this.addSlot(new ToggleSlot(playerInventory,
                        column + row * 9 + 9, 8 + column * 18 + 87, 84 + row * 18 + 1)));
            }
        }
        for (int column = 0; column < 9; ++column) {
            inventorySlots.add((ToggleSlot) this.addSlot(
                    new ToggleSlot(playerInventory, column, 8 + column * 18 + 87, 143)));
        }
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getDataSlot(int slot) {
        return data.get(slot);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == MenuButton.TOGGLE_SYNTH.getId()) {
            ((SequencerBlockEntity) container).toggleSynth();
            return true;
        }
        if (id == MenuButton.SEQUENCE_TAB.getId()) {
            enableSequencerScreen();
            disableSynthScreen();
            return true;
        }
        if (id == MenuButton.EDIT_TAB.getId()) {
            disableSynthScreen();
            disableSequencerScreen();
            return true;
        }
        if (id == MenuButton.SYNTH_TAB.getId()) {
            enableSynthScreen();
            disableSequencerScreen();
            return true;
        }
        return false;
    }

    public void enableSequencerScreen() {
        dnaInputSlot.setActive(true);
        emptyVialOutputSlot.setActive(true);
        storageSlot.setActive(true);
    }

    public void disableSequencerScreen() {
        dnaInputSlot.setActive(false);
        emptyVialOutputSlot.setActive(false);
        storageSlot.setActive(false);
    }

    public void enableSynthScreen() {
        waterInputSlot.setActive(true);
        boneMatterInputSlot.setActive(true);
        sugarInputSlot.setActive(true);
        plantMatterInputSlot.setActive(true);
        emptyVialInputSlot.setActive(true);
        dnaTestTubeOutputSlot.setActive(true);
    }

    public void disableSynthScreen() {
        waterInputSlot.setActive(false);
        boneMatterInputSlot.setActive(false);
        sugarInputSlot.setActive(false);
        plantMatterInputSlot.setActive(false);
        emptyVialInputSlot.setActive(false);
        dnaTestTubeOutputSlot.setActive(false);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index >= 9) {
                for (int i = 0; i < 9; i++) {
                    if (this.inventorySlots.get(i).mayPlace(slotStack)) {
                        if (this.moveItemStackTo(slotStack, i, i + 1, false)) {
                            break;
                        }
                    }
                }
            } else if (!this.moveItemStackTo(slotStack, 18, 54, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public void sendUpdate(DinoData data) {
        Network.getNetworkHandler().sendToServer(new UpdateEditInfoPacket(data, pos), true);
    }
}
