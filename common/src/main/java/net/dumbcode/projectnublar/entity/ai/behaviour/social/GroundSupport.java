package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/**
 * Cliff edges (playtest 4 cliff investigation, fix P12c): when a body counts as standing on ground, and how a
 * path node at a drop is typed. Before P12c every ground test asked "is there ground under any column of the
 * footprint?", so a Gallimimus (hitbox 1.2) with its centre 0.6 past a rim still "stood", and paths, slots,
 * graze spots and side steps all went right up to the rim.
 * <ul>
 *   <li><b>Centre rule</b> ({@link #standTop}, the herd stand rule {@code BodyFit.standY}): the columns that
 *       hold the body's centre point ({@link #centreColumnMin}, {@link #centreColumnMax}: one, or two or four
 *       when the centre lies on a column boundary) need ground under at least half of them
 *       ({@link #centreSupported}). The hitbox then keeps at least half its width over ground on each axis,
 *       so a body must be moved about 0.6 before it falls. A centre exactly on a rim line counts (half the
 *       body on ground, as on a vanilla rim node); a quarter past it does not.</li>
 *   <li><b>Path nodes</b> ({@link #nodeEdge}, every dinosaur's {@code FenceAwareNodeEvaluator}): over the
 *       vanilla footprint (floor(width + 1) columns square) a node with ground under every column is
 *       {@link Edge#FULL}; one with its centre over ground but a drop beside it is a {@link Edge#RIM} (costly,
 *       so routes keep one block in from a rim where they can); one whose centre is not over ground (for the
 *       Gallimimus: ground under only 1 of its 4 columns, a pit's corner point) is a {@link Edge#HANG}
 *       (blocked). A column grounds a node when its ground lies at most one step down
 *       ({@link #columnGrounds}), so a one-block terrace step is not a rim. Planned drops of up to 3 blocks
 *       onto solid ground stay allowed (lead decision: the drop limit stays 3): a node with no ground under
 *       any column is left to vanilla's own fall test.</li>
 *   <li><b>Guard</b> ({@link #guardChecksGround}, {@code HerdMoveControl.guard}): the box 0.6 ahead must
 *       also stand, except while the body walks a vanilla path facing its next node.</li>
 * </ul>
 * Pure: {@code BodyFit} reads the blocks and hands one result per block column.
 */
final class GroundSupport {

    /** A column whose scan met a hazard (fluid, fire, fences ...) before ground: the body may not stand there. */
    static final double HAZARD = Double.POSITIVE_INFINITY;
    /**
     * A coordinate this close to a whole number lies on a column boundary: the centre then touches the columns
     * on both sides. Probe positions are multiples of 0.25 ({@link ClearanceBox#quantise}) and node centres are
     * whole or half numbers, so the boundary is met exactly; the margin only absorbs rounding.
     */
    static final double BOUNDARY_EPSILON = 1.0E-7D;

    /** What a path node stands on ({@link #nodeEdge}). */
    enum Edge {
        /** Ground under every footprint column: vanilla's own type. */
        FULL,
        /** Ground under the centre, a drop beside it: costly ({@code DANGER_OTHER}), no corner cutting. */
        RIM,
        /** No ground under the centre, but under some column: blocked. */
        HANG,
        /** No ground under any column: vanilla's own type (it tests the fall itself). */
        NONE
    }

    /** The ground top of one block column under a body, NaN if it has none within the drop, {@link #HAZARD} if a hazard comes first. */
    @FunctionalInterface
    interface ColumnTop {
        double top(int x, int z);
    }

    /** Whether one block column grounds a path node ({@link #columnGrounds}). */
    @FunctionalInterface
    interface ColumnGround {
        boolean grounded(int x, int z);
    }

    private GroundSupport() {
    }

    /** The lowest block column that holds the centre coordinate {@code c} on one axis: c - 1 on a column boundary, else floor(c). */
    static int centreColumnMin(double c) {
        double whole = Math.rint(c);
        return Math.abs(c - whole) < BOUNDARY_EPSILON ? (int) whole - 1 : (int) Math.floor(c);
    }

    /** The highest block column that holds the centre coordinate {@code c} on one axis: c on a column boundary, else floor(c). */
    static int centreColumnMax(double c) {
        double whole = Math.rint(c);
        return Math.abs(c - whole) < BOUNDARY_EPSILON ? (int) whole : (int) Math.floor(c);
    }

    /**
     * The centre rule: ground under at least half of the {@code centreColumns} that hold the centre point. One
     * column (inside a column) needs it; two (on a rim line) need one, so a body may stand half over a straight
     * rim; four (on a corner point) need two, so a body on a pit's corner point with three quarters over the
     * void does not stand.
     */
    static boolean centreSupported(int grounded, int centreColumns) {
        return grounded > 0 && grounded * 2 >= centreColumns;
    }

