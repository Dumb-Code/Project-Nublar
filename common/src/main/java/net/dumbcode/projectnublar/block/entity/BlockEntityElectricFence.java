package net.dumbcode.projectnublar.block.entity;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.dumbcode.projectnublar.block.api.fence.ConnectionType;
import net.dumbcode.projectnublar.block.api.fence.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.api.fence.FencePowerService;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.util.LineUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Block entity for a wire-only fence block. Persists its {@link Connection}s under the frozen
 * {@code connections} list tag and caches the merged collision shape.
 */
public class BlockEntityElectricFence extends BlockEntityElectricFenceBase implements ConnectableBlockEntity {

    private static final String CONNECTIONS_TAG = "connections";

    public BlockEntityElectricFence(BlockPos pos, BlockState state) {
        super(BlockInit.ELECTRIC_FENCE_BLOCK_ENTITY.get(), pos, state);
    }

    protected BlockEntityElectricFence(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void resetCollidableCache(){
        super.resetCollidableCache();
    }

    @Override
    public void saveData(CompoundTag compound) {
        super.saveData(compound);
        ListTag nbt = new ListTag();
        for (Connection connection : this.fenceConnections) {
            nbt.add(connection.writeToNBT(new CompoundTag()));
        }
        compound.put(CONNECTIONS_TAG, nbt);
    }

    @Override
    public void loadData(CompoundTag compound) {
        super.loadData(compound);
        this.fenceConnections.clear();
        ListTag nbt = compound.getList(CONNECTIONS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < nbt.size(); i++) {
            try {
                Connection connection = Connection.fromNBT(nbt.getCompound(i), this);
                if(connection.isValid()) {
                    this.fenceConnections.add(connection);
                }
            } catch (RuntimeException ignored) {
                // Invalid saved geometry is skipped to keep old or corrupted worlds loadable.
            }
        }

        this.onConnectionChanged();
    }

    @Override
    public VoxelShape getOrCreateCollision() {
        return super.getOrCreateCollision();
    }

    @Override
    public void addConnection(Connection connection) {
        super.addConnection(connection);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Set<Connection.CompiledRenderData> compiledRenderData() {
        return super.compiledRenderData();
    }


    @Override
    public Set<Connection> getConnections() {
        return super.getConnections();
    }

    /** Breaks nearby wire segments around the center of each connected run. */
    public void breakFence(int intensity) {
        if (!(this.level instanceof ServerLevel level) || intensity <= 0) {
            return;
        }

        Set<FenceRunTarget> runs = new LinkedHashSet<>();
        for (Connection connection : this.fenceConnections) {
            BlockPos fromBase = FencePowerService.getBasePos(level, connection.getFrom());
            BlockPos toBase = FencePowerService.getBasePos(level, connection.getTo());
            runs.add(new FenceRunTarget(connection.getType(), fromBase, toBase));
        }

        Map<BlockPos, Set<RunSegment>> affectedSegments = new LinkedHashMap<>();
        for (FenceRunTarget run : runs) {
            collectBreakTargets(run, intensity, affectedSegments);
        }

        for (Map.Entry<BlockPos, Set<RunSegment>> entry : affectedSegments.entrySet()) {
            breakConnectionsAt(level, entry.getKey(), entry.getValue());
        }
    }

    private void collectBreakTargets(FenceRunTarget run, int intensity, Map<BlockPos, Set<RunSegment>> affectedSegments) {
        for (double offset : run.type().getOffsets()) {
            List<BlockPos> baseLayer = LineUtils.getBlocksInbetween(run.fromBase(), run.toBase(), offset);
            if (baseLayer.isEmpty()) {
                continue;
            }

            double center = (baseLayer.size() - 1) / 2.0D;
            for (int index = 0; index < baseLayer.size(); index++) {
                double distanceFromCenter = Math.abs(index - center);
                int verticalLimit = verticalBreakLimit(distanceFromCenter, intensity, run.type().getHeight());
                if (verticalLimit <= 0) {
                    continue;
                }

                for (int y = 0; y < verticalLimit; y++) {
                    BlockPos from = run.fromBase().above(y);
                    BlockPos to = run.toBase().above(y);
                    BlockPos target = baseLayer.get(index).above(y);
                    if (target.equals(from) || target.equals(to)) {
                        continue;
                    }
                    affectedSegments
                        .computeIfAbsent(target, ignored -> new LinkedHashSet<>())
                        .add(new RunSegment(from, to, offset));
                }
            }
        }
    }

    private static int verticalBreakLimit(double distanceFromCenter, int intensity, int height) {
        if (distanceFromCenter <= 0.5D) {
            return Math.min(height, intensity);
        }
        if (distanceFromCenter <= 1.5D) {
            return Math.min(height, intensity / 2 + 1);
        }
        return 0;
    }

    private static void breakConnectionsAt(ServerLevel level, BlockPos pos, Set<RunSegment> affectedSegments) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ConnectableBlockEntity connectable)) {
            return;
        }

        boolean changed = false;
        boolean hasLiveConnection = false;
        for (Connection connection : connectable.getConnections()) {
            if (matchesAny(affectedSegments, connection)) {
                changed |= connection.setBrokenSilently(true);
            }
            hasLiveConnection |= !connection.isBroken();
        }

        if (!changed) {
            return;
        }

        if (blockEntity instanceof BlockEntityElectricFenceBase fence) {
            fence.onConnectionChanged();
        }
        blockEntity.setChanged();

        BlockState state = level.getBlockState(pos);
        if (!hasLiveConnection && state.is(BlockInit.ELECTRIC_FENCE.get())) {
            level.destroyBlock(pos, true);
        } else {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private static boolean matchesAny(Set<RunSegment> affectedSegments, Connection connection) {
        for (RunSegment segment : affectedSegments) {
            if (segment.matches(connection)) {
                return true;
            }
        }
        return false;
    }

    private record FenceRunTarget(ConnectionType type, BlockPos fromBase, BlockPos toBase) {}

    private record RunSegment(BlockPos from, BlockPos to, double offset) {
        private boolean matches(Connection connection) {
            return Double.compare(this.offset, connection.getOffset()) == 0
                && ((this.from.equals(connection.getFrom()) && this.to.equals(connection.getTo()))
                    || (this.from.equals(connection.getTo()) && this.to.equals(connection.getFrom())));
        }
    }
}
