package net.dumbcode.projectnublar.block.entity;

import net.blay09.mods.balm.platform.energy.BalmEnergyStorageProvider;
import net.blay09.mods.balm.platform.energy.DefaultEnergyStorage;
import net.blay09.mods.balm.platform.energy.EnergyStorage;
import net.dumbcode.projectnublar.block.ElectricFencePostBlock;
import net.dumbcode.projectnublar.block.api.fence.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.api.fence.FencePowerService;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.util.LineUtils;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Block entity of a fence-post column base. Holds the pole's energy buffer, distributes power to
 * connected poles, and caches the rendered rotation/shape. NBT keys {@code rotation_flipped} and
 * {@code energy} are frozen save contracts.
 */
public class BlockEntityElectricFencePole extends BlockEntityElectricFence implements ConnectableBlockEntity, GeoBlockEntity, BalmEnergyStorageProvider {

    private static final String ROTATION_FLIPPED_TAG = "rotation_flipped";
    private static final String ENERGY_TAG = "energy";

    // Behavioral constants
    private static final int ENERGY_CAPACITY = 350;
    private static final int ENERGY_MAX_TRANSFER = 350;
    private static final int POWERED_DRAIN_PER_TICK = 10;
    /** Above this stored energy the pole shares a 300-energy budget with connected poles. */
    private static final int DISTRIBUTION_THRESHOLD = 300;
    private static final int DISTRIBUTION_BUDGET = 300;
    private static final double HALF_TURN_DEGREES = 180D;
    private static final double FULL_TURN_DEGREES = 360D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public boolean flippedAround;

    public VoxelShape cachedShape = Shapes.block();

    private double cachedRotation = 0;

    public boolean shouldRefreshNextTick = true;

    public boolean isFlippedAround() {
        return flippedAround;
    }

    public VoxelShape getCachedShape() {
        return cachedShape;
    }

    public double getCachedRotation() {
        return cachedRotation;
    }

    public boolean isShouldRefreshNextTick() {
        return shouldRefreshNextTick;
    }

    private NotifyingInsertOnlyEnergyStorage energyStorage;


    public BlockEntityElectricFencePole(BlockPos pos, BlockState state) {
        super(BlockInit.ELECTRIC_FENCE_POST_BLOCK_ENTITY.get(),pos, state);
    }

    @Override
    public void saveData(ValueOutput output) {
        SaveVersion.write(output);
        output.putBoolean(ROTATION_FLIPPED_TAG, this.flippedAround);
        output.putInt(ENERGY_TAG, this.getEnergyStorage().getEnergy());
        super.saveData(output);
    }

    @Override
    public void loadData(ValueInput input) {
        this.flippedAround = input.getBooleanOr(ROTATION_FLIPPED_TAG, false);
        this.getEnergyStorage().setEnergy(input.getIntOr(ENERGY_TAG, 0));
        super.loadData(input);
    }

    @Override
    public boolean removedByFenceRemovers() {
        return false;
    }


