package net.dumbcode.projectnublar.block.entity;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.block.api.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.init.TagInit;
import net.dumbcode.projectnublar.item.DiskStorageItem;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

public class SequencerBlockEntity extends SyncingContainerBlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack storage = ItemStack.EMPTY;
    private ItemStack dna_input = ItemStack.EMPTY;
    private ItemStack empty_vial_output = ItemStack.EMPTY;
    private ItemStack water = ItemStack.EMPTY;
    private ItemStack bone_matter = ItemStack.EMPTY;
    private ItemStack sugar = ItemStack.EMPTY;
    private ItemStack plant_matter = ItemStack.EMPTY;
    private ItemStack empty_tube_input = ItemStack.EMPTY;
    private ItemStack dna_test_tube_output = ItemStack.EMPTY;
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

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int slot) {
            return switch (slot) {
                case 0 -> Mth.floor(SequencerBlockEntity.this.sequencingTime);
                case 1 -> SequencerBlockEntity.this.getTotalSequencingTime();
                case 2 -> SequencerBlockEntity.this.waterLevel;
                case 3 -> SequencerBlockEntity.this.boneMatterLevel;
                case 4 -> SequencerBlockEntity.this.sugarLevel;
                case 5 -> SequencerBlockEntity.this.plantMatterLevel;
                case 6 -> SequencerBlockEntity.this.synthTime;
                default -> 0;
            };
        }

        public void set(int slot, int value) {
            switch (slot) {
                case 0 -> SequencerBlockEntity.this.sequencingTime = value;
                case 2 -> SequencerBlockEntity.this.waterLevel = value;
                case 3 -> SequencerBlockEntity.this.boneMatterLevel = value;
                case 4 -> SequencerBlockEntity.this.sugarLevel = value;
                case 5 -> SequencerBlockEntity.this.plantMatterLevel = value;
                case 6 -> SequencerBlockEntity.this.synthTime = value;
            }
        }

        public int getCount() {
            return 7;
        }
    };

    public DinoData getDinoData() {
        return dinoData;
    }

    public void setDinoData(DinoData dinoData) {
        this.dinoData = dinoData;
        updateBlock();
    }

    private int getTotalSequencingTime() {
        return storage.isEmpty() ? 0 : ((DiskStorageItem) storage.getItem()).getProcessingTime();
    }

    public SequencerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.SEQUENCER_BLOCK_ENTITY.get(), pos, state);
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

    public void tick(Level world, BlockPos pos, BlockState pState, SequencerBlockEntity be) {
        if (!world.isClientSide) {
            boolean shouldUpdate = false;
            if (!storage.isEmpty() && !dna_input.isEmpty() && ((empty_vial_output.isEmpty() || empty_vial_output.is(dna_input.getItem())) || empty_vial_output.getCount() < 64)) {
                double currentPercent = 0;
                DNAData dnaData = DNAData.loadFromNBT(dna_input.getTag().getCompound("DNAData"));
                String storageName = dnaData.getStorageName();
                DNAData storedDNA = null;
                if (storage.getOrCreateTag().contains(dnaData.getStorageName())) {
                    storedDNA = DNAData.loadFromNBT(storage.getTag().getCompound(storageName));
                    currentPercent = storedDNA.getDnaPercentage();
                }
                if (currentPercent < 1) {
                    sequencingTime++;
                    if (sequencingTime >= getTotalSequencingTime()) {
                        if (empty_vial_output.isEmpty()) {
                            empty_vial_output = new ItemStack(dna_input.getItem());
                        } else {
                            empty_vial_output.grow(1);
                        }
                        if (storedDNA != null) {
                            DNAData combinedDNA = DNAData.combineDNA(storedDNA, dnaData);
                            storage.getOrCreateTag().put(storageName, combinedDNA.saveToNBT(new CompoundTag()));
                        } else {
                            storage.getOrCreateTag().put(storageName, dnaData.saveToNBT(new CompoundTag()));
                        }

                        dna_input.shrink(1);
                        sequencingTime = 0;
                    }
                }
                shouldUpdate = true;
            } else if (sequencingTime != 0) {
                sequencingTime = 0;
                shouldUpdate = true;
            }
            if(water.is(Items.WATER_BUCKET) && waterLevel == 0) {
                waterLevel = 2;
                water = new ItemStack(Items.BUCKET);
                shouldUpdate = true;
            }
            if(bone_matter.is(TagInit.BONE_MATTER) && boneMatterLevel < 48) {
                boneMatterLevel++;
                bone_matter.shrink(1);
                shouldUpdate = true;
            }
            if(sugar.is(TagInit.SUGAR) && sugarLevel < 16) {
                sugarLevel++;
                sugar.shrink(1);
                shouldUpdate = true;
            }
            if(plant_matter.is(TagInit.PLANT_MATTER) && plantMatterLevel < 4) {
                plantMatterLevel++;
                plant_matter.shrink(1);
                shouldUpdate = true;
            }
            if(isSynthesizing && canSynth()){
                synthTime++;
                if(synthTime > 400){
                    synthTime = 0;
                    dna_test_tube_output = new ItemStack(ItemInit.TEST_TUBE_ITEM.get());
                    dna_test_tube_output.getOrCreateTag().put("DinoData", dinoData.toNBT());
                    waterLevel -= 1;
                    boneMatterLevel -= 32;
                    sugarLevel -= 8;
                    plantMatterLevel -= 2;
                    empty_tube_input.shrink(1);
                    isSynthesizing = false;
                }
                shouldUpdate = true;
            }
            if(shouldUpdate) {
                updateBlock();
            }
        }
    }

    @Override
    protected void saveData(CompoundTag tag) {
        tag.put("storage", storage.save(new CompoundTag()));
        tag.put("dna_input", dna_input.save(new CompoundTag()));
        tag.put("empty_vial_output", empty_vial_output.save(new CompoundTag()));
        tag.put("water", water.save(new CompoundTag()));
        tag.put("bone_matter", bone_matter.save(new CompoundTag()));
        tag.put("sugar", sugar.save(new CompoundTag()));
        tag.put("plant_matter", plant_matter.save(new CompoundTag()));
        tag.put("empty_tube_input", empty_tube_input.save(new CompoundTag()));
        tag.put("dna_test_tube_output", dna_test_tube_output.save(new CompoundTag()));
        tag.putFloat("sequencingTime", sequencingTime);
        tag.putBoolean("hasComputer", hasComputer);
        tag.putBoolean("hasDoor", hasDoor);
        tag.putBoolean("hasScreen", hasScreen);
        tag.putInt("waterLevel", waterLevel);
        tag.putInt("boneMatterLevel", boneMatterLevel);
        tag.putInt("sugarLevel", sugarLevel);
        tag.putInt("plantMatterLevel", plantMatterLevel);
        tag.put("dinoData", dinoData.toNBT());
        tag.putBoolean("isSynthesizing", isSynthesizing);
        tag.putInt("synthTime", synthTime);
    }

    @Override
    protected void loadData(CompoundTag tag) {
        storage = ItemStack.of(tag.getCompound("storage"));
        dna_input = ItemStack.of(tag.getCompound("dna_input"));
        empty_vial_output = ItemStack.of(tag.getCompound("empty_vial_output"));
        water = ItemStack.of(tag.getCompound("water"));
        bone_matter = ItemStack.of(tag.getCompound("bone_matter"));
        sugar = ItemStack.of(tag.getCompound("sugar"));
        plant_matter = ItemStack.of(tag.getCompound("plant_matter"));
        empty_tube_input = ItemStack.of(tag.getCompound("empty_tube_input"));
        dna_test_tube_output = ItemStack.of(tag.getCompound("dna_test_tube_output"));
        sequencingTime = tag.getFloat("sequencingTime");
        hasComputer = tag.getBoolean("hasComputer");
        hasDoor = tag.getBoolean("hasDoor");
        hasScreen = tag.getBoolean("hasScreen");
        waterLevel = tag.getInt("waterLevel");
        boneMatterLevel = tag.getInt("boneMatterLevel");
        sugarLevel = tag.getInt("sugarLevel");
        plantMatterLevel = tag.getInt("plantMatterLevel");
        dinoData = DinoData.fromNBT(tag.getCompound("dinoData"));
        isSynthesizing = tag.getBoolean("isSynthesizing");
        synthTime = tag.getInt("synthTime");
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.projectnublar.sequencer").withStyle(ChatFormatting.WHITE);
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new SequencerMenu(i, inventory, null, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return switch (i) {
            case 0 -> storage;
            case 1 -> dna_input;
            case 2 -> empty_vial_output;
            case 3 -> water;
            case 4 -> bone_matter;
            case 5 -> sugar;
            case 6 -> plant_matter;
            case 7 -> empty_tube_input;
            case 8 -> dna_test_tube_output;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        switch (i) {
            case 0 -> {
                if (!storage.isEmpty()) {
                    if (storage.getCount() <= i1) {
                        ItemStack itemstack = storage;
                        storage = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = storage.split(i1);
                    if (storage.isEmpty()) {
                        storage = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 1 -> {
                if (!dna_input.isEmpty()) {
                    if (dna_input.getCount() <= i1) {
                        ItemStack itemstack = dna_input;
                        dna_input = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = dna_input.split(i1);
                    if (dna_input.isEmpty()) {
                        dna_input = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 2 -> {
                if (!empty_vial_output.isEmpty()) {
                    if (empty_vial_output.getCount() <= i1) {
                        ItemStack itemstack = empty_vial_output;
                        empty_vial_output = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = empty_vial_output.split(i1);
                    if (empty_vial_output.isEmpty()) {
                        empty_vial_output = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 3 -> {
                if (!water.isEmpty()) {
                    if (water.getCount() <= i1) {
                        ItemStack itemstack = water;
                        water = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = water.split(i1);
                    if (water.isEmpty()) {
                        water = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 4 -> {
                if (!bone_matter.isEmpty()) {
                    if (bone_matter.getCount() <= i1) {
                        ItemStack itemstack = bone_matter;
                        bone_matter = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = bone_matter.split(i1);
                    if (bone_matter.isEmpty()) {
                        bone_matter = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 5 -> {
                if (!sugar.isEmpty()) {
                    if (sugar.getCount() <= i1) {
                        ItemStack itemstack = sugar;
                        sugar = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = sugar.split(i1);
                    if (sugar.isEmpty()) {
                        sugar = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 6 -> {
                if (!plant_matter.isEmpty()) {
                    if (plant_matter.getCount() <= i1) {
                        ItemStack itemstack = plant_matter;
                        plant_matter = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = plant_matter.split(i1);
                    if (plant_matter.isEmpty()) {
                        plant_matter = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 7 -> {
                if (!empty_tube_input.isEmpty()) {
                    if (empty_tube_input.getCount() <= i1) {
                        ItemStack itemstack = empty_tube_input;
                        empty_tube_input = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = empty_tube_input.split(i1);
                    if (empty_tube_input.isEmpty()) {
                        empty_tube_input = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            case 8 -> {
                if (!dna_test_tube_output.isEmpty()) {
                    if (dna_test_tube_output.getCount() <= i1) {
                        ItemStack itemstack = dna_test_tube_output;
                        dna_test_tube_output = ItemStack.EMPTY;
                        return itemstack;
                    }
                    ItemStack itemstack1 = dna_test_tube_output.split(i1);
                    if (dna_test_tube_output.isEmpty()) {
                        dna_test_tube_output = ItemStack.EMPTY;
                    }
                    return itemstack1;
                }
                return ItemStack.EMPTY;
            }
            default -> {
                return ItemStack.EMPTY;
            }
        }
    }


    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        switch (i) {
            case 0 -> {
                storage = itemStack;
                sequencingTime = 0;
            }
            case 1 -> {
                dna_input = itemStack;
                sequencingTime = 0;
            }
            case 2 -> empty_vial_output = itemStack;
            case 3 -> water = itemStack;
            case 4 -> bone_matter = itemStack;
            case 5 -> sugar = itemStack;
            case 6 -> plant_matter = itemStack;
            case 7 -> empty_tube_input = itemStack;
            case 8 -> dna_test_tube_output = itemStack;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        storage = ItemStack.EMPTY;
        dna_input = ItemStack.EMPTY;
        empty_vial_output = ItemStack.EMPTY;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    public boolean canSynth(){
        return !empty_tube_input.isEmpty() && plantMatterLevel >= 2 && sugarLevel >= 8 && boneMatterLevel >= 32 && waterLevel >= 1;
    }
    public void toggleSynth() {
        if(canSynth()) {
            isSynthesizing = !isSynthesizing;
        } else {
            isSynthesizing = false;
            synthTime = 0;
        }
        updateBlock();
    }
}
