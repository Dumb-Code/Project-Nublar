package net.dumbcode.projectnublar.block.entity;

import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.block.api.IMachineParts;
import net.dumbcode.projectnublar.block.api.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.item.BulbItem;
import net.dumbcode.projectnublar.item.ContainerUpgradeItem;
import net.dumbcode.projectnublar.item.PlantTankItem;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
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

public class IncubatorBlockEntity extends SyncingContainerBlockEntity implements GeoBlockEntity, IMachineParts {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack plantMatterStack = ItemStack.EMPTY;
    private NonNullList<Slot> items = NonNullList.withSize(9, Slot.EMPTY);
    private NonNullList<Integer> eggProgress = NonNullList.withSize(9, 0);
    private int plantMatter = 0;
    private ItemStack containerStack = ItemStack.EMPTY;
    private ItemStack bulbStack = ItemStack.EMPTY;
    private ItemStack tankStack = ItemStack.EMPTY;
    private ItemStack nestStack = ItemStack.EMPTY;
    private ItemStack lidStack = ItemStack.EMPTY;
    private ItemStack baseStack = ItemStack.EMPTY;
    private ItemStack armStack = ItemStack.EMPTY;

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
        public int get(int pIndex) {
            return switch (pIndex) {
                case 0 -> plantMatter;
                case 1 -> getMaxPlantMatter();
                case 2 -> getSlotCount();
                default -> eggProgress.get(pIndex - 3);
            };
        }

        @Override
        public void set(int pIndex, int pValue) {

        }

