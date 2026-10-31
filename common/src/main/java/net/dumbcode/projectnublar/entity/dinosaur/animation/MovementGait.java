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
 * The leg cycle's target rate is the smoothed speed over the speed the clip was made for ({@link #authoredSpeed}:
 * the species' measured stride, Task 45), clamped, so the planted feet keep pace with the ground. The walk and the
 * run each have their own stride, so the walk and the run target different step rates at the same ground speed (a
 * Brachiosaurus at the run switch: 14 % fewer steps per tick in the run).
 * <p>
 * The rate does not jump to that target (Task 52). At a switch between walk and run it keeps the step rate (cycles
 * per tick) of the gait before: rate times the new cycle over the old cycle, as on 1.20.1. On the following ticks,
 * and on every tick with no switch, it moves toward the target by at most {@link #MAX_RATE_STEP}. So the step rate
 * changes smoothly over a few ticks instead of at the switch. A walk from a stand takes its target at once. A carried
 * rate can lie outside the clamp for those few ticks; the target never does.
 * <p>
 * The limit only softens the rate; it does not protect the leg phase. GeckoLib 5 adds each frame's time times the
 * speed to the clip's time ({@code AnimationController.checkControllerState}, javap of 5.5.6), so a change of the
 * speed never moves the leg phase. (GeckoLib 4 played a cycle at the speed times the ticks since its start, so there a
 * change also moved the phase, and 1.20.1 limited the change for that reason.)
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
    /**
     * Clamp of the leg cycle rate, as a multiple of the rate the animation was made for. The floor is low because a
     * long-stride walk is made for a fast ground speed: the Velociraptor's walk plays at about 0.25 when it grazes
     * (modifier 0.6), so a floor of 0.5 made its feet twice as fast as the ground.
     */
    public static final double MIN_ANIMATION_SPEED = 0.2D;
    public static final double MAX_ANIMATION_SPEED = 2.0D;
    /**
     * Most the leg cycle rate moves toward its target in one tick (Task 52). A starting value: the lead tunes it by
     * eye in game. Larger is a faster change of the step rate after a speed change or a walk to run switch.
     */
    public static final double MAX_RATE_STEP = 0.05D;
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

    /**
     * The ground the planted feet cover in one leg cycle, in blocks at render scale 1: the walk ({@code move.walk}) and
     * the run (the species' {@link AnimationChoice#RUN} clip).
     */
    public record Strides(double walk, double run) {
        /** A species with no measurement: its leg clips play at rate 1. */
        public static final Strides NOT_MEASURED = new Strides(0D, 0D);
    }

    /**
     * A leg clip as the gait reads it on one tick.
     *
     * @param authoredSpeed {@link #authoredSpeed} of the clip; 0 when not known (the clip then plays at rate 1)
     * @param cycleTicks    the clip's length in ticks; 0 or less when not known (a switch then takes the target rate)
     */
    public record LegClip(double authoredSpeed, int cycleTicks) {
        /** A clip with no known speed or length: it plays at rate 1. */
        public static final LegClip NOT_MEASURED = new LegClip(0D, 0);

        /** The clip of a stride and length at a render scale ({@link MovementGait#authoredSpeed} gives its speed). */
        public static LegClip of(double strideBlocks, double scale, int cycleTicks) {
            return new LegClip(MovementGait.authoredSpeed(strideBlocks, scale, cycleTicks), cycleTicks);
        }
    }

    private double groundSpeed;
    private Gait gait = Gait.IDLE;
    private int heldTicks = HOLD_TICKS;
    private double animationSpeed = 1D;

    /**
     * The strides of the species with the registry id {@code speciesId}. Measured from the species' model and clip
     * files, not seen in game (Task 15 round 2, R3): the foot contact point followed through one cycle of GeckoLib
     * 5.5.6's own evaluation of the clip at render scale 1; the stride is the mean of two estimates, the planted foot's
     * backward speed times the cycle (D1) and twice the foot's front-to-back range (D3). The comments give the clip
     * length and D1 / D3 in blocks. Starting values: the lead tunes them by eye in game.
     */
    public static Strides strides(String speciesId) {
        return switch (speciesId) {
            // move.walk 19.2 ticks: 6.48 / 6.47; run 14.2 ticks: 12.61 / 8.21
            case "velociraptor" -> new Strides(6.5D, 10.4D);
            // move.walk 45 ticks: 5.66 / 6.11; run 20 ticks: 9.66 / 8.56
            case "tyrannosaurus_rex" -> new Strides(5.9D, 9.1D);
            // move.walk 37.5 ticks: 2.40 / 2.96; run 15.8 ticks: 6.71 / 5.21
            case "gallimimus" -> new Strides(2.7D, 6.0D);
            // move.walk 26.7 ticks: 3.85 / 3.95; run 20 ticks: 6.11 / 5.84
            case "dilophosaurus" -> new Strides(3.9D, 6.0D);
            // move.walk 60 ticks: 1.63 / 2.17; run ("Sprint ") 18.3 ticks: 2.70 / 2.78
            case "triceratops" -> new Strides(1.9D, 2.7D);
            // move.walk 80.8 ticks: 1.89 / 2.77; run 55.8 ticks: 2.27 / 3.25
            case "brachiosaurus" -> new Strides(2.3D, 2.8D);
            default -> Strides.NOT_MEASURED;
        };
    }

    /**
     * Ground speed in blocks per tick that a leg cycle is made for: the ground the planted feet cover in one cycle at
     * render scale 1 ({@code strideBlocks}, measured per species and clip), times the animal's render scale, over the
     * clip's length. Played at this ground speed at rate 1, the planted feet stay put on the ground.
     *
     * @param strideBlocks the clip's stride per cycle at render scale 1, in blocks; 0 when not measured
     * @param scale        the animal's render scale ({@code Dinosaur.scaleFactor()})
     * @param cycleTicks   the clip's length in ticks; 0 or less when not known
     * @return the speed, or 0 when the stride or the length is not known
     */
    public static double authoredSpeed(double strideBlocks, double scale, int cycleTicks) {
        return cycleTicks > 0 ? strideBlocks * scale / cycleTicks : 0D;
    }

    /**
     * One client tick.
     *
     * @param groundSpeed horizontal blocks moved on this tick
     * @param attribute   the animal's {@code MOVEMENT_SPEED} attribute
     * @param walk        the walk clip
     * @param run         the run clip (the walk clip for a species that plays its walk for a run)
     */
    public void tick(double groundSpeed, double attribute, LegClip walk, LegClip run) {
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
        Gait previous = this.gait;
        if (wanted != this.gait && this.heldTicks >= HOLD_TICKS) {
            this.gait = wanted;
            this.heldTicks = 0;
        }
        LegClip clip = this.gait == Gait.RUN ? run : walk;
        if (this.gait == Gait.IDLE || clip.authoredSpeed() <= 0D) {
            this.animationSpeed = 1D;
            return;
        }
        double target = Math.max(MIN_ANIMATION_SPEED,
                Math.min(MAX_ANIMATION_SPEED, this.groundSpeed / clip.authoredSpeed()));
        this.animationSpeed = nextRate(previous, previous == Gait.RUN ? run : walk, clip, target);
    }

    /**
     * The leg cycle rate on a moving tick (class comment): the target from a stand, the step rate of the gait before
     * at a walk to run or run to walk switch, else one limited step toward the target.
     */
    private double nextRate(Gait previous, LegClip from, LegClip to, double target) {
        if (previous == Gait.IDLE) {
            return target;
        }
        if (previous != this.gait) {
            boolean lengthsKnown = from.cycleTicks() > 0 && to.cycleTicks() > 0;
            return lengthsKnown ? this.animationSpeed * to.cycleTicks() / from.cycleTicks() : target;
        }
        double change = target - this.animationSpeed;
        return this.animationSpeed + Math.max(-MAX_RATE_STEP, Math.min(MAX_RATE_STEP, change));
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
