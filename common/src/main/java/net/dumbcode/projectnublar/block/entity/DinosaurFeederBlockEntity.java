package net.dumbcode.projectnublar.block.entity;

import net.blay09.mods.balm.platform.energy.BalmEnergyStorageProvider;
import net.blay09.mods.balm.platform.energy.EnergyStorage;
import net.dumbcode.projectnublar.block.api.sync.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;

public class DinosaurFeederBlockEntity extends SyncingContainerBlockEntity implements GeoBlockEntity, BalmEnergyStorageProvider {

    private static final String FOOD_SLOT_1_TAG = "food_slot_1";
    private static final String FOOD_SLOT_2_TAG = "food_slot_2";
    private static final String FOOD_SLOT_3_TAG = "food_slot_3";

    private AnimatableInstanceCache INSTANCE = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation CLOSED_IDLE = RawAnimation.begin().thenPlayAndHold("idle");
    protected static final RawAnimation DISPENSE_FOOD = RawAnimation.begin().thenPlayAndHold("open");

    protected PlayState deployAnimController(final AnimationTest<DinosaurFeederBlockEntity> test) {
            if(this.shouldDispenseFood) {
                return test.setAndContinue(DISPENSE_FOOD);
            } else {
                return test.setAndContinue(CLOSED_IDLE);
            }
    }
    private ItemStack foodSlot1 = ItemStack.EMPTY;
    private ItemStack foodSlot2 = ItemStack.EMPTY;
    private ItemStack foodSlot3 = ItemStack.EMPTY;

    private int progress;

    private final int maxProgress = 100;
    public boolean shouldDispenseFood;
    public boolean coolDown = false;
    public boolean shouldDisplayFood;

