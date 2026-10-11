package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/** The one herd timer per server JVM, read by the debug stick. Server thread only. */
public final class HerdTimings {

    private static final TickTimer TIMER = new TickTimer();

    private HerdTimings() {
    }

    static void record(long gameTime, long nanos) {
        TIMER.add(gameTime, nanos);
    }

    public static double averageMillis() {
        return TIMER.averageMillis();
    }

    public static double worstMillis() {
        return TIMER.worstMillis();
    }
}
