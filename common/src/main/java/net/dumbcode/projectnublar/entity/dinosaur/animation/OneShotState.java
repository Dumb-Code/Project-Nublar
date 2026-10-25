package net.dumbcode.projectnublar.entity.dinosaur.animation;

/**
 * The server's current one-shot action and its countdown (fix P10). Pure. {@code DinoOneShots} mirrors
 * {@link #current} into the synced byte.
 */
public final class OneShotState {
    private OneShotAction current = OneShotAction.NONE;
    private int remaining;

    public OneShotAction current() {
        return this.current;
    }

    /**
     * Starts {@code action} for {@code ticks} ticks.
     *
     * @return false when {@code ticks} is not positive or the playing action has the same or a higher priority
     */
    public boolean start(OneShotAction action, int ticks) {
        if (ticks <= 0 || !OneShotAction.mayReplace(this.current, action)) {
            return false;
        }
        this.current = action;
        this.remaining = ticks;
        return true;
    }

    /**
     * One server tick.
     *
     * @return true when the action ended on this tick
     */
    public boolean tick() {
        if (this.current == OneShotAction.NONE) {
            return false;
        }
        if (--this.remaining > 0) {
            return false;
        }
        clear();
        return true;
    }

    public void clear() {
        this.current = OneShotAction.NONE;
        this.remaining = 0;
    }
}
