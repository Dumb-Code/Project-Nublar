package net.dumbcode.projectnublar.block.entity;

import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.InsertOnlyEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import java.util.function.Consumer;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.block.api.multiblock.IMachineParts;
import net.dumbcode.projectnublar.block.api.sync.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.item.DiskStorageItem;
import net.dumbcode.projectnublar.item.TankItem;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.registry.TagInit;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The DNA sequencer machine. It has three concerns, ticked in this order on the server:
 *
 * <ol>
 *   <li><b>Sequencing</b>: drains DNA vials from the input slot into the storage drive.
 *   <li><b>Resource intake</b>: converts water buckets / bone matter / sugar / plant matter items
 *       into internal fluid/resource levels.
 *   <li><b>Synthesis</b>: while toggled on and resourced, fills an empty test tube with the
 *       configured {@link DinoData}.
 * </ol>
 *
 * <p>All NBT tag names (including the snake_case ones mirroring old field names) are frozen save
 * contracts. Container slot indices 0–8 and ContainerData indices 0–9 are frozen menu contracts.
 */
public class SequencerBlockEntity extends SyncingContainerBlockEntity
        implements GeoBlockEntity, IMachineParts, BotariumEnergyBlock<WrappedBlockEnergyContainer> {

    // Container slot indices (frozen contract with SequencerMenu).
    public static final int SLOT_STORAGE = 0;
    public static final int SLOT_DNA_INPUT = 1;
    public static final int SLOT_EMPTY_VIAL_OUTPUT = 2;
    public static final int SLOT_WATER = 3;
    public static final int SLOT_BONE_MATTER = 4;
    public static final int SLOT_SUGAR = 5;
    public static final int SLOT_PLANT_MATTER = 6;
    public static final int SLOT_EMPTY_TUBE_INPUT = 7;
    public static final int SLOT_DNA_TEST_TUBE_OUTPUT = 8;

    // ContainerData indices (frozen sync contract with SequencerMenu/SequencerScreen).
    public static final int DATA_SEQUENCING_TIME = 0;
    public static final int DATA_TOTAL_SEQUENCING_TIME = 1;
    public static final int DATA_WATER_LEVEL = 2;
    public static final int DATA_BONE_MATTER_LEVEL = 3;
    public static final int DATA_SUGAR_LEVEL = 4;
    public static final int DATA_PLANT_MATTER_LEVEL = 5;
    public static final int DATA_SYNTH_TIME = 6;
    public static final int DATA_MAX_PLANT_MATTER_LEVEL = 7;
    public static final int DATA_MAX_WATER_LEVEL = 8;
    public static final int DATA_MAX_SYNTH_TIME = 9;
    public static final int DATA_COUNT = 10;

    // NBT tag names (frozen save contracts).
    private static final String STORAGE_TAG = "storage";
    private static final String DNA_INPUT_TAG = "dna_input";
    private static final String EMPTY_VIAL_OUTPUT_TAG = "empty_vial_output";
    private static final String WATER_TAG = "water";
    private static final String BONE_MATTER_TAG = "bone_matter";
    private static final String SUGAR_TAG = "sugar";
    private static final String PLANT_MATTER_TAG = "plant_matter";
    private static final String EMPTY_TUBE_INPUT_TAG = "empty_tube_input";
    private static final String DNA_TEST_TUBE_OUTPUT_TAG = "dna_test_tube_output";
    private static final String SEQUENCING_TIME_TAG = "sequencingTime";
    private static final String HAS_COMPUTER_TAG = "hasComputer";
    private static final String HAS_DOOR_TAG = "hasDoor";
    private static final String HAS_SCREEN_TAG = "hasScreen";
    private static final String WATER_LEVEL_TAG = "waterLevel";
    private static final String BONE_MATTER_LEVEL_TAG = "boneMatterLevel";
    private static final String SUGAR_LEVEL_TAG = "sugarLevel";
    private static final String PLANT_MATTER_LEVEL_TAG = "plantMatterLevel";
    private static final String DINO_DATA_TAG = "DinoData";
    private static final String IS_SYNTHESIZING_TAG = "isSynthesizing";
    private static final String SYNTH_TIME_TAG = "synthTime";
    private static final String COMPUTER_CHIP_TAG = "computer_chip";
    private static final String TANK_TAG = "tank";

    // Behavioral constants
    private static final int DEFAULT_MAX_SYNTH_TIME_TICKS = 10 * 20 * 60;
    private static final int DEFAULT_MAX_WATER_LEVEL = 1000;
    private static final int DEFAULT_MAX_PLANT_MATTER_LEVEL = 16;
    private static final int WATER_PER_BUCKET = 1000;
    private static final int SYNTH_WATER_COST = 500;
    private static final int SYNTH_RESOURCE_COST = 8;
    private static final int BASE_ENERGY_CONSUMPTION = 32;
    private static final int GOLD_CHIP_EXTRA_ENERGY = 32;
    private static final int IRON_CHIP_EXTRA_ENERGY = 16;
    private static final int ENERGY_CAPACITY = 1000;
    private static final int ENERGY_MAX_TRANSFER = 1000;
    private static final int VIAL_OUTPUT_STACK_LIMIT = 64;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private ItemStack storage = ItemStack.EMPTY;
    private ItemStack dnaInput = ItemStack.EMPTY;
    private ItemStack emptyVialOutput = ItemStack.EMPTY;
    private ItemStack water = ItemStack.EMPTY;
    private ItemStack boneMatter = ItemStack.EMPTY;
    private ItemStack sugar = ItemStack.EMPTY;
    private ItemStack plantMatter = ItemStack.EMPTY;
    private ItemStack emptyTubeInput = ItemStack.EMPTY;
    private ItemStack dnaTestTubeOutput = ItemStack.EMPTY;
    private ItemStack computerChip = ItemStack.EMPTY;
    private ItemStack tank = ItemStack.EMPTY;

    private float sequencingTime = 0;
    private boolean hasComputer = false;
    private boolean hasDoor = false;
    private boolean hasScreen = false;
    private int waterLevel = 0;
    private int boneMatterLevel = 0;
    private int sugarLevel = 0;
    private int plantMatterLevel = 0;
    private DinoData dinoData = new DinoData();
    private boolean isSynthesizing = false;
    private int synthTime = 0;
    private WrappedBlockEnergyContainer energyContainer;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int slot) {
            return switch (slot) {
                case DATA_SEQUENCING_TIME -> Mth.floor(SequencerBlockEntity.this.sequencingTime);
                case DATA_TOTAL_SEQUENCING_TIME -> SequencerBlockEntity.this.getTotalSequencingTime();
                case DATA_WATER_LEVEL -> SequencerBlockEntity.this.waterLevel;
                case DATA_BONE_MATTER_LEVEL -> SequencerBlockEntity.this.boneMatterLevel;
                case DATA_SUGAR_LEVEL -> SequencerBlockEntity.this.sugarLevel;
                case DATA_PLANT_MATTER_LEVEL -> SequencerBlockEntity.this.plantMatterLevel;
                case DATA_SYNTH_TIME -> SequencerBlockEntity.this.synthTime;
                case DATA_MAX_PLANT_MATTER_LEVEL -> SequencerBlockEntity.this.getMaxPlantMatterLevel();
                case DATA_MAX_WATER_LEVEL -> SequencerBlockEntity.this.getMaxWaterLevel();
                case DATA_MAX_SYNTH_TIME -> SequencerBlockEntity.this.getMaxSynthTime();
                default -> 0;
            };
        }

        @Override
        public void set(int slot, int value) {
            switch (slot) {
                case DATA_SEQUENCING_TIME -> SequencerBlockEntity.this.sequencingTime = value;
                case DATA_WATER_LEVEL -> SequencerBlockEntity.this.waterLevel = value;
                case DATA_BONE_MATTER_LEVEL -> SequencerBlockEntity.this.boneMatterLevel = value;
                case DATA_SUGAR_LEVEL -> SequencerBlockEntity.this.sugarLevel = value;
                case DATA_PLANT_MATTER_LEVEL -> SequencerBlockEntity.this.plantMatterLevel = value;
                case DATA_SYNTH_TIME -> SequencerBlockEntity.this.synthTime = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public SequencerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.SEQUENCER_BLOCK_ENTITY.get(), pos, state);
    }

    public DinoData getDinoData() {
        return dinoData;
    }

    public void setDinoData(DinoData dinoData) {
        this.dinoData = dinoData;
        updateBlock();
    }

    public int getMaxSynthTime() {
        return computerChip.isEmpty()
                ? DEFAULT_MAX_SYNTH_TIME_TICKS
                : ((ComputerChipItem) computerChip.getItem()).getMaxSynthTime();
    }

    public int getMaxWaterLevel() {
        return tank.isEmpty() ? DEFAULT_MAX_WATER_LEVEL : ((TankItem) tank.getItem()).getSynthFluid();
    }

    public int getMaxPlantMatterLevel() {
        return tank.isEmpty()
                ? DEFAULT_MAX_PLANT_MATTER_LEVEL
                : ((TankItem) tank.getItem()).getSynthPlant();
    }

    private int getTotalSequencingTime() {
        return storage.isEmpty() ? 0 : ((DiskStorageItem) storage.getItem()).getProcessingTime();
    }

    public boolean isHasComputer() {
        return hasComputer;
    }

    public void setHasComputer(boolean hasComputer) {
        this.hasComputer = hasComputer;
        updateBlock();
    }

    public boolean isHasDoor() {
        return hasDoor;
    }

    public void setHasDoor(boolean hasDoor) {
        this.hasDoor = hasDoor;
        updateBlock();
    }

    public boolean isHasScreen() {
        return hasScreen;
    }

    public void setHasScreen(boolean hasScreen) {
        this.hasScreen = hasScreen;
        updateBlock();
    }

    public boolean isSynthesizing() {
        return isSynthesizing;
    }

    public void tick(Level world, BlockPos pos, BlockState state, SequencerBlockEntity blockEntity) {
        if (!world.isClientSide) {
            boolean shouldUpdate = false;
            shouldUpdate |= tickSequencing();
            shouldUpdate |= intakeWater();
            shouldUpdate |= intakeBoneMatter();
            shouldUpdate |= intakeSugar();
            shouldUpdate |= intakePlantMatter();
            shouldUpdate |= tickSynthesis();
            if (shouldUpdate) {
                updateBlock();
            }
        }
    }

    /**
     * Sequences the DNA vial in the input slot into the storage drive, one tick of progress at a
     * time, and ejects an empty vial when a sample completes.
     */
    private boolean tickSequencing() {
        if (!storage.isEmpty() && !dnaInput.isEmpty() && dnaInput.hasTag()
                && ((emptyVialOutput.isEmpty() || emptyVialOutput.is(dnaInput.getItem()))
                        || emptyVialOutput.getCount() < VIAL_OUTPUT_STACK_LIMIT)) {
            double currentPercent = 0;
            DNAData dnaData = DNAData.loadFromNBT(dnaInput.getTag().getCompound("DNAData"));
            String storageName = dnaData.getStorageName();
            DNAData storedDna = null;
            if (storage.getOrCreateTag().contains(dnaData.getStorageName())) {
                storedDna = DNAData.loadFromNBT(storage.getTag().getCompound(storageName));
                currentPercent = storedDna.getDnaPercentage();
            }
            if (currentPercent < 1) {
                sequencingTime++;
                if (sequencingTime >= getTotalSequencingTime()) {
                    if (emptyVialOutput.isEmpty()) {
                        emptyVialOutput = new ItemStack(dnaInput.getItem());
                    } else {
                        emptyVialOutput.grow(1);
                    }
                    if (storedDna != null) {
                        DNAData combinedDna = DNAData.combineDNA(storedDna, dnaData);
                        storage.getOrCreateTag().put(storageName, combinedDna.saveToNBT(new CompoundTag()));
                    } else {
                        storage.getOrCreateTag().put(storageName, dnaData.saveToNBT(new CompoundTag()));
                    }

                    dnaInput.shrink(1);
                    sequencingTime = 0;
                }
            }
            return true;
        } else if (sequencingTime != 0) {
            sequencingTime = 0;
            return true;
        }
        return false;
    }

    private boolean intakeWater() {
        if (water.is(Items.WATER_BUCKET) && waterLevel <= getMaxWaterLevel() - WATER_PER_BUCKET) {
            waterLevel += WATER_PER_BUCKET;
            water = new ItemStack(Items.BUCKET);
            return true;
        }
        return false;
    }

    /**
     * Accepts any item in the {@code projectnublar:bone_matter} tag.
     *
     * <p>TODO(BUG): {@code SequencerMenu}'s slot filter only accepts {@code Items.BONE_MEAL}, so
     * other tagged items can be piped in but not inserted through the GUI. Also note the cap is
     * {@code getMaxPlantMatterLevel()}, not a dedicated bone-matter cap (same below for sugar).
     */
    private boolean intakeBoneMatter() {
        if (boneMatter.is(TagInit.BONE_MATTER) && boneMatterLevel < getMaxPlantMatterLevel()) {
            boneMatterLevel++;
            boneMatter.shrink(1);
            return true;
        }
        return false;
    }

    private boolean intakeSugar() {
        if (sugar.is(TagInit.SUGAR) && sugarLevel < getMaxPlantMatterLevel()) {
            sugarLevel++;
            sugar.shrink(1);
            return true;
        }
        return false;
    }

    private boolean intakePlantMatter() {
        if (plantMatter.is(TagInit.PLANT_MATTER) && plantMatterLevel < getMaxPlantMatterLevel()) {
            plantMatterLevel++;
            plantMatter.shrink(1);
            return true;
        }
        return false;
    }

    /**
     * Advances synthesis while toggled on and resourced; on completion produces the DNA test tube
     * and consumes resources.
     *
     * <p>TODO(BUG): completion uses {@code synthTime > getMaxSynthTime()} (strict), so synthesis
     * runs one tick longer than the displayed maximum.
     */
    private boolean tickSynthesis() {
        if (isSynthesizing && canSynth()) {
            synthTime++;
            getEnergyStorage().internalExtract(calculateEnergyConsumption(), true);
            if (synthTime > getMaxSynthTime()) {
                synthTime = 0;
                dnaTestTubeOutput = new ItemStack(ItemInit.TEST_TUBE_ITEM.get());
                dnaTestTubeOutput.getOrCreateTag().put(DINO_DATA_TAG, dinoData.toNBT());
                waterLevel -= SYNTH_WATER_COST;
                boneMatterLevel -= SYNTH_RESOURCE_COST;
                sugarLevel -= SYNTH_RESOURCE_COST;
                plantMatterLevel -= SYNTH_RESOURCE_COST;
                emptyTubeInput.shrink(1);
                isSynthesizing = false;
            }
            return true;
        }
        return false;
    }

    public int calculateEnergyConsumption() {
        int consumption = BASE_ENERGY_CONSUMPTION;
        if (computerChip.getItem() == ItemInit.GOLD_COMPUTER_CHIP.get()) {
            consumption += GOLD_CHIP_EXTRA_ENERGY;
        }
        if (computerChip.getItem() == ItemInit.IRON_COMPUTER_CHIP.get()) {
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
    protected void saveData(CompoundTag tag) {
        tag.put(STORAGE_TAG, storage.save(new CompoundTag()));
        tag.put(DNA_INPUT_TAG, dnaInput.save(new CompoundTag()));
        tag.put(EMPTY_VIAL_OUTPUT_TAG, emptyVialOutput.save(new CompoundTag()));
        tag.put(WATER_TAG, water.save(new CompoundTag()));
        tag.put(BONE_MATTER_TAG, boneMatter.save(new CompoundTag()));
        tag.put(SUGAR_TAG, sugar.save(new CompoundTag()));
        tag.put(PLANT_MATTER_TAG, plantMatter.save(new CompoundTag()));
        tag.put(EMPTY_TUBE_INPUT_TAG, emptyTubeInput.save(new CompoundTag()));
        tag.put(DNA_TEST_TUBE_OUTPUT_TAG, dnaTestTubeOutput.save(new CompoundTag()));
        tag.putFloat(SEQUENCING_TIME_TAG, sequencingTime);
        tag.putBoolean(HAS_COMPUTER_TAG, hasComputer);
        tag.putBoolean(HAS_DOOR_TAG, hasDoor);
        tag.putBoolean(HAS_SCREEN_TAG, hasScreen);
        tag.putInt(WATER_LEVEL_TAG, waterLevel);
        tag.putInt(BONE_MATTER_LEVEL_TAG, boneMatterLevel);
        tag.putInt(SUGAR_LEVEL_TAG, sugarLevel);
        tag.putInt(PLANT_MATTER_LEVEL_TAG, plantMatterLevel);
        tag.put(DINO_DATA_TAG, dinoData.toNBT());
        tag.putBoolean(IS_SYNTHESIZING_TAG, isSynthesizing);
        tag.putInt(SYNTH_TIME_TAG, synthTime);
        tag.put(COMPUTER_CHIP_TAG, computerChip.save(new CompoundTag()));
        tag.put(TANK_TAG, tank.save(new CompoundTag()));
    }

    @Override
    protected void loadData(CompoundTag tag) {
        storage = ItemStack.of(tag.getCompound(STORAGE_TAG));
        dnaInput = ItemStack.of(tag.getCompound(DNA_INPUT_TAG));
        emptyVialOutput = ItemStack.of(tag.getCompound(EMPTY_VIAL_OUTPUT_TAG));
        water = ItemStack.of(tag.getCompound(WATER_TAG));
        boneMatter = ItemStack.of(tag.getCompound(BONE_MATTER_TAG));
        sugar = ItemStack.of(tag.getCompound(SUGAR_TAG));
        plantMatter = ItemStack.of(tag.getCompound(PLANT_MATTER_TAG));
        emptyTubeInput = ItemStack.of(tag.getCompound(EMPTY_TUBE_INPUT_TAG));
        dnaTestTubeOutput = ItemStack.of(tag.getCompound(DNA_TEST_TUBE_OUTPUT_TAG));
        sequencingTime = tag.getFloat(SEQUENCING_TIME_TAG);
        hasComputer = tag.getBoolean(HAS_COMPUTER_TAG);
        hasDoor = tag.getBoolean(HAS_DOOR_TAG);
        hasScreen = tag.getBoolean(HAS_SCREEN_TAG);
        waterLevel = tag.getInt(WATER_LEVEL_TAG);
        boneMatterLevel = tag.getInt(BONE_MATTER_LEVEL_TAG);
        sugarLevel = tag.getInt(SUGAR_LEVEL_TAG);
        plantMatterLevel = tag.getInt(PLANT_MATTER_LEVEL_TAG);
        dinoData = DinoData.fromNBT(tag.getCompound(DINO_DATA_TAG));
        isSynthesizing = tag.getBoolean(IS_SYNTHESIZING_TAG);
        synthTime = tag.getInt(SYNTH_TIME_TAG);
        computerChip = ItemStack.of(tag.getCompound(COMPUTER_CHIP_TAG));
        tank = ItemStack.of(tag.getCompound(TANK_TAG));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.projectnublar.sequencer")
                .withStyle(ChatFormatting.WHITE);
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new SequencerMenu(containerId, inventory, null, this, dataAccess);
    }

    // TODO(BUG): reports 3 while slots 0–8 are used; SequencerMenu works around this with its own
    // slot handling, so the value is load-bearing and must not be "fixed".
    @Override
    public int getContainerSize() {
        return 3;
    }

    // TODO(BUG): always reports non-empty regardless of contents.
    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case SLOT_STORAGE -> storage;
            case SLOT_DNA_INPUT -> dnaInput;
            case SLOT_EMPTY_VIAL_OUTPUT -> emptyVialOutput;
            case SLOT_WATER -> water;
            case SLOT_BONE_MATTER -> boneMatter;
            case SLOT_SUGAR -> sugar;
            case SLOT_PLANT_MATTER -> plantMatter;
            case SLOT_EMPTY_TUBE_INPUT -> emptyTubeInput;
            case SLOT_DNA_TEST_TUBE_OUTPUT -> dnaTestTubeOutput;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return switch (slot) {
            case SLOT_STORAGE -> removeFromSlot(storage, stack -> storage = stack, count);
            case SLOT_DNA_INPUT -> removeFromSlot(dnaInput, stack -> dnaInput = stack, count);
            case SLOT_EMPTY_VIAL_OUTPUT ->
                    removeFromSlot(emptyVialOutput, stack -> emptyVialOutput = stack, count);
            case SLOT_WATER -> removeFromSlot(water, stack -> water = stack, count);
            case SLOT_BONE_MATTER -> removeFromSlot(boneMatter, stack -> boneMatter = stack, count);
            case SLOT_SUGAR -> removeFromSlot(sugar, stack -> sugar = stack, count);
            case SLOT_PLANT_MATTER -> removeFromSlot(plantMatter, stack -> plantMatter = stack, count);
            case SLOT_EMPTY_TUBE_INPUT ->
                    removeFromSlot(emptyTubeInput, stack -> emptyTubeInput = stack, count);
            case SLOT_DNA_TEST_TUBE_OUTPUT ->
                    removeFromSlot(dnaTestTubeOutput, stack -> dnaTestTubeOutput = stack, count);
            default -> ItemStack.EMPTY;
        };
    }

    /**
     * Shared body of the nine formerly copy-pasted {@code removeItem} branches; the per-slot
     * logic was byte-identical apart from the field being mutated.
     */
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
    public void setItem(int slot, ItemStack itemStack) {
        switch (slot) {
            case SLOT_STORAGE -> {
                storage = itemStack;
                sequencingTime = 0;
            }
            case SLOT_DNA_INPUT -> {
                if (!ItemStack.isSameItemSameTags(dnaInput, itemStack)) {
                    sequencingTime = 0;
                }
                dnaInput = itemStack;
            }
            case SLOT_EMPTY_VIAL_OUTPUT -> emptyVialOutput = itemStack;
            case SLOT_WATER -> water = itemStack;
            case SLOT_BONE_MATTER -> boneMatter = itemStack;
            case SLOT_SUGAR -> sugar = itemStack;
            case SLOT_PLANT_MATTER -> plantMatter = itemStack;
            case SLOT_EMPTY_TUBE_INPUT -> emptyTubeInput = itemStack;
            case SLOT_DNA_TEST_TUBE_OUTPUT -> dnaTestTubeOutput = itemStack;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** Note: only clears the first three slots, matching the original behavior. */
    @Override
    public void clearContent() {
        storage = ItemStack.EMPTY;
        dnaInput = ItemStack.EMPTY;
        emptyVialOutput = ItemStack.EMPTY;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public boolean canSynth() {
        return !emptyTubeInput.isEmpty()
                && plantMatterLevel >= SYNTH_RESOURCE_COST
                && sugarLevel >= SYNTH_RESOURCE_COST
                && boneMatterLevel >= SYNTH_RESOURCE_COST
                && waterLevel >= SYNTH_WATER_COST
                && getEnergyStorage().getStoredEnergy() > calculateEnergyConsumption();
    }

    public void toggleSynth() {
        if (canSynth()) {
            isSynthesizing = !isSynthesizing;
        } else {
            isSynthesizing = false;
            synthTime = 0;
        }
        updateBlock();
    }

    @Override
    public NonNullList<ItemStack> getMachineParts() {
        NonNullList<ItemStack> parts = NonNullList.of(
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, computerChip, tank);
        if (hasComputer) {
            parts.set(0, new ItemStack(ItemInit.SEQUENCER_COMPUTER.get()));
        }
        if (hasDoor) {
            parts.set(1, new ItemStack(ItemInit.SEQUENCER_DOOR.get()));
        }
        if (hasScreen) {
            parts.set(2, new ItemStack(ItemInit.SEQUENCER_SCREEN.get()));
        }
        return parts;
    }

    public void setChip(ItemStack chipItem) {
        computerChip = chipItem;
        updateBlock();
    }

    public void setTank(ItemStack tankItem) {
        tank = tankItem;
        updateBlock();
    }
}
