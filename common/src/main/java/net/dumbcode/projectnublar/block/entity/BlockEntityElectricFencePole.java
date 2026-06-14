package net.dumbcode.projectnublar.block.entity;

import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.InsertOnlyEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.dumbcode.projectnublar.block.ElectricFencePostBlock;
import net.dumbcode.projectnublar.block.api.fence.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.api.fence.FencePowerService;
import net.dumbcode.projectnublar.block.api.geometry.MathUtils;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.util.LineUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
public class BlockEntityElectricFencePole extends BlockEntityElectricFence implements ConnectableBlockEntity, GeoBlockEntity, BotariumEnergyBlock<WrappedBlockEnergyContainer> {

    private static final String ROTATION_FLIPPED_TAG = "rotation_flipped";
    private static final String ENERGY_TAG = "energy";

    // Behavioral constants
    private static final int ENERGY_CAPACITY = 350;
    private static final int ENERGY_MAX_TRANSFER = 350;
    private static final int POWERED_DRAIN_PER_TICK = 10;
    /** Above this stored energy the pole shares a 300-energy budget with connected poles. */
    private static final int DISTRIBUTION_THRESHOLD = 300;
    private static final int DISTRIBUTION_BUDGET = 300;

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

    private WrappedBlockEnergyContainer energyContainer;


    public BlockEntityElectricFencePole(BlockPos pos, BlockState state) {
        super(BlockInit.ELECTRIC_FENCE_POST_BLOCK_ENTITY.get(),pos, state);
    }

    @Override
    public void saveData(CompoundTag compound) {
        compound.putBoolean(ROTATION_FLIPPED_TAG, this.flippedAround);
        compound.put(ENERGY_TAG, this.getEnergyStorage().serialize(new CompoundTag()));
        super.saveData(compound);
    }

    @Override
    public void loadData(CompoundTag compound) {
        this.flippedAround = compound.getBoolean(ROTATION_FLIPPED_TAG);
        this.getEnergyStorage().deserialize(compound.getCompound(ENERGY_TAG));
        super.loadData(compound);
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
            this.cachedRotation = this.computeRotation();
        }
        double oldRotation = this.cachedRotation;
        this.cachedRotation = this.computeRotation();
        if (oldRotation != this.cachedRotation) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
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

        if (this.getEnergyStorage().getStoredEnergy() > 0) {
            this.getEnergyStorage().internalExtract(POWERED_DRAIN_PER_TICK, false);
        }

        syncPoweredBlockstates(state);
        if (this.getEnergyStorage().getStoredEnergy() > DISTRIBUTION_THRESHOLD) {
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
        boolean powered = this.getEnergyStorage().getStoredEnergy() > 0;
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

        List<WrappedBlockEnergyContainer> list = new ArrayList<>();
        for (BlockEntityElectricFencePole pole : poles) {
            list.add(pole.getEnergyStorage());
        }
        list.sort(Comparator.comparing(WrappedBlockEnergyContainer::getStoredEnergy));
        long share = DISTRIBUTION_BUDGET / list.size();
        if (share <= 0) {
            return;
        }
        for (WrappedBlockEnergyContainer storage : list) {
            long sendEnergy = storage.internalInsert(this.getEnergyStorage().internalExtract(share, true), true);
            this.getEnergyStorage().internalExtract(sendEnergy, false);
            storage.internalInsert(sendEnergy, false);
        }
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
        double rotation = 0;

        if(this.level == null || !this.level.isLoaded(this.getBlockPos())) {
            return this.flippedAround ? 0F : 180F;
        }

        BlockState state = this.level.getBlockState(this.getBlockPos());
        if (state.getBlock() instanceof ElectricFencePostBlock pole) {
            BlockEntity te = this.level.getBlockEntity(this.getBlockPos().below(state.getValue((pole).getIndexProperty())));
            if (te instanceof BlockEntityElectricFencePole ef) {
                if (!ef.getConnections().isEmpty()) {

                    List<Connection> differingConnections = new ArrayList<>();
                    for (Connection connection : ef.getConnections()) {
                        boolean has = false;
                        for (Connection dc : differingConnections) {
                            if (connection.getFrom().equals(dc.getFrom()) && connection.getTo().equals(dc.getTo())) {
                                has = true;
                                break;
                            }
                        }
                        if (!has) {
                            differingConnections.add(connection);
                        }
                    }

                    if (differingConnections.size() == 1) {
                        Connection connection = differingConnections.get(0);
                        double[] in = connection.getIn();
                        rotation += (float) Math.toDegrees(Math.atan((in[2] - in[3]) / (in[1] - in[0]))) + 90;
                    } else {
                        Connection connection1 = differingConnections.get(0);
                        Connection connection2 = differingConnections.get(1);

                        double[] in1 = connection1.getIn();
                        double[] in2 = connection2.getIn();

                        double angle1 = MathUtils.horizontalDegree(in1[1] - in1[0], in1[2] - in1[3], connection1.getPosition().equals(connection1.getMin()));
                        double angle2 = MathUtils.horizontalDegree(in2[1] - in2[0], in2[2] - in2[3], connection2.getPosition().equals(connection2.getMin()));

                        rotation += (float) (angle1 + (angle2 - angle1) / 2D);
                    }
                }

                rotation += pole.getType().getRotationOffset();
                if (ef.isFlippedAround()) {
                    rotation += 180;
                }
            }
        }
        return rotation;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        BlockEntityElectricFencePole basePole = this.getBasePoleForEnergyStorage();
        if (basePole != null && basePole != this) {
            return basePole.getEnergyStorage();
        }
        return energyContainer == null
                ? this.energyContainer = new WrappedBlockEnergyContainer(
                        this,
                        new NotifyingInsertOnlyEnergyContainer(
                                ENERGY_CAPACITY,
                                ENERGY_MAX_TRANSFER,
                                this::onEnergyChanged))
                : this.energyContainer;
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

    private static class NotifyingInsertOnlyEnergyContainer extends InsertOnlyEnergyContainer {
        private final Runnable onChange;

        private NotifyingInsertOnlyEnergyContainer(long energyCapacity, long maxInsert, Runnable onChange) {
            super(energyCapacity, maxInsert);
            this.onChange = onChange;
        }

        @Override
        public void setEnergy(long energy) {
            long previous = this.getStoredEnergy();
            super.setEnergy(energy);
            if (previous != this.getStoredEnergy()) {
                this.onChange.run();
            }
        }

        @Override
        public void deserialize(CompoundTag root) {
            long previous = this.getStoredEnergy();
            super.deserialize(root);
            if (previous != this.getStoredEnergy()) {
                this.onChange.run();
            }
        }

    }
}