    /**
     * The herd stand rule's ground (fix P12c, the centre column rule): the highest ground top under the box
     * columns of a box {@code halfWidth} each side of (x, z), or NaN if a column meets a hazard first or the
     * centre is not over ground ({@link #centreSupported}). Before P12c any column with ground was enough.
     */
    static double standTop(double x, double z, double halfWidth, ColumnTop columns) {
        int centreMinX = centreColumnMin(x);
        int centreMaxX = centreColumnMax(x);
        int centreMinZ = centreColumnMin(z);
        int centreMaxZ = centreColumnMax(z);
        double top = Double.NaN;
        int centreGrounded = 0;
        for (int bx = (int) Math.floor(x - halfWidth + BOUNDARY_EPSILON); bx <= (int) Math.floor(x + halfWidth - BOUNDARY_EPSILON); bx++) {
            for (int bz = (int) Math.floor(z - halfWidth + BOUNDARY_EPSILON); bz <= (int) Math.floor(z + halfWidth - BOUNDARY_EPSILON); bz++) {
                double column = columns.top(bx, bz);
                if (column == HAZARD) {
                    return Double.NaN;
                }
                if (Double.isNaN(column)) {
                    continue;
                }
                if (Double.isNaN(top) || column > top) {
                    top = column;
                }
                if (bx >= centreMinX && bx <= centreMaxX && bz >= centreMinZ && bz <= centreMaxZ) {
                    centreGrounded++;
                }
            }
        }
        int centreColumns = (centreMaxX - centreMinX + 1) * (centreMaxZ - centreMinZ + 1);
        return centreSupported(centreGrounded, centreColumns) ? top : Double.NaN;
    }

    /**
     * What the path node at (nodeX, nodeZ) stands on, over the vanilla footprint of {@code footprint} columns
     * square from the node (the body stands at node + footprint / 2 on each axis,
     * {@link ClearanceBox#nodeCentre}). For the Gallimimus (footprint 2): 4 of 4 columns grounded is
     * {@link Edge#FULL}, 2 or 3 a {@link Edge#RIM}, 1 a {@link Edge#HANG}, 0 {@link Edge#NONE}. A one-column
     * footprint is FULL or NONE: the body lies inside its column.
     */
    static Edge nodeEdge(int nodeX, int nodeZ, int footprint, ColumnGround ground) {
        double centreX = nodeX + footprint * 0.5D;
        double centreZ = nodeZ + footprint * 0.5D;
        int centreMinX = centreColumnMin(centreX);
        int centreMaxX = centreColumnMax(centreX);
        int centreMinZ = centreColumnMin(centreZ);
        int centreMaxZ = centreColumnMax(centreZ);
        int grounded = 0;
        int centreGrounded = 0;
        for (int bx = nodeX; bx < nodeX + footprint; bx++) {
            for (int bz = nodeZ; bz < nodeZ + footprint; bz++) {
                if (ground.grounded(bx, bz)) {
                    grounded++;
                    if (bx >= centreMinX && bx <= centreMaxX && bz >= centreMinZ && bz <= centreMaxZ) {
                        centreGrounded++;
                    }
                }
            }
        }
        if (grounded == 0) {
            return Edge.NONE;
        }
        if (grounded == footprint * footprint) {
            return Edge.FULL;
        }
        int centreColumns = (centreMaxX - centreMinX + 1) * (centreMaxZ - centreMinZ + 1);
        return centreSupported(centreGrounded, centreColumns) ? Edge.RIM : Edge.HANG;
    }

    /**
     * Whether a column whose highest ground top in the node's rows is {@code groundTop} (NaN: none) grounds the
     * path node at {@code nodeY}: the ground lies at most {@link BodyFit#STEP_HEIGHT} below the node floor. So
     * a carpet, a slab or a one-block terrace step down grounds it (vanilla walks a 2-wide body straddling a
     * one-block step on every hillside, and marking those as rims would price every hill), and a deeper drop
     * does not.
     */
    static boolean columnGrounds(int nodeY, double groundTop) {
        return groundTop >= nodeY - BodyFit.STEP_HEIGHT;
    }

    /**
     * Whether the guard tests the ground 0.6 ahead this tick (fix P12c, cliff cause 3). Only a body on the
     * ground and out of water ({@code grounded}): one in the air is already falling or stepping down a planned
     * drop, and in water the stand rule refuses everything (fluid is a hazard), so the test would hold a
     * swimmer in place. On a vanilla path the test runs only while the yaw lags the next node
     * ({@code facesNextNode} false): the path's nodes passed the rim rules ({@link #nodeEdge}), and the straight
     * line between two of them is trusted, so a leader on a ridge or crossing a narrow gap the body bridges is
     * never held. A lagging yaw (a turn, cliff cause 5) carries the body off that line, which is when it goes
     * over a rim. Steering, side steps and unstacks always test.
     */
    static boolean guardChecksGround(boolean grounded, boolean onPath, boolean facesNextNode) {
        return grounded && !(onPath && facesNextNode);
    }
}
