package net.dumbcode.projectnublar.block.entity;

import earth.terrarium.botarium.common.energy.EnergyApi;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.dumbcode.projectnublar.block.GeneratorBlock;
import net.dumbcode.projectnublar.block.api.sync.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.menutypes.GeneratorMenu;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorBlockEntity extends SyncingContainerBlockEntity implements BotariumEnergyBlock<WrappedBlockEnergyContainer> {
    private static final String FUEL_TAG = "fuel";
    private static final int CONTAINER_SIZE = 1;
    private static final int STORED_ENERGY_DATA_INDEX = 0;
    private static final int MAX_ENERGY_DATA_INDEX = 1;
    private static final int DATA_COUNT = 2;
    private static final int CREATIVE_ENERGY_INSERT_AMOUNT = 999999;
    private static final int CREATIVE_DISTRIBUTION_LIMIT = 256;
    private static final int FUEL_BURN_INTERVAL_TICKS = 20;
    private static final int ENERGY_PER_FUEL_ITEM = 4;

    private WrappedBlockEnergyContainer energyContainer;
    private ItemStack fuelStack = ItemStack.EMPTY;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.GENERATOR.get(), pos, state);
    }

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int slot) {
            return switch (slot) {
                case STORED_ENERGY_DATA_INDEX -> (int) GeneratorBlockEntity.this.energyContainer.getStoredEnergy();
                case MAX_ENERGY_DATA_INDEX -> (int) GeneratorBlockEntity.this.energyContainer.getMaxCapacity();
                default -> 0;
            };
        }

        @Override
        public void set(int slot, int value) {

        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    @Override
    protected void saveData(CompoundTag tag) {
        tag.put(FUEL_TAG, fuelStack.save(new CompoundTag()));
    }

    @Override
    protected void loadData(CompoundTag tag) {
        fuelStack = ItemStack.of(tag.getCompound(FUEL_TAG));
    }

    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        Block block = getBlockState().getBlock();
        if (block instanceof GeneratorBlock gb && this.energyContainer == null) {
            this.energyContainer =
                    new WrappedBlockEnergyContainer(
                            this,
                            new SimpleEnergyContainer(
                                    gb.getMaxEnergy(), gb.getEnergyOutput(), gb.getEnergyInput()));
        }
        return this.energyContainer;
    }

    public void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (state.getBlock() == BlockInit.CREATIVE_GENERATOR.get()) {
            tickCreativeGenerator();
        } else {
            tickFuelGenerator(level, state, be);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Generator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new GeneratorMenu(pContainerId, pInventory, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return fuelStack.isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return fuelStack;
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return fuelStack.split(pAmount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        fuelStack = pStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
        fuelStack = ItemStack.EMPTY;
    }

    private void tickCreativeGenerator() {
        getEnergyStorage().internalInsert(CREATIVE_ENERGY_INSERT_AMOUNT, false);
        EnergyApi.distributeEnergyNearby(this, CREATIVE_DISTRIBUTION_LIMIT);
    }

    private void tickFuelGenerator(Level level, BlockState state, GeneratorBlockEntity be) {
        if (!fuelStack.isEmpty()) {
            burnFuelItemIfReady(level);
        }
        EnergyApi.distributeEnergyNearby(
                this,
                Math.min(
                        ((GeneratorBlock) state.getBlock()).getEnergyOutput(),
                        be.getEnergyStorage().getStoredEnergy()));
        updateBlock();
    }

    private void burnFuelItemIfReady(Level level) {
        if (level.getGameTime() % FUEL_BURN_INTERVAL_TICKS == 0
                && getEnergyStorage().getStoredEnergy() < getEnergyStorage().getMaxCapacity()) {
            fuelStack.shrink(1);
            getEnergyStorage().internalInsert(ENERGY_PER_FUEL_ITEM, false);
        }
    }
}