        @Override
        public int getCount() {
            return 11;
        }
    };

    public int getSlotCount() {
        return containerStack.isEmpty()? 3 : ((ContainerUpgradeItem) containerStack.getItem()).getContainerSize();
    }

    public int getX(int index) {
        return items.get(index).x;
    }

    public int getY(int index) {
        return items.get(index).y;
    }

    public void updateSlot(int index, int x, int y) {
        items.set(index - 1, items.get(index - 1).withX(x).withY(y));
        updateBlock();
    }

    @Override
    protected void saveData(CompoundTag tag) {
        items.forEach(slot -> {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("x", slot.x);
            slotTag.putInt("y", slot.y);
            slot.stack.save(slotTag);
            tag.put("slot" + items.indexOf(slot), slotTag);
        });
        tag.put("plantMatterStack", plantMatterStack.save(new CompoundTag()));
        tag.putInt("plantMatter", plantMatter);
        CompoundTag containerTag = new CompoundTag();
        containerStack.save(containerTag);
        tag.put("containerStack", containerTag);
        CompoundTag bulbTag = new CompoundTag();
        bulbStack.save(bulbTag);
        tag.put("bulbStack", bulbTag);
        CompoundTag tankTag = new CompoundTag();
        tankStack.save(tankTag);
        tag.put("tankStack", tankTag);
        CompoundTag nestTag = new CompoundTag();
        nestStack.save(nestTag);
        tag.put("nestStack", nestTag);
        CompoundTag lidTag = new CompoundTag();
        lidStack.save(lidTag);
        tag.put("lidStack", lidTag);
        CompoundTag baseTag = new CompoundTag();
        baseStack.save(baseTag);
        tag.put("baseStack", baseTag);
        CompoundTag armTag = new CompoundTag();
        armStack.save(armTag);
        tag.put("armStack", armTag);
    }

    @Override
    protected void loadData(CompoundTag tag) {
        for (int i = 0; i < 9; i++) {
            CompoundTag slotTag = tag.getCompound("slot" + i);
            items.set(i, new Slot(ItemStack.of(slotTag), slotTag.getInt("x"), slotTag.getInt("y")));
        }
        plantMatterStack = ItemStack.of(tag.getCompound("plantMatterStack"));
        plantMatter = tag.getInt("plantMatter");
        containerStack = ItemStack.of(tag.getCompound("containerStack"));
        bulbStack = ItemStack.of(tag.getCompound("bulbStack"));
        tankStack = ItemStack.of(tag.getCompound("tankStack"));
        nestStack = ItemStack.of(tag.getCompound("nestStack"));
        lidStack = ItemStack.of(tag.getCompound("lidStack"));
        baseStack = ItemStack.of(tag.getCompound("baseStack"));
        armStack = ItemStack.of(tag.getCompound("armStack"));
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
        return bulbStack.isEmpty() ? 18 * 20 : ((BulbItem) bulbStack.getItem()).getTicksPerPercent();
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Incubator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new IncubatorMenu(pContainerId, pInventory, this, dataAccess, worldPosition);
    }

    public int getMaxPlantMatter() {
        return getTankStack().isEmpty() ? 64 : ((PlantTankItem) getTankStack().getItem()).getMaxPlantMatter();
    }

    @Override
    public int getContainerSize() {
        return 10;
    }

    @Override
    public boolean isEmpty() {
        return plantMatterStack.isEmpty() && items.stream().allMatch(slot -> slot.stack.isEmpty());
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return switch (pSlot) {
            case 9 -> plantMatterStack;
            default -> items.get(pSlot).stack;
        };
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        switch (pSlot) {
            case 9 -> {
                ItemStack stack = plantMatterStack.split(pAmount);
                if (plantMatterStack.isEmpty()) {
                    plantMatterStack = ItemStack.EMPTY;
                }
                return stack;
            }
            default -> {
                Slot slot = items.get(pSlot);
                ItemStack stack = slot.stack.split(pAmount);
                if (slot.stack.isEmpty()) {
                    items.set(pSlot, Slot.EMPTY);
                }
                return stack;
            }
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        switch (pSlot) {
            case 9 -> {
                ItemStack stack = plantMatterStack;
                plantMatterStack = ItemStack.EMPTY;
                return stack;
            }
            default -> {
                Slot slot = items.get(pSlot);
                ItemStack stack = slot.stack;
                items.set(pSlot, Slot.EMPTY);
                return stack;
            }
        }
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        switch (pSlot) {
            case 9 -> plantMatterStack = pStack;
            default -> items.set(pSlot, items.get(pSlot).withStack(pStack));
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
        plantMatterStack = ItemStack.EMPTY;
        items = NonNullList.withSize(9, Slot.EMPTY);
    }

    public void tick(Level world, BlockPos pos, BlockState pState, IncubatorBlockEntity be) {
        if (!world.isClientSide) {
            if (!be.getItem(9).isEmpty()) {
                if (be.plantMatter < be.getMaxPlantMatter()) {
                    be.plantMatter += 1;
                    be.getItem(9).shrink(1);
                    updateBlock();
                }
            }
            if (level.getGameTime() % getTicksPerPercent() == 0)
                if (be.items.stream().anyMatch(slot -> !slot.stack.isEmpty())) {
                    for (int i = 0; i < be.getSlotCount(); i++) {
                        Slot slot = be.items.get(i);
                        if (!slot.stack.isEmpty() && slot.stack.is(ItemInit.UNINCUBATED_EGG.get())) {
                            DinoData data = DinoData.fromStack(slot.stack);
                            if (data.getIncubationProgress() < 1) {
                                data.setIncubationProgress(data.getIncubationProgress() + 0.01);
                                data.setIncubationTimeLeft(Mth.floor(be.getTicksPerPercent() * ((1 - data.getIncubationProgress()) * 100)));
                                data.toStack(slot.stack);
                                updateBlock();
                            } else if (data.getIncubationProgress() >= 1) {
                                ItemStack dinoEgg = ItemInit.INCUBATED_EGG.get().getDefaultInstance();
                                data.setIncubationProgress(-1);
                                data.setIncubationTimeLeft(-1);
                                data.toStack(dinoEgg);
                                slot = slot.withStack(dinoEgg);
                                be.items.set(i, slot);
                                updateBlock();
                            }
                        }
                    }
                }
        }
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
        return NonNullList.of(ItemStack.EMPTY,containerStack, bulbStack, tankStack, nestStack, lidStack, baseStack, armStack);
    }

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
