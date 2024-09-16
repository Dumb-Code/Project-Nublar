package net.dumbcode.projectnublar.block.entity;

import net.dumbcode.projectnublar.api.NublarMath;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.block.api.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.item.FilterItem;
import net.dumbcode.projectnublar.item.TankItem;
import net.dumbcode.projectnublar.menutypes.ProcessorMenu;
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

public class ProcessorBlockEntity extends SyncingContainerBlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    ItemStack water = ItemStack.EMPTY;
    ItemStack input = ItemStack.EMPTY;
    ItemStack testTube = ItemStack.EMPTY;
    ItemStack filter = ItemStack.EMPTY;
    ItemStack tankUpgrade =  ItemStack.EMPTY;
    ItemStack chipUpgrade = ItemStack.EMPTY;
    NonNullList<ItemStack> output = NonNullList.withSize(9, ItemStack.EMPTY);
    float fluidLevel = 0;;
    int cookingProgress = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int slot) {
            return switch (slot) {
                case 0 -> Mth.floor(ProcessorBlockEntity.this.fluidLevel);
                case 1 -> ProcessorBlockEntity.this.getMaxFluidLevel();
                case 2 -> ProcessorBlockEntity.this.cookingProgress;
                case 3 -> ProcessorBlockEntity.this.getMaxProcessingTime();
                default -> 0;
            };
        }

        public void set(int slot, int value) {
            switch (slot) {
                case 0:
                    ProcessorBlockEntity.this.fluidLevel = value;
                    break;
                case 1:
                    break;
                case 2:
                    ProcessorBlockEntity.this.cookingProgress = value;
                    break;
                case 3:
            }

        }

        public int getCount() {
            return 4;
        }
    };

    public ProcessorBlockEntity(BlockPos $$1, BlockState $$2) {
        super(BlockInit.PROCESSOR_BLOCK_ENTITY.get(), $$1, $$2);
    }

    public void tick(Level world, BlockPos pos, BlockState pState, ProcessorBlockEntity be) {
        if (Mth.floor(be.fluidLevel) <= be.getMaxFluidLevel() - 1000 && be.getItem(0).is(Items.WATER_BUCKET)) {
            be.setItem(0, new ItemStack(Items.BUCKET));
            be.fluidLevel = Mth.clamp(be.fluidLevel + 1000, 0, be.getMaxFluidLevel());
        }
        if (be.fluidLevel > 0 && !be.input.isEmpty() && !be.testTube.isEmpty() && !be.filter.isEmpty()) {
            if (be.cookingProgress < be.getMaxProcessingTime()) {
                be.cookingProgress++;
                be.fluidLevel -= 250f / be.getMaxProcessingTime();
            } else {
                be.cookingProgress = 0;
                ItemStack stack = new ItemStack(ItemInit.TEST_TUBE_ITEM.get());
                DNAData dnaData = DNAData.loadFromNBT(be.input.getTag().getCompound("DNAData"));
                double dnaPercentage = dnaData.getDnaPercentage();
                DNAData ttData = new DNAData();
                ttData.setDnaPercentage(NublarMath.round(dnaPercentage * be.getFilterEfficiency(),2));
                ttData.setEntityType(dnaData.getEntityType());
                ttData.setVariant(dnaData.getVariant());
                stack.getOrCreateTag().put("DNAData", ttData.saveToNBT(new CompoundTag()));
                be.input.shrink(1);
                be.testTube.shrink(1);
                be.addToOutput(stack);
                if(be.filter.isDamageableItem()) {
                    be.filter.setDamageValue(be.filter.getDamageValue() + 1);
                    if (be.filter.getDamageValue() >= be.filter.getMaxDamage()) {
                        be.filter = ItemStack.EMPTY;
                    }
                }
            }
            be.updateBlock();
        }
    }

    public void saveData(CompoundTag pTag) {
        pTag.put("water", water.save(new CompoundTag()));
        pTag.put("input", input.save(new CompoundTag()));
        pTag.put("testTube", testTube.save(new CompoundTag()));
        for (int i = 0; i < output.size(); i++) {
            pTag.put("item" + i, output.get(i).save(new CompoundTag()));
        }
        pTag.putFloat("fluidLevel", fluidLevel);
        pTag.putInt("cookingProgress", cookingProgress);
        pTag.put("filter", filter.save(new CompoundTag()));
        pTag.put("tankUpgrade", tankUpgrade.save(new CompoundTag()));
        pTag.put("chipUpgrade", chipUpgrade.save(new CompoundTag()));
    }

    public void loadData(CompoundTag pTag) {
        water = ItemStack.of(pTag.getCompound("water"));
        input = ItemStack.of(pTag.getCompound("input"));
        testTube = ItemStack.of(pTag.getCompound("testTube"));
        for (int i = 0; i < output.size(); i++) {
            output.set(i, ItemStack.of(pTag.getCompound("item" + i)));
        }
        fluidLevel = pTag.getFloat("fluidLevel");
        cookingProgress = pTag.getInt("cookingProgress");
        filter = ItemStack.of(pTag.getCompound("filter"));
        tankUpgrade = ItemStack.of(pTag.getCompound("tankUpgrade"));
        chipUpgrade = ItemStack.of(pTag.getCompound("chipUpgrade"));
    }


    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.projectnublar.processor").withStyle(ChatFormatting.WHITE);
    }

    @Override
    public int getContainerSize() {
        return 15;
    }

    @Override
    public boolean isEmpty() {
        return water.isEmpty() && input.isEmpty() && testTube.isEmpty() && output.stream().allMatch(ItemStack::isEmpty) && filter.isEmpty() && tankUpgrade.isEmpty() && chipUpgrade.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return switch (i) {
            case 0 -> water;
            case 1 -> input;
            case 2 -> testTube;
            case 12 -> filter;
            case 13 -> tankUpgrade;
            case 14 -> chipUpgrade;
            default -> output.get(i - 3);
        };
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        switch (i) {
            case 0 -> {
                ItemStack stack = water;
                water = ItemStack.EMPTY;
                return stack;
            }
            case 1 -> {
                ItemStack stack = input;
                input = ItemStack.EMPTY;
                return stack;
            }
            case 2 -> {
                ItemStack stack = testTube;
                testTube = ItemStack.EMPTY;
                return stack;
            }
            case 12 -> {
                ItemStack stack = filter;
                filter = ItemStack.EMPTY;
                return stack;
            }
            case 13 -> {
                ItemStack stack = tankUpgrade;
                tankUpgrade = ItemStack.EMPTY;
                return stack;
            }
            case 14 -> {
                ItemStack stack = chipUpgrade;
                chipUpgrade = ItemStack.EMPTY;
                return stack;
            }
            default -> {
                ItemStack stack = output.get(i - 3);
                output.set(i - 3, ItemStack.EMPTY);
                return stack;
            }
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    public double getFilterEfficiency() {
        if(filter.isEmpty()) return 0;
        float damage = (filter.getMaxDamage() - filter.getDamageValue())/ (float)filter.getMaxDamage();
        float effPercent = (0.75f * damage) + 0.25f;
        return filter.isEmpty() ? 0 : ((FilterItem)filter.getItem()).getEfficiency() * effPercent;
    }
    public int getMaxFluidLevel() {
        return tankUpgrade.isEmpty() ? 2000 : ((TankItem)tankUpgrade.getItem()).getFluidAmount();
    }
    public int getMaxProcessingTime() {
        return chipUpgrade.isEmpty() ? 20 * 60 * 4 : ((ComputerChipItem)chipUpgrade.getItem()).getMaxProcessingTime();
    }
    @Override
    public void setItem(int i, ItemStack itemStack) {
        switch (i) {
            case 0 -> {
                water = itemStack;
                if (itemStack.is(Items.WATER_BUCKET)) {
                    if (Mth.floor(fluidLevel) < getMaxFluidLevel() - 1000) {
                        fluidLevel = Mth.clamp(fluidLevel + 1000, 0, getMaxFluidLevel());
                        water = new ItemStack(Items.BUCKET);
                    }
                }
            }
            case 1 -> {
                input = itemStack;
                cookingProgress = 0;
            }
            case 2 -> testTube = itemStack;
            case 12 -> filter = itemStack;
            case 13 -> tankUpgrade = itemStack;
            case 14 -> chipUpgrade = itemStack;
            default -> output.set(i - 3, itemStack);
        }
        updateBlock();
    }

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
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new ProcessorMenu(i, inventory, null, this, dataAccess);
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
