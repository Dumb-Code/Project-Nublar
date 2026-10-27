package net.dumbcode.projectnublar.block.api.fence;

import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Right-clicking a fence block next to a broken wire with a wire spool repairs one connection.
 * The loaders call {@link #tryRepair} from their right-click-on-block hook and stop the click
 * there when it returns {@code true}.
 */
public final class WireSpoolRepair {

    /**
     * Repairs one broken wire of the connectable block on the clicked face, on the server only.
     * A click on the top or bottom face repairs the broken wire vertically nearest to that face;
     * a click on a side repairs the first broken wire. A repair swings the arm and uses one spool
     * (none in creative).
     *
     * @param level  the level of the click
     * @param player the player who clicked
     * @param hand   the hand that holds {@code held}
     * @param pos    the clicked block
     * @param side   the clicked face; the repaired block is the neighbour on this face
     * @param held   the stack in {@code hand}
     * @return true when a wire was repaired, so the loader cancels the rest of the click
     */
    public static boolean tryRepair(Level level, Player player, InteractionHand hand, BlockPos pos, Direction side, ItemStack held) {
        if (level.isClientSide()) {
            return false;
        }
        if (side != null && !held.isEmpty() && held.getItem() == ItemInit.WIRE_SPOOL.get()) {
            BlockEntity tile = level.getBlockEntity(pos.relative(side));
            if (tile instanceof ConnectableBlockEntity cb) {
                Connection repaired = side.getAxis() == Direction.Axis.Y
                        ? repairVerticallyNearestWire(cb, side)
                        : repairFirstBrokenWire(cb);
                if (repaired != null) {
                    BlockConnectableBase.placeEffect(player, hand, level, pos);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Repairs the broken wire vertically closest to the clicked face. The
     * {@code Double.MIN_VALUE}/{@code MAX_VALUE} seeds and the
     * {@code side == DOWN == yin > yRef} comparison are frozen heuristics.
     *
     * @return the repaired wire, or null when none is broken
     */
    private static Connection repairVerticallyNearestWire(ConnectableBlockEntity cb, Direction side) {
        double yRef = side == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
        Connection ref = null;
        for (Connection connection : cb.getConnections()) {
            if (connection.isBroken()) {
                double[] in = connection.getIn();
                double yin = (in[4] + in[5]) / 2D;
                if (side == Direction.DOWN == yin > yRef) {
                    yRef = yin;
                    ref = connection;
                }
            }
        }
        if (ref != null) {
            repair(cb, ref);
        }
        return ref;
    }

    /** Repairs the first broken wire; returns it, or null when none is broken. */
    private static Connection repairFirstBrokenWire(ConnectableBlockEntity cb) {
        for (Connection connection : cb.getConnections()) {
            if (connection.isBroken()) {
                repair(cb, connection);
                return connection;
            }
        }
        return null;
    }

    private static void repair(ConnectableBlockEntity cb, Connection connection) {
        connection.setBroken(false);
        if (cb instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
    }

    private WireSpoolRepair() {
    }
}
