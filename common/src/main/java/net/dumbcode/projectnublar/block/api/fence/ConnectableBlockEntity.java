package net.dumbcode.projectnublar.block.api.fence;

import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraft.core.BlockPos;
import java.util.List;
import java.util.Set;

public interface ConnectableBlockEntity {
    void addConnection(Connection connection);

    Set<Connection> getConnections();

    VoxelShape getOrCreateCollision();

    default List<BlockConnectableBase.ConnectionAxisAlignedBB> getOrCreateCollisionBoxes(BlockPos pos) {
        return BlockConnectableBase.createBoundingBox(this.getConnections(), pos);
    }

    default boolean removedByFenceRemovers() {
        return true;
    }
}
