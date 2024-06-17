package net.dumbcode.projectnublar.block.entity;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.block.api.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
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

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int slot) {
            return switch (slot) {
                case 0 -> Mth.floor(SequencerBlockEntity.this.sequencingTime);
                case 1 -> SequencerBlockEntity.this.getTotalSequencingTime();
                case 2 -> SequencerBlockEntity.this.waterLevel;
                case 3 -> SequencerBlockEntity.this.boneMatterLevel;
                case 4 -> SequencerBlockEntity.this.sugarLevel;
                case 5 -> SequencerBlockEntity.this.plantMatterLevel;
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
            }
        }

        public int getCount() {
            return 6;
        }
    };

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

    public void tick(Level world, BlockPos pos, BlockState pState, SequencerBlockEntity be) {
        if (!world.isClientSide) {
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
                updateBlock();
            } else {
                sequencingTime = 0;
                updateBlock();
            }
        }
    }

    @Override
    protected void saveData(CompoundTag tag) {
        tag.put("storage", storage.save(new CompoundTag()));
        tag.put("dna_input", dna_input.save(new CompoundTag()));
        tag.put("empty_vial_output", empty_vial_output.save(new CompoundTag()));
        tag.putFloat("sequencingTime", sequencingTime);
        tag.putBoolean("hasComputer", hasComputer);
        tag.putBoolean("hasDoor", hasDoor);
        tag.putBoolean("hasScreen", hasScreen);
    }

    @Override
    protected void loadData(CompoundTag tag) {
        storage = ItemStack.of(tag.getCompound("storage"));
        dna_input = ItemStack.of(tag.getCompound("dna_input"));
        empty_vial_output = ItemStack.of(tag.getCompound("empty_vial_output"));
        sequencingTime = tag.getFloat("sequencingTime");
        hasComputer = tag.getBoolean("hasComputer");
        hasDoor = tag.getBoolean("hasDoor");
        hasScreen = tag.getBoolean("hasScreen");
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
}
