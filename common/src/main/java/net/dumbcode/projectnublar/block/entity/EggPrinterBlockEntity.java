package net.dumbcode.projectnublar.block.entity;

import net.blay09.mods.balm.platform.energy.BalmEnergyStorageProvider;
import net.blay09.mods.balm.platform.energy.DefaultEnergyStorage;
import net.blay09.mods.balm.platform.energy.EnergyStorage;
import java.util.function.Consumer;
import net.dumbcode.projectnublar.block.api.multiblock.IMachineParts;
import net.dumbcode.projectnublar.block.api.sync.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.menutypes.EggPrinterMenu;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

/**
 * The egg printer machine: consumes an embryo syringe and bonemeal to print an artificial egg,
 * returning the empty syringe.
 *
 * <p>Container slot indices 0–3 and ContainerData indices 0–3 are frozen menu contracts. All NBT
 * tag names are frozen save contracts. Note that the {@code sensor} and {@code chip} part stacks
 * are intentionally <b>not</b> persisted (original behavior - they are lost on reload).
 */
public class EggPrinterBlockEntity extends SyncingContainerBlockEntity
        implements GeoBlockEntity, IMachineParts, BalmEnergyStorageProvider {

    // Container slot indices (frozen contract with EggPrinterMenu).
    public static final int SLOT_EMBRYO_INPUT = 0;
    public static final int SLOT_BONEMEAL_INPUT = 1;
    public static final int SLOT_EGG_OUTPUT = 2;
    public static final int SLOT_SYRINGE_OUTPUT = 3;
    private static final int CONTAINER_SIZE = 4;

    // ContainerData indices (frozen sync contract with EggPrinterMenu/EggPrinterScreen).
    public static final int DATA_BONEMEAL_AMOUNT = 0;
    public static final int DATA_BONEMEAL_MAX = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_MAX_PROGRESS = 3;
    public static final int DATA_COUNT = 4;

    // NBT tag names (frozen save contracts).
    private static final String EMBRYO_INPUT_TAG = "embryoInput";
    private static final String BONEMEAL_INPUT_TAG = "bonemealInput";
    private static final String EGG_OUTPUT_TAG = "eggOutput";
    private static final String SYRINGE_OUTPUT_TAG = "syringeOutput";
    private static final String BONEMEAL_AMOUNT_TAG = "bonemealAmount";
    private static final String BONEMEAL_MAX_TAG = "bonemealMax";
    private static final String PROGRESS_TAG = "progress";
    private static final String MAX_PROGRESS_TAG = "maxProgress";
    private static final String IS_PRINTING_TAG = "isPrinting";

    // Behavioral constants (values frozen).
    private static final int DEFAULT_BONEMEAL_MAX = 30;
    private static final int BONEMEAL_PER_PRINT = 16;
    private static final int DEFAULT_MAX_PROGRESS_TICKS = 20 * 60 * 10;
    private static final int MIN_ENERGY_TO_PRINT = 32;
    private static final int BASE_ENERGY_CONSUMPTION = 32;
    private static final int DIAMOND_CHIP_EXTRA_ENERGY = 24;
    private static final int IRON_CHIP_EXTRA_ENERGY = 8;
    private static final int GOLD_CHIP_EXTRA_ENERGY = 16;
    private static final int SENSOR_EXTRA_ENERGY = 8;
    private static final int ENERGY_CAPACITY = 1000;
    private static final int ENERGY_MAX_TRANSFER = 1000;
    private static final int CRACK_ROLL_BOUND = 10;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack embryoInput = ItemStack.EMPTY;
    private ItemStack bonemealInput = ItemStack.EMPTY;
    private ItemStack eggOutput = ItemStack.EMPTY;
    private ItemStack syringeOutput = ItemStack.EMPTY;
    private int bonemealAmount = 0;
    private int bonemealMax = DEFAULT_BONEMEAL_MAX;
    private int progress = 0;
    private int maxProgress = DEFAULT_MAX_PROGRESS_TICKS;
    private boolean isPrinting = false;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_BONEMEAL_AMOUNT -> bonemealAmount;
                case DATA_BONEMEAL_MAX -> bonemealMax;
                case DATA_PROGRESS -> progress;
                case DATA_MAX_PROGRESS -> getMaxProgress();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_BONEMEAL_AMOUNT -> bonemealAmount = value;
                case DATA_BONEMEAL_MAX -> bonemealMax = value;
                case DATA_PROGRESS -> progress = value;
                case DATA_MAX_PROGRESS -> maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    /** Multiblock parts; not persisted to NBT (see class Javadoc). */
    private ItemStack sensor = ItemStack.EMPTY;
    private ItemStack chip = ItemStack.EMPTY;
    /** Insert-only buffer; a change updates and syncs this block entity, as 1.20.1 did on every insert. */
    private final EnergyStorage energyStorage = new DefaultEnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_TRANSFER, 0) {
        @Override
        public void setChanged() {
            if (getLevel() != null) {
                updateBlock();
            }
        }
    };

    public EggPrinterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * TODO(BUG): returns 0 without a sensor and -1 with one, and the cracked-egg roll below is
     * {@code nextInt(10) == getBreakChance()}. So a sensor makes cracking impossible (-1 never
     * rolled) while no sensor cracks ~10% of eggs. Inverted-looking but load-bearing; also note
     * the float-vs-int comparison.
     */
    public float getBreakChance() {
        return sensor.isEmpty() ? 0 : -1;
    }

    public int getMaxProgress() {
        return chip.isEmpty() ? maxProgress : ((ComputerChipItem) chip.getItem()).getMaxPrintTime();
    }

    public void tick(Level world, BlockPos pos, BlockState state, EggPrinterBlockEntity blockEntity) {
        boolean shouldUpdate = false;
        if (bonemealAmount < bonemealMax && !bonemealInput.isEmpty()) {
            bonemealAmount += 1;
            bonemealInput.shrink(1);
            shouldUpdate = true;
        }
        isPrinting = !embryoInput.isEmpty()
                && bonemealAmount >= BONEMEAL_PER_PRINT
                && eggOutput.isEmpty()
                && getEnergyStorage().getEnergy() > MIN_ENERGY_TO_PRINT;
        if (isPrinting) {
            // TODO(BUG): 1.20.1 simulated the extract (simulate=true), so machines never use energy; fixing it is a machine-package change.
            progress += 1;
            if (progress >= getMaxProgress()) {
                finishPrinting();
            }
            shouldUpdate = true;
        }
        if (shouldUpdate) {
            updateBlock();
        }
    }

    /** Produces the (possibly cracked) egg, consumes bonemeal, and returns the empty syringe. */
    private void finishPrinting() {
        progress = 0;
        bonemealAmount -= BONEMEAL_PER_PRINT;
        isPrinting = false;
        eggOutput = new ItemStack(
                level.getRandom().nextInt(CRACK_ROLL_BOUND) == getBreakChance()
                        ? ItemInit.CRACKED_ARTIFICIAL_EGG.get()
                        : ItemInit.ARTIFICIAL_EGG.get());
        embryoInput = ItemStack.EMPTY;
        if (syringeOutput.isEmpty()) {
            syringeOutput = new ItemStack(ItemInit.SYRINGE.get());
        } else {
            syringeOutput.grow(1);
        }
    }

    public int calculateEnergyConsumption() {
        int consumption = BASE_ENERGY_CONSUMPTION;
        if (chip.getItem() == ItemInit.DIAMOND_COMPUTER_CHIP.get()) {
            consumption += DIAMOND_CHIP_EXTRA_ENERGY;
        }
        if (chip.getItem() == ItemInit.IRON_COMPUTER_CHIP.get()) {
            consumption += IRON_CHIP_EXTRA_ENERGY;
        }
        if (chip.getItem() == ItemInit.GOLD_COMPUTER_CHIP.get()) {
            consumption += GOLD_CHIP_EXTRA_ENERGY;
        }
        if (!sensor.isEmpty()) {
            consumption += SENSOR_EXTRA_ENERGY;
        }
        return consumption;
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    protected void saveData(ValueOutput output) {
        SaveVersion.write(output);
        output.store(EMBRYO_INPUT_TAG, ItemStack.OPTIONAL_CODEC, embryoInput);
        output.store(BONEMEAL_INPUT_TAG, ItemStack.OPTIONAL_CODEC, bonemealInput);
        output.store(EGG_OUTPUT_TAG, ItemStack.OPTIONAL_CODEC, eggOutput);
        output.store(SYRINGE_OUTPUT_TAG, ItemStack.OPTIONAL_CODEC, syringeOutput);
        output.putInt(BONEMEAL_AMOUNT_TAG, bonemealAmount);
        output.putInt(BONEMEAL_MAX_TAG, bonemealMax);
        output.putInt(PROGRESS_TAG, progress);
        output.putInt(MAX_PROGRESS_TAG, maxProgress);
        output.putBoolean(IS_PRINTING_TAG, isPrinting);
    }

    @Override
    protected void loadData(ValueInput input) {
        embryoInput = input.read(EMBRYO_INPUT_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        bonemealInput = input.read(BONEMEAL_INPUT_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        eggOutput = input.read(EGG_OUTPUT_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        syringeOutput = input.read(SYRINGE_OUTPUT_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        bonemealAmount = input.getIntOr(BONEMEAL_AMOUNT_TAG, 0);
        bonemealMax = input.getIntOr(BONEMEAL_MAX_TAG, 0);
        progress = input.getIntOr(PROGRESS_TAG, 0);
        maxProgress = input.getIntOr(MAX_PROGRESS_TAG, 0);
        isPrinting = input.getBooleanOr(IS_PRINTING_TAG, false);
    }

    @Override
    protected Component getDefaultName() {
        return Component.nullToEmpty("Egg Printer");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new EggPrinterMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return embryoInput.isEmpty() && bonemealInput.isEmpty()
                && eggOutput.isEmpty() && syringeOutput.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case SLOT_EMBRYO_INPUT -> embryoInput;
            case SLOT_BONEMEAL_INPUT -> bonemealInput;
            case SLOT_EGG_OUTPUT -> eggOutput;
            case SLOT_SYRINGE_OUTPUT -> syringeOutput;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return switch (slot) {
            case SLOT_EMBRYO_INPUT -> removeFromSlot(embryoInput, stack -> embryoInput = stack, count);
            case SLOT_BONEMEAL_INPUT ->
                    removeFromSlot(bonemealInput, stack -> bonemealInput = stack, count);
            case SLOT_EGG_OUTPUT -> removeFromSlot(eggOutput, stack -> eggOutput = stack, count);
            case SLOT_SYRINGE_OUTPUT ->
                    removeFromSlot(syringeOutput, stack -> syringeOutput = stack, count);
            default -> ItemStack.EMPTY;
        };
    }

    /** Shared body of the four formerly copy-pasted, byte-identical removeItem branches. */
    private static ItemStack removeFromSlot(ItemStack current, Consumer<ItemStack> setter, int count) {
        if (!current.isEmpty()) {
            if (current.getCount() <= count) {
                setter.accept(ItemStack.EMPTY);
                return current;
            }
            ItemStack split = current.split(count);
            if (current.isEmpty()) {
                setter.accept(ItemStack.EMPTY);
            }
            return split;
        }
        return ItemStack.EMPTY;
    }

    // TODO(BUG): always returns EMPTY, so the slot contents are lost instead of returned when the
    // container is cleared through this path.
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        switch (slot) {
            case SLOT_EMBRYO_INPUT -> embryoInput = stack;
            case SLOT_BONEMEAL_INPUT -> bonemealInput = stack;
            case SLOT_EGG_OUTPUT -> eggOutput = stack;
            case SLOT_SYRINGE_OUTPUT -> syringeOutput = stack;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        embryoInput = ItemStack.EMPTY;
        bonemealInput = ItemStack.EMPTY;
        eggOutput = ItemStack.EMPTY;
        syringeOutput = ItemStack.EMPTY;
    }

    /** Drops the machine parts after the inventory (26.2 drops the inventory), as 1.20.1 did. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.getMachineParts());
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void setSensor(ItemStack sensorStack) {
        this.sensor = sensorStack;
    }

    public void setChip(ItemStack chipStack) {
        this.chip = chipStack;
    }

    @Override
    public NonNullList<ItemStack> getMachineParts() {
        return NonNullList.of(ItemStack.EMPTY, sensor, chip);
    }
}
