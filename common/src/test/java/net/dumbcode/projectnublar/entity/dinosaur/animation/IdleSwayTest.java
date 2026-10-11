package net.dumbcode.projectnublar.entity.dinosaur.animation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdleSwayTest {

    private static int index(String bone) {
        for (int i = 0; i < IdleSway.BONE_COUNT; i++) {
            if (IdleSway.bone(i).equals(bone)) {
                return i;
            }
        }
        throw new AssertionError("no sway bone " + bone);
    }

    /** Molang {@code math.cos} takes degrees. */
    private static double cos(double degrees) {
        return Math.cos(Math.toRadians(degrees));
    }

    @Test
    void bonesAreTheMiscIdleBonesOfTheGallimimusModel() {
        // gallimimus.animation.json misc.idle also names bicepLeft and bicepRight; gallimimus.geo.json has neither.
        List<String> bones = new ArrayList<>();
        for (int i = 0; i < IdleSway.BONE_COUNT; i++) {
            bones.add(IdleSway.bone(i));
        }
        assertEquals(List.of("hips", "neck1", "head", "tail1", "tail2", "tail3"), bones);
    }

    @Test
    void matchesTheGallimimusMiscIdleMolang() {
        // query.life_time is in seconds. Expressions copied from gallimimus.animation.json misc.idle.
        for (double t : new double[]{0D, 0.37D, 1.8D, 5.5D, 123.4D}) {
            assertEquals(cos(t * 100) * 0.5, IdleSway.pitchDegrees(index("hips"), t), 1.0E-9D, "hips at " + t);
            assertEquals(cos(t * 100 + 90) * 1, IdleSway.pitchDegrees(index("neck1"), t), 1.0E-9D, "neck1 at " + t);
            assertEquals(cos(t * 100 + 100) * -3, IdleSway.pitchDegrees(index("head"), t), 1.0E-9D, "head at " + t);
            assertEquals(cos(t * 100 - 60) * 2, IdleSway.pitchDegrees(index("tail1"), t), 1.0E-9D, "tail1 x at " + t);
            assertEquals(cos(t * 50 - 210) * 2, IdleSway.yawDegrees(index("tail1"), t), 1.0E-9D, "tail1 y at " + t);
            assertEquals(cos(t * 100 - 90) * 2, IdleSway.pitchDegrees(index("tail2"), t), 1.0E-9D, "tail2 x at " + t);
            assertEquals(cos(t * 50 - 250) * 2, IdleSway.yawDegrees(index("tail2"), t), 1.0E-9D, "tail2 y at " + t);
            assertEquals(cos(t * 100 - 120) * 2, IdleSway.pitchDegrees(index("tail3"), t), 1.0E-9D, "tail3 x at " + t);
            assertEquals(cos(t * 50 - 300) * 2, IdleSway.yawDegrees(index("tail3"), t), 1.0E-9D, "tail3 y at " + t);
            for (String still : new String[]{"hips", "neck1", "head"}) {
                assertEquals(0D, IdleSway.yawDegrees(index(still), t), 0D, still + " has no yaw sway");
            }
        }
    }

    @Test
    void neverMoreThanThreeDegrees() {
        for (int i = 0; i < IdleSway.BONE_COUNT; i++) {
            for (double t = 0D; t < 10D; t += 0.05D) {
                assertTrue(Math.abs(IdleSway.pitchDegrees(i, t)) <= 3D, IdleSway.bone(i) + " pitch at " + t);
                assertTrue(Math.abs(IdleSway.yawDegrees(i, t)) <= 3D, IdleSway.bone(i) + " yaw at " + t);
            }
        }
    }

    @Test
    void pitchRepeatsEvery3Point6SecondsAndYawEvery7Point2() {
        int tail = index("tail2");
        assertEquals(IdleSway.pitchDegrees(tail, 1.1D), IdleSway.pitchDegrees(tail, 1.1D + 3.6D), 1.0E-9D);
        assertEquals(IdleSway.yawDegrees(tail, 1.1D), IdleSway.yawDegrees(tail, 1.1D + 7.2D), 1.0E-9D);
    }
}
