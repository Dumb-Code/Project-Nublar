package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaderTrailTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void emptyTrailHasNoPoint() {
        LeaderTrail trail = new LeaderTrail();
        assertNull(trail.pointAt(0D));
        assertTrue(Double.isNaN(trail.project(0D, 0D, Double.NaN, 5D)));
        assertTrue(Double.isNaN(trail.headArc()));
    }

    @Test
    void recordsOnlyEveryBlockWalked() {
        LeaderTrail trail = new LeaderTrail();
        assertTrue(trail.record(0D, 64D, 0D) >= 0);
        assertEquals(-1, trail.record(0.5D, 64D, 0D));
        assertTrue(trail.record(1D, 64D, 0D) >= 0);
        assertEquals(2, trail.size());
    }

    @Test
    void arcGrowsWithTheDistanceWalked() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(3D, 64D, 0D);
        trail.record(3D, 64D, 4D);
        assertEquals(7D, trail.headArc(), EPSILON);
        assertEquals(0D, trail.tailArc(), EPSILON);
    }

    @Test
    void pointAtInterpolatesAndGivesTheDirection() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(3D, 64D, 0D);
        trail.record(3D, 66D, 4D);
        LeaderTrail.TrailPoint point = trail.pointAt(5D);
        assertEquals(3D, point.x(), EPSILON);
        assertEquals(2D, point.z(), EPSILON);
        assertEquals(65D, point.y(), EPSILON);
        assertEquals(0D, point.dirX(), EPSILON);
        assertEquals(1D, point.dirZ(), EPSILON);
        assertEquals(5D, point.arc(), EPSILON);
    }

    @Test
    void pointAtClampsToTheTrail() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(3D, 64D, 0D);
        assertEquals(3D, trail.pointAt(100D).x(), EPSILON);
        assertEquals(0D, trail.pointAt(-5D).x(), EPSILON);
    }

    @Test
    void projectFindsTheNearestArc() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(3D, 64D, 0D);
        trail.record(3D, 64D, 4D);
        assertEquals(4D, trail.project(4D, 1D, Double.NaN, 5D), EPSILON);
    }

    @Test
    void projectWithAHintIgnoresFarSegments() {
        // A U-turn: the return leg passes close to the first one.
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(10D, 64D, 0D);
        trail.record(10D, 64D, 2D);
        trail.record(0D, 64D, 2D);
        assertEquals(20D, trail.project(2D, 1.1D, Double.NaN, 5D), EPSILON);
        assertEquals(2D, trail.project(2D, 1.1D, 2D, 5D), EPSILON);
    }

    @Test
    void ringBufferKeepsTheNewest128() {
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x < 200; x++) {
            trail.record(x, 64D, 0D);
        }
        assertEquals(LeaderTrail.CAPACITY, trail.size());
        assertEquals(199D, trail.headArc(), EPSILON);
        assertEquals(72D, trail.tailArc(), EPSILON);
        assertEquals(72D, trail.pointAt(0D).x(), EPSILON);
    }

    @Test
    void sideRoomIsUnknownUntilMeasuredAndUsesTheTighterEnd() {
        LeaderTrail trail = new LeaderTrail();
        int first = trail.record(0D, 64D, 0D);
        int second = trail.record(2D, 64D, 0D);
        trail.setRoom(first, 3F, 1F);
        assertTrue(Float.isNaN(trail.pointAt(1D).roomPos()));
        trail.setRoom(second, 2F, 4F);
        assertEquals(2F, trail.pointAt(1D).roomPos(), 0F);
        assertEquals(1F, trail.pointAt(1D).roomNeg(), 0F);
    }

    @Test
    void minRoomTakesTheTightestPointInTheRange() {
        LeaderTrail trail = new LeaderTrail();
        for (int x = 0; x <= 10; x++) {
            trail.setRoom(trail.record(x, 64D, 0D), x == 7 ? 1F : 5F, 5F);
        }
        assertEquals(5F, trail.minRoom(2D, 5D, true), 0F);
        assertEquals(1F, trail.minRoom(2D, 6.5D, true), 0F, "the range touches the segment that ends at the tight point");
        assertEquals(5F, trail.minRoom(8D, 10D, true), 0F);
        assertEquals(5F, trail.minRoom(2D, 6.5D, false), 0F);
        assertEquals(5F, trail.minRoom(-3D, 1D, true), 0F, "clamped to the trail");
        trail.setRoom(trail.record(11D, 64D, 0D), 5F, Float.NaN);
        assertTrue(Float.isNaN(trail.minRoom(9D, 11D, false)), "an unmeasured point makes the range unknown");
    }

    @Test
    void clearEmptiesTheTrail() {
        LeaderTrail trail = new LeaderTrail();
        trail.record(0D, 64D, 0D);
        trail.record(2D, 64D, 0D);
        trail.clear();
        assertEquals(0, trail.size());
        assertNull(trail.newest());
    }
}
