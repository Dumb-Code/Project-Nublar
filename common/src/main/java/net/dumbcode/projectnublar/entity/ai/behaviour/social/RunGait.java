package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/**
 * Walk or run animation from the real ground speed (fix P10). Pure; it lives here because it reads the ground
 * speed law in {@link GroundSpeed}.
 * <p>
 * Ground speed is quadratic in the speed setting, so the threshold is a ground speed: the one of setting 1.0,
 * between the herd's walks (graze 0.6, roaming journey 0.8) and its runs (urgent trips and catch-up 1.2,
 * chases 1.5). Hysteresis: a walker starts to run above the speed of {@link #ENTER_MODIFIER}, a runner walks
 * again below the speed of {@link #EXIT_MODIFIER}, so the animation does not flicker at the threshold.
 * <p>
 * Fix P12a: {@link #authoredSpeed} is the ground speed each leg cycle is made for, so the client can scale the
 * cycle rate by the real ground speed ({@code MovementGait}). The run is made for the walk's stride per cycle, from
 * the species' own cycle lengths, so the step rate does not change at the run switch for any species.
 * <p>
 * P13b: the stamina a walk or a run costs ({@link #staminaDrain}).
 */
public final class RunGait {
    /** A walker runs above the ground speed of this setting. */
    static final double ENTER_MODIFIER = 1.05D;
    /** A runner walks below the ground speed of this setting. */
    static final double EXIT_MODIFIER = 0.95D;
    /**
     * The walk cycle is made for the ground speed of this setting (the roaming journey): the Gallimimus
     * {@code move.walk} (1.875 s) then covers about 3 blocks per cycle.
     */
    static final double WALK_AUTHORED_MODIFIER = 0.8D;
    /**
     * Fallback when a cycle length is not known: the run cycle is made for the ground speed of this setting
     * (catch-up), where the Gallimimus {@code run} (0.79 s) covers about 3 blocks, as its walk does.
     */
    static final double RUN_AUTHORED_MODIFIER = 1.2D;
    /**
     * A walk drains this fraction of the species' stamina drain (P13b, ruling Q3: 25 % less than before, so a
     * Gallimimus on a roaming journey walks about 80 blocks from the start minimum of 150 to tired, 80; 60 before).
     */
    static final double WALK_DRAIN_FACTOR = 0.75D;
    /** A run drains this multiple of the species' stamina drain (as before P13b). */
    static final double RUN_DRAIN_FACTOR = 2D;

    private RunGait() {
    }

    /**
     * @param wasRunning  the run animation played on the previous tick
     * @param groundSpeed horizontal blocks per tick (the client animation passes its smoothed speed, P12a)
     * @param attribute   the animal's {@code MOVEMENT_SPEED} attribute
     */
    public static boolean running(boolean wasRunning, double groundSpeed, double attribute) {
        if (wasRunning) {
            return groundSpeed >= GroundSpeed.groundSpeed(EXIT_MODIFIER, attribute);
        }
        return groundSpeed > GroundSpeed.groundSpeed(ENTER_MODIFIER, attribute);
    }

    /**
     * The ground speed in blocks per tick that the walk or the run ({@code running}) cycle is made for.
     * <p>
     * The walk is made for {@link #WALK_AUTHORED_MODIFIER}. The run is made for the same stride per cycle as the walk
     * (P12a fix round 1): walk speed x walk length / run length. At one ground speed both cycles then step at the same
     * rate, so the legs neither speed up nor slow down at the run switch. The species' walk-to-run length ratio goes
     * from 1.33 (dilophosaurus) to 3.3 (triceratops); one fixed run speed slowed the legs of three species at the
     * switch. When a length is not known (0 or less), the run uses {@link #RUN_AUTHORED_MODIFIER}.
     *
     * @param walkCycleTicks the species' walk length in ticks; 0 or less when not known
     * @param runCycleTicks  the species' run length in ticks; 0 or less when not known
     */
    public static double authoredSpeed(boolean running, double attribute, int walkCycleTicks, int runCycleTicks) {
        double walk = GroundSpeed.groundSpeed(WALK_AUTHORED_MODIFIER, attribute);
        if (!running) {
            return walk;
        }
        if (walkCycleTicks <= 0 || runCycleTicks <= 0) {
            return GroundSpeed.groundSpeed(RUN_AUTHORED_MODIFIER, attribute);
        }
        return walk * walkCycleTicks / runCycleTicks;
    }

    /**
     * The stamina one second of self-propelled movement costs (P13b, ruling Q3): a walk {@link #WALK_DRAIN_FACTOR}
     * of the species' drain, a run {@link #RUN_DRAIN_FACTOR} of it (unchanged). Every species, through
     * {@code DinoNeedsUtils.tickStamina}.
     *
     * @param perSecond the species' {@code stamina_drain_per_second}
     * @param running   {@link #running} for the tick the drain is taken (it is taken once a second)
     */
    public static double staminaDrain(double perSecond, boolean running) {
        return perSecond * (running ? RUN_DRAIN_FACTOR : WALK_DRAIN_FACTOR);
    }
}
