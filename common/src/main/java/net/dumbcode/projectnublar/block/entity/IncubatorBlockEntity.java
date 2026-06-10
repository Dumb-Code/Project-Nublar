package net.dumbcode.projectnublar.block.entity;

import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.InsertOnlyEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.block.api.multiblock.IMachineParts;
import net.dumbcode.projectnublar.block.api.sync.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.item.BulbItem;
import net.dumbcode.projectnublar.item.ContainerUpgradeItem;
import net.dumbcode.projectnublar.item.PlantTankItem;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The incubator machine: holds up to nine freely-positionable egg slots plus a plant-matter input
 * (container slot 9), and slowly incubates unincubated eggs into incubated ones.
 *
 * <p>Container slot indices 0–9 and ContainerData indices 0–10 are frozen menu contracts. All NBT
 * tag names are frozen save contracts.
 *
 * <p>Note the slot-index handshake: {@code UpdateIncubatorSlotPacket} carries the menu slot
 * index and {@link #updateSlot} subtracts 1 to get the container index. This off-by-design
 * agreement with {@code IncubatorScreen} must be preserved on both ends.
 */
public class IncubatorBlockEntity extends SyncingContainerBlockEntity
        implements GeoBlockEntity, IMachineParts, BotariumEnergyBlock<WrappedBlockEnergyContainer> {

    // Container layout (frozen contract with IncubatorMenu).
    public static final int EGG_SLOT_COUNT = 9;
    public static final int SLOT_PLANT_MATTER = 9;
    private static final int CONTAINER_SIZE = 10;

    // ContainerData indices (frozen sync contract).
    public static final int DATA_PLANT_MATTER = 0;
    public static final int DATA_MAX_PLANT_MATTER = 1;
    public static final int DATA_SLOT_COUNT = 2;
    public static final int DATA_EGG_PROGRESS_START = 3;
    public static final int DATA_COUNT = 11;

    // NBT tag names (frozen save contracts).
    private static final String SLOT_TAG_PREFIX = "slot";
    private static final String SLOT_X_TAG = "x";
    private static final String SLOT_Y_TAG = "y";
    private static final String PLANT_MATTER_STACK_TAG = "plantMatterStack";
    private static final String PLANT_MATTER_TAG = "plantMatter";
    private static final String CONTAINER_STACK_TAG = "containerStack";
    private static final String BULB_STACK_TAG = "bulbStack";
    private static final String TANK_STACK_TAG = "tankStack";
    private static final String NEST_STACK_TAG = "nestStack";
    private static final String LID_STACK_TAG = "lidStack";
    private static final String BASE_STACK_TAG = "baseStack";
    private static final String ARM_STACK_TAG = "armStack";
    private static final String ENERGY_TAG = "energy";

    // Behavioral constants (values frozen).
    private static final int DEFAULT_SLOT_COUNT = 3;
    private static final int DEFAULT_TICKS_PER_PERCENT = 18 * 20;
    private static final int DEFAULT_MAX_PLANT_MATTER = 64;
    private static final double INCUBATION_PROGRESS_PER_STEP = 0.01;
    private static final int BASE_ENERGY_CONSUMPTION = 32;
    private static final int GOLD_TANK_EXTRA_ENERGY = 8;
    private static final int IRON_TANK_EXTRA_ENERGY = 4;
    private static final int SMALL_CONTAINER_EXTRA_ENERGY = 4;
    private static final int LARGE_CONTAINER_EXTRA_ENERGY = 8;
    private static final int WARM_BULB_EXTRA_ENERGY = 5;
    private static final int WARMER_BULB_EXTRA_ENERGY = 10;
    private static final int HOT_BULB_EXTRA_ENERGY = 16;
    private static final int ENERGY_CAPACITY = 1000;
    private static final int ENERGY_MAX_TRANSFER = 1000;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack plantMatterStack = ItemStack.EMPTY;
    private NonNullList<Slot> items = NonNullList.withSize(EGG_SLOT_COUNT, Slot.EMPTY);
    private final NonNullList<Integer> eggProgress = NonNullList.withSize(EGG_SLOT_COUNT, 0);
    private int plantMatter = 0;
    private ItemStack containerStack = ItemStack.EMPTY;
    private ItemStack bulbStack = ItemStack.EMPTY;
    private ItemStack tankStack = ItemStack.EMPTY;
    private ItemStack nestStack = ItemStack.EMPTY;
    private ItemStack lidStack = ItemStack.EMPTY;
    private ItemStack baseStack = ItemStack.EMPTY;
    private ItemStack armStack = ItemStack.EMPTY;
    private WrappedBlockEnergyContainer energyContainer;

    public IncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.INCUBATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack getContainerStack() {
        return containerStack;
    }

    public void setContainerStack(ItemStack containerStack) {
        this.containerStack = containerStack;
        updateBlock();
    }

    public ItemStack getBulbStack() {
        return bulbStack;
    }

    public void setBulbStack(ItemStack bulbStack) {
        this.bulbStack = bulbStack;
        updateBlock();
    }

    public ItemStack getTankStack() {
        return tankStack;
    }

    public void setTankStack(ItemStack tankStack) {
        this.tankStack = tankStack;
        updateBlock();
    }

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PLANT_MATTER -> plantMatter;
                case DATA_MAX_PLANT_MATTER -> getMaxPlantMatter();
                case DATA_SLOT_COUNT -> getSlotCount();
                default -> eggProgress.get(index - DATA_EGG_PROGRESS_START);
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public int getSlotCount() {
        return containerStack.isEmpty()
                ? DEFAULT_SLOT_COUNT
                : ((ContainerUpgradeItem) containerStack.getItem()).getContainerSize();
    }

    public int getX(int index) {
        return items.get(index).x;
    }

    public int getY(int index) {
        return items.get(index).y;
    }

    /**
     * Moves an egg slot. {@code index} is the menu slot index sent by
     * {@code UpdateIncubatorSlotPacket}; the {@code - 1} converts it to the container index
     * (frozen handshake, see class Javadoc).
     */
    public void updateSlot(int index, int x, int y) {
        items.set(index - 1, items.get(index - 1).withX(x).withY(y));
        updateBlock();
    }

    @Override
    protected void saveData(CompoundTag tag) {
        // TODO(BUG): "slot" + items.indexOf(slot) returns the index of the first equal slot, so
        // two slots with equal contents/positions produce duplicate keys and one entry is lost.
        items.forEach(slot -> {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt(SLOT_X_TAG, slot.x);
            slotTag.putInt(SLOT_Y_TAG, slot.y);
            slot.stack.save(slotTag);
            tag.put(SLOT_TAG_PREFIX + items.indexOf(slot), slotTag);
        });
        tag.put(PLANT_MATTER_STACK_TAG, plantMatterStack.save(new CompoundTag()));
        tag.putInt(PLANT_MATTER_TAG, plantMatter);
        tag.put(CONTAINER_STACK_TAG, containerStack.save(new CompoundTag()));
        tag.put(BULB_STACK_TAG, bulbStack.save(new CompoundTag()));
        tag.put(TANK_STACK_TAG, tankStack.save(new CompoundTag()));
        tag.put(NEST_STACK_TAG, nestStack.save(new CompoundTag()));
        tag.put(LID_STACK_TAG, lidStack.save(new CompoundTag()));
        tag.put(BASE_STACK_TAG, baseStack.save(new CompoundTag()));
        tag.put(ARM_STACK_TAG, armStack.save(new CompoundTag()));
        tag.put(ENERGY_TAG, energyContainer.serialize(new CompoundTag()));
    }

    @Override
    protected void loadData(CompoundTag tag) {
        for (int i = 0; i < EGG_SLOT_COUNT; i++) {
            CompoundTag slotTag = tag.getCompound(SLOT_TAG_PREFIX + i);
            items.set(i, new Slot(
                    ItemStack.of(slotTag), slotTag.getInt(SLOT_X_TAG), slotTag.getInt(SLOT_Y_TAG)));
        }
        plantMatterStack = ItemStack.of(tag.getCompound(PLANT_MATTER_STACK_TAG));
        plantMatter = tag.getInt(PLANT_MATTER_TAG);
        containerStack = ItemStack.of(tag.getCompound(CONTAINER_STACK_TAG));
        bulbStack = ItemStack.of(tag.getCompound(BULB_STACK_TAG));
        tankStack = ItemStack.of(tag.getCompound(TANK_STACK_TAG));
        nestStack = ItemStack.of(tag.getCompound(NEST_STACK_TAG));
        lidStack = ItemStack.of(tag.getCompound(LID_STACK_TAG));
        baseStack = ItemStack.of(tag.getCompound(BASE_STACK_TAG));
        armStack = ItemStack.of(tag.getCompound(ARM_STACK_TAG));
        energyContainer.deserialize(tag.getCompound(ENERGY_TAG));
    }

    public ItemStack getNestStack() {
        return nestStack;
    }

    public void setNestStack(ItemStack nestStack) {
        this.nestStack = nestStack;
        updateBlock();
    }

    public ItemStack getLidStack() {
        return lidStack;
    }

    public void setLidStack(ItemStack lidStack) {
        this.lidStack = lidStack;
        updateBlock();
    }

    public ItemStack getBaseStack() {
        return baseStack;
    }

    public void setBaseStack(ItemStack baseStack) {
        this.baseStack = baseStack;
        updateBlock();
    }

    public ItemStack getArmStack() {
        return armStack;
    }

    public void setArmStack(ItemStack armStack) {
        this.armStack = armStack;
        updateBlock();
    }

    public int getTicksPerPercent() {
        return bulbStack.isEmpty()
                ? DEFAULT_TICKS_PER_PERCENT
                : ((BulbItem) bulbStack.getItem()).getTicksPerPercent();
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Incubator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new IncubatorMenu(containerId, inventory, this, dataAccess, worldPosition);
    }

    public int getMaxPlantMatter() {
        return getTankStack().isEmpty()
                ? DEFAULT_MAX_PLANT_MATTER
                : ((PlantTankItem) getTankStack().getItem()).getMaxPlantMatter();
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return plantMatterStack.isEmpty() && items.stream().allMatch(slot -> slot.stack.isEmpty());
    }

    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case SLOT_PLANT_MATTER -> plantMatterStack;
            default -> items.get(slot).stack;
        };
    }

    @Override
    public ItemStack removeItem(int slotIndex, int count) {
        if (slotIndex == SLOT_PLANT_MATTER) {
            ItemStack stack = plantMatterStack.split(count);
            if (plantMatterStack.isEmpty()) {
                plantMatterStack = ItemStack.EMPTY;
            }
            return stack;
        }
        Slot slot = items.get(slotIndex);
        ItemStack stack = slot.stack.split(count);
        if (slot.stack.isEmpty()) {
            items.set(slotIndex, Slot.EMPTY);
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        if (slotIndex == SLOT_PLANT_MATTER) {
            ItemStack stack = plantMatterStack;
            plantMatterStack = ItemStack.EMPTY;
            return stack;
        }
        Slot slot = items.get(slotIndex);
        ItemStack stack = slot.stack;
        items.set(slotIndex, Slot.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slotIndex, ItemStack stack) {
        if (slotIndex == SLOT_PLANT_MATTER) {
            plantMatterStack = stack;
        } else {
            items.set(slotIndex, items.get(slotIndex).withStack(stack));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        plantMatterStack = ItemStack.EMPTY;
        items = NonNullList.withSize(EGG_SLOT_COUNT, Slot.EMPTY);
    }

    public void tick(Level world, BlockPos pos, BlockState state, IncubatorBlockEntity blockEntity) {
        if (!world.isClientSide) {
            blockEntity.intakePlantMatter();
            if (level.getGameTime() % getTicksPerPercent() == 0) {
                blockEntity.incubateEggs();
            }
        }
    }

    /** Converts one plant-matter item per tick into the internal plant-matter level. */
    private void intakePlantMatter() {
        if (!getItem(SLOT_PLANT_MATTER).isEmpty()) {
            if (plantMatter < getMaxPlantMatter()) {
                plantMatter += 1;
                getItem(SLOT_PLANT_MATTER).shrink(1);
                updateBlock();
            }
        }
    }

    /**
     * Advances every unincubated egg by one percent step; finished eggs convert into incubated
     * eggs.
     *
     * <p>TODO(BUG): the finished egg's incubation progress is set to -1 (the "not set" sentinel)
     * rather than being left at/clamped to 1.
     */
    private void incubateEggs() {
        if (items.stream().anyMatch(slot -> !slot.stack.isEmpty())) {
            getEnergyStorage().internalExtract(calculateEnergyConsumption(), true);
            for (int i = 0; i < getSlotCount(); i++) {
                Slot slot = items.get(i);
                if (!slot.stack.isEmpty() && slot.stack.is(ItemInit.UNINCUBATED_EGG.get())) {
                    DinoData data = DinoData.fromStack(slot.stack);
                    if (data.getIncubationProgress() < 1) {
                        data.setIncubationProgress(
                                data.getIncubationProgress() + INCUBATION_PROGRESS_PER_STEP);
                        data.setIncubationTimeLeft(Mth.floor(
                                getTicksPerPercent() * ((1 - data.getIncubationProgress()) * 100)));
                        data.toStack(slot.stack);
                        updateBlock();
                    } else if (data.getIncubationProgress() >= 1) {
                        ItemStack dinoEgg = ItemInit.INCUBATED_EGG.get().getDefaultInstance();
                        data.setIncubationProgress(-1);
                        data.setIncubationTimeLeft(-1);
                        data.toStack(dinoEgg);
                        slot = slot.withStack(dinoEgg);
                        items.set(i, slot);
                        updateBlock();
                    }
                }
            }
        }
    }

    public int calculateEnergyConsumption() {
        int consumption = BASE_ENERGY_CONSUMPTION;
        if (tankStack.getItem() == ItemInit.GOLD_PLANT_TANK.get()) {
            consumption += GOLD_TANK_EXTRA_ENERGY;
        }
        if (tankStack.getItem() == ItemInit.IRON_PLANT_TANK.get()) {
            consumption += IRON_TANK_EXTRA_ENERGY;
        }
        if (containerStack.getItem() == ItemInit.SMALL_CONTAINER_UPGRADE.get()) {
            consumption += SMALL_CONTAINER_EXTRA_ENERGY;
        }
        if (containerStack.getItem() == ItemInit.LARGE_CONTAINER_UPGRADE.get()) {
            consumption += LARGE_CONTAINER_EXTRA_ENERGY;
        }
        if (bulbStack.getItem() == ItemInit.WARM_BULB.get()) {
            consumption += WARM_BULB_EXTRA_ENERGY;
        }
        if (bulbStack.getItem() == ItemInit.WARMER_BULB.get()) {
            consumption += WARMER_BULB_EXTRA_ENERGY;
        }
        if (bulbStack.getItem() == ItemInit.HOT_BULB.get()) {
            consumption += HOT_BULB_EXTRA_ENERGY;
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public NonNullList<ItemStack> getMachineParts() {
        return NonNullList.of(ItemStack.EMPTY,
                containerStack, bulbStack, tankStack, nestStack, lidStack, baseStack, armStack);
    }

    /** An egg slot: its stack plus the free-form on-screen position chosen by the player. */
    public record Slot(ItemStack stack, int x, int y) {
        public static final Slot EMPTY = new Slot(ItemStack.EMPTY, 0, -100);

        public Slot withStack(ItemStack stack) {
            return new Slot(stack, x, y);
        }

        public Slot withX(int x) {
            return new Slot(stack, x, y);
        }

        public Slot withY(int y) {
            return new Slot(stack, x, y);
        }
    }
}
