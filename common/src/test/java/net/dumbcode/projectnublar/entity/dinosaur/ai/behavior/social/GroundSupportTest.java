package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P12c cliff edges: the centre-column stand rule, the path node rim rules and the guard's ground test, as pure
 * predicates over small ground grids.
 */
class GroundSupportTest {

    private static final double EPSILON = 1.0E-9D;
    /** The ground top of a '#' column in the grids below. */
    private static final double GROUND = 64D;
    /** Gallimimus clearance box: 1.25 wide. */
    private static final double HALF = 0.625D;

    /**
     * A ground grid: row index = z, character index = x, both from 0. '#' a column with ground (top
     * {@link #GROUND}), '.' no ground within the drop, 'H' a hazard first. Outside the grid: no ground.
     */
    private static GroundSupport.ColumnTop tops(String... rows) {
        return (x, z) -> {
            if (z < 0 || z >= rows.length || x < 0 || x >= rows[z].length()) {
                return Double.NaN;
            }
            char c = rows[z].charAt(x);
            return c == '#' ? GROUND : c == 'H' ? GroundSupport.HAZARD : Double.NaN;
        };
    }

    private static GroundSupport.ColumnGround grounds(String... rows) {
        GroundSupport.ColumnTop tops = tops(rows);
        return (x, z) -> !Double.isNaN(tops.top(x, z));
    }

    @Test
    void anInteriorCoordinateHasOneCentreColumn() {
        assertEquals(3, GroundSupport.centreColumnMin(3.25D));
        assertEquals(3, GroundSupport.centreColumnMax(3.25D));
        assertEquals(-1, GroundSupport.centreColumnMin(-0.75D));
        assertEquals(-1, GroundSupport.centreColumnMax(-0.75D));
    }

    @Test
    void aCoordinateOnAColumnBoundaryTouchesBothColumns() {
        assertEquals(2, GroundSupport.centreColumnMin(3D));
        assertEquals(3, GroundSupport.centreColumnMax(3D));
        assertEquals(-2, GroundSupport.centreColumnMin(-1D));
        assertEquals(-1, GroundSupport.centreColumnMax(-1D));
    }

    @Test
    void theCentreNeedsGroundUnderAtLeastHalfOfItsColumns() {
        assertTrue(GroundSupport.centreSupported(1, 1));
        assertFalse(GroundSupport.centreSupported(0, 1));
        assertTrue(GroundSupport.centreSupported(1, 2), "on a rim line: half the hitbox on ground");
        assertFalse(GroundSupport.centreSupported(0, 2));
        assertTrue(GroundSupport.centreSupported(2, 4));
        assertFalse(GroundSupport.centreSupported(1, 4), "on the corner point of a pit: three quarters over the void");
        assertTrue(GroundSupport.centreSupported(4, 4));
    }

    @Test
    void aBodyStandsOnlyWhileItsCentreIsOverGround() {
        // A cliff: columns x >= 2 have no ground. The box spans columns 1 and 2 at every centre below.
        GroundSupport.ColumnTop cliff = tops("##....", "##....", "##....", "##....");
        assertEquals(GROUND, GroundSupport.standTop(1.5D, 1.5D, HALF, cliff), EPSILON);
        assertEquals(GROUND, GroundSupport.standTop(2.0D, 1.5D, HALF, cliff), EPSILON, "centre on the rim line");
        // The old rule (ground under any column) said "stand" here: the box still has column 1 under it.
        assertTrue(Double.isNaN(GroundSupport.standTop(2.25D, 1.5D, HALF, cliff)), "centre a quarter past the rim");
        assertTrue(Double.isNaN(GroundSupport.standTop(2.5D, 1.5D, HALF, cliff)));
    }

    @Test
    void aBodyOnThePitCornerPointNeedsTwoOfTheFourColumns() {
        GroundSupport.ColumnTop corner = tops("#...", "....", "....");
        assertTrue(Double.isNaN(GroundSupport.standTop(1D, 1D, HALF, corner)), "1 of 4 columns under the corner point");
        GroundSupport.ColumnTop rim = tops("##..", "....", "....");
        assertEquals(GROUND, GroundSupport.standTop(1D, 1D, HALF, rim), EPSILON, "2 of 4: half the hitbox on ground");
    }

