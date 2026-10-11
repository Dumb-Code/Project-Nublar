package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import java.util.List;
import java.util.function.Predicate;

/**
 * The grazing area of a herd (spec: redesign, "Graze"): a fixed centre held by {@link DinoGroup}, a radius
 * that gives every member a graze-spacing square, random sampling, and the spot choice. Pure arithmetic on
 * horizontal points; the block checks come in as a predicate.
 */
final class HerdArea {

    /** A member samples this many candidates when it picks a graze spot (spec: 8). */
    static final int SPOT_CANDIDATES = 8;
    /** Graze spacing = personal space + this (4.2 for Gallimimus; the spec's 5 assumed a 2-wide body). */
    static final double GRAZE_EXTRA = 2.0D;
    /**
     * The herd area is this many times the radius of the graze-spacing squares alone, so sqrt(6) gives 6 times
     * the area (lead: 3x in playtest fix P9, doubled to 6x in P11; it may change after playtesting): the herd
     * spreads out, while the graze spacing between spots stays {@link #GRAZE_EXTRA} + personal space. Each herd
     * holds its own scale ({@code DinoGroup.areaScale}), this by default.
     */
    static final double HERD_AREA_SCALE = Math.sqrt(6D);

    /** A horizontal position. */
    record Point(double x, double z) {
    }

    private HerdArea() {
    }

    static double grazeSpacing(double personalSpace) {
        return personalSpace + GRAZE_EXTRA;
    }

    /**
     * Herd radius: {@code herdSize} squares of side {@code grazeSpacing}, plus half a body, times {@code scale}
     * (the herd's area scale, {@link #HERD_AREA_SCALE} by default). Gallimimus, 1.2 wide, graze spacing 4.2:
     * at scale 1, 5.9 for 5 and 11.2 for 20; at {@link #HERD_AREA_SCALE}, 14.4 for 5 and 27.4 for 20.
     */
    static double radius(int herdSize, double grazeSpacing, double bodyWidth, double scale) {
        return scale * (grazeSpacing * Math.sqrt(herdSize / Math.PI) + bodyWidth / 2D);
    }

    static boolean inside(double centreX, double centreZ, double radius, double x, double z) {
        double dx = x - centreX;
        double dz = z - centreZ;
        return dx * dx + dz * dz <= radius * radius;
    }

    /** A point uniform over the disc; both units in [0, 1]. */
    static Point sample(double centreX, double centreZ, double radius, double angleUnit, double distanceUnit) {
        double angle = angleUnit * 2D * Math.PI;
        double distance = radius * Math.sqrt(distanceUnit);
        return new Point(centreX + Math.cos(angle) * distance, centreZ + Math.sin(angle) * distance);
    }

    /** A point uniform over the ring between {@code minDistance} and {@code maxDistance}; both units in [0, 1]. */
    static Point sampleRing(double fromX, double fromZ, double minDistance, double maxDistance, double angleUnit, double distanceUnit) {
        double angle = angleUnit * 2D * Math.PI;
        double distance = Math.sqrt(minDistance * minDistance + (maxDistance * maxDistance - minDistance * minDistance) * distanceUnit);
        return new Point(fromX + Math.cos(angle) * distance, fromZ + Math.sin(angle) * distance);
    }

    /** Distance from the point to the nearest occupied point; {@link Double#MAX_VALUE} if none. */
    static double roominess(Point point, List<Point> occupied) {
        double nearest = Double.MAX_VALUE;
        for (Point other : occupied) {
            double dx = point.x() - other.x();
            double dz = point.z() - other.z();
            nearest = Math.min(nearest, dx * dx + dz * dz);
        }
        return nearest == Double.MAX_VALUE ? nearest : Math.sqrt(nearest);
    }

    /** Index of the roomiest candidate, the first on a tie, or {@code -1} if there is none. */
    static int roomiest(List<Point> candidates, List<Point> occupied) {
        int best = -1;
        double bestRoom = -1D;
        for (int i = 0; i < candidates.size(); i++) {
            double room = roominess(candidates.get(i), occupied);
            if (room > bestRoom) {
                bestRoom = room;
                best = i;
            }
        }
        return best;
    }

    /**
     * Spec spot choice: the roomiest candidate that is farther than {@code spacing} from every reserved spot
     * and passes {@code usable} (fit, ground, straight-line reach). Candidates are tried roomiest first, so
     * the costly predicate runs only until one passes. {@code -1} if none qualifies.
     */
    static int pickSpot(List<Point> candidates, List<Point> reserved, double spacing, Predicate<Point> usable) {
        int count = candidates.size();
        double[] room = new double[count];
        for (int i = 0; i < count; i++) {
            room[i] = roominess(candidates.get(i), reserved);
        }
        boolean[] tried = new boolean[count];
        for (int round = 0; round < count; round++) {
            int best = -1;
            for (int i = 0; i < count; i++) {
                if (!tried[i] && (best < 0 || room[i] > room[best])) {
                    best = i;
                }
            }
            tried[best] = true;
            if (room[best] <= spacing) {
                return -1;
            }
            if (usable.test(candidates.get(best))) {
                return best;
            }
        }
        return -1;
    }
}
