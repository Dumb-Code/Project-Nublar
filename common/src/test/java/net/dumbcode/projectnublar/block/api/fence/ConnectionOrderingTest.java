package net.dumbcode.projectnublar.block.api.fence;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A wire's "next" side is its {@code getTo()} side: that is the half the renderer draws from the centre toward
 * {@code to}, and the half {@code brokenSide(world, true)} must inspect. Four orientations: east-west and
 * north-south, each built from either end.
 *
 * <p>The {@code Connection} constructor cannot run here (its collision shape needs the Minecraft bootstrap), so
 * this test pins {@link Connection#sideNeighbour}, the rule {@code brokenSide} uses to pick the neighbour.
 */
class ConnectionOrderingTest {

    @Test
    void eastWestBuiltFromTheWest() {
        check(new BlockPos(0, 64, 0), new BlockPos(4, 64, 0));
    }

    @Test
    void eastWestBuiltFromTheEast() {
        check(new BlockPos(4, 64, 0), new BlockPos(0, 64, 0));
    }

    @Test
    void northSouthBuiltFromTheNorth() {
        check(new BlockPos(0, 64, 0), new BlockPos(0, 64, 4));
    }

    @Test
    void northSouthBuiltFromTheSouth() {
        check(new BlockPos(0, 64, 4), new BlockPos(0, 64, 0));
    }

    /** The middle block's neighbour toward {@code to} is "next", the one toward {@code from} is "previous". */
    private static void check(BlockPos from, BlockPos to) {
        BlockPos step = new BlockPos(Integer.signum(to.getX() - from.getX()), 0, Integer.signum(to.getZ() - from.getZ()));
        BlockPos towardFrom = from.offset(step);
        BlockPos towardTo = towardFrom.offset(step).offset(step);

        assertEquals(towardTo, Connection.sideNeighbour(true, towardFrom, towardTo), "the next side is the to side");
        assertEquals(towardFrom, Connection.sideNeighbour(false, towardFrom, towardTo), "the previous side is the from side");
    }
}
