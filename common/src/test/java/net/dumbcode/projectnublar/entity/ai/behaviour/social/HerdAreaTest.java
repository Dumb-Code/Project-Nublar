package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdAreaTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void radiusGivesEveryMemberAGrazeSpacingSquare() {
        assertEquals(5D * Math.sqrt(20D / Math.PI) + 1D, HerdArea.radius(20, 5D, 2D, 1D), EPSILON);
        assertEquals(13.6D, HerdArea.radius(20, 5D, 2D, 1D), 0.05D);
        assertEquals(7.3D, HerdArea.radius(5, 5D, 2D, 1D), 0.05D);
    }

    @Test
    void herdAreaScaleGivesSixTimesTheArea() {
        // Lead, playtest fix P11: 6x the area (P9's 3x doubled), so the radius is sqrt(6) x the spacing-square radius.
        assertEquals(Math.sqrt(6D), HerdArea.HERD_AREA_SCALE, EPSILON);
        double unscaled = HerdArea.radius(20, 4.2D, 1.2D, 1D);
        double scaled = HerdArea.radius(20, 4.2D, 1.2D, HerdArea.HERD_AREA_SCALE);
        assertEquals(6D, scaled * scaled / (unscaled * unscaled), EPSILON);
        assertEquals(11.2D, unscaled, 0.05D);
        assertEquals(27.4D, scaled, 0.05D);
        assertEquals(14.4D, HerdArea.radius(5, 4.2D, 1.2D, HerdArea.HERD_AREA_SCALE), 0.05D); // 14.45 (the brief rounds it to 14.5)
    }

    @Test
    void grazeSpacingIsPersonalSpacePlusTwo() {
        assertEquals(5D, HerdArea.grazeSpacing(3D), EPSILON);
    }

    @Test
    void insideIncludesTheEdge() {
        assertTrue(HerdArea.inside(0D, 0D, 5D, 3D, 4D));
        assertFalse(HerdArea.inside(0D, 0D, 5D, 3D, 4.01D));
    }

    @Test
    void sampleStaysInsideTheRadius() {
        for (double angle = 0D; angle <= 1D; angle += 0.1D) {
            for (double distance = 0D; distance <= 1D; distance += 0.1D) {
                HerdArea.Point point = HerdArea.sample(10D, -4D, 7D, angle, distance);
                assertTrue(HerdArea.inside(10D, -4D, 7D + EPSILON, point.x(), point.z()), "outside: " + point);
            }
        }
    }

    @Test
    void sampleRingKeepsTheDistanceBand() {
        for (double angle = 0D; angle <= 1D; angle += 0.1D) {
            for (double distance = 0D; distance <= 1D; distance += 0.1D) {
                HerdArea.Point point = HerdArea.sampleRing(0D, 0D, 3D, 8D, angle, distance);
                double length = Math.sqrt(point.x() * point.x() + point.z() * point.z());
                assertTrue(length >= 3D - EPSILON && length <= 8D + EPSILON, "out of the band: " + length);
            }
        }
    }

    @Test
    void roomiestPicksTheCandidateFarthestFromEveryone() {
        List<HerdArea.Point> occupied = List.of(new HerdArea.Point(0D, 0D), new HerdArea.Point(10D, 0D));
        List<HerdArea.Point> candidates = List.of(new HerdArea.Point(1D, 0D), new HerdArea.Point(5D, 0D), new HerdArea.Point(9D, 0D));
        assertEquals(1, HerdArea.roomiest(candidates, occupied));
    }

    @Test
    void roomiestOfNoCandidatesIsMinusOne() {
        assertEquals(-1, HerdArea.roomiest(List.of(), List.of(new HerdArea.Point(0D, 0D))));
    }

    @Test
    void withNobodyElseTheFirstCandidateWins() {
        assertEquals(0, HerdArea.roomiest(List.of(new HerdArea.Point(1D, 1D), new HerdArea.Point(2D, 2D)), List.of()));
    }

    @Test
    void pickSpotSkipsCandidatesTooCloseToAReservation() {
        List<HerdArea.Point> candidates = List.of(new HerdArea.Point(3D, 0D), new HerdArea.Point(6D, 0D));
        assertEquals(1, HerdArea.pickSpot(candidates, List.of(new HerdArea.Point(0D, 0D)), 5D, point -> true));
    }

    @Test
    void pickSpotTriesTheRoomiestFirstAndStopsAtTheFirstUsable() {
        List<HerdArea.Point> candidates = List.of(new HerdArea.Point(6D, 0D), new HerdArea.Point(10D, 0D), new HerdArea.Point(20D, 0D));
        int[] calls = new int[1];
        int picked = HerdArea.pickSpot(candidates, List.of(new HerdArea.Point(0D, 0D)), 5D, point -> {
            calls[0]++;
            return point.x() < 15D;
        });
        assertEquals(1, picked);
        assertEquals(2, calls[0], "the 20-block candidate is tried first and refused, then the 10-block one");
    }

    @Test
    void pickSpotWithNothingUsableIsMinusOne() {
        List<HerdArea.Point> candidates = List.of(new HerdArea.Point(6D, 0D), new HerdArea.Point(10D, 0D));
        assertEquals(-1, HerdArea.pickSpot(candidates, List.of(), 5D, point -> false));
    }
}
