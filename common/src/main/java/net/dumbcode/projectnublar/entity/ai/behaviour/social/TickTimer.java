package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/**
 * Sums the time herd code spends in each game tick and keeps the last {@link #WINDOW} finished ticks,
 * for the performance budget: average and worst tick. Ticks in which herd code did not run are not
 * counted, which makes the average a little pessimistic. Server thread only.
 */
final class TickTimer {

    static final int WINDOW = 600;

    private final long[] totals = new long[WINDOW];
    private int next;
    private int filled;
    private long currentTick = Long.MIN_VALUE;
    private long currentTotal;

    void add(long gameTime, long nanos) {
        if (gameTime != this.currentTick) {
            closeTick();
            this.currentTick = gameTime;
        }
        this.currentTotal += nanos;
    }

    double averageMillis() {
        if (this.filled == 0) {
            return 0D;
        }
        long sum = 0L;
        for (int i = 0; i < this.filled; i++) {
            sum += this.totals[i];
        }
        return sum / (double) this.filled / 1.0E6D;
    }

    double worstMillis() {
        long worst = 0L;
        for (int i = 0; i < this.filled; i++) {
            worst = Math.max(worst, this.totals[i]);
        }
        return worst / 1.0E6D;
    }

    private void closeTick() {
        if (this.currentTick == Long.MIN_VALUE) {
            return;
        }
        this.totals[this.next] = this.currentTotal;
        this.next = (this.next + 1) % WINDOW;
        this.filled = Math.min(this.filled + 1, WINDOW);
        this.currentTotal = 0L;
    }
}
