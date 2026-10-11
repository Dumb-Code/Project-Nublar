package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The present members of one herd as plain numbers: position, velocity per tick, body width, resting,
 * tight ground ({@link HerdMoveControl#tightGround}, measured by each member's own move control), whether
 * it wants to move ({@link HerdMoveControl#wantsToMove}), walking and rank (0 = the leader). {@code GroupManager.bodiesOf} refills it once per game tick, so the
 * steering of 20 members reads 20 positions instead of resolving 20 x 20 entities. A member's velocity is
 * its move since the previous snapshot (kept here; a member new to the snapshot gets 0), not the entity's
 * own {@code x - xo}: the level resets that right before each entity's tick, so every member that had not
 * ticked yet would read 0 and count as standing. Pure: no Minecraft classes. Server thread only.
 * <p>
 * Spec "Who yields": walking members that outrank the asker are predicted along their velocity; every
 * other member counts as standing. So the lower rank waits for the higher, and walkers go round standing
 * members.
 */
final class HerdBodies {

    /** Masks look this many ticks ahead (spec: "within 10 ticks"). */
    static final int PREDICT_TICKS = 10;
    /** A member moving at least this many blocks per tick counts as walking. */
    static final double WALKING_SPEED = 0.01D;
    private static final int INITIAL_SIZE = 8;

    private long tick = Long.MIN_VALUE;
    private int count;
    private UUID[] ids = new UUID[INITIAL_SIZE];
    private double[] xs = new double[INITIAL_SIZE];
    private double[] zs = new double[INITIAL_SIZE];
    private double[] vxs = new double[INITIAL_SIZE];
    private double[] vzs = new double[INITIAL_SIZE];
    private double[] widths = new double[INITIAL_SIZE];
    private boolean[] resting = new boolean[INITIAL_SIZE];
    private boolean[] tight = new boolean[INITIAL_SIZE];
    private boolean[] wantsToMove = new boolean[INITIAL_SIZE];
    private boolean[] walking = new boolean[INITIAL_SIZE];
    private int[] ranks = new int[INITIAL_SIZE];
    /** The previous snapshot's ids and positions, for the velocity; reused, never reallocated unless the herd grows. */
    private int previousCount;
    private long previousTicks = 1L;
    private UUID[] previousIds = new UUID[INITIAL_SIZE];
    private double[] previousXs = new double[INITIAL_SIZE];
    private double[] previousZs = new double[INITIAL_SIZE];

    long tick() {
        return this.tick;
    }

    int count() {
        return this.count;
    }

    /** Starts a new snapshot for {@code tick}; the members so far become the previous snapshot. */
    void begin(long tick) {
        if (this.previousIds.length < this.count) {
            this.previousIds = new UUID[this.ids.length];
            this.previousXs = new double[this.ids.length];
            this.previousZs = new double[this.ids.length];
        }
        System.arraycopy(this.ids, 0, this.previousIds, 0, this.count);
        System.arraycopy(this.xs, 0, this.previousXs, 0, this.count);
        System.arraycopy(this.zs, 0, this.previousZs, 0, this.count);
        this.previousCount = this.count;
        this.previousTicks = Math.max(1L, tick - this.tick);
        this.tick = tick;
        this.count = 0;
    }

    /**
     * Adds a member. Its velocity is its move per tick since the previous snapshot, 0 if it was not in it;
     * it walks when that is at least {@link #WALKING_SPEED}. {@code tight}: it stands in tight ground;
     * {@code wantsToMove}: a path, steering or an override means to move it.
     */
    void add(UUID id, double x, double z, double width, boolean resting, boolean tight, boolean wantsToMove, int rank) {
        if (this.count == this.ids.length) {
            grow();
        }
        int i = this.count++;
        int previous = previousIndexOf(id, i);
        double vx = previous < 0 ? 0D : (x - this.previousXs[previous]) / this.previousTicks;
        double vz = previous < 0 ? 0D : (z - this.previousZs[previous]) / this.previousTicks;
        this.ids[i] = id;
        this.xs[i] = x;
        this.zs[i] = z;
        this.vxs[i] = vx;
        this.vzs[i] = vz;
        this.widths[i] = width;
        this.resting[i] = resting;
        this.tight[i] = tight;
        this.wantsToMove[i] = wantsToMove;
        this.walking[i] = vx * vx + vz * vz > WALKING_SPEED * WALKING_SPEED;
        this.ranks[i] = rank;
    }

    /** Index of the member in the previous snapshot, or -1. Members come in the same order each tick, so {@code hint} usually hits. */
    private int previousIndexOf(UUID id, int hint) {
        if (hint < this.previousCount && this.previousIds[hint].equals(id)) {
            return hint;
        }
        for (int i = 0; i < this.previousCount; i++) {
            if (this.previousIds[i].equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /** Index of the member, or -1. */
    int indexOf(UUID id) {
        for (int i = 0; i < this.count; i++) {
            if (this.ids[i].equals(id)) {
                return i;
            }
        }
        return -1;
    }

    UUID id(int i) {
        return this.ids[i];
    }

    double x(int i) {
        return this.xs[i];
    }

    double z(int i) {
        return this.zs[i];
    }

    boolean walking(int i) {
        return this.walking[i];
    }

    /** {@code true} if the member stands in tight ground ({@link HerdMoveControl#tightGround}). */
    boolean tight(int i) {
        return this.tight[i];
    }

    /** {@code true} if something means to move the member ({@link HerdMoveControl#wantsToMove}). */
    boolean wantsToMove(int i) {
        return this.wantsToMove[i];
    }

    /** The hard minimum between two members ({@link HerdSpacing#hardMinimum}): tight when either stands in tight ground. */
    double minimum(int a, int b) {
        return HerdSpacing.hardMinimum(this.widths[a], this.widths[b], this.resting[a] || this.resting[b], this.tight[a] || this.tight[b]);
    }

    /** {@code true} if member a goes first: lower rank number, then the lower UUID. */
    boolean outranks(int a, int b) {
        return this.ranks[a] < this.ranks[b] || this.ranks[a] == this.ranks[b] && this.ids[a].compareTo(this.ids[b]) < 0;
    }

    /**
     * Mask test: would {@code self}, moving (vx, vz) per tick, come inside any member's hard minimum within
     * {@link #PREDICT_TICKS}? Walking members that outrank {@code self} move along their velocity; all
     * others count as standing.
     */
    boolean wouldCrowd(int self, double vx, double vz) {
        return crowder(self, vx, vz) >= 0;
    }

    /**
     * The nearest member whose hard minimum {@code self}, moving (vx, vz) per tick, would enter within
     * {@link #PREDICT_TICKS} (the {@link #wouldCrowd} test), or -1: the member a yield is to, which the
     * make-way ask names (playtest 2 fix P8b). The nearest, not the first, so a queue asks the member right
     * ahead of it. A full pass over the members only on a crowded heading (an open one is a full pass anyway).
     */
    int crowder(int self, double vx, double vz) {
        int nearest = -1;
        double nearestSqr = Double.POSITIVE_INFINITY;
        for (int i = 0; i < this.count; i++) {
            if (i == self) {
                continue;
            }
            boolean moving = predictedMoving(self, i);
            if (HerdSpacing.wouldCrowd(this.xs[self], this.zs[self], vx, vz, this.xs[i], this.zs[i],
                    moving ? this.vxs[i] : 0D, moving ? this.vzs[i] : 0D, minimum(self, i), PREDICT_TICKS)) {
                double dx = this.xs[i] - this.xs[self];
                double dz = this.zs[i] - this.zs[self];
                double distanceSqr = dx * dx + dz * dz;
                if (distanceSqr < nearestSqr) {
                    nearestSqr = distanceSqr;
                    nearest = i;
                }
            }
        }
        return nearest;
    }

    /** Guard test: the member whose hard minimum the point (aheadX, aheadZ) enters while getting closer, or -1. */
    int blockerAt(int self, double aheadX, double aheadZ) {
        for (int i = 0; i < this.count; i++) {
            if (i != self && HerdSpacing.stepCrowds(this.xs[self], this.zs[self], aheadX, aheadZ, this.xs[i], this.zs[i], minimum(self, i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * The member that limits how far {@code self} can walk along the unit direction (dirX, dirZ): the one
     * whose hard minimum {@code self} reaches first ({@link #freeTo}), if that is closer than {@code cap};
     * otherwise -1. Members behind or beside the line are ignored. One pass, no allocation.
     */
    int nearestAhead(int self, double dirX, double dirZ, double cap) {
        int nearest = -1;
        double free = cap;
        for (int i = 0; i < this.count; i++) {
            if (i == self) {
                continue;
            }
            double distance = freeTo(self, i, dirX, dirZ);
            if (distance < free) {
                free = distance;
                nearest = i;
            }
        }
        return nearest;
    }

    /**
     * How far {@code self} can walk along the unit direction (dirX, dirZ) before it reaches member
     * {@code i}'s hard minimum (0 if it is inside already), or {@link Double#POSITIVE_INFINITY} if the member
     * is behind or beside the line.
     */
    double freeTo(int self, int i, double dirX, double dirZ) {
        double rx = this.xs[i] - this.xs[self];
        double rz = this.zs[i] - this.zs[self];
        double along = rx * dirX + rz * dirZ;
        if (along <= 0D) {
            return Double.POSITIVE_INFINITY;
        }
        double minimum = minimum(self, i);
        double lateralSqr = rx * rx + rz * rz - along * along;
        if (lateralSqr >= minimum * minimum) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.max(0D, along - Math.sqrt(minimum * minimum - lateralSqr));
    }

    /**
     * Spec "Who yields", the member masks' rule: {@code self} predicts member {@code i} along its velocity
     * only if {@code i} walks and outranks it; every other member counts as standing. Speed matching uses
     * the same rule (playtest 4 fix P12b), so a matched follow and the masks agree.
     */
    boolean predictedMoving(int self, int i) {
        return this.walking[i] && outranks(i, self);
    }

    /** Member {@code i}'s velocity along the unit direction (dirX, dirZ), in blocks per tick; 0 if it moves against it. */
    double speedAlong(int i, double dirX, double dirZ) {
        return Math.max(0D, this.vxs[i] * dirX + this.vzs[i] * dirZ);
    }

    /**
     * Playtest 4 fix P12b, fix round 1: whether a standing member answers a make-way ask
     * ({@code HerdMoveControl.askToMakeWay}). A member that is up always answers. A resting member answers
     * only the herd leader, and gets up for it: the leader walks a vanilla path that cannot go round the
     * body, and the guard holds it at speed 0, so without this it would wait for the whole rest. A follower
     * goes round a resting member (or waits, then walks a fallback path) instead. A member that still sits
     * down (the ~69-tick sit phase of {@code Rest}) answers nobody: the leader's wait is bounded by that phase.
     *
     * @param sitting       the member is in its sit-down phase
     * @param resting       the member rests (lies down, after the sit-down phase)
     * @param askedByLeader the walker that asks is the herd leader
     */
    static boolean answersMakeWay(boolean sitting, boolean resting, boolean askedByLeader) {
        return !sitting && (!resting || askedByLeader);
    }

    /** {@code true} if any member other than {@code self} stands within {@code spacing} of (x, z). */
    boolean anyWithin(int self, double x, double z, double spacing) {
        for (int i = 0; i < this.count; i++) {
            if (i == self) {
                continue;
            }
            double dx = this.xs[i] - x;
            double dz = this.zs[i] - z;
            if (dx * dx + dz * dz < spacing * spacing) {
                return true;
            }
        }
        return false;
    }

    /** The highest-ranked member inside {@code self}'s hard minimum that outranks {@code self}, or -1 (spec "Unstack"). */
    int crowdedBy(int self) {
        int best = -1;
        for (int i = 0; i < this.count; i++) {
            if (i == self || !outranks(i, self)) {
                continue;
            }
            double dx = this.xs[i] - this.xs[self];
            double dz = this.zs[i] - this.zs[self];
            double minimum = minimum(self, i);
            if (dx * dx + dz * dz < minimum * minimum && (best < 0 || outranks(i, best))) {
                best = i;
            }
        }
        return best;
    }

    /** The members inside {@code self}'s hard minimum, for {@link HerdSpacing#away}; no allocation if there are none. */
    List<HerdSpacing.Neighbour> inside(int self) {
        List<HerdSpacing.Neighbour> found = List.of();
        for (int i = 0; i < this.count; i++) {
            if (i == self) {
                continue;
            }
            double dx = this.xs[i] - this.xs[self];
            double dz = this.zs[i] - this.zs[self];
            double minimum = minimum(self, i);
            if (dx * dx + dz * dz < minimum * minimum) {
                if (found.isEmpty()) {
                    found = new ArrayList<>(2);
                }
                found.add(new HerdSpacing.Neighbour(dx, dz, minimum));
            }
        }
        return found;
    }

    private void grow() {
        int size = this.ids.length * 2;
        this.ids = Arrays.copyOf(this.ids, size);
        this.xs = Arrays.copyOf(this.xs, size);
        this.zs = Arrays.copyOf(this.zs, size);
        this.vxs = Arrays.copyOf(this.vxs, size);
        this.vzs = Arrays.copyOf(this.vzs, size);
        this.widths = Arrays.copyOf(this.widths, size);
        this.resting = Arrays.copyOf(this.resting, size);
        this.tight = Arrays.copyOf(this.tight, size);
        this.wantsToMove = Arrays.copyOf(this.wantsToMove, size);
        this.walking = Arrays.copyOf(this.walking, size);
        this.ranks = Arrays.copyOf(this.ranks, size);
    }
}
