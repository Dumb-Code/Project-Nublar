package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/**
 * Stuck detection (spec "Stuck and re-path"): stuck means less than {@link #MIN_PROGRESS} blocks of
 * progress in {@link #WINDOW_TICKS} ticks while not waiting. Progress is a number that grows as the member
 * gets on: the arc on the trail in travel, minus the distance to the spot in graze. Waiting (held back on
 * purpose, or yielding to members) restarts the window. Reports once per window. A stuck report restarts
 * the window but does not count as progress: {@link #lastProgressTick()} keeps the tick of the last real
 * progress, wait or reset, so a member that stays stuck is visible to checks. Pure; one per follower.
 */
final class ProgressMonitor {

    static final int WINDOW_TICKS = 40;
    static final double MIN_PROGRESS = 0.5D;

    private double baseline = Double.NaN;
    private long baselineTick;
    private long lastProgressTick;

    /** Starts a new window at {@code measure}; with NaN the next {@link #update} sets the baseline. */
    void reset(double measure, long now) {
        this.baseline = measure;
        this.baselineTick = now;
        this.lastProgressTick = now;
    }

    /** @return {@code true} if the member is stuck; the window then restarts */
    boolean update(double measure, boolean waiting, long now) {
        if (Double.isNaN(this.baseline) || waiting || measure - this.baseline >= MIN_PROGRESS) {
            reset(measure, now);
            return false;
        }
        if (now - this.baselineTick >= WINDOW_TICKS) {
            this.baseline = measure;
            this.baselineTick = now;
            return true;
        }
        return false;
    }

    /** The tick of the last real progress, wait or reset; a stuck window restart does not move it. */
    long lastProgressTick() {
        return this.lastProgressTick;
    }
}
