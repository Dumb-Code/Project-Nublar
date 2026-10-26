package net.dumbcode.projectnublar.block.entity;

import net.blay09.mods.balm.platform.energy.BalmEnergyStorageProvider;
import net.blay09.mods.balm.platform.energy.DefaultEnergyStorage;
import net.blay09.mods.balm.platform.energy.EnergyStorage;
import net.dumbcode.projectnublar.block.GeneratorBlock;
import net.dumbcode.projectnublar.block.api.sync.SyncingContainerBlockEntity;
import net.dumbcode.projectnublar.menutypes.GeneratorMenu;
import net.dumbcode.projectnublar.platform.Services;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The coal and creative generators. The coal generator burns one fuel item every 20 ticks for 4
 * energy; the creative generator refills itself every tick. Both push energy into their six
 * neighbours every tick.
 *
 * <p>The storage takes no energy from outside ({@code canFill()} follows
 * {@link GeneratorBlock#getEnergyInput()}, 0 for both generators) and gives up to
 * {@link GeneratorBlock#getEnergyOutput()} per drain. The stored energy is not saved, as on 1.20.1.
 *
 * <p>Push path: a neighbour that is a {@link BalmEnergyStorageProvider} (every Nublar block that
 * holds energy) is filled directly in common code, so Nublar-to-Nublar power never depends on a
 * loader capability or on the Team Reborn Energy switch. Any other neighbour goes through
 * {@link net.dumbcode.projectnublar.platform.services.IPlatformHelper#insertEnergy}. Balm 26.2.0.9
 * has a block lookup ({@code Balm.capabilities().getCapability(level, pos, state, blockEntity, side,
 * CommonCapabilities.ENERGY_STORAGE)}), but the generator does not use it: on Fabric it is Balm's own
 * {@code balm:energy_storage} lookup, and Balm's Team Reborn Energy bridge goes one way only (it shows
 * Balm storages to Team Reborn Energy). So it does not find a block that has Team Reborn Energy
 * only, and the 1.20.1 energy library did reach those blocks.
 */
public class GeneratorBlockEntity extends SyncingContainerBlockEntity implements BalmEnergyStorageProvider {
    private static final String FUEL_TAG = "fuel";
    private static final int CONTAINER_SIZE = 1;
    private static final int STORED_ENERGY_DATA_INDEX = 0;
    private static final int MAX_ENERGY_DATA_INDEX = 1;
    private static final int DATA_COUNT = 2;
    private static final int CREATIVE_ENERGY_INSERT_AMOUNT = 999999;
    private static final int CREATIVE_DISTRIBUTION_LIMIT = 256;
    private static final int FUEL_BURN_INTERVAL_TICKS = 20;
    private static final int ENERGY_PER_FUEL_ITEM = 4;

    private EnergyStorage energyStorage;
    private ItemStack fuelStack = ItemStack.EMPTY;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.GENERATOR.get(), pos, state);
    }

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int slot) {
            return switch (slot) {
                case STORED_ENERGY_DATA_INDEX -> GeneratorBlockEntity.this.energyStorage.getEnergy();
                case MAX_ENERGY_DATA_INDEX -> GeneratorBlockEntity.this.energyStorage.getCapacity();
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
    protected void saveData(ValueOutput output) {
        SaveVersion.write(output);
        output.store(FUEL_TAG, ItemStack.OPTIONAL_CODEC, fuelStack);
    }

    @Override
    protected void loadData(ValueInput input) {
        fuelStack = input.read(FUEL_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        Block block = getBlockState().getBlock();
        if (block instanceof GeneratorBlock gb && this.energyStorage == null) {
            this.energyStorage =
                    new DefaultEnergyStorage(gb.getMaxEnergy(), gb.getEnergyInput(), gb.getEnergyOutput());
        }
        return this.energyStorage;
    }

    /** Runs one generator tick: makes energy, then pushes it into the neighbours. */
    public void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (state.getBlock() == BlockInit.CREATIVE_GENERATOR.get()) {
            tickCreativeGenerator(level, pos);
        } else {
            tickFuelGenerator(level, pos, state, be);
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

    /**
     * Drops nothing when the generator is broken, as on 1.20.1. 26.2 drops a container's items by
     * default.
     */
    // TODO(BUG): 1.20.1 loses the fuel when the generator is broken. Deleting this override makes the
    // fuel drop (the 26.2 default), which is a gameplay change for the lead to decide.
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    }

    private void tickCreativeGenerator(Level level, BlockPos pos) {
        generate(CREATIVE_ENERGY_INSERT_AMOUNT);
        pushToNeighbours(level, pos, CREATIVE_DISTRIBUTION_LIMIT);
    }

    private void tickFuelGenerator(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (!fuelStack.isEmpty()) {
            burnFuelItemIfReady(level);
        }
        pushToNeighbours(
                level,
                pos,
                Math.min(
                        ((GeneratorBlock) state.getBlock()).getEnergyOutput(),
                        be.getEnergyStorage().getEnergy()));
        updateBlock();
    }

    private void burnFuelItemIfReady(Level level) {
        if (level.getGameTime() % FUEL_BURN_INTERVAL_TICKS == 0
                && getEnergyStorage().getEnergy() < getEnergyStorage().getCapacity()) {
            fuelStack.shrink(1);
            generate(ENERGY_PER_FUEL_ITEM);
        }
    }

    /**
     * Adds energy the generator made itself. It skips the storage's fill limit (0 for both
     * generators) and stops at the capacity, as the internal insert did on 1.20.1.
     */
    private void generate(int amount) {
        EnergyStorage storage = getEnergyStorage();
        storage.setEnergy(Math.min(storage.getCapacity(), storage.getEnergy() + amount));
        updateBlock();
    }

    /**
     * Pushes up to {@code amount} energy into the six neighbours with the split rule of the 1.20.1
     * energy library's {@code EnergyApi.distributeEnergyNearby}: neighbours in
     * {@link Direction#values()} order, sorted (stable) by how much of {@code amount} each would
     * take, least first; each one gets an even share of what is left, so what one cannot take goes
     * to the next ones. The
     * generator gives up only what a neighbour accepted. A neighbour that takes nothing is left
     * out: under the 1.20.1 rule it sorted first and took nothing, so the totals are the same.
     */
    private void pushToNeighbours(Level level, BlockPos pos, int amount) {
        EnergyStorage storage = getEnergyStorage();
        int remaining = storage.drain(amount, true);
        if (remaining == 0) {
            return;
        }
        List<Receiver> receivers = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            int wouldTake = insertInto(level, pos.relative(direction), direction.getOpposite(), amount, true);
            if (wouldTake > 0) {
                receivers.add(new Receiver(direction, wouldTake));
            }
        }
        receivers.sort(Comparator.comparingInt(Receiver::wouldTake));
        int receiversLeft = receivers.size();
        for (Receiver receiver : receivers) {
            BlockPos target = pos.relative(receiver.direction());
            Direction side = receiver.direction().getOpposite();
            int offered = storage.drain(remaining / receiversLeft, true);
            int accepted = insertInto(level, target, side, offered, true);
            if (accepted > 0) {
                storage.drain(accepted, false);
                insertInto(level, target, side, accepted, false);
            }
            remaining -= accepted;
            receiversLeft--;
        }
    }

    /**
     * Offers energy to the block at {@code pos} on {@code side}: directly when it is a
     * {@link BalmEnergyStorageProvider}, otherwise through the loader's energy lookup.
     */
    private static int insertInto(Level level, BlockPos pos, Direction side, int amount, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof BalmEnergyStorageProvider provider) {
            EnergyStorage storage = provider.getEnergyStorage(side);
            return storage == null ? 0 : storage.fill(amount, simulate);
        }
        return Services.PLATFORM.insertEnergy(level, pos, side, amount, simulate);
    }

    /** A neighbour in the push, with how much of the full amount it would take. */
    private record Receiver(Direction direction, int wouldTake) {
    }
}
