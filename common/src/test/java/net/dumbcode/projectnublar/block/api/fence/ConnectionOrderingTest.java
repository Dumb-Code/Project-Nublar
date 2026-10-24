package net.dumbcode.projectnublar.block.api.fence;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins only the side rule, {@link Connection#sideNeighbour}: {@code next} means the {@code to}-side neighbour.
 *
 * <p>The orientation guarantee (east-west and north-south, each built from either end) comes from the private
 * {@code Connection} constructor, which swaps the ends and the neighbours as a pair in its {@code compared < 0}
 * block. That guarantee was checked in game on both axes. The constructor cannot run under the unit-test stand-in
 * (its collision shape needs the Minecraft bootstrap), so this test does not cover it.
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
