package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundSpeedTest {

    private static final double EPSILON = 1.0E-9D;
    /** The attribute every dinosaur spawns with (the config multiplier is not applied). */
    private static final double ATTRIBUTE = Dinosaur.BASE_MOVEMENT_SPEED;
    /** The floor must clear the animation threshold by this much (velocity sync, a step up, a turn). */
    private static final double MARGIN = 1.4D;

    @Test
    void groundSpeedIsQuadraticInTheModifier() {
        // Vanilla: push 0.98 s^2 per tick, 0.546 of the velocity kept: d = 0.98 s^2 / 0.454 = 2.1586 s^2.
        assertEquals(0.98D / (1D - 0.6D * 0.91D), GroundSpeed.BLOCKS_PER_SPEED_SQUARED, EPSILON);
        assertEquals(0.1349D, GroundSpeed.groundSpeed(1D, ATTRIBUTE), 0.0005D);
        assertEquals(GroundSpeed.groundSpeed(1D, ATTRIBUTE) / 4D, GroundSpeed.groundSpeed(0.5D, ATTRIBUTE), EPSILON);
        assertEquals(0D, GroundSpeed.groundSpeed(0D, ATTRIBUTE), EPSILON);
    }

    @Test
    void modifierForInvertsGroundSpeed() {
        assertEquals(0.8D, GroundSpeed.modifierFor(GroundSpeed.groundSpeed(0.8D, ATTRIBUTE), ATTRIBUTE), EPSILON);
        assertEquals(0D, GroundSpeed.modifierFor(0D, ATTRIBUTE), EPSILON);
    }

    @Test
    void modifierFactorScalesTheGroundSpeedByTheGivenFraction() {
        // A slow-down factor is meant for the ground speed: the modifier takes its square root.
        assertEquals(0.5D, GroundSpeed.modifierFactor(0.25D), EPSILON);
        double slowed = GroundSpeed.groundSpeed(0.8D * GroundSpeed.modifierFactor(0.4D), ATTRIBUTE);
        assertEquals(0.4D * GroundSpeed.groundSpeed(0.8D, ATTRIBUTE), slowed, EPSILON);
    }

    @Test
    void storedVelocityIsTheMoveAfterFriction() {
        assertEquals(0.546D * 0.1D, GroundSpeed.storedVelocity(0.1D), EPSILON);
    }

    @Test
    void minWalkModifierKeepsTheWalkAnimationOnEveryHeading() {
        // The client plays the walk animation while (|vx| + |vz|) / 2 of the synced velocity is at least
        // 0.005. An axis heading is the tightest case (|vx| + |vz| = v); a diagonal gives sqrt(2) more.
        double threshold = GroundSpeed.modifierFor(GroundSpeed.WALK_ANIMATION_BLOCKS_PER_TICK, ATTRIBUTE);
        assertEquals(0.37D, threshold, 0.005D);
        assertTrue(GroundSpeed.MIN_WALK_MODIFIER > threshold);
        double perTick = GroundSpeed.groundSpeed(GroundSpeed.MIN_WALK_MODIFIER, ATTRIBUTE);
        double axis = GroundSpeed.storedVelocity(perTick) / 2D;
        double diagonal = GroundSpeed.storedVelocity(perTick) * (Math.sin(Math.PI / 4D) + Math.cos(Math.PI / 4D)) / 2D;
        assertTrue(axis >= GroundSpeed.WALK_ANIMATION_VELOCITY * MARGIN, "axis heading, was " + axis);
        assertTrue(diagonal >= GroundSpeed.WALK_ANIMATION_VELOCITY * MARGIN, "diagonal heading, was " + diagonal);
    }

    @Test
    void movingIsAStoredVelocityAboveTheWalkingThreshold() {
        // Playtest 2 fix P8a: a body held by the guard (velocity cut to a fifth each tick) stops counting as
        // moving within a tick; the slowest herd walk (graze, 0.049 blocks per tick, 0.027 stored) is well above.
        assertEquals(GroundSpeed.storedVelocity(HerdBodies.WALKING_SPEED), GroundSpeed.MOVING_STORED_SPEED, EPSILON);
        double graze = GroundSpeed.storedVelocity(GroundSpeed.groundSpeed(FollowFormation.GRAZE_SPEED, ATTRIBUTE));
        assertTrue(GroundSpeed.moving(graze, 0D));
        assertTrue(GroundSpeed.moving(graze * 0.7D, graze * 0.7D));
        assertFalse(GroundSpeed.moving(graze * HerdMoveControl.GUARD_VELOCITY_FACTOR, 0D));
        assertFalse(GroundSpeed.moving(0D, 0D));
        assertFalse(GroundSpeed.moving(GroundSpeed.MOVING_STORED_SPEED, 0D), "the threshold itself is not moving");
    }

    @Test
    void walkModifierStandsBelowTheStopThresholdAndFloorsAboveIt() {
        assertEquals(0D, GroundSpeed.walkModifier(0D), EPSILON);
        assertEquals(0D, GroundSpeed.walkModifier(GroundSpeed.WALK_STOP_MODIFIER - 0.01D), EPSILON);
        assertEquals(GroundSpeed.MIN_WALK_MODIFIER, GroundSpeed.walkModifier(GroundSpeed.WALK_STOP_MODIFIER), EPSILON);
        assertEquals(GroundSpeed.MIN_WALK_MODIFIER, GroundSpeed.walkModifier(GroundSpeed.MIN_WALK_MODIFIER - 0.1D), EPSILON);
        assertEquals(0.8D, GroundSpeed.walkModifier(0.8D), EPSILON);
    }
}
