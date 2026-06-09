package net.dumbcode.projectnublar.block.api.fence;

import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;

public interface ConnectableBlockEntity {
    void addConnection(Connection connection);

    Set<Connection> getConnections();

    VoxelShape getOrCreateCollision();

    default boolean removedByFenceRemovers() {
        return true;
    }
}
