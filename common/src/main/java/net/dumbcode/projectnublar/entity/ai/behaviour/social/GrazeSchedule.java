package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.function.DoubleSupplier;

/**
 * When a grazing follower picks a spot (spec "Graze", lead decision 6). On entering GRAZE the first pick
 * is staggered over {@link #ENTER_STAGGER_TICKS}; a spot is held for 600 to 1800 ticks; then, with a 50%
 * chance, the member moves to a spot at most {@link #SHORT_MOVE_RANGE} away, otherwise it stays another
 * period. An invalid spot is replaced at once; with no spot found it tries again after {@link #RETRY_TICKS}.
 * During the stay the member eats, rests or idles with its animations (the behaviours that already exist).
 * Pure; one instance per follower.
 */
final class GrazeSchedule {

    static final int ENTER_STAGGER_TICKS = 60;
    static final int STAY_MIN_TICKS = 600;
    static final int STAY_SPREAD_TICKS = 1200;
    static final double MOVE_CHANCE = 0.5D;
    static final int RETRY_TICKS = 100;
    static final double SHORT_MOVE_RANGE = 6.0D;

    /** What the follower does about its spot this tick. */
    enum Action {
        /** Keep the spot (or keep waiting for the next try). */
        HOLD,
        /** Pick a spot anywhere in the herd area. */
        PICK_ANY,
        /** Pick a spot near the current one; keep the current one if none is found. */
        PICK_NEAR
    }

    private long nextDecision;

    /** The herd entered GRAZE: the first pick comes after {@code unit x ENTER_STAGGER_TICKS} ticks. */
    void enter(long now, double unit) {
        this.nextDecision = now + (long) (unit * ENTER_STAGGER_TICKS);
    }

    /**
     * Decides what to do about the spot this tick. {@code PICK_ANY} and {@code PICK_NEAR} repeat every tick
     * until the caller calls {@link #spotTaken} or {@link #noSpot}. On {@code noSpot} the caller must first
     * clear an invalid spot (so {@code hasSpot} is false), or {@code PICK_ANY} repeats every tick.
     */
    Action decide(long now, boolean hasSpot, boolean spotValid, DoubleSupplier random) {
        if (hasSpot && !spotValid) {
            return Action.PICK_ANY;
        }
        if (now < this.nextDecision) {
            return Action.HOLD;
        }
        if (!hasSpot) {
            return Action.PICK_ANY;
        }
        if (random.getAsDouble() < MOVE_CHANCE) {
            return Action.PICK_NEAR;
        }
        spotTaken(now, random);
        return Action.HOLD;
    }

    /** A spot was taken or kept: hold it for 600 to 1800 ticks. */
    void spotTaken(long now, DoubleSupplier random) {
        this.nextDecision = now + STAY_MIN_TICKS + (long) (random.getAsDouble() * STAY_SPREAD_TICKS);
    }

    /** No spot was found: try again after {@link #RETRY_TICKS}. */
    void noSpot(long now) {
        this.nextDecision = now + RETRY_TICKS;
    }

    /** The spot failed (two failed fallback paths): decide again now. */
    void repickNow(long now) {
        this.nextDecision = now;
    }

    long nextDecision() {
        return this.nextDecision;
    }
}
