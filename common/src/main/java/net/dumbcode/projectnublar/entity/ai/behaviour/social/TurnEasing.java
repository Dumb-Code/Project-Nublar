package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;

/**
 * Eased turning for dinosaurs (playtest 4 fix P12b, motion cause 3). Before, the yaw turned at a fixed
 * {@link Dinosaur#MAX_TURN_PER_TICK} until aligned and then stopped (start and end at full rate), and the
 * body yaw copied the yaw: a follower swivelled 30 degrees in 3 hard ticks.
 * <ul>
 *   <li>{@link #yawStep}: the turn rate ramps in by {@link #TURN_ACCELERATION} per tick up to
 *       {@link Dinosaur#MAX_TURN_PER_TICK}, and ramps out as the rest of the turn gets small, so it lands
 *       exactly on the wanted yaw without overshoot.</li>
 *   <li>{@link #bodyStep}: the body yaw moves {@link #BODY_TURN_FRACTION} of the way to its target each
 *       tick, at most {@link #BODY_MAX_STEP}, instead of copying it.</li>
 * </ul>
 * P13b (ruling 8): softer defaults, the ramp, the ease-out and the body fraction 1.5 x slower.
 * Pure arithmetic; {@link Dinosaur#MAX_TURN_PER_TICK} is a compile-time constant.
 */
public final class TurnEasing {

    /**
     * The turn rate grows by at most this many degrees per tick, each tick: 2, 4, 6, 8, then the top rate 10
     * (P13b, ruling 8: softer, 1.5 x slower than P12b's 3). A 30-degree turn takes 6 ticks (2, 4, 6, 8, 6.3, 3.7
     * degrees; P12b: 5 ticks, 3, 6, 9, 8.5, 3.5).
     */
    static final float TURN_ACCELERATION = 2F;
    /**
     * The deceleration (degrees per tick, per tick) the ease-out plans with: the rate is at most
     * sqrt(2 x this x the rest of the turn), so it falls as the turn nears its end. P13b: 2 (P12b: 3), so a turn
     * slows earlier and lands more gently.
     */
    static final float TURN_EASE_OUT = 2F;
    /**
     * Each tick the body yaw closes this fraction of the gap to its target yaw. P13b: a third (P12b: half), so
     * the body follows the yaw 1.5 x more slowly; behind a full-rate turn it lags 20 to 30 degrees
     * (P12b: 10 to 20). A 90-degree gap closes in 14 ticks (P12b: 10).
     */
    static final float BODY_TURN_FRACTION = 1F / 3F;
    /**
     * The body yaw never moves more than this many degrees in one tick. Not softened in P13b: it must stay
     * above {@link Dinosaur#MAX_TURN_PER_TICK}, or a body that lags a full-rate turn could never catch up.
     */
    static final float BODY_MAX_STEP = 15F;
    /** A body gap this small (degrees) closes at once, so the easing ends instead of creeping. */
    static final float BODY_SNAP_DEGREES = 1F;

    private TurnEasing() {
    }

    /**
     * This tick's signed yaw change toward a yaw {@code delta} degrees away (wrapped to [-180, 180)), given
     * last tick's signed change {@code previousRate} (0 if the body did not turn last tick). Ramps in from
     * the previous rate (a previous rate the other way counts as 0), never above
     * {@link Dinosaur#MAX_TURN_PER_TICK}, ramps out near the end, and never passes {@code delta}.
     */
    public static float yawStep(float delta, float previousRate) {
        if (delta == 0F) {
            return 0F;
        }
        float sign = Math.signum(delta);
        float remaining = Math.abs(delta);
        float base = Math.signum(previousRate) == sign ? Math.abs(previousRate) : 0F;
        float easeOut = (float) Math.sqrt(2D * TURN_EASE_OUT * remaining);
        float rate = Math.min(Math.min(Dinosaur.MAX_TURN_PER_TICK, easeOut), base + TURN_ACCELERATION);
        return sign * Math.min(rate, remaining);
    }

    /**
     * This tick's signed body-yaw change for a body {@code gap} degrees (wrapped) from its target yaw:
     * {@link #BODY_TURN_FRACTION} of it, at most {@link #BODY_MAX_STEP}; a gap within
     * {@link #BODY_SNAP_DEGREES} closes at once.
     */
    public static float bodyStep(float gap) {
        if (Math.abs(gap) <= BODY_SNAP_DEGREES) {
            return gap;
        }
        float step = gap * BODY_TURN_FRACTION;
        return Math.max(-BODY_MAX_STEP, Math.min(BODY_MAX_STEP, step));
    }
}
