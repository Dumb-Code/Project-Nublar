package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Caps the fallback paths one herd starts: at most {@link #PER_TICK} per tick, first come first served,
 * and at most one per member every {@link #COOLDOWN_TICKS}. A member that wants a path asks every tick;
 * a member that stops asking drops out of the queue. Server thread only.
 */
final class FallbackBudget {

    static final int PER_TICK = 2;
    static final int COOLDOWN_TICKS = 60;

    private final ArrayDeque<UUID> queue = new ArrayDeque<>();
    private final Map<UUID, Long> lastRequest = new HashMap<>();
    private final Map<UUID, Long> lastGranted = new HashMap<>();
    private long tick = Long.MIN_VALUE;
    private int grantedThisTick;
    private int mostGrantedInOneTick;

    /** {@code true} if the member may start a fallback path now. */
    boolean tryAcquire(UUID member, long gameTime) {
        if (gameTime != this.tick) {
            this.tick = gameTime;
            this.grantedThisTick = 0;
            this.queue.removeIf(id -> gameTime - this.lastRequest.getOrDefault(id, Long.MIN_VALUE) > 1L);
        }
        Long granted = this.lastGranted.get(member);
        if (granted != null && gameTime - granted < COOLDOWN_TICKS) {
            return false;
        }
        this.lastRequest.put(member, gameTime);
        if (!this.queue.contains(member)) {
            this.queue.addLast(member);
        }
        int position = 0;
        for (UUID queued : this.queue) {
            if (queued.equals(member)) {
                break;
            }
            position++;
        }
        if (position >= PER_TICK - this.grantedThisTick) {
            return false;
        }
        this.queue.remove(member);
        this.lastGranted.put(member, gameTime);
        this.grantedThisTick++;
        this.mostGrantedInOneTick = Math.max(this.mostGrantedInOneTick, this.grantedThisTick);
        return true;
    }

    /**
     * The guard held the member's path and stopped it ({@code HerdMoveControl.PATH_HOLD_TICKS}): its cooldown
     * restarts now, so it does not ask for the same path again at once (playtest 2 fix P8a).
     */
    void holdEnded(UUID member, long gameTime) {
        this.lastGranted.put(member, gameTime);
    }

    /** Drops a member that left the herd. */
    void forget(UUID member) {
        this.queue.remove(member);
        this.lastRequest.remove(member);
        this.lastGranted.remove(member);
    }

    /** The most grants in a single tick so far; never above {@link #PER_TICK}. For GameTests. */
    int mostGrantedInOneTick() {
        return this.mostGrantedInOneTick;
    }
}
