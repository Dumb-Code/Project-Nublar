package net.dumbcode.projectnublar.block.entity;

import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.block.api.fence.BlockConnectableBase;
import net.dumbcode.projectnublar.block.api.fence.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.api.fence.FencePowerService;
import net.dumbcode.projectnublar.block.api.sync.SyncingBlockEntity;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BlockEntityElectricFenceBase extends SyncingBlockEntity implements ConnectableBlockEntity {
    public final Set<Connection> fenceConnections = Sets.newLinkedHashSet();

    volatile VoxelShape collidableCache;
    private List<BlockConnectableBase.ConnectionAxisAlignedBB> collisionBoxesCache;
    private final Map<Connection, Boolean> poweredCache = new LinkedHashMap<>();
    private long poweredCacheGameTime = Long.MIN_VALUE;

    public BlockEntityElectricFenceBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public BlockEntityElectricFenceBase(BlockPos pos, BlockState state) {
        super(BlockInit.ELECTRIC_FENCE_BLOCK_ENTITY.get(),pos, state);
    }

    @Override
    public void addConnection(Connection connection) {
        Connection existing = this.findExistingConnection(connection);
        if (existing != null) {
            if (!connection.isBroken() && existing.setBrokenSilently(false)) {
                this.onConnectionChanged();
                this.setChanged();
            }
            return;
        }

        if (this.fenceConnections.add(connection)) {
            this.onConnectionChanged();
            this.setChanged();
        }
    }

    private Connection findExistingConnection(Connection reference) {
        for (Connection connection : this.fenceConnections) {
            if (connection.equals(reference)) {
                return connection;
            }
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public Set<Connection.CompiledRenderData> compiledRenderData() {
        Set<Connection.CompiledRenderData> compiled = Sets.newLinkedHashSet();
        if (this.level == null) {
            return compiled;
        }
        for (Connection connection : this.getConnections()) {
            compiled.add(connection.compileRenderData(this.level));
        }
        return compiled;
    }

    @Override
    public Set<Connection> getConnections() {
        return Collections.unmodifiableSet(this.fenceConnections);
    }

    @Override
    public VoxelShape getOrCreateCollision() {
        if (this.collidableCache == null) {
            VoxelShape shape = Shapes.empty();
            for (Connection connection : this.fenceConnections) {
                if (!connection.isBroken()) {
                    shape = Shapes.or(shape, connection.getCollisionShape());
                }
            }
            this.collidableCache = shape;
        }
        return this.collidableCache;
    }

    @Override
    public List<BlockConnectableBase.ConnectionAxisAlignedBB> getOrCreateCollisionBoxes(BlockPos pos) {
        if (this.collisionBoxesCache == null) {
            this.collisionBoxesCache = new ArrayList<>();
            for (Connection connection : this.fenceConnections) {
                if (!connection.isBroken()) {
                    this.collisionBoxesCache.addAll(BlockConnectableBase.createBoundingBox(Collections.singleton(connection), pos));
                }
            }
        }
        return this.collisionBoxesCache;
    }

    public boolean isConnectionPowered(Connection connection) {
        if (this.level == null) {
            return false;
        }
        long gameTime = this.level instanceof Level ? this.level.getGameTime() : Long.MIN_VALUE;
        if (this.poweredCacheGameTime != gameTime) {
            this.poweredCache.clear();
            this.poweredCacheGameTime = gameTime;
        }
        return this.poweredCache.computeIfAbsent(connection, c -> FencePowerService.computePowered(this.level, c));
    }

    public void invalidatePowerCache() {
        this.poweredCache.clear();
        this.poweredCacheGameTime = Long.MIN_VALUE;
    }

    public void onConnectionChanged() {
        this.resetCollidableCache();
        this.invalidatePowerCache();
        for (Connection connection : this.fenceConnections) {
            connection.invalidateRenderData();
        }
        this.triggerModelUpdate();
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
        this.collisionBoxesCache = null;
    }
}
