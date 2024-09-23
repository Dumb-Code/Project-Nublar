package net.dumbcode.projectnublar.entity.ik.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class MathUtil {
    public static Vec3 toVec3(Vector3d vector3d) {
        return new Vec3(vector3d.x(), vector3d.y(), vector3d.z());
    }

    public static Vector3d toVector3d(Vec3 vector3d) {
        return new Vector3d(vector3d.x(), vector3d.y(), vector3d.z());
    }

    public static Vec3 getFlatRotationVector(Entity entity) {
        return getFlatRotationVector(entity.getYRot());
    }

    public static Vec3 getFlatRotationVector(double yRot) {
        float f = 0;
        float g = (float) (-yRot * 0.017453292F);
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3(i * j, -k, h * j).normalize();
    }

    public static Vec3 getOpposingFlatRotationVector(Entity entity) {
        return getFlatRotationVector(entity.getYRot() - 180);
    }

    public static Vec3 getRotation(Vec3 base, Vec3 target) {
        /*
        double $$3 = target.x - base.x;
        double $$4 = target.y - base.y;
        double $$5 = target.z - base.z;
        double $$6 = Math.sqrt($$3 * $$3 + $$5 * $$5);
        float xRot = Mth.wrapDegrees((float)(-(Mth.atan2($$4, $$6) * 57.2957763671875)));
        float yRot = Mth.wrapDegrees((float)(Mth.atan2($$5, $$3) * 57.2957763671875) - 90.0F);
        return new Vec2(xRot, yRot);
         */
        float dz = (float) (target.z - base.z);
        float dx = (float) (target.x - base.x);
        float dy = (float) (target.y - base.y);

        float yaw = (float) Mth.atan2(dz, dx);
        float pitch = (float) Mth.atan2(Math.sqrt(dz * dz + dx * dx), dy);
        return wrapAngles(new Vec3(yaw, pitch, 0));
    }

    private static Vec3 wrapAngles(Vec3 vec3) {
        double x = vec3.x;
        double y = vec3.y;
        double z = vec3.z;

        while (x > Math.PI) x -= 2 * Math.PI;
        while (x < -Math.PI) x += 2 * Math.PI;

        while (y > Math.PI) y -= 2 * Math.PI;
        while (y < -Math.PI) y += 2 * Math.PI;

        while (z > Math.PI) z -= 2 * Math.PI;
        while (z < -Math.PI) z += 2 * Math.PI;

        return new Vec3(x,y,z);
    }

    public static Vec3 dividePos(Vec3 v1, double divide) {
        return new Vec3(v1.x() / divide, v1.y() / divide, v1.z() / divide);
    }

    public static Vec3 getAverage(Vec3... points) {

        Vec3 sum = Vec3.ZERO;

        for (Vec3 point : points) {
            sum.add(point);
        }

        return dividePos(sum, points.length);
    }

    /**
     * @param A Point you want the angle of
     * @param B the other point
     * @param C the other point
     * @return the angle of A in radiant [0 - 180]
     **/
    public static double calculateAngle(Vec3 A, Vec3 B, Vec3 C) {
        double a = C.distanceTo(B);
        double b = A.distanceTo(C);
        double c = A.distanceTo(B);

        double cosA = (b * b + c * c - a * a) / (2 * b * c);

        return Math.acos(cosA);
    }

    /**
     * @param A Point you want the angle of
     * @param B the other point
     * @param C the other point
     * @return the angle of A in radiant [0 - 360]
     **/
    public static double calculate360Angle(Vec3 A, Vec3 B, Vec3 C, Vec3 upVec) {
        // Create vectors AB and BC
        Vec3 AB = B.subtract(A);
        Vec3 BC = C.subtract(B);

        // Cross product of AB and BC
        Vec3 crossProduct = AB.cross(BC);

        // Dot product of AB and BC
        double dotProduct = AB.dot(BC);

        // Angle in radians using atan2 (magnitude of the cross product gives the sine of the angle)
        double theta = Math.atan2(crossProduct.length(), dotProduct);

        // Convert to degrees
        double thetaDegrees = Math.toDegrees(theta);

        // Calculate normal vector to determine direction of angle
        Vec3 normal = crossProduct.normalize(); // This is the Z-axis of the plane

        // Determine if the angle should be negative or positive
        double direction = upVec.dot(normal);
        if (direction < 0) {
            thetaDegrees = 360 - thetaDegrees;
        }

        return thetaDegrees;
    }

    /**
     * @param RotatedPoint point which gets rotated
     * @param stationaryPoint reference point
     * @param stationaryPoint2 reference point
     * @param angle in degrees
     * @return returns the rotated position
     */
    public static Vec3 rotatePointOnAPlain(Vec3 RotatedPoint, Vec3 stationaryPoint, Vec3 stationaryPoint2, double angle) {

        Vec3 axis = getUpDirection(RotatedPoint, stationaryPoint, stationaryPoint2);

        return rotatePointOnAPlainAround(RotatedPoint, stationaryPoint, angle, axis);
    }

    public static Vec3 rotatePointOnAPlainAround(Vec3 RotatedPoint, Vec3 stationaryPoint, double angle, Vec3 rotationAxis) {
        Vector3d A = new Vector3d(stationaryPoint.x(), stationaryPoint.y(), stationaryPoint.z()); // Point A
        //rotated vector
        Vector3d C = new Vector3d(RotatedPoint.x(), RotatedPoint.y(), RotatedPoint.z()); // Point C

        // Create the rotation quaternion
        Quaterniond rotation = new Quaterniond().rotateAxis(Math.toRadians(angle), rotationAxis.x, rotationAxis.y, rotationAxis.z);

        // Rotate the vector
        Vector3d rotatedV1 = new Vector3d();
        rotation.transform(new Vector3d(C).sub(A), rotatedV1);

        return new Vec3(new Vector3d(rotatedV1).add(A).x(), new Vector3d(rotatedV1).add(A).y(), new Vector3d(rotatedV1).add(A).z());
    }

    public static Vec3 getUpDirection(Vec3 v1, Vec3 v2, Vec3 v3) {
        // Calculate AB and AC
        Vec3 AB = v2.subtract(v1);
        Vec3 AC = v3.subtract(v1);

        Vec3 axis = AB.cross(AC).normalize();

        return axis;
    }

    public static Vec3 getClosestNormalRelativeToEntity(Vec3 basePoint, Vec3 v2, Vec3 v3, Entity entity) {
        Vec3 referencePoint = getFlatRotationVector(entity.getYRot() + 90);
        Vec3 normal = getNormalClosestTo(basePoint, v2, v3, basePoint.add(referencePoint.scale(100)));
        return normal;
    }

    public static Vec3 getNormalClosestTo(Vec3 basePoint, Vec3 v2, Vec3 v3, Vec3 orientationPoint) {
        Vec3 normal = getUpDirection(basePoint, v2, v3);
        Vec3 oppositeNormal = normal.reverse();

        return basePoint.add(normal).distanceTo(orientationPoint) < basePoint.add(oppositeNormal).distanceTo(orientationPoint) ? normal : oppositeNormal;
    }

    public static Vec3 lerpVec3(int step, Vec3 OldPos, Vec3 newPos) {
        double d = 1.0 / (double)step;
        double newX = Mth.lerp(d, OldPos.x(), newPos.x());
        double newY = Mth.lerp(d, OldPos.y(), newPos.y());
        double newZ = Mth.lerp(d, OldPos.z(), newPos.z());
        return new Vec3(newX, newY, newZ);
    }

    public static Vec3 lerpVector3d(int step, Vector3d OldPos, Vector3d newPos) {
        double d = 1.0 / (double)step;
        double newX = Mth.lerp(d, OldPos.x(), newPos.x());
        double newY = Mth.lerp(d, OldPos.y(), newPos.y());
        double newZ = Mth.lerp(d, OldPos.z(), newPos.z());
        return new Vec3(newX, newY, newZ);
    }

    public static Vec3 convertToFlatVector(Vec3 v1) {
        return new Vec3((float) v1.x(), 0, (float) v1.z());
    }
}
