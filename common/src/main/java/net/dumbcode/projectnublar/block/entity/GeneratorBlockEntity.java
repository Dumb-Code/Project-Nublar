package net.dumbcode.projectnublar.block.entity;

import earth.terrarium.botarium.common.energy.EnergyApi;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.dumbcode.projectnublar.block.GeneratorBlock;
import net.dumbcode.projectnublar.block.api.sync.SyncingBlockEntity;
import net.dumbcode.projectnublar.block.api.sync.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.menutypes.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorBlockEntity extends SyncingContainerBlockEntity implements BotariumEnergyBlock<WrappedBlockEnergyContainer> {


    private WrappedBlockEnergyContainer energyContainer;
    private ItemStack fuelStack = ItemStack.EMPTY;


    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.GENERATOR.get(), pos, state);
    }
    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int slot) {
            return switch (slot) {
                case 0 -> (int)GeneratorBlockEntity.this.energyContainer.getStoredEnergy();
                case 1 -> (int)GeneratorBlockEntity.this.energyContainer.getMaxCapacity();
                default -> 0;
            };
        }

        public void set(int slot, int value) {

        }

        public int getCount() {
            return 2;
        }
    };



    @Override
    protected void saveData(CompoundTag tag) {
        tag.put("fuel",fuelStack.save(new CompoundTag()));
    }

    @Override
    protected void loadData(CompoundTag tag) {
        fuelStack = ItemStack.of(tag.getCompound("fuel"));
    }

    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        Block block = getBlockState().getBlock();
        if (block instanceof GeneratorBlock gb && this.energyContainer == null) {
            this.energyContainer = new WrappedBlockEnergyContainer(this, new SimpleEnergyContainer(gb.getMaxEnergy(), gb.getEnergyOutput(), gb.getEnergyInput()));
        }
        return this.energyContainer;
    }

    public void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (state.getBlock() == BlockInit.CREATIVE_GENERATOR.get()) {
            getEnergyStorage().internalInsert(999999, false);
            EnergyApi.distributeEnergyNearby(this, 256);
        } else {
            if(!fuelStack.isEmpty()){
                if(level.getGameTime() % 20 == 0 && getEnergyStorage().getStoredEnergy() < getEnergyStorage().getMaxCapacity()){
                    fuelStack.shrink(1);
                    getEnergyStorage().internalInsert(4,false);
                }
            }
            EnergyApi.distributeEnergyNearby(this, Math.min(((GeneratorBlock)state.getBlock()).getEnergyOutput(),be.getEnergyStorage().getStoredEnergy()));
            updateBlock();
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Generator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new GeneratorMenu(pContainerId,pInventory, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return 1;
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
}
