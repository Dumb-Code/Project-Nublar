package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

/**
 * Walk or run animation from the real ground speed (fix P10). Pure; it lives here because it reads the ground
 * speed law in {@link GroundSpeed}.
 * <p>
 * Ground speed is quadratic in the speed setting, so the threshold is a ground speed: the one of setting 1.0,
 * between the herd's walks (graze 0.6, roaming journey 0.8) and its runs (urgent trips and catch-up 1.2,
 * chases 1.5). Hysteresis: a walker starts to run above the speed of {@link #ENTER_MODIFIER}, a runner walks
 * again below the speed of {@link #EXIT_MODIFIER}, so the animation does not flicker at the threshold.
 * <p>
 * The leg cycle rate that keeps the feet in pace with this speed is the client's ({@code MovementGait}, fix P12a).
 * <p>
 * P13b: the stamina a walk or a run costs ({@link #staminaDrain}).
 */
public final class RunGait {
    /** A walker runs above the ground speed of this setting. */
    static final double ENTER_MODIFIER = 1.05D;
    /** A runner walks below the ground speed of this setting. */
    static final double EXIT_MODIFIER = 0.95D;
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
