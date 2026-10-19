package net.dumbcode.projectnublar.block.api.fence;

import net.dumbcode.projectnublar.block.ElectricFencePostBlock;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFenceBase;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.util.LineUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FencePowerService {

    private FencePowerService() {
    }

    public static boolean isPowered(BlockGetter level, Connection connection) {
        BlockEntity owner = level.getBlockEntity(connection.getPosition());
        if (owner instanceof BlockEntityElectricFenceBase fence) {
            return fence.isConnectionPowered(connection);
        }
        return computePowered(level, connection);
    }

    public static boolean computePowered(BlockGetter level, Connection connection) {
        return isRunComplete(level, connection)
            && (endpointHasEnergy(level, connection.getFrom()) || endpointHasEnergy(level, connection.getTo()));
    }

    public static boolean isRunComplete(BlockGetter level, Connection connection) {
        if (connection.isBroken()) {
            return false;
        }
        for (BlockPos pos : LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset())) {
            if (!isLoaded(level, pos)) {
                return false;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ConnectableBlockEntity connectable) || !hasLiveMatchingConnection(connectable, connection)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasLiveMatchingConnection(ConnectableBlockEntity connectable, Connection reference) {
        for (Connection connection : connectable.getConnections()) {
            if (connection.lazyEquals(reference) && !connection.isBroken()) {
                return true;
            }
        }
        return false;
    }

    public static BlockEntityElectricFencePole getBasePole(BlockGetter level, BlockPos pos) {
        if (!isLoaded(level, pos)) {
            return null;
        }

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ElectricFencePostBlock post) {
            BlockPos basePos = pos.below(state.getValue(post.getIndexProperty()));
            if (isLoaded(level, basePos)) {
                BlockEntity baseEntity = level.getBlockEntity(basePos);
                if (baseEntity instanceof BlockEntityElectricFencePole pole) {
                    return pole;
                }
            }
        }

        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof BlockEntityElectricFencePole pole ? pole : null;
    }

    public static BlockPos getBasePos(BlockGetter level, BlockPos pos) {
        if (!isLoaded(level, pos)) {
            return pos;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ElectricFencePostBlock post) {
            return pos.below(state.getValue(post.getIndexProperty()));
        }
        return pos;
    }

    private static boolean endpointHasEnergy(BlockGetter level, BlockPos endpoint) {
        BlockEntity endpointEntity = isLoaded(level, endpoint) ? level.getBlockEntity(endpoint) : null;
        if (endpointEntity instanceof BlockEntityElectricFencePole pole && pole.getEnergyStorage().getStoredEnergy() > 0) {
            return true;
        }

        BlockEntityElectricFencePole basePole = getBasePole(level, endpoint);
        return basePole != null && basePole != endpointEntity && basePole.getEnergyStorage().getStoredEnergy() > 0;
    }

    private static boolean isLoaded(BlockGetter level, BlockPos pos) {
        return !(level instanceof Level realLevel) || realLevel.isLoaded(pos);
    }
}
