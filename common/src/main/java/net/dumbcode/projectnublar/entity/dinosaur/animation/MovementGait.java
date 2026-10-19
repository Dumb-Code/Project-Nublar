package net.dumbcode.projectnublar.entity.dinosaur.animation;

import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.RunGait;

/**
 * Stand, walk or run for the leg animation, and the leg cycle rate (fix P12a). Pure; one per animal, fed once per
 * client tick.
 * <p>
 * The client sees the body's position in 3-tick pieces, so the distance moved in one tick is noisy: a late packet
 * gives a still tick, two close packets a burst, and a guard tick on the server a near stop. To keep one such tick
 * from changing the gait:
 * <ul>
 *   <li>the gait reads an exponential average of the ground speed ({@link #SMOOTHING}), and one tick counts at most
 *       {@link #MAX_GROUND_SPEED};</li>
 *   <li>a gait lasts at least {@link #HOLD_TICKS} before the next change (the first change after a long stand is
 *       free);</li>
 *   <li>walk or run uses the {@link RunGait} hysteresis on the smoothed speed.</li>
 * </ul>
 * The leg cycle plays at the smoothed speed over the speed it was made for ({@link RunGait#authoredSpeed}), clamped,
 * so the feet do not slide and the step rate does not jump at the run switch.
 * <p>
 * GeckoLib plays a cycle at {@code speed x (ticks since the cycle started)}, so a change of the speed also moves
 * the leg phase by {@code change x ticks since the start}. The change per tick is limited so that this never moves
 * the legs more than {@link #MAX_RATE_ERROR} of the wanted rate: at most {@code MAX_RATE_ERROR x speed / elapsed},
 * where elapsed is the ticks since the gait started, but not more than one cycle. The legs then never run backward.
 * A new gait starts its animation from the first frame, so there the speed may change at once. A walk from a stand
 * takes its target at once. A change between walk and run keeps the step rate (cycles per tick) of the previous gait
 * (P12a fix round 1): in a settled walk the speed follows an acceleration slowly, so taking the run's target at once
 * made the legs step up to about 1.9 times faster at the switch (a Gallimimus that speeds up over 1 to 10 ticks).
 * Early in the new cycle the limit is wide, so the speed then reaches its target in a few ticks; for those ticks it
 * may lie outside the clamp.
 */
public final class MovementGait {
    /** Weight of the newest tick in the exponential average of the ground speed (time constant about 3 ticks). */
    public static final double SMOOTHING = 0.3D;
    /**
     * A smoothed ground speed (blocks per tick) at or above this is a walk: under the steering floor (modifier 0.45,
     * 0.027 blocks per tick) and above GeckoLib's old walk threshold for a body on an axis heading (0.018).
     */
    public static final double MOVING_SPEED = 0.015D;
    /** A gait plays at least this many ticks before it changes. */
    public static final int HOLD_TICKS = 8;
    /** Clamp of the leg cycle rate, as a multiple of the rate the animation was made for. */
    public static final double MIN_ANIMATION_SPEED = 0.5D;
    public static final double MAX_ANIMATION_SPEED = 2.0D;
    /** Largest error of the leg rate that a change of the animation speed may cause, as a fraction of the speed. */
    public static final double MAX_RATE_ERROR = 0.5D;
    /** Cycle length in ticks when the species' animation length is not known (about the Gallimimus walk). */
    public static final int FALLBACK_CYCLE_TICKS = 40;
    /**
     * Most blocks one tick counts in the average (P12a fix round 1). The fastest setting the AI uses is a chase
     * (modifier 1.5: 0.30 blocks per tick at {@code MOVEMENT_SPEED} 0.25), and a burst tick moves 4/3 of that (0.40).
     * A longer move in one tick is a teleport or a position correction, not a step: it counts as one fast step, so a
     * standing animal walks for the hold and does not run, and the average is back down within a few ticks.
     */
    public static final double MAX_GROUND_SPEED = 0.42D;

    /** The leg animation state. */
    public enum Gait {
        IDLE,
        WALK,
        RUN
    }

    private double groundSpeed;
    private Gait gait = Gait.IDLE;
    private int heldTicks = HOLD_TICKS;
    /** Ticks since the gait changed, up to {@link Integer#MAX_VALUE}. */
    private int gaitTicks;
    private double animationSpeed = 1D;

    /**
     * One client tick.
     *
     * @param groundSpeed    horizontal blocks moved on this tick
     * @param attribute      the animal's {@code MOVEMENT_SPEED} attribute
     * @param walkCycleTicks length of the species' walk animation in ticks; 0 or less when not known
     * @param runCycleTicks  length of the species' run animation in ticks; 0 or less when not known
     */
    public void tick(double groundSpeed, double attribute, int walkCycleTicks, int runCycleTicks) {
        this.groundSpeed += SMOOTHING * (Math.min(groundSpeed, MAX_GROUND_SPEED) - this.groundSpeed);
        Gait wanted;
        if (this.groundSpeed < MOVING_SPEED) {
            wanted = Gait.IDLE;
        } else {
            wanted = RunGait.running(this.gait == Gait.RUN, this.groundSpeed, attribute) ? Gait.RUN : Gait.WALK;
        }
        if (this.heldTicks < HOLD_TICKS) {
            this.heldTicks++;
        }
        if (this.gaitTicks < Integer.MAX_VALUE) {
            this.gaitTicks++;
        }
        Gait previous = this.gait;
        boolean changed = wanted != this.gait && this.heldTicks >= HOLD_TICKS;
        if (changed) {
            this.gait = wanted;
            this.heldTicks = 0;
            this.gaitTicks = 0;
        }
        if (this.gait == Gait.IDLE) {
            this.animationSpeed = 1D;
            return;
        }
        double target = Math.max(MIN_ANIMATION_SPEED, Math.min(MAX_ANIMATION_SPEED,
                this.groundSpeed / RunGait.authoredSpeed(this.gait == Gait.RUN, attribute, walkCycleTicks, runCycleTicks)));
        int cycle = this.gait == Gait.RUN ? runCycleTicks : walkCycleTicks;
        if (changed) {
            int previousCycle = previous == Gait.RUN ? runCycleTicks : walkCycleTicks;
            // Walk to run or back: the same cycles per tick as before (class comment). From a stand: the target.
            boolean keepStepRate = previous != Gait.IDLE && cycle > 0 && previousCycle > 0;
            this.animationSpeed = keepStepRate ? this.animationSpeed * cycle / previousCycle : target;
            return;
        }
        double elapsed = Math.min(this.gaitTicks, (cycle > 0 ? cycle : FALLBACK_CYCLE_TICKS) / this.animationSpeed);
        double step = MAX_RATE_ERROR * this.animationSpeed / Math.max(1D, elapsed);
        this.animationSpeed += Math.max(-step, Math.min(step, target - this.animationSpeed));
    }

    public Gait gait() {
        return this.gait;
    }

    /** Whether the legs walk or run. */
    public boolean moving() {
        return this.gait != Gait.IDLE;
    }

    /** The leg cycle rate for the movement controller: 1 plays the animation as made. */
    public double animationSpeed() {
        return this.animationSpeed;
    }

    /** The smoothed ground speed, blocks per tick. */
    public double groundSpeed() {
        return this.groundSpeed;
    }
}
