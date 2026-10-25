package net.dumbcode.projectnublar.entity.dinosaur.animation;

/**
 * The startled rule (fix P10). Pure. A threat that is within {@link #RADIUS} at this check but was not at the
 * previous check has approached: that startles the animal, at most once per {@link #COOLDOWN_TICKS}. A threat
 * that stands inside does not. The first check only records (there is no earlier check to compare with).
 * <p>
 * Use per check: {@link #beginCheck}, {@link #inside} for each threat inside the radius, then {@link #endCheck}.
 * No allocation: two fixed id arrays swap each check. More than {@link #MAX_TRACKED} threats inside at once
 * are not all recorded; an unrecorded one counts as an approach at the next check (the cooldown limits that).
 */
public final class StartleWatch {
    /** A threat closer than this, in blocks, counts as inside. */
    public static final double RADIUS = 6.0D;
    /** Ticks after a startle before the animal can be startled again. */
    public static final int COOLDOWN_TICKS = 400;
    /** Ticks between checks. */
    public static final int CHECK_TICKS = 10;
    static final int MAX_TRACKED = 16;

    private int[] inside = new int[MAX_TRACKED];
    private int insideCount;
    private int[] next = new int[MAX_TRACKED];
    private int nextCount;
    private boolean primed;
    private boolean approached;
    private long readyTick = Long.MIN_VALUE;

    public void beginCheck() {
        this.nextCount = 0;
        this.approached = false;
    }

    /** A threat (entity id) is inside the radius at this check. */
    public void inside(int id) {
        if (this.nextCount < MAX_TRACKED) {
            this.next[this.nextCount++] = id;
        }
        if (this.primed && !wasInside(id)) {
            this.approached = true;
        }
    }

    /**
     * Ends the check.
     *
     * @param allowed the animal may be startled now (not attacking, dead or resting); an approach while not
     *                allowed is still recorded, so the threat later counts as standing inside
     * @return true when the animal is startled now
     */
    public boolean endCheck(long now, boolean allowed) {
        int[] swap = this.inside;
        this.inside = this.next;
        this.insideCount = this.nextCount;
        this.next = swap;
        this.primed = true;
        if (this.approached && allowed && now >= this.readyTick) {
            this.readyTick = now + COOLDOWN_TICKS;
            return true;
        }
        return false;
    }

    private boolean wasInside(int id) {
        for (int i = 0; i < this.insideCount; i++) {
            if (this.inside[i] == id) {
                return true;
            }
        }
        return false;
    }
}