    public void setFlippedAround(boolean flippedAround) {
        this.flippedAround = flippedAround;
        this.triggerModelUpdate();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), Blocks.AIR.defaultBlockState(), this.getBlockState(), 3);
        }
    }

    protected static final VoxelShape DEFAULT_SHAPE = Shapes.create(.875 -.03125 * 3,0,.5 - .0625, 1-.03125 * 3,1,.5 + .0625);


    public void tick(Level world, BlockPos blockPos, BlockState pState, BlockEntityElectricFencePole be) {
        if (this.shouldRefreshNextTick) {
            this.shouldRefreshNextTick = false;
            this.triggerModelUpdate();
        }
        double rotation = this.computeRotation();
        if (Double.compare(this.cachedRotation, rotation) != 0) {
            this.cachedRotation = rotation;
            world.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
        if (!world.isClientSide) {
            tickEnergy(world, blockPos);
        }
    }

    private void tickEnergy(Level world, BlockPos blockPos) {
        BlockState state = world.getBlockState(blockPos);
        if (!(state.getBlock() instanceof ElectricFencePostBlock post)
            || state.getValue(post.getIndexProperty()) != 0) {
            return;
        }

        if (this.getEnergyStorage().getEnergy() > 0) {
            consume(POWERED_DRAIN_PER_TICK);
        }

        syncPoweredBlockstates(state);
        if (this.getEnergyStorage().getEnergy() > DISTRIBUTION_THRESHOLD) {
            distributePowerToConnectedPoles();
        }
    }

    /** Mirrors the powered flag onto every block of the column. */
    private void updatePoweredBlockstates(BlockState state, boolean powered) {
        for (int y = 0; y < ((ElectricFencePostBlock) state.getBlock()).getType().getHeight(); y++) {
            BlockPos pos = this.getBlockPos().above(y);
            BlockState s = this.level.getBlockState(pos);
            if (s.getBlock() == state.getBlock() && s.getValue(ElectricFencePostBlock.POWERED_PROPERTY) != powered) {
                this.level.setBlock(pos, s.setValue(ElectricFencePostBlock.POWERED_PROPERTY, powered), 3);
            }
        }
    }

    private void syncPoweredBlockstates(BlockState state) {
        boolean powered = this.getEnergyStorage().getEnergy() > 0;
        if (state.getBlock() instanceof ElectricFencePostBlock
            && state.getValue(ElectricFencePostBlock.POWERED_PROPERTY) != powered) {
            updatePoweredBlockstates(state, powered);
            invalidateConnectedPowerCaches();
        }
    }

    /** Shares a fixed energy budget evenly with the poles at the other ends of our wires. */
    private void distributePowerToConnectedPoles() {
        Set<BlockEntityElectricFencePole> poles = new LinkedHashSet<>();
        for (Connection connection : this.getConnections()) {
            if (!FencePowerService.isRunComplete(this.level, connection)) {
                continue;
            }
            BlockEntityElectricFencePole fromPole = FencePowerService.getBasePole(this.level, connection.getFrom());
            BlockEntityElectricFencePole toPole = FencePowerService.getBasePole(this.level, connection.getTo());
            if (fromPole != null && fromPole != this) {
                poles.add(fromPole);
            }
            if (toPole != null && toPole != this) {
                poles.add(toPole);
            }
        }

        if (poles.isEmpty()) {
            return;
        }

        List<BlockEntityElectricFencePole> list = new ArrayList<>(poles);
        list.sort(Comparator.comparingInt(pole -> pole.getEnergyStorage().getEnergy()));
        int share = DISTRIBUTION_BUDGET / list.size();
        if (share <= 0) {
            return;
        }
        for (BlockEntityElectricFencePole pole : list) {
            EnergyStorage storage = pole.getEnergyStorage();
            int sendEnergy = storage.fill(Math.min(share, this.getEnergyStorage().getEnergy()), true);
            consume(sendEnergy);
            storage.fill(sendEnergy, false);
            pole.updateBlock();
        }
    }

    /**
     * Takes up to {@code amount} out of this pole's insert-only storage (its own drain and its
     * share to other poles), then sends a block update, as the internal extract did on 1.20.1.
     */
    private void consume(int amount) {
        EnergyStorage storage = this.getEnergyStorage();
        storage.setEnergy(storage.getEnergy() - Math.min(amount, storage.getEnergy()));
        this.updateBlock();
    }

    private void invalidateConnectedPowerCaches() {
        this.invalidatePowerCache();
        if (this.level == null) {
            return;
        }
        for (Connection connection : this.getConnections()) {
            for (BlockPos pos : LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset())) {
                BlockEntity blockEntity = this.level.getBlockEntity(pos);
                if (blockEntity instanceof BlockEntityElectricFenceBase fence) {
                    fence.invalidatePowerCache();
                }
            }
        }
    }

    @Override
    public VoxelShape getOrCreateCollision() {
        VoxelShape shape = super.getOrCreateCollision();
        if (shape.isEmpty()) {
            shape = Shapes.or(shape,DEFAULT_SHAPE);
        }
        return shape;
    }


    public double computeRotation() {
        RotationContext context = this.getRotationContext();
        if (context == null) {
            return this.getDefaultRotation();
        }

        double rotation = this.computeConnectionRotation(context.basePole(), context.basePos())
            + context.post().getType().getRotationOffset();
        if (context.basePole().isFlippedAround()) {
            rotation += HALF_TURN_DEGREES;
        }
        return normalizeFullTurn(rotation);
    }

    private RotationContext getRotationContext() {
        BlockState state = this.level != null && this.level.isLoaded(this.getBlockPos())
            ? this.level.getBlockState(this.getBlockPos())
            : this.getBlockState();
        if (!(state.getBlock() instanceof ElectricFencePostBlock post)) {
            return null;
        }

        int index = state.getValue(post.getIndexProperty());
        BlockPos basePos = this.getBlockPos().below(index);
        if (index == 0) {
            return new RotationContext(post, this, basePos);
        }
        if (this.level == null || !this.level.isLoaded(basePos)) {
            return null;
        }

        BlockEntity blockEntity = this.level.getBlockEntity(basePos);
        if (blockEntity instanceof BlockEntityElectricFencePole pole) {
            return new RotationContext(post, pole, basePos);
        }
        return null;
    }

    private double getDefaultRotation() {
        BlockState state = this.getBlockState();
        double rotation = state.getBlock() instanceof ElectricFencePostBlock post
            ? post.getType().getRotationOffset()
            : 0D;
        if (this.flippedAround) {
            rotation += HALF_TURN_DEGREES;
        }
        return normalizeFullTurn(rotation);
    }

    private double computeConnectionRotation(BlockEntityElectricFencePole basePole, BlockPos basePos) {
        List<RunRotation> runs = new ArrayList<>();
        for (Connection connection : basePole.getConnections()) {
            RunRotation run = this.createRunRotation(basePos, connection);
            if (run != null && !containsRun(runs, run)) {
                runs.add(run);
            }
        }

        if (runs.isEmpty()) {
            return 0D;
        }
        if (runs.size() == 1) {
            return runs.get(0).angle();
        }
        return averageLineAngles(runs.get(0).angle(), runs.get(1).angle());
    }

    private RunRotation createRunRotation(BlockPos basePos, Connection connection) {
        if (this.level == null) {
            return null;
        }

        BlockPos fromBase = FencePowerService.getBasePos(this.level, connection.getFrom());
        BlockPos toBase = FencePowerService.getBasePos(this.level, connection.getTo());
        BlockPos otherBase;
        if (fromBase.equals(basePos)) {
            otherBase = toBase;
        } else if (toBase.equals(basePos)) {
            otherBase = fromBase;
        } else {
            return null;
        }

        int dx = otherBase.getX() - basePos.getX();
        int dz = otherBase.getZ() - basePos.getZ();
        if (dx == 0 && dz == 0) {
            return null;
        }

        double angle = normalizeHalfTurn(90D - Math.toDegrees(Math.atan2(dz, dx)));
        return new RunRotation(fromBase, toBase, angle);
    }

    private static boolean containsRun(List<RunRotation> runs, RunRotation candidate) {
        for (RunRotation run : runs) {
            if (run.matches(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static double averageLineAngles(double first, double second) {
        double delta = wrapDegrees(second - first);
        if (delta > 90D) {
            delta -= HALF_TURN_DEGREES;
        } else if (delta < -90D) {
            delta += HALF_TURN_DEGREES;
        }
        return normalizeHalfTurn(first + delta / 2D);
    }

    private static double wrapDegrees(double degrees) {
        double wrapped = degrees % FULL_TURN_DEGREES;
        if (wrapped >= HALF_TURN_DEGREES) {
            wrapped -= FULL_TURN_DEGREES;
        } else if (wrapped < -HALF_TURN_DEGREES) {
            wrapped += FULL_TURN_DEGREES;
        }
        return wrapped;
    }

    private static double normalizeHalfTurn(double degrees) {
        double normalized = degrees % HALF_TURN_DEGREES;
        return normalized < 0D ? normalized + HALF_TURN_DEGREES : normalized;
    }

    private static double normalizeFullTurn(double degrees) {
        double normalized = degrees % FULL_TURN_DEGREES;
        return normalized < 0D ? normalized + FULL_TURN_DEGREES : normalized;
    }

    private record RotationContext(ElectricFencePostBlock post, BlockEntityElectricFencePole basePole, BlockPos basePos) {
    }

    private record RunRotation(BlockPos fromBase, BlockPos toBase, double angle) {
        private boolean matches(RunRotation other) {
            return this.fromBase.equals(other.fromBase) && this.toBase.equals(other.toBase)
                || this.fromBase.equals(other.toBase) && this.toBase.equals(other.fromBase);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    @Override
    public EnergyStorage getEnergyStorage() {
        BlockEntityElectricFencePole basePole = this.getBasePoleForEnergyStorage();
        if (basePole != null && basePole != this) {
            return basePole.getEnergyStorage();
        }
        return energyStorage == null
                ? this.energyStorage = new NotifyingInsertOnlyEnergyStorage(
                        ENERGY_CAPACITY, ENERGY_MAX_TRANSFER, this::onEnergyChanged)
                : this.energyStorage;
    }

    private BlockEntityElectricFencePole getBasePoleForEnergyStorage() {
        if (this.level == null) {
            return this;
        }
        BlockState state = this.level.getBlockState(this.getBlockPos());
        if (state.getBlock() instanceof ElectricFencePostBlock post) {
            int index = state.getValue(post.getIndexProperty());
            if (index > 0) {
                BlockEntity blockEntity = this.level.getBlockEntity(this.getBlockPos().below(index));
                if (blockEntity instanceof BlockEntityElectricFencePole pole) {
                    return pole;
                }
            }
        }
        return this;
    }

    private void onEnergyChanged() {
        this.invalidatePowerCache();
        this.setChanged();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        BlockState state = this.level.getBlockState(this.getBlockPos());
        if (state.getBlock() instanceof ElectricFencePostBlock post
            && state.getValue(post.getIndexProperty()) == 0) {
            syncPoweredBlockstates(state);
        }
    }

    /**
     * An insert-only storage that runs {@code onChange} whenever the stored amount changes: on a
     * fill from a generator or another pole, on the pole's own drain, and on load.
     */
    private static class NotifyingInsertOnlyEnergyStorage extends DefaultEnergyStorage {
        private final Runnable onChange;

        private NotifyingInsertOnlyEnergyStorage(int energyCapacity, int maxInsert, Runnable onChange) {
            super(energyCapacity, maxInsert, 0);
            this.onChange = onChange;
        }

        @Override
        public int fill(int maxFill, boolean simulate) {
            int previous = this.getEnergy();
            int filled = super.fill(maxFill, simulate);
            if (previous != this.getEnergy()) {
                this.onChange.run();
            }
            return filled;
        }

        @Override
        public void setEnergy(int energy) {
            int previous = this.getEnergy();
            super.setEnergy(energy);
            if (previous != this.getEnergy()) {
                this.onChange.run();
            }
        }
    }
}
