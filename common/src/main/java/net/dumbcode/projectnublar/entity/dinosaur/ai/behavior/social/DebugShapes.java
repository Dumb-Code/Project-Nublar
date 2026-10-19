package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

/**
 * Point sets for the herd debug visuals ({@code HerdDebugVisuals}): a ring, a line and the edges of a
 * box. Each shape hands its points to a sink one by one, so the caller allocates nothing per point. Pure.
 */
final class DebugShapes {

    /** Receives a horizontal point. */
    interface FlatSink {
        void accept(double x, double z);
    }

    /** Receives a point in space. */
    interface PointSink {
        void accept(double x, double y, double z);
    }

    private DebugShapes() {
    }

    /** {@code points} points evenly round the circle of {@code radius} about (cx, cz), the first along +x. */
    static void ring(double cx, double cz, double radius, int points, FlatSink sink) {
        for (int i = 0; i < points; i++) {
            double angle = 2D * Math.PI * i / points;
            sink.accept(cx + Math.cos(angle) * radius, cz + Math.sin(angle) * radius);
        }
    }

    /** {@code points} points evenly from (x0, y0, z0) to (x1, y1, z1), both ends included (one point: the start). */
    static void line(double x0, double y0, double z0, double x1, double y1, double z1, int points, PointSink sink) {
        for (int i = 0; i < points; i++) {
            double t = points == 1 ? 0D : i / (double) (points - 1);
            sink.accept(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t, z0 + (z1 - z0) * t);
        }
    }

    /**
     * The outline of the box {@code halfWidth} each side of (cx, cz), from {@code feetY} up by {@code height}:
     * its 8 corners once each, then {@code interior} evenly spaced points inside each of the 12 edges, so
     * 8 + 12 x {@code interior} points in all.
     */
    static void boxEdges(double cx, double feetY, double cz, double halfWidth, double height, int interior, PointSink sink) {
        double x0 = cx - halfWidth;
        double x1 = cx + halfWidth;
        double y0 = feetY;
        double y1 = feetY + height;
        double z0 = cz - halfWidth;
        double z1 = cz + halfWidth;
        for (int corner = 0; corner < 8; corner++) {
            sink.accept((corner & 1) == 0 ? x0 : x1, (corner & 2) == 0 ? y0 : y1, (corner & 4) == 0 ? z0 : z1);
        }
        for (int k = 1; k <= interior; k++) {
            double t = k / (double) (interior + 1);
            double x = x0 + (x1 - x0) * t;
            double y = y0 + (y1 - y0) * t;
            double z = z0 + (z1 - z0) * t;
            // The four edges along x, then along y, then along z.
            sink.accept(x, y0, z0);
            sink.accept(x, y0, z1);
            sink.accept(x, y1, z0);
            sink.accept(x, y1, z1);
            sink.accept(x0, y, z0);
            sink.accept(x0, y, z1);
            sink.accept(x1, y, z0);
            sink.accept(x1, y, z1);
            sink.accept(x0, y0, z);
            sink.accept(x0, y1, z);
            sink.accept(x1, y0, z);
            sink.accept(x1, y1, z);
        }
    }
}