    @Test
    void aHazardUnderAnyColumnStillRefuses() {
        GroundSupport.ColumnTop hazard = tops("#H..", "#H..", "#H..");
        assertTrue(Double.isNaN(GroundSupport.standTop(1.25D, 1.5D, HALF, hazard)));
    }

    @Test
    void theStandHeightIsStillTheHighestColumnTop() {
        // A planned drop of 2 blocks (the drop limit stays 3, lead decision): the centre is over the lower
        // ground, a side column still on the upper ground, and the body stands on the upper ground.
        GroundSupport.ColumnTop step = (x, z) -> x <= 1 ? 64D : 62D;
        assertEquals(64D, GroundSupport.standTop(2.25D, 1.5D, HALF, step), EPSILON);
        assertEquals(62D, GroundSupport.standTop(3.5D, 1.5D, HALF, step), EPSILON);
    }

    @Test
    void aNodeWithGroundUnderEveryColumnIsFull() {
        assertSame(GroundSupport.Edge.FULL, GroundSupport.nodeEdge(1, 1, 2, grounds("####", "####", "####", "####")));
    }

    @Test
    void aNodeOnARimLineIsARim() {
        // Footprint columns 1..2 x 1..2; columns x >= 2 have no ground: 2 of 4.
        assertSame(GroundSupport.Edge.RIM, GroundSupport.nodeEdge(1, 1, 2, grounds("##..", "##..", "##..", "##..")));
        // An inner corner of a pit: 3 of 4.
        assertSame(GroundSupport.Edge.RIM, GroundSupport.nodeEdge(1, 1, 2, grounds("####", "####", "##..", "##..")));
    }

    @Test
    void aNodeOnAPitCornerWithOneColumnOnGroundIsAHang() {
        assertSame(GroundSupport.Edge.HANG, GroundSupport.nodeEdge(1, 1, 2, grounds("##..", "##..", "....", "....")));
    }

    @Test
    void aNodeWithNoGroundIsLeftToVanilla() {
        assertSame(GroundSupport.Edge.NONE, GroundSupport.nodeEdge(1, 1, 2, grounds("....", "....", "....", "....")));
    }

    @Test
    void aOneColumnFootprintIsFullOrNone() {
        assertSame(GroundSupport.Edge.FULL, GroundSupport.nodeEdge(0, 0, 1, grounds("#.", "..")));
        assertSame(GroundSupport.Edge.NONE, GroundSupport.nodeEdge(1, 0, 1, grounds("#.", "..")));
    }

    @Test
    void aThreeColumnFootprintUsesItsCentreColumn() {
        // Footprint columns 0..2 x 0..2, centre column (1, 1).
        assertSame(GroundSupport.Edge.HANG, GroundSupport.nodeEdge(0, 0, 3, grounds("###", "#.#", "###")),
                "8 of 9 columns, but none under the centre");
        assertSame(GroundSupport.Edge.RIM, GroundSupport.nodeEdge(0, 0, 3, grounds("##.", "##.", "##.")));
    }

    @Test
    void aColumnGroundsANodeWithinOneStepDown() {
        assertTrue(GroundSupport.columnGrounds(64, 64.0625D), "a carpet in the node cell");
        assertTrue(GroundSupport.columnGrounds(64, 64D), "a full block under the node");
        assertTrue(GroundSupport.columnGrounds(64, 63.5D), "a bottom slab under the node");
        assertTrue(GroundSupport.columnGrounds(64, 63D), "a one-block terrace step down is a step, not a rim");
        assertFalse(GroundSupport.columnGrounds(64, 62.5D), "more than one step down is a drop");
        assertFalse(GroundSupport.columnGrounds(64, Double.NaN));
    }

    @Test
    void theGuardTestsGroundUnlessThePathIsFacedOrTheBodyIsOffTheGround() {
        assertTrue(GroundSupport.guardChecksGround(true, false, false), "steering, side steps, unstacks");
        assertTrue(GroundSupport.guardChecksGround(true, false, true));
        assertTrue(GroundSupport.guardChecksGround(true, true, false), "on a path, but the yaw lags the next node");
        assertFalse(GroundSupport.guardChecksGround(true, true, true), "on a path facing its next node");
        assertFalse(GroundSupport.guardChecksGround(false, false, false), "in the air or in water");
    }
}
