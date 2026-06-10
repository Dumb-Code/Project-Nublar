package net.dumbcode.projectnublar.block.entity;

import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.InsertOnlyEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.util.NublarMath;
import net.dumbcode.projectnublar.block.api.sync.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.item.FilterItem;
import net.dumbcode.projectnublar.item.TankItem;
import net.dumbcode.projectnublar.menutypes.ProcessorMenu;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The DNA processor machine: consumes a fossil/amber input, a test tube, water, and filter
 * durability to produce a DNA vial whose percentage is scaled by the filter's efficiency.
 *
 * <p>Container slot indices are frozen menu contracts: 0 = water, 1 = input, 2 = test tube,
 * 3–11 = the nine output slots, and the non-contiguous 12/13/14 = filter/tank/chip upgrades.
 * All NBT tag names are frozen save contracts.
 */
public class ProcessorBlockEntity extends SyncingContainerBlockEntity
        implements GeoBlockEntity, BotariumEnergyBlock<WrappedBlockEnergyContainer> {

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
    private WrappedBlockEnergyContainer energyContainer;

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
                blockEntity.getEnergyStorage().internalExtract(calculateEnergyConsumption(), true);
            } else {
                blockEntity.finishProcessing();
            }
            blockEntity.updateBlock();
        }
    }

    /** Converts a water bucket in the water slot into 1000 mB of internal fluid. */
    private void intakeWater() {
        if (Mth.floor(fluidLevel) <= getMaxFluidLevel() - WATER_PER_BUCKET
                && getItem(SLOT_WATER).is(Items.WATER_BUCKET)) {
            setItem(SLOT_WATER, new ItemStack(Items.BUCKET));
            fluidLevel = Mth.clamp(fluidLevel + WATER_PER_BUCKET, 0, getMaxFluidLevel());
        }
    }

    /** Produces the filtered DNA vial, consumes inputs, and wears down the filter. */
    private void finishProcessing() {
        cookingProgress = 0;
        ItemStack stack = new ItemStack(ItemInit.TEST_TUBE_ITEM.get());
        DNAData dnaData = DNAData.loadFromNBT(input.getTag().getCompound("DNAData"));
        double dnaPercentage = dnaData.getDnaPercentage();
        DNAData testTubeData = new DNAData();
        testTubeData.setDnaPercentage(
                NublarMath.round(dnaPercentage * getFilterEfficiency(), DNA_PERCENT_DECIMALS));
        testTubeData.setEntityType(dnaData.getEntityType());
        testTubeData.setVariant(dnaData.getVariant());
        stack.getOrCreateTag().put("DNAData", testTubeData.saveToNBT(new CompoundTag()));
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
    public WrappedBlockEnergyContainer getEnergyStorage() {
        return energyContainer == null
                ? this.energyContainer = new WrappedBlockEnergyContainer(
                        this, new InsertOnlyEnergyContainer(ENERGY_CAPACITY, ENERGY_MAX_TRANSFER))
                : this.energyContainer;
    }

    @Override
    public void saveData(CompoundTag tag) {
        tag.put(WATER_TAG, water.save(new CompoundTag()));
        tag.put(INPUT_TAG, input.save(new CompoundTag()));
        tag.put(TEST_TUBE_TAG, testTube.save(new CompoundTag()));
        for (int i = 0; i < output.size(); i++) {
            tag.put(OUTPUT_ITEM_TAG_PREFIX + i, output.get(i).save(new CompoundTag()));
        }
        tag.putFloat(FLUID_LEVEL_TAG, fluidLevel);
        tag.putInt(COOKING_PROGRESS_TAG, cookingProgress);
        tag.put(FILTER_TAG, filter.save(new CompoundTag()));
        tag.put(TANK_UPGRADE_TAG, tankUpgrade.save(new CompoundTag()));
        tag.put(CHIP_UPGRADE_TAG, chipUpgrade.save(new CompoundTag()));
        tag.put(ENERGY_TAG, energyContainer.serialize(new CompoundTag()));
    }

    @Override
    public void loadData(CompoundTag tag) {
        water = ItemStack.of(tag.getCompound(WATER_TAG));
        input = ItemStack.of(tag.getCompound(INPUT_TAG));
        testTube = ItemStack.of(tag.getCompound(TEST_TUBE_TAG));
        for (int i = 0; i < output.size(); i++) {
            output.set(i, ItemStack.of(tag.getCompound(OUTPUT_ITEM_TAG_PREFIX + i)));
        }
        fluidLevel = tag.getFloat(FLUID_LEVEL_TAG);
        cookingProgress = tag.getInt(COOKING_PROGRESS_TAG);
        filter = ItemStack.of(tag.getCompound(FILTER_TAG));
        tankUpgrade = ItemStack.of(tag.getCompound(TANK_UPGRADE_TAG));
        chipUpgrade = ItemStack.of(tag.getCompound(CHIP_UPGRADE_TAG));
        energyContainer.deserialize(tag.getCompound(ENERGY_TAG));
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
                        fluidLevel = Mth.clamp(fluidLevel + WATER_PER_BUCKET, 0, getMaxFluidLevel());
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
            } else if (ItemStack.isSameItemSameTags(output.get(i), stack)) {
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

    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(4);
    }
}