    public DinosaurFeederBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockInit.FEEDER_BLOCK_ENTITY.get(), pos, blockState);
    }
    public void tick(Level world, BlockPos pos, BlockState pState, DinosaurFeederBlockEntity be) {
        if (this.shouldDispenseFood) {
            progress++;
            if (progress >= maxProgress) {
                progress = 0;
                coolDown = false;
                updateBlock();
            }
        } else if (progress > 0 && !this.shouldDispenseFood) {
            progress = 0;
            updateBlock();
        }
        if(!this.shouldDispenseFood) {
            coolDown = false;
        }
    }
    @Override
    protected Component getDefaultName() {
        return Component.nullToEmpty("Dinosaur Feeder");
    }

    public void setTexture(boolean flag) {
        this.shouldDisplayFood = flag;
        this.setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return null;
    }

    /**
     * Saves the flags and the food slots. Before Task 41 the slots were not saved (the same on 1.20.1), so a reloaded
     * feeder showed food but held none. The client sync sends only the flags ({@link #getUpdateTag}).
     */
    @Override
    protected void saveData(ValueOutput output) {
        SaveVersion.write(output);
        output.putInt("progress", progress);
        output.putBoolean("shouldDispenseFood", shouldDispenseFood);
        output.putBoolean("shouldDisplayFood", shouldDisplayFood);
        output.store(FOOD_SLOT_1_TAG, ItemStack.OPTIONAL_CODEC, foodSlot1);
        output.store(FOOD_SLOT_2_TAG, ItemStack.OPTIONAL_CODEC, foodSlot2);
        output.store(FOOD_SLOT_3_TAG, ItemStack.OPTIONAL_CODEC, foodSlot3);
    }

    @Override
    protected void loadData(ValueInput input) {
        shouldDispenseFood = input.getBooleanOr("shouldDispenseFood", false);
        shouldDisplayFood = input.getBooleanOr("shouldDisplayFood", false);
        progress = input.getIntOr("progress", 0);
        foodSlot1 = input.read(FOOD_SLOT_1_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        foodSlot2 = input.read(FOOD_SLOT_2_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        foodSlot3 = input.read(FOOD_SLOT_3_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    /**
     * Moves as much of the held stack as fits into the slots: a slot that holds the same food grows, an empty slot
     * takes a copy, and the held stack shrinks by what went in. Then the food display is set on the server and synced
     * (the rule of {@link #dispenseFood}: food shows while the hatch is open). Before Task 41 a slot held the player's own
     * stack object and only the client set the display, so the feeder lost its food when the player moved that stack,
     * and the next sync hid the food. Returns whether anything went in.
     */
    public boolean fill(ItemStack held) {
        int before = held.getCount();
        for (int slot = 0; slot < getContainerSize() && !held.isEmpty(); slot++) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                setItem(slot, held.split(getMaxStackSize(held)));
            } else if (ItemStack.isSameItemSameComponents(stack, held)) {
                int moved = Math.min(held.getCount(), getMaxStackSize(stack) - stack.getCount());
                stack.grow(moved);
                held.shrink(moved);
            }
        }
        if (held.getCount() == before) {
            return false;
        }
        setTexture(this.shouldDispenseFood);
        return true;
    }

    /** Whether a slot holds food in this animal's diet (meat for a meat-eater; never for a plant-eater). */
    public boolean holdsFoodFor(Dinosaur dinosaur) {
        if (dinosaur.getDinoDiet() == null) {
            return false;
        }
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = getItem(slot);
            if (!stack.isEmpty() && dinosaur.getDinoDiet().foodMap().containsKey(stack.getItem().getDescriptionId())) {
                return true;
            }
        }
        return false;
    }

    /** The feeder holds no energy yet, as on 1.20.1; a generator next to it gives it nothing. */
    @Override
    public EnergyStorage getEnergyStorage() {
        return null;
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    public void dispenseFood(){
        this.shouldDispenseFood = !this.shouldDispenseFood;
        coolDown = true;
        if(this.shouldDispenseFood) {
            this.setTexture(!this.isEmpty());
        } else {
            this.setTexture(false);
        }
        this.setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public boolean isEmpty() {
        return foodSlot1.isEmpty() && foodSlot2.isEmpty() && foodSlot3.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return switch(i){
            case 0 -> foodSlot1;
            case 1 -> foodSlot2;
            case 2 -> foodSlot3;
            default -> ItemStack.EMPTY;
        };
    }

    /** Takes up to {@code i1} items from slot {@code i}; a change marks the feeder for saving. */
    @Override
    public ItemStack removeItem(int i, int i1) {
        ItemStack removed = switch(i){
            case 0 -> {
                if(!foodSlot1.isEmpty()){
                    ItemStack stack;
                    if(foodSlot1.getCount() <= i1) {
                        stack = foodSlot1;
                        foodSlot1 = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = foodSlot1.split(i1);
                        if(foodSlot1.isEmpty()){
                            foodSlot1 = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            case 1 -> {
                if(!foodSlot2.isEmpty()){
                    ItemStack stack;
                    if(foodSlot2.getCount() <= i1) {
                        stack = foodSlot2;
                        foodSlot2 = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = foodSlot2.split(i1);
                        if(foodSlot2.isEmpty()){
                            foodSlot2 = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            case 2 -> {
                if(!foodSlot3.isEmpty()){
                    ItemStack stack;
                    if(foodSlot3.getCount() <= i1) {
                        stack = foodSlot3;
                        foodSlot3 = ItemStack.EMPTY;
                        yield stack;
                    } else {
                        stack = foodSlot3.split(i1);
                        if(foodSlot3.isEmpty()){
                            foodSlot3 = ItemStack.EMPTY;
                        }
                        yield stack;
                    }
                }
                yield ItemStack.EMPTY;
            }
            default -> ItemStack.EMPTY;
        };
        if (!removed.isEmpty()) {
            this.setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        switch (i){
            case 0:
                foodSlot1 = itemStack;
                break;
            case 1:
                foodSlot2 = itemStack;
                break;
            case 2:
                foodSlot3 = itemStack;
                break;
        }
        // The slots are saved now, so a change marks the feeder for saving.
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        shouldDisplayFood = false;
        foodSlot1 = ItemStack.EMPTY;
        foodSlot2 = ItemStack.EMPTY;
        foodSlot3 = ItemStack.EMPTY;
        this.setChanged();
        if (!level.isClientSide()) {
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag =new CompoundTag();
        tag.putBoolean("shouldDispenseFood", shouldDispenseFood);
        tag.putBoolean("shouldDisplayFood", shouldDisplayFood);
        return tag;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        // The 1.20.1 "if contains" checks: keep the current value when the key is missing.
        shouldDispenseFood = input.getBooleanOr("shouldDispenseFood", shouldDispenseFood);
        shouldDisplayFood = input.getBooleanOr("shouldDisplayFood", shouldDisplayFood);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return super.getUpdatePacket();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this::deployAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return INSTANCE;
    }
}
