package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebugShapesTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void ringHasTheRequestedPointsAtTheRadius() {
        List<double[]> points = new ArrayList<>();
        DebugShapes.ring(10D, -4D, 6D, 24, (x, z) -> points.add(new double[] {x, z}));
        assertEquals(24, points.size());
        for (double[] point : points) {
            double dx = point[0] - 10D;
            double dz = point[1] + 4D;
            assertEquals(6D, Math.sqrt(dx * dx + dz * dz), EPSILON);
        }
        // The first point lies along +x; the seventh (a quarter turn) along +z.
        assertEquals(16D, points.get(0)[0], EPSILON);
        assertEquals(-4D, points.get(0)[1], EPSILON);
        assertEquals(10D, points.get(6)[0], EPSILON);
        assertEquals(2D, points.get(6)[1], EPSILON);
    }

    @Test
    void lineRunsFromStartToEndEvenly() {
        List<double[]> points = new ArrayList<>();
        DebugShapes.line(0D, 1D, 0D, 2D, 1D, 0D, 5, (x, y, z) -> points.add(new double[] {x, y, z}));
        assertEquals(5, points.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(i * 0.5D, points.get(i)[0], EPSILON);
            assertEquals(1D, points.get(i)[1], EPSILON);
            assertEquals(0D, points.get(i)[2], EPSILON);
        }
    }

    @Test
    void boxEdgesHaveEveryCornerOnceAndInteriorPointsOnEdges() {
        List<double[]> points = new ArrayList<>();
        // Centre (5, 7) at feet height 64, half width 1, height 3: x in [4, 6], y in [64, 67], z in [6, 8].
        DebugShapes.boxEdges(5D, 64D, 7D, 1D, 3D, 2, (x, y, z) -> points.add(new double[] {x, y, z}));
        assertEquals(8 + 12 * 2, points.size());
        int corners = 0;
        for (double[] point : points) {
            boolean xEdge = near(point[0], 4D) || near(point[0], 6D);
            boolean yEdge = near(point[1], 64D) || near(point[1], 67D);
            boolean zEdge = near(point[2], 6D) || near(point[2], 8D);
            int onFaces = (xEdge ? 1 : 0) + (yEdge ? 1 : 0) + (zEdge ? 1 : 0);
            assertTrue(onFaces >= 2, "not on an edge: " + point[0] + " " + point[1] + " " + point[2]);
            if (onFaces == 3) {
                corners++;
            }
        }
        assertEquals(8, corners);
        // An interior point of the edge along x at the bottom near side sits a third of the way along.
        assertTrue(points.stream().anyMatch(p -> near(p[0], 4D + 2D / 3D) && near(p[1], 64D) && near(p[2], 6D)));
    }

    private static boolean near(double value, double expected) {
        return Math.abs(value - expected) < EPSILON;
    }
}
