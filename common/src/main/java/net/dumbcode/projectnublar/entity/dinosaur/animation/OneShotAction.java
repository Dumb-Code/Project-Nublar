package net.dumbcode.projectnublar.entity.dinosaur.animation;

/**
 * The timed one-shot actions the server starts and the client plays (fix P10): the journey-start call, the
 * leader's watch and startled. One synced byte carries the current one ({@link #id}); the server clears it after
 * the animation length ({@link OneShotState}). Pure.
 * <p>
 * Priority is the declaration order: a playing action is replaced only by a later one ({@link #mayReplace}).
 * Startled replaces the call and the watch; the journey call replaces the watch.
 */
public enum OneShotAction {
    NONE(null),
    SENTRY(AnimationChoice.SENTRY),
    CALLING(AnimationChoice.CALL),
    STARTLED(AnimationChoice.STARTLED);

    /** Sentry length when the server cannot read the file: the Gallimimus sentry, 4.74 s. */
    public static final int SENTRY_FALLBACK_TICKS = 95;
    /** Startled length when the server cannot read the file: the Gallimimus startled, 3.71 s. */
    public static final int STARTLED_FALLBACK_TICKS = 74;
    /** The journey's first walk waits for the call at most this long, even when the call is longer. */
    public static final int JOURNEY_CALL_MAX_WAIT_TICKS = 60;
    /** The leader's watch is checked every 200-400 ticks. */
    public static final int SENTRY_CHECK_MIN_TICKS = 200;
    public static final int SENTRY_CHECK_SPREAD_TICKS = 200;
    /** The chance that a watch check starts the watch. */
    public static final double SENTRY_CHANCE = 0.3D;

    private static final OneShotAction[] BY_ID = values();

    private final AnimationChoice choice;

    OneShotAction(AnimationChoice choice) {
        this.choice = choice;
    }

    /** The animation state this action plays; {@code null} for {@link #NONE}. */
    public AnimationChoice choice() {
        return this.choice;
    }

    public byte id() {
        return (byte) ordinal();
    }

    /** The action for a synced id; {@link #NONE} for an unknown id. */
    public static OneShotAction byId(byte id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : NONE;
    }

    /** Whether {@code next} may start while {@code current} plays. */
    public static boolean mayReplace(OneShotAction current, OneShotAction next) {
        return next != NONE && next.ordinal() > current.ordinal();
    }

    /**
     * How long the action plays, in ticks, or 0 when it does not play.
     *
     * @param known the length from the species' file, {@link AnimationChoice#LENGTH_NOT_DEFINED} or
     *              {@link AnimationChoice#LENGTH_UNKNOWN}
     */
    public int lengthTicks(int known) {
        if (known == AnimationChoice.LENGTH_NOT_DEFINED) {
            return 0;
        }
        if (known == AnimationChoice.LENGTH_UNKNOWN) {
            return switch (this) {
                case SENTRY -> SENTRY_FALLBACK_TICKS;
                case STARTLED -> STARTLED_FALLBACK_TICKS;
                // Unknown file: the leader sets off at once, as before the call existed.
                case CALLING, NONE -> 0;
            };
        }
        return known;
    }

    /** How long a roaming journey's first walk waits for a call of {@code callTicks}. */
    public static int journeyWaitTicks(int callTicks) {
        return Math.min(callTicks, JOURNEY_CALL_MAX_WAIT_TICKS);
    }

    /** The tick of the next watch check after one at {@code now}. */
    public static long nextSentryCheck(long now, double roll) {
        return now + SENTRY_CHECK_MIN_TICKS + (long) (roll * (SENTRY_CHECK_SPREAD_TICKS + 1));
    }

    public static boolean sentryRollPasses(double roll) {
        return roll < SENTRY_CHANCE;
    }
}
