package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClearanceBoxTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void growthScaleMatchesTheRenderers() {
        assertEquals(0.25D, ClearanceBox.growthScale(1, 0D), EPSILON);
        assertEquals(0.75D, ClearanceBox.growthScale(3, 0D), EPSILON);
        assertEquals(1D, ClearanceBox.growthScale(4, 0D), EPSILON);
        assertEquals(1.5D, ClearanceBox.growthScale(4, 50D), EPSILON);
    }

    @Test
    void gallimimusBoxIsTheHitboxWidthRoundedUpAndAQuarterTaller() {
        // Playtest 2 fix P8a: no side margin (the hitbox is the physical body), so 1.2 rounds up to 1.25:
        // 0.375 of slack either side in a 2-wide gap, against 0.125 with the old 1.75 box.
        ClearanceBox box = ClearanceBox.of("gallimimus", 1.2D, 2.6D, 4, 0D);
        assertEquals(1.25D, box.width(), EPSILON);
        assertEquals(3.0D, box.height(), EPSILON);
    }

    @Test
    void aModelLargerThanItsHitboxSetsTheBox() {
        ClearanceBox box = ClearanceBox.of("triceratops", 2D, 3D, 4, 0D);
        assertEquals(3.5D, box.width(), EPSILON);
        assertEquals(6.25D, box.height(), EPSILON);
    }

    @Test
    void unknownSpeciesUsesTheHitbox() {
        ClearanceBox box = ClearanceBox.of("unknown", 1D, 1D, 4, 0D);
        assertEquals(1.0D, box.width(), EPSILON);
        assertEquals(1.25D, box.height(), EPSILON);
    }

    @Test
    void sizeGeneNeverRaisesTheBoxTopAboveWhatTheHitboxNeeds() {
        // The gene scales the model (2.53 for Gallimimus) while the hitbox stays 2.6. Before P8a a gene of
        // +8.7 % or more made the box 3.25 tall and refused every 3-high canopy the body fits.
        assertEquals(3.0D, ClearanceBox.of("gallimimus", 1.2D, 2.6D, 4, 10D).height(), EPSILON);
        assertEquals(3.0D, ClearanceBox.of("gallimimus", 1.2D, 2.6D, 4, 100D).height(), EPSILON);
        // A species whose adult model is taller than its hitbox at gene 0 keeps that model top (lead decision 5).
        assertEquals(6.25D, ClearanceBox.of("triceratops", 2D, 3D, 4, 50D).height(), EPSILON);
        // A negative gene still shrinks the model top, down to the hitbox.
        assertEquals(3.25D, ClearanceBox.of("triceratops", 2D, 3D, 4, -50D).height(), EPSILON);
    }

    @Test
    void sizesRoundUpToAQuarter() {
        ClearanceBox box = new ClearanceBox(1.1D, 1.3D);
        assertEquals(1.25D, box.width(), EPSILON);
        assertEquals(1.5D, box.height(), EPSILON);
    }

    @Test
    void probeKeyTellsQuartersApart() {
        ClearanceBox box = new ClearanceBox(2.5D, 3.25D);
        assertNotEquals(box.probeKey(10.0D, 64D, 5D), box.probeKey(10.25D, 64D, 5D));
    }

    @Test
    void probeKeyIsTheSameInsideOneQuarter() {
        ClearanceBox box = new ClearanceBox(2.5D, 3.25D);
        assertEquals(box.probeKey(10.01D, 64D, 5D), box.probeKey(10.1D, 64D, 5D));
    }

    @Test
    void nodeCentreIsWhereVanillaWalksTheBody() {
        // Path.getEntityPosAtNode: node + floor(hitbox width + 1) / 2, so a 2-column body stands a whole
        // block past the node, a 1-column body half a block.
        assertEquals(11.0D, ClearanceBox.nodeCentre(10, 1.2D), EPSILON);
        assertEquals(10.5D, ClearanceBox.nodeCentre(10, 0.6D), EPSILON);
        assertEquals(12.0D, ClearanceBox.nodeCentre(10, 3.4D), EPSILON);
        assertEquals(-4.0D, ClearanceBox.nodeCentre(-5, 1.2D), EPSILON);
    }

    @Test
    void nodeCentreIsAlreadyOnTheProbeGrid() {
        double wide = ClearanceBox.nodeCentre(7, 1.2D);
        double narrow = ClearanceBox.nodeCentre(7, 0.6D);
        assertEquals(wide, ClearanceBox.quantise(wide), EPSILON);
        assertEquals(narrow, ClearanceBox.quantise(narrow), EPSILON);
    }

    @Test
    void columnFeetRestOnTheNodeCellsOwnShapeFirst() {
        // A carpet (1/16) in the node cell over a full block: the feet are on the carpet.
        assertEquals(10.0625D, ClearanceBox.columnFeet(10, 0.0625D, 1.0D), EPSILON);
        // A top slab in the node cell: the body steps onto it (at most one step up inside the cell).
        assertEquals(11.0D, ClearanceBox.columnFeet(10, 1.0D, 1.0D), EPSILON);
        assertEquals(11.0D, ClearanceBox.columnFeet(10, 1.5D, 1.0D), EPSILON);
    }

    @Test
    void columnFeetRestOnTheRowBelowWhenTheNodeCellIsEmpty() {
        // Vanilla puts the node above a bottom slab (slabs are not pathfindable): feet half a block down.
        assertEquals(9.5D, ClearanceBox.columnFeet(10, 0D, 0.5D), EPSILON);
        assertEquals(10.0D, ClearanceBox.columnFeet(10, 0D, 1.0D), EPSILON);
        // Never above the node floor from the row below (a wall's 1.5 cannot be stepped onto anyway).
        assertEquals(10.0D, ClearanceBox.columnFeet(10, 0D, 1.5D), EPSILON);
    }

    @Test
    void columnFeetIsNaNOverAHole() {
        assertTrue(Double.isNaN(ClearanceBox.columnFeet(10, 0D, 0D)));
    }

    @Test
    void probeKeyDependsOnFeetHeightAndBoxSize() {
        ClearanceBox box = new ClearanceBox(2.5D, 3.25D);
        assertNotEquals(box.probeKey(10D, 64D, 5D), box.probeKey(10D, 64.5D, 5D));
        assertNotEquals(box.probeKey(10D, 64D, 5D), new ClearanceBox(2.5D, 3.5D).probeKey(10D, 64D, 5D));
    }
}
