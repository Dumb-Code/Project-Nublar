package net.dumbcode.projectnublar.block.entity;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.dumbcode.projectnublar.block.api.fence.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.util.LineUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
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
        this.collidableCache = null;
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
            Connection connection = Connection.fromNBT(nbt.getCompound(i), this);
            if(connection.isValid()) {
                this.fenceConnections.add(connection);
            }
        }

        if(this.level != null) {
            this.triggerModelUpdate();
        }
    }

    @Override
    public VoxelShape getOrCreateCollision() {
        if(this.collidableCache == null) {
            VoxelShape shape = Shapes.empty();
            for (Connection connection : this.fenceConnections) {
                shape = Shapes.or(shape, connection.getCollisionShape());
            }
            this.collidableCache = shape;
        }

        return this.collidableCache;
    }

    @Override
    public void addConnection(Connection connection) {
        this.fenceConnections.add(connection);
        this.triggerModelUpdate();
        this.setChanged();
    }

    @Override
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

    /**
     * Breaks the surrounding fence. Used for entities who "attack" the fence.
     *
     * <p>TODO(BUG): the midpoint math relies on integer division ({@code blocks.size() / 2}), so
     * for even-sized runs the "center" is biased. A pre-existing note asked
     * for more randomness here, but the real issue is that the whole code should be completely rewritten.
     * This is extremely unoptimized.
     * 
     * @param intensity Intensity at which the fence breaks.
     */
    public void breakFence(int intensity) {
        for (Connection connection : fenceConnections) {
            for (double offset : connection.getType().getOffsets()) {
                List<BlockPos> blocks = LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), offset);
                for (int k = 0; k < blocks.size(); k++) {
                    for (int i = 0; i < connection.getType().getHeight(); i++) {
                        BlockPos position = blocks.get(k).above(i);
                        if ((k == blocks.size() / 2 - 1 || k == blocks.size() / 2 + 1) && i < intensity / 2 + 1) {
                            this.level.destroyBlock(position, true);
                        } else if (k == blocks.size() / 2 && i < intensity) {
                            this.level.destroyBlock(position, true);
                        }
                    }
                }
            }
        }
    }
}
