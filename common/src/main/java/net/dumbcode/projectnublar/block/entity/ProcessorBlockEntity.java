package net.dumbcode.projectnublar.block.entity;

import net.blay09.mods.balm.platform.energy.BalmEnergyStorageProvider;
import net.blay09.mods.balm.platform.energy.DefaultEnergyStorage;
import net.blay09.mods.balm.platform.energy.EnergyStorage;
import net.blay09.mods.balm.platform.fluid.BalmFluidTankProvider;
import net.blay09.mods.balm.platform.fluid.FluidTank;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.util.NublarMath;
import net.dumbcode.projectnublar.block.api.sync.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.item.FilterItem;
import net.dumbcode.projectnublar.item.TankItem;
import net.dumbcode.projectnublar.menutypes.ProcessorMenu;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

/**
 * The DNA processor machine: consumes a fossil/amber input, a test tube, water, and filter
 * durability to produce a DNA vial whose percentage is scaled by the filter's efficiency.
 *
 * <p>Container slot indices are frozen menu contracts: 0 = water, 1 = input, 2 = test tube,
 * 3–11 = the nine output slots, and the non-contiguous 12/13/14 = filter/tank/chip upgrades.
 * All NBT tag names are frozen save contracts.
 *
 * <p>The water sits in a one-slot Balm {@link FluidTank} ({@link #getFluidTank()}), so pipes
 * from other mods can fill it through Balm's loader bridges.
 */
