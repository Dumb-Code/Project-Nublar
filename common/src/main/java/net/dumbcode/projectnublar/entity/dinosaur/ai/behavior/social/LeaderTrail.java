package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.jetbrains.annotations.Nullable;

/**
 * The points the herd leader actually walked on the current journey, one per {@link #SPACING} block, in a
 * ring buffer of {@link #CAPACITY} (spec "Trail"). Each point keeps its arc length from the journey start
 * and the side room beside it on both sides, measured once with the clearance box ({@code HerdLeading}).
 * Followers project themselves on the trail and steer to points by arc, so they inherit a route the
 * leader's same-size body has walked and their targets move smoothly. Pure.
 */
final class LeaderTrail {

    static final int CAPACITY = 128;
    /** The leader records a point every this many blocks walked. */
    static final double SPACING = 1.0D;

    /**
     * A point on the trail: position, the unit travel direction there, its arc length, and the side room
     * toward {@code (-dirZ, dirX)} ({@code roomPos}) and toward {@code (dirZ, -dirX)} ({@code roomNeg}).
     * Room is NaN until measured. A single-point trail has direction (0, 0).
     */
    record TrailPoint(double x, double y, double z, double dirX, double dirZ, double arc, float roomPos, float roomNeg) {
    }

    private final double[] xs = new double[CAPACITY];
    private final double[] ys = new double[CAPACITY];
    private final double[] zs = new double[CAPACITY];
    private final double[] arcs = new double[CAPACITY];
    private final float[] roomPos = new float[CAPACITY];
    private final float[] roomNeg = new float[CAPACITY];
    private int newest = -1;
    private int size;

    int size() {
        return this.size;
    }

    void clear() {
        this.newest = -1;
        this.size = 0;
    }

    /**
     * Appends the position if it is at least {@link #SPACING} from the newest point.
     *
     * @return the buffer index of the new point (for {@link #setRoom}), or -1 if nothing was recorded
     */
    int record(double x, double y, double z) {
        double arc = 0D;
        if (this.size > 0) {
            double dx = x - this.xs[this.newest];
            double dz = z - this.zs[this.newest];
            double step = Math.sqrt(dx * dx + dz * dz);
            if (step < SPACING) {
                return -1;
            }
            arc = this.arcs[this.newest] + step;
        }
        this.newest = (this.newest + 1) % CAPACITY;
        this.xs[this.newest] = x;
        this.ys[this.newest] = y;
        this.zs[this.newest] = z;
        this.arcs[this.newest] = arc;
        this.roomPos[this.newest] = Float.NaN;
        this.roomNeg[this.newest] = Float.NaN;
        if (this.size < CAPACITY) {
            this.size++;
        }
        return this.newest;
    }

    /** x of the recorded point at logical index {@code k} (0 = oldest, {@link #size} - 1 = newest). Debug visuals. */
    double x(int k) {
        return this.xs[at(k)];
    }

    /** y of the recorded point at logical index {@code k}. */
    double y(int k) {
        return this.ys[at(k)];
    }

    /** z of the recorded point at logical index {@code k}. */
    double z(int k) {
        return this.zs[at(k)];
    }

    /** Arc length of the newest point; NaN if empty. */
    double headArc() {
        return this.size == 0 ? Double.NaN : this.arcs[this.newest];
    }

    /** Arc length of the oldest kept point; NaN if empty. */
    double tailArc() {
        return this.size == 0 ? Double.NaN : this.arcs[at(0)];
    }

    /** The newest point, with the direction of the last segment; {@code null} if empty. */
    @Nullable TrailPoint newest() {
        return this.size == 0 ? null : pointAt(headArc());
    }

