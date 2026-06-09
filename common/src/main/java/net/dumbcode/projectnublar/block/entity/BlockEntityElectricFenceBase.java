package net.dumbcode.projectnublar.block.entity;

import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.block.api.fence.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.api.sync.SyncingBlockEntity;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BlockEntityElectricFenceBase extends SyncingBlockEntity implements ConnectableBlockEntity {
    public final Set<Connection> fenceConnections = Sets.newLinkedHashSet();

    volatile VoxelShape collidableCache;

    public BlockEntityElectricFenceBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public BlockEntityElectricFenceBase(BlockPos pos, BlockState state) {
        super(BlockInit.ELECTRIC_FENCE_BLOCK_ENTITY.get(),pos, state);
    }

    @Override
    public void addConnection(Connection connection) {
        this.fenceConnections.add(connection);
    }
    @OnlyIn(Dist.CLIENT)
    public Set<Connection.CompiledRenderData> compiledRenderData() {
        return this.getConnections().stream()
                .map(c -> c.compileRenderData(this.level))
                .collect(Collectors.toSet());
    }
    @Override
    public Set<Connection> getConnections() {
        return Collections.unmodifiableSet(this.fenceConnections);
    }

    @Override
    public VoxelShape getOrCreateCollision() {
        return null;
    }

    @Override
    protected void saveData(CompoundTag tag) {

    }

    @Override
    protected void loadData(CompoundTag tag) {

    }

    public void triggerModelUpdate(){

    }

    public void resetCollidableCache(){
        this.collidableCache = null;
    }
}
