package net.dumbcode.projectnublar.block.api.geometry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;


public class RotatedRayBox {

    private final AABB box;
    private final Vector3f origin;
    private final Matrix4f forward;
    private final Matrix4f backwards;

    public RotatedRayBox(AABB box, Vector3f origin, Matrix4f forward, Matrix4f backwards) {
        this.box = box;
        this.origin = origin;
        this.forward = forward;
        this.backwards = backwards;
    }

    public AABB getBox() {
        return box;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Matrix4f getForward() {
        return forward;
    }

    public Matrix4f getBackwards() {
        return backwards;
    }

    @Nullable
    public Result rayTrace(Position startIn, Position endIn) {
        Vector3f start = new Vector3f((float)(startIn.x() - this.origin.x()), (float)(startIn.y() - this.origin.y()), (float)(startIn.z() - this.origin.z()));
        Vector3f end = new Vector3f((float)(endIn.x() - this.origin.x()), (float)(endIn.y() - this.origin.y()), (float)(endIn.z() - this.origin.z()));

        this.transform(start, this.forward);
        this.transform(end, this.forward);

        Vec3 sv = new Vec3(start);
        Vec3 ev = new Vec3(end);

        Vec3 diff = sv.subtract(ev);

        //Due to the calculations, the points can appear inside the aabb, meaning the aabb calcualtion is wrong. This is just to extend both points a substantial amount to make it work
        sv = sv.add(diff.x*100, diff.y*100, diff.z*100);
        ev = ev.subtract(diff.x*100, diff.y*100, diff.z*100);

        BlockHitResult result = AABB.clip(Collections.singleton(this.box), sv, ev, BlockPos.ZERO);
        if(result != null) {
            Direction hitDir = result.getDirection();
            Vec3 hit = result.getLocation();
            double dist = hit.distanceToSqr(start.x(), start.y(), start.z());

            Vector3f hitVec = new Vector3f((float) hit.x, (float) hit.y, (float) hit.z);
            this.transform(hitVec, this.backwards);


            Vec3i vec = result.getDirection().getUnitVec3i();
            Vector3f sidevec = new Vector3f(vec.getX(), vec.getY(), vec.getZ());
            this.transform(sidevec, this.backwards);


            result = new BlockHitResult(new Vec3(hitVec), Direction.getApproximateNearest(sidevec.x(), sidevec.y(), sidevec.z()), BlockPos.ZERO, true);

            return new Result(this, result, hitDir, start, end, startIn, endIn, hit, dist);
        }
        return null;
    }

    private void transform(Vector3f vec, Matrix4f mat) {
        Vector4f v = new Vector4f(vec.x(), vec.y(), vec.z(), 1);
        v.mul(mat);
        vec.set(v.x(), v.y(), v.z());
    }

    public Vector3f[] points(double x, double y, double z) {
        return points(RotatedRayBox.this.box, x, y, z);
    }

    public Vector3f[] points(AABB box, double x, double y, double z) {
        int[] values = new int[] {0, 1};
        Vector3f[] points = new Vector3f[8];

        for (int xb : values) {
            for (int yb : values) {
                for (int zb : values) {
                    points[(xb << 2) + (yb << 1) + zb] = new Vector3f(
                            (float) (xb == 1 ? box.maxX : box.minX),
                            (float) (yb == 1 ? box.maxY : box.minY),
                            (float) (zb == 1 ? box.maxZ : box.minZ)
                    );
                }
            }
        }
        for (Vector3f point : points) {
            RotatedRayBox.this.transform(point, RotatedRayBox.this.backwards);
            point.add((float) x, (float) y, (float) z);
        }
        return points;
    }

    public static class Builder {
        private final AABB box;
        private Vector3f origin = new Vector3f(0, 0, 0);
        // A plain JOML matrix, not PoseStack: PoseStack is client-only and fence wires are built on
        // the server. PoseStack.mulPose did the same matrix.rotate(quat) on an identity matrix.
        private final Matrix4f matrix = new Matrix4f();

        public Builder(AABB box) {
            this.box = box;
        }

        public Builder origin(double x, double y, double z) {
            this.origin = new Vector3f((float) x, (float) y, (float) z);
            return this;
        }

        public Builder rotate(double angle, float x, float y, float z) {
            Quaternionf quat = new Quaternionf();
            //todo: check if this is correct
            this.matrix.rotate(quat.rotateAxis((float) angle, x, y, z,quat));
            return this;
        }

        public RotatedRayBox build() {
            Matrix4f pose = new Matrix4f(this.matrix);
            Matrix4f backwards = new Matrix4f(pose);
            backwards.invert();
            return new RotatedRayBox(this.box, this.origin, pose, backwards);
        }
    }


    public record Result(RotatedRayBox parent, BlockHitResult result, Direction hitDir, Vector3f startRotated, Vector3f endRotated, Position start, Position end, Vec3 hitRotated, double distance) {
    }
}