    /** The point at {@code arc}, clamped to the trail and interpolated; side room is the tighter of the two ends. */
    @Nullable TrailPoint pointAt(double arc) {
        if (this.size == 0) {
            return null;
        }
        if (this.size == 1) {
            int i = this.newest;
            return new TrailPoint(this.xs[i], this.ys[i], this.zs[i], 0D, 0D, this.arcs[i], this.roomPos[i], this.roomNeg[i]);
        }
        double clamped = Math.max(tailArc(), Math.min(headArc(), arc));
        int segment = segmentAt(clamped);
        int a = at(segment);
        int b = at(segment + 1);
        double length = this.arcs[b] - this.arcs[a];
        double t = (clamped - this.arcs[a]) / length;
        return new TrailPoint(this.xs[a] + (this.xs[b] - this.xs[a]) * t,
                this.ys[a] + (this.ys[b] - this.ys[a]) * t,
                this.zs[a] + (this.zs[b] - this.zs[a]) * t,
                (this.xs[b] - this.xs[a]) / length, (this.zs[b] - this.zs[a]) / length, clamped,
                Math.min(this.roomPos[a], this.roomPos[b]), Math.min(this.roomNeg[a], this.roomNeg[b]));
    }

    /**
     * Arc of the trail point closest to (x, z). With a hint (not NaN), only segments within {@code window}
     * of the hint are searched, so a follower does not jump to a nearby later leg after a U-turn; if none
     * is in the window, the whole trail is searched. NaN if empty.
     */
    double project(double x, double z, double hintArc, double window) {
        if (this.size == 0) {
            return Double.NaN;
        }
        if (this.size == 1) {
            return this.arcs[this.newest];
        }
        double arc = Double.isNaN(hintArc) ? Double.NaN : nearest(x, z, hintArc - window, hintArc + window);
        return Double.isNaN(arc) ? nearest(x, z, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) : arc;
    }

    void setRoom(int index, float roomPos, float roomNeg) {
        this.roomPos[index] = roomPos;
        this.roomNeg[index] = roomNeg;
    }

    /**
     * The tightest side room on one side ({@code positive}: toward {@code (-dirZ, dirX)}) over the trail from
     * {@code fromArc} to {@code toArc}, both clamped to the trail: the points of every segment the range
     * touches. NaN if any of them is unmeasured, or the trail is empty. Followers clamp their lane offset
     * with it, so they move in before a narrowing instead of hitting its edge.
     */
    float minRoom(double fromArc, double toArc, boolean positive) {
        if (this.size == 0) {
            return Float.NaN;
        }
        float[] room = positive ? this.roomPos : this.roomNeg;
        if (this.size == 1) {
            return room[this.newest];
        }
        double from = Math.max(tailArc(), Math.min(headArc(), fromArc));
        double to = Math.max(from, Math.min(headArc(), toArc));
        int last = Math.min(this.size - 1, segmentAt(to) + 1);
        float min = Float.POSITIVE_INFINITY;
        for (int k = segmentAt(from); k <= last; k++) {
            min = Math.min(min, room[at(k)]);
        }
        return min;
    }

    private double nearest(double x, double z, double fromArc, double toArc) {
        double bestArc = Double.NaN;
        double bestDistance = Double.MAX_VALUE;
        for (int k = 0; k < this.size - 1; k++) {
            int a = at(k);
            int b = at(k + 1);
            if (this.arcs[b] < fromArc || this.arcs[a] > toArc) {
                continue;
            }
            double sx = this.xs[b] - this.xs[a];
            double sz = this.zs[b] - this.zs[a];
            double lengthSqr = sx * sx + sz * sz;
            double t = Math.max(0D, Math.min(1D, ((x - this.xs[a]) * sx + (z - this.zs[a]) * sz) / lengthSqr));
            double px = this.xs[a] + sx * t - x;
            double pz = this.zs[a] + sz * t - z;
            double distance = px * px + pz * pz;
            if (distance < bestDistance) {
                bestDistance = distance;
                bestArc = this.arcs[a] + (this.arcs[b] - this.arcs[a]) * t;
            }
        }
        return bestArc;
    }

    /** Logical index (0 = oldest) of the segment start whose arc is the largest not above {@code arc}. */
    private int segmentAt(double arc) {
        int low = 0;
        int high = this.size - 2;
        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            if (this.arcs[at(mid)] <= arc) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    /** Buffer index of logical index {@code k} (0 = oldest). */
    private int at(int k) {
        return Math.floorMod(this.newest - (this.size - 1) + k, CAPACITY);
    }
}