public class ProcessorBlockEntity extends SyncingContainerBlockEntity
        implements GeoBlockEntity, BalmEnergyStorageProvider, BalmFluidTankProvider {

    // Container slot indices (frozen contract with ProcessorMenu).
    public static final int SLOT_WATER = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_TEST_TUBE = 2;
    public static final int OUTPUT_SLOT_START = 3;
    public static final int OUTPUT_SLOT_COUNT = 9;
    public static final int SLOT_FILTER = 12;
    public static final int SLOT_TANK_UPGRADE = 13;
    public static final int SLOT_CHIP_UPGRADE = 14;
    private static final int CONTAINER_SIZE = 15;

    // ContainerData indices (sync contract with ProcessorMenu/ProcessorScreen is the same as before).
    public static final int DATA_FLUID_LEVEL = 0;
    public static final int DATA_MAX_FLUID_LEVEL = 1;
    public static final int DATA_COOKING_PROGRESS = 2;
    public static final int DATA_MAX_PROCESSING_TIME = 3;
    public static final int DATA_COUNT = 4;

    // NBT tag names (frozen save contracts).
    private static final String WATER_TAG = "water";
    private static final String INPUT_TAG = "input";
    private static final String TEST_TUBE_TAG = "testTube";
    private static final String OUTPUT_ITEM_TAG_PREFIX = "item";
    private static final String FLUID_LEVEL_TAG = "fluidLevel";
    private static final String COOKING_PROGRESS_TAG = "cookingProgress";
    private static final String FILTER_TAG = "filter";
    private static final String TANK_UPGRADE_TAG = "tankUpgrade";
    private static final String CHIP_UPGRADE_TAG = "chipUpgrade";
    private static final String ENERGY_TAG = "energy";

    // Behavioral constants (values frozen).
    private static final int DEFAULT_MAX_PROCESSING_TIME_TICKS = 20 * 60 * 4;
    private static final int DEFAULT_MAX_FLUID_LEVEL = 2000;
    private static final int WATER_PER_BUCKET = 1000;
    /** Total fluid drained over one full processing run, spread per tick. */
    private static final float FLUID_PER_FULL_PROCESS = 250f;
    private static final int BASE_ENERGY_CONSUMPTION = 32;
    private static final int DIAMOND_CHIP_EXTRA_ENERGY = 32;
    private static final int GOLD_CHIP_EXTRA_ENERGY = 16;
    private static final int IRON_CHIP_EXTRA_ENERGY = 8;
    private static final int ENERGY_CAPACITY = 1000;
    private static final int ENERGY_MAX_TRANSFER = 1000;
    /** Filter efficiency scales linearly from 25% (fully damaged) to 100% (undamaged). */
    private static final float FILTER_EFFICIENCY_DAMAGE_FACTOR = 0.75f;
    private static final float FILTER_EFFICIENCY_BASE = 0.25f;
    private static final int DNA_PERCENT_DECIMALS = 2;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private ItemStack water = ItemStack.EMPTY;
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack testTube = ItemStack.EMPTY;
    private ItemStack filter = ItemStack.EMPTY;
    private ItemStack tankUpgrade = ItemStack.EMPTY;
    private ItemStack chipUpgrade = ItemStack.EMPTY;
    private final NonNullList<ItemStack> output = NonNullList.withSize(OUTPUT_SLOT_COUNT, ItemStack.EMPTY);
    private float fluidLevel = 0;
    private int cookingProgress = 0;
    /** Insert-only buffer; a change updates and syncs this block entity, as 1.20.1 did on every insert. */
    private final EnergyStorage energyStorage = new DefaultEnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_TRANSFER, 0) {
        @Override
        public void setChanged() {
            if (getLevel() != null) {
                updateBlock();
            }
        }
    };
    private final WaterTank waterTank = new WaterTank();

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int slot) {
            return switch (slot) {
                case DATA_FLUID_LEVEL -> Mth.floor(ProcessorBlockEntity.this.fluidLevel);
                case DATA_MAX_FLUID_LEVEL -> ProcessorBlockEntity.this.getMaxFluidLevel();
                case DATA_COOKING_PROGRESS -> ProcessorBlockEntity.this.cookingProgress;
                case DATA_MAX_PROCESSING_TIME -> ProcessorBlockEntity.this.getMaxProcessingTime();
                default -> 0;
            };
        }

        @Override
        public void set(int slot, int value) {
            switch (slot) {
                case DATA_FLUID_LEVEL -> ProcessorBlockEntity.this.fluidLevel = value;
                case DATA_COOKING_PROGRESS -> ProcessorBlockEntity.this.cookingProgress = value;
                default -> {
                    // Max values (1 and 3) are derived from upgrades and not settable.
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.PROCESSOR_BLOCK_ENTITY.get(), pos, state);
    }

    public void tick(Level world, BlockPos pos, BlockState state, ProcessorBlockEntity blockEntity) {
        blockEntity.intakeWater();
        if (blockEntity.fluidLevel > 0 && !blockEntity.input.isEmpty()
                && !blockEntity.testTube.isEmpty() && !blockEntity.filter.isEmpty()) {
            if (blockEntity.cookingProgress < blockEntity.getMaxProcessingTime()) {
                blockEntity.cookingProgress++;
                blockEntity.fluidLevel -= FLUID_PER_FULL_PROCESS / blockEntity.getMaxProcessingTime();
                // TODO(BUG): 1.20.1 simulated the extract (simulate=true), so machines never use energy; fixing it is a machine-package change.
            } else {
                blockEntity.finishProcessing();
            }
            blockEntity.updateBlock();
        }
    }

    /** Converts a water bucket in the water slot into 1000 mB in the tank when a whole bucket fits. */
    private void intakeWater() {
        if (getItem(SLOT_WATER).is(Items.WATER_BUCKET)
                && waterTank.fill(0, Fluids.WATER, WATER_PER_BUCKET, true) == WATER_PER_BUCKET) {
            setItem(SLOT_WATER, new ItemStack(Items.BUCKET));
            waterTank.fill(0, Fluids.WATER, WATER_PER_BUCKET, false);
        }
    }

    /** Produces the filtered DNA vial, consumes inputs, and wears down the filter. */
    private void finishProcessing() {
        cookingProgress = 0;
        ItemStack stack = new ItemStack(ItemInit.TEST_TUBE_ITEM.get());
        DNAData dnaData = DNAData.fromStack(input);
        double dnaPercentage = dnaData.getDnaPercentage();
        DNAData testTubeData = new DNAData();
        testTubeData.setDnaPercentage(
                NublarMath.round(dnaPercentage * getFilterEfficiency(), DNA_PERCENT_DECIMALS));
        testTubeData.setEntityType(dnaData.getEntityType());
        testTubeData.setVariant(dnaData.getVariant());
        testTubeData.saveTo(stack);
        input.shrink(1);
        testTube.shrink(1);
        addToOutput(stack);
        if (filter.isDamageableItem()) {
            filter.setDamageValue(filter.getDamageValue() + 1);
            if (filter.getDamageValue() >= filter.getMaxDamage()) {
                filter = ItemStack.EMPTY;
            }
        }
    }

    public int calculateEnergyConsumption() {
        int consumption = BASE_ENERGY_CONSUMPTION;
        if (chipUpgrade.getItem() == ItemInit.DIAMOND_COMPUTER_CHIP.get()) {
            consumption += DIAMOND_CHIP_EXTRA_ENERGY;
        }
        if (chipUpgrade.getItem() == ItemInit.GOLD_COMPUTER_CHIP.get()) {
            consumption += GOLD_CHIP_EXTRA_ENERGY;
        }
        if (chipUpgrade.getItem() == ItemInit.IRON_COMPUTER_CHIP.get()) {
            consumption += IRON_CHIP_EXTRA_ENERGY;
        }
        return consumption;
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public FluidTank getFluidTank() {
        return waterTank;
    }

    @Override
    public void saveData(ValueOutput data) {
        SaveVersion.write(data);
        data.store(WATER_TAG, ItemStack.OPTIONAL_CODEC, water);
        data.store(INPUT_TAG, ItemStack.OPTIONAL_CODEC, input);
        data.store(TEST_TUBE_TAG, ItemStack.OPTIONAL_CODEC, testTube);
        for (int i = 0; i < output.size(); i++) {
            data.store(OUTPUT_ITEM_TAG_PREFIX + i, ItemStack.OPTIONAL_CODEC, output.get(i));
        }
        data.putFloat(FLUID_LEVEL_TAG, fluidLevel);
        data.putInt(COOKING_PROGRESS_TAG, cookingProgress);
        data.store(FILTER_TAG, ItemStack.OPTIONAL_CODEC, filter);
        data.store(TANK_UPGRADE_TAG, ItemStack.OPTIONAL_CODEC, tankUpgrade);
        data.store(CHIP_UPGRADE_TAG, ItemStack.OPTIONAL_CODEC, chipUpgrade);
        data.putInt(ENERGY_TAG, energyStorage.getEnergy());
    }

    @Override
    public void loadData(ValueInput data) {
        water = data.read(WATER_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        input = data.read(INPUT_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        testTube = data.read(TEST_TUBE_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        for (int i = 0; i < output.size(); i++) {
            output.set(i, data.read(OUTPUT_ITEM_TAG_PREFIX + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        }
        fluidLevel = data.getFloatOr(FLUID_LEVEL_TAG, 0F);
        cookingProgress = data.getIntOr(COOKING_PROGRESS_TAG, 0);
        filter = data.read(FILTER_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        tankUpgrade = data.read(TANK_UPGRADE_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        chipUpgrade = data.read(CHIP_UPGRADE_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        energyStorage.setEnergy(data.getIntOr(ENERGY_TAG, 0));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.projectnublar.processor")
                .withStyle(ChatFormatting.WHITE);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return water.isEmpty() && input.isEmpty() && testTube.isEmpty()
                && output.stream().allMatch(ItemStack::isEmpty)
                && filter.isEmpty() && tankUpgrade.isEmpty() && chipUpgrade.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case SLOT_WATER -> water;
            case SLOT_INPUT -> input;
            case SLOT_TEST_TUBE -> testTube;
            case SLOT_FILTER -> filter;
            case SLOT_TANK_UPGRADE -> tankUpgrade;
            case SLOT_CHIP_UPGRADE -> chipUpgrade;
            default -> output.get(slot - OUTPUT_SLOT_START);
        };
    }

    /** Note: this ignores the requested count and always removes the whole stack */
    @Override
    public ItemStack removeItem(int slot, int count) {
        switch (slot) {
            case SLOT_WATER -> {
                ItemStack stack = water;
                water = ItemStack.EMPTY;
                return stack;
            }
            case SLOT_INPUT -> {
                ItemStack stack = input;
                input = ItemStack.EMPTY;
                return stack;
            }
            case SLOT_TEST_TUBE -> {
                ItemStack stack = testTube;
                testTube = ItemStack.EMPTY;
                return stack;
            }
            case SLOT_FILTER -> {
                ItemStack stack = filter;
                filter = ItemStack.EMPTY;
                return stack;
            }
            case SLOT_TANK_UPGRADE -> {
                ItemStack stack = tankUpgrade;
                tankUpgrade = ItemStack.EMPTY;
                return stack;
            }
            case SLOT_CHIP_UPGRADE -> {
                ItemStack stack = chipUpgrade;
                chipUpgrade = ItemStack.EMPTY;
                return stack;
            }
            default -> {
                ItemStack stack = output.get(slot - OUTPUT_SLOT_START);
                output.set(slot - OUTPUT_SLOT_START, ItemStack.EMPTY);
                return stack;
            }
        }
    }

    // TODO(BUG): always returns EMPTY, so the slot contents are lost instead of returned when the
    // container is cleared through this path.
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    public double getFilterEfficiency() {
        if (filter.isEmpty()) {
            return 0;
        }
        float remainingDurability =
                (filter.getMaxDamage() - filter.getDamageValue()) / (float) filter.getMaxDamage();
        float efficiencyPercent =
                (FILTER_EFFICIENCY_DAMAGE_FACTOR * remainingDurability) + FILTER_EFFICIENCY_BASE;
        return filter.isEmpty()
                ? 0
                : ((FilterItem) filter.getItem()).getEfficiency() * efficiencyPercent;
    }

    public int getMaxFluidLevel() {
        return tankUpgrade.isEmpty()
                ? DEFAULT_MAX_FLUID_LEVEL
                : ((TankItem) tankUpgrade.getItem()).getFluidAmount();
    }

    public int getMaxProcessingTime() {
        return chipUpgrade.isEmpty()
                ? DEFAULT_MAX_PROCESSING_TIME_TICKS
                : ((ComputerChipItem) chipUpgrade.getItem()).getMaxProcessingTime();
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        switch (slot) {
            case SLOT_WATER -> {
                water = itemStack;
                if (itemStack.is(Items.WATER_BUCKET)) {
                    // Note the strict '<' here versus '<=' in intakeWater
                    if (Mth.floor(fluidLevel) < getMaxFluidLevel() - WATER_PER_BUCKET) {
                        waterTank.fill(0, Fluids.WATER, WATER_PER_BUCKET, false);
                        water = new ItemStack(Items.BUCKET);
                    }
                }
            }
            case SLOT_INPUT -> {
                input = itemStack;
                cookingProgress = 0;
            }
            case SLOT_TEST_TUBE -> testTube = itemStack;
            case SLOT_FILTER -> filter = itemStack;
            case SLOT_TANK_UPGRADE -> tankUpgrade = itemStack;
            case SLOT_CHIP_UPGRADE -> chipUpgrade = itemStack;
            default -> output.set(slot - OUTPUT_SLOT_START, itemStack);
        }
        updateBlock();
    }

    /** Inserts into the first empty or stackable output slot; returns false when full. */
    public boolean addToOutput(ItemStack stack) {
        for (int i = 0; i < output.size(); i++) {
            if (output.get(i).isEmpty()) {
                output.set(i, stack);
                return true;
            } else if (ItemStack.isSameItemSameComponents(output.get(i), stack)) {
                output.get(i).grow(stack.getCount());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        water = ItemStack.EMPTY;
        input = ItemStack.EMPTY;
        testTube = ItemStack.EMPTY;
        filter = ItemStack.EMPTY;
        tankUpgrade = ItemStack.EMPTY;
        chipUpgrade = ItemStack.EMPTY;
        output.clear();
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ProcessorMenu(containerId, inventory, null, this, dataAccess);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /**
     * The Processor's water as a Balm tank with one slot (slot 0) that holds water only. The
     * water-bucket slot, a hopper that puts a bucket into that slot, and pipes from other mods
     * (through Balm's loader bridges, with no Nublar code) all fill it through {@link #fill}.
     * Nothing drains it from outside: the Processor uses its own water while it works.
     *
     * <p>The capacity is read live from {@link #getMaxFluidLevel()}, because it follows the tank
     * upgrade item in its slot. The amount stays in the float field {@code fluidLevel}, because a
     * run uses {@code FLUID_PER_FULL_PROCESS / maxProcessingTime} each tick, which is less than
     * 1 mB; the tank shows the whole units ({@code Mth.floor}) and a fill keeps the fraction.
     */
    private final class WaterTank implements FluidTank {

        @Override
        public int fill(int slot, Fluid fluid, int maxFill, boolean simulate) {
            if (!canFill(slot, fluid)) {
                return 0;
            }
            int filled = Math.max(0, Math.min(maxFill, getCapacity(slot) - getAmount(slot)));
            if (!simulate && filled > 0) {
                fluidLevel = Math.min(fluidLevel + filled, getCapacity(slot));
                setChanged();
            }
            return filled;
        }

        @Override
        public int drain(int slot, Fluid fluid, int maxDrain, boolean simulate) {
            return 0;
        }

        @Override
        public Fluid getFluid(int slot) {
            return isEmpty(slot) ? Fluids.EMPTY : Fluids.WATER;
        }

        /** Sets the amount for water; any other fluid empties the tank, which holds water only. */
        @Override
        public void setFluid(int slot, Fluid fluid, int amount) {
            if (slot == 0) {
                fluidLevel = fluid == Fluids.WATER ? amount : 0;
            }
        }

        @Override
        public int getAmount(int slot) {
            return slot == 0 ? Mth.floor(fluidLevel) : 0;
        }

        @Override
        public void setAmount(int slot, int amount) {
            if (slot == 0) {
                fluidLevel = amount;
            }
        }

        @Override
        public int getCapacity(int slot) {
            return slot == 0 ? getMaxFluidLevel() : 0;
        }

        @Override
        public boolean canDrain(int slot, Fluid fluid) {
            return false;
        }

        @Override
        public boolean canFill(int slot, Fluid fluid) {
            return slot == 0 && fluid == Fluids.WATER;
        }

        @Override
        public boolean isEmpty(int slot) {
            return getAmount(slot) <= 0;
        }

        @Override
        public int getSlotCount() {
            return 1;
        }
    }
}
