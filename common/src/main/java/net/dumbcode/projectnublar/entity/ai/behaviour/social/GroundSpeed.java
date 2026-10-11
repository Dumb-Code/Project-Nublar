package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/**
 * The vanilla ground speed law and the walk-animation floor, in one place (playtest 1 fix P1). Pure.
 * <p>
 * A mob's ground speed is <b>quadratic</b> in its effective speed {@code s = modifier x MOVEMENT_SPEED}
 * (1.20.1, Mojang names): {@code MoveControl.tick} calls {@code Mob.setSpeed(s)}, which also sets the
 * forward input {@code zza = s}; {@code LivingEntity.aiStep} damps the input by {@link #INPUT_DAMPING}
 * and calls {@code travel}, whose {@code moveRelative(getFrictionInfluencedSpeed(0.6), input)} scales the
 * input (length 0.98 s, under 1 so not normalised) by {@code s x 0.216 / 0.6^3 = s}. So the push per tick
 * is {@code 0.98 s^2}, and after the move the horizontal velocity keeps {@link #VELOCITY_KEPT}. At steady
 * state the move per tick is {@code d = 0.98 s^2 / (1 - 0.546) = 2.16 s^2}: {@code 0.135 x modifier^2}
 * blocks for a dinosaur (attribute 0.25). Vanilla's {@code PathNavigation.doStuckDetection} squares the
 * speed for the same reason.
 * <p>
 * Every slow-down factor is meant for the ground speed, so the modifier takes its square root
 * ({@link #modifierFactor}). The client plays the walk animation only while the synced velocity (the stored
 * one, {@code 0.546 d}) has {@code (|vx| + |vz|) / 2 >= 0.005} ({@code DinosaurRenderer}), so a steering
 * member is never driven below {@link #MIN_WALK_MODIFIER}: under {@link #WALK_STOP_MODIFIER} it stands
 * instead ({@link #walkModifier}).
 */
final class GroundSpeed {

    /** Friction of ordinary ground blocks (the vanilla default). */
    static final double BLOCK_FRICTION = 0.6D;
    /** Vanilla drag on a living entity's horizontal velocity, multiplied by the block friction on ground. */
    static final double AIR_DRAG = 0.91D;
    /** The fraction of the horizontal velocity kept after each tick on ordinary ground: 0.546. */
    static final double VELOCITY_KEPT = BLOCK_FRICTION * AIR_DRAG;
    /** {@code LivingEntity.aiStep} damps the forward input by this before {@code travel}. */
    static final double INPUT_DAMPING = 0.98D;
    /** Blocks per tick per unit of the effective speed squared, at steady state on ordinary ground: 2.16. */
    static final double BLOCKS_PER_SPEED_SQUARED = INPUT_DAMPING / (1D - VELOCITY_KEPT);
    /** The client's walk-animation threshold on (|vx| + |vz|) / 2 of the synced velocity ({@code DinosaurRenderer.getMotionAnimThreshold}). */
    static final double WALK_ANIMATION_VELOCITY = 0.005D;
    /**
     * The move per tick that just reaches the threshold on an axis heading, the tightest case
     * ({@code |vx| + |vz| = v}; a diagonal gives sqrt(2) more): {@code 2 x 0.005 / 0.546 = 0.0183}.
     */
    static final double WALK_ANIMATION_BLOCKS_PER_TICK = 2D * WALK_ANIMATION_VELOCITY / VELOCITY_KEPT;
    /**
     * The least modifier a steering member walks at. The threshold modifier is
     * {@code sqrt(0.0183 / 2.16) / 0.25 = 0.37}; 0.45 moves 0.0273 blocks per tick, which is 1.49 times the
     * threshold on an axis heading and 2.1 times on a diagonal ({@code GroundSpeedTest}): room for the
     * velocity sync, a step up or a turn.
     */
    static final double MIN_WALK_MODIFIER = 0.45D;
    /** Below this wanted modifier (a fifth of the floor's ground speed) a steering member stands instead of creeping. */
    static final double WALK_STOP_MODIFIER = 0.2D;
    /**
     * A stored horizontal velocity (what is left after friction, {@link #storedVelocity}) above this counts as
     * moving: the walking threshold {@link HerdBodies#WALKING_SPEED} after friction, 0.0055. The slowest herd
     * walk (graze at 0.6: 0.049 blocks per tick, 0.027 stored) is well above it; a body the guard holds (its
     * velocity cut to a fifth each tick) is under it within a tick.
     */
    static final double MOVING_STORED_SPEED = storedVelocity(HerdBodies.WALKING_SPEED);

    private GroundSpeed() {
    }

    /** Steady-state blocks per tick on ordinary ground for a speed modifier and a {@code MOVEMENT_SPEED} attribute. */
    static double groundSpeed(double modifier, double attribute) {
        double speed = modifier * attribute;
        return BLOCKS_PER_SPEED_SQUARED * speed * speed;
    }

    /** The speed modifier that moves {@code groundSpeed} blocks per tick: the inverse of {@link #groundSpeed}. */
    static double modifierFor(double groundSpeed, double attribute) {
        return Math.sqrt(groundSpeed / BLOCKS_PER_SPEED_SQUARED) / attribute;
    }

    /** The factor on the modifier that scales the ground speed by {@code groundSpeedFactor}: its square root. */
    static double modifierFactor(double groundSpeedFactor) {
        return Math.sqrt(groundSpeedFactor);
    }

    /** The horizontal velocity stored (and sent to the client) after a move of {@code groundSpeed} blocks. */
    static double storedVelocity(double groundSpeed) {
        return groundSpeed * VELOCITY_KEPT;
    }

    /** Whether a stored horizontal velocity (vx, vz) is above {@link #MOVING_STORED_SPEED}: the body moves. */
    static boolean moving(double storedVx, double storedVz) {
        return storedVx * storedVx + storedVz * storedVz > MOVING_STORED_SPEED * MOVING_STORED_SPEED;
    }

    /**
     * The modifier a steering member walks at for a wanted one: 0 (stand) under {@link #WALK_STOP_MODIFIER},
     * otherwise at least {@link #MIN_WALK_MODIFIER}, so the walk animation plays whenever the body moves.
     */
    static double walkModifier(double wanted) {
        if (wanted < WALK_STOP_MODIFIER) {
            return 0D;
        }
        return Math.max(MIN_WALK_MODIFIER, wanted);
    }
}
