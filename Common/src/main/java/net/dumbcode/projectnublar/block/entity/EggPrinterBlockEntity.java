package net.dumbcode.projectnublar.block.entity;

import net.dumbcode.projectnublar.block.api.IMachineParts;
import net.dumbcode.projectnublar.block.api.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.menutypes.EggPrinterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class EggPrinterBlockEntity extends SyncingContainerBlockEntity implements GeoBlockEntity, IMachineParts {
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack embryoInput = ItemStack.EMPTY;
    private ItemStack bonemealInput = ItemStack.EMPTY;
    private ItemStack eggOutput = ItemStack.EMPTY;
    private ItemStack syringeOutput = ItemStack.EMPTY;
    private int bonemealAmount = 0;
    private int bonemealMax = 30;
    private int progress = 0;
    private int maxProgress = 20 * 60 * 10;
    private boolean isPrinting = false;
    private ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            switch (index){
                case 0:
                    return bonemealAmount;
                case 1:
                    return bonemealMax;
                case 2:
                    return progress;
                case 3:
                    return getMaxProgress();
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index){
                case 0:
                    bonemealAmount = value;
                    break;
                case 1:
                    bonemealMax = value;
                    break;
                case 2:
                    progress = value;
                    break;
                case 3:
                    maxProgress = value;
                    break;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private ItemStack sensor = ItemStack.EMPTY;
    private ItemStack chip = ItemStack.EMPTY;

    public EggPrinterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), pos, state);
    }

    public float getBreakChance() {
        return sensor.isEmpty() ? 0 : -1;
    }
    public int getMaxProgress(){
        return chip.isEmpty() ? maxProgress : ((ComputerChipItem)chip.getItem()).getMaxPrintTime();
    }
    public void tick(Level world, BlockPos pos, BlockState pState, EggPrinterBlockEntity be) {
        boolean shouldUpdate = false;
        if(bonemealAmount < bonemealMax && !bonemealInput.isEmpty()){
            bonemealAmount += 1;
            bonemealInput.shrink(1);
            shouldUpdate = true;
        }
        isPrinting = !embryoInput.isEmpty() && bonemealAmount >= 16 && eggOutput.isEmpty();
        if(isPrinting){
            progress += 1;
            if(progress >= getMaxProgress()){
                progress = 0;
                bonemealAmount-=16;
                isPrinting = false;
                eggOutput = new ItemStack(level.random.nextInt(10) == getBreakChance() ? ItemInit.CRACKED_ARTIFICIAL_EGG.get() : ItemInit.ARTIFICIAL_EGG.get());
                embryoInput = ItemStack.EMPTY;
                if(syringeOutput.isEmpty()) {
                    syringeOutput = new ItemStack(ItemInit.SYRINGE.get());
                } else {
                    syringeOutput.grow(1);
                }
            }
            shouldUpdate = true;
        }
        if(shouldUpdate){
            updateBlock();
        }
    }
    @Override
    protected void saveData(CompoundTag tag) {
        tag.put("embryoInput", embryoInput.save(new CompoundTag()));
        tag.put("bonemealInput", bonemealInput.save(new CompoundTag()));
        tag.put("eggOutput", eggOutput.save(new CompoundTag()));
        tag.put("syringeOutput", syringeOutput.save(new CompoundTag()));
        tag.putInt("bonemealAmount", bonemealAmount);
        tag.putInt("bonemealMax", bonemealMax);
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putBoolean("isPrinting", isPrinting);
    }

    @Override
    protected void loadData(CompoundTag tag) {
        embryoInput = ItemStack.of(tag.getCompound("embryoInput"));
        bonemealInput = ItemStack.of(tag.getCompound("bonemealInput"));
        eggOutput = ItemStack.of(tag.getCompound("eggOutput"));
        syringeOutput = ItemStack.of(tag.getCompound("syringeOutput"));
        bonemealAmount = tag.getInt("bonemealAmount");
        bonemealMax = tag.getInt("bonemealMax");
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
        isPrinting = tag.getBoolean("isPrinting");
    }

    @Override
    protected Component getDefaultName() {
        return Component.nullToEmpty("Egg Printer");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new EggPrinterMenu(pContainerId, pInventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return embryoInput.isEmpty() && bonemealInput.isEmpty() && eggOutput.isEmpty() && syringeOutput.isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return switch (pSlot) {
            case 0 -> embryoInput;
            case 1 -> bonemealInput;
            case 2 -> eggOutput;
            case 3 -> syringeOutput;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return switch (pSlot) {
            case 0 -> {
                if (!embryoInput.isEmpty()) {
                    ItemStack stack;
                    if (embryoInput.getCount() <= pAmount) {
                        stack = embryoInput;
                        embryoInput = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = embryoInput.split(pAmount);
                        if (embryoInput.isEmpty()) {
                            embryoInput = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            case 1 -> {
                if (!bonemealInput.isEmpty()) {
                    ItemStack stack;
                    if (bonemealInput.getCount() <= pAmount) {
                        stack = bonemealInput;
                        bonemealInput = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = bonemealInput.split(pAmount);
                        if (bonemealInput.isEmpty()) {
                            bonemealInput = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            case 2 -> {
                if (!eggOutput.isEmpty()) {
                    ItemStack stack;
                    if (eggOutput.getCount() <= pAmount) {
                        stack = eggOutput;
                        eggOutput = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = eggOutput.split(pAmount);
                        if (eggOutput.isEmpty()) {
                            eggOutput = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            case 3 -> {
                if (!syringeOutput.isEmpty()) {
                    ItemStack stack;
                    if (syringeOutput.getCount() <= pAmount) {
                        stack = syringeOutput;
                        syringeOutput = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = syringeOutput.split(pAmount);
                        if (syringeOutput.isEmpty()) {
                            syringeOutput = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        switch (pSlot){
            case 0:
                embryoInput = pStack;
                break;
            case 1:
                bonemealInput = pStack;
                break;
            case 2:
                eggOutput = pStack;
                break;
            case 3:
                syringeOutput = pStack;
                break;
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
        embryoInput = ItemStack.EMPTY;
        bonemealInput = ItemStack.EMPTY;
        eggOutput = ItemStack.EMPTY;
        syringeOutput = ItemStack.EMPTY;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void setSensor(ItemStack copy) {
        this.sensor = copy;
    }

    public void setChip(ItemStack copy) {
        this.chip = copy;
    }

    @Override
    public NonNullList<ItemStack> getMachineParts() {
        return NonNullList.of(ItemStack.EMPTY, sensor, chip);
    }
}
