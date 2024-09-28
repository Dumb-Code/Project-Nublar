package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.world.phys.Vec3;

public abstract class AngleConstraintIKChain extends StretchingIKChain {

    public AngleConstraintIKChain(double... lengths) {
        super(lengths);
    }

    public AngleConstraintIKChain(Segment... segments) {
        super(segments);
    }

    @Override
    public void reachBackwards(Vec3 base) {
        this.getFirst().move(base);
        this.segments.get(1).move(this.getConstrainedPosForRootSegment());

        for (int i = 0; i < this.segments.size() - 1; i++) {
            Segment currentSegment = this.get(i);
            Segment nextSegment = this.get(i + 1);

            nextSegment.move(moveSegment(nextSegment.getPosition(), currentSegment.getPosition(), currentSegment.length));

            if (i < this.segments.size() - 2) {
                Segment nextNextSegment = this.get(i + 2);
                nextNextSegment.move(this.getConstrainedPositions(currentSegment.getPosition(), nextSegment, nextNextSegment.getPosition()));
            }
        }

        this.endJoint = moveSegment(this.endJoint, this.getLast().getPosition(), this.getLast().length);
        this.endJoint = this.getConstrainedPositions(this.get(this.segments.size() - 2).getPosition(), this.getLast(), this.endJoint);
    }

    public abstract Vec3 getReferencePoint();

    public Vec3 getConstrainedPosForRootSegment() {
        Vec3 C = this.getFirst().getPosition().subtract(0, 1, 0);

        double angle = Math.toDegrees(MathUtil.calculateAngle(this.getFirst().getPosition(), this.segments.get(1).getPosition(), C));
        double clampedAngle = Math.min(this.getFirst().angleSize, angle);

        if (clampedAngle == angle) return this.segments.get(1).getPosition();

        double angleDelta = clampedAngle - angle;

        Vec3 normal = MathUtil.getNormalClosestTo(this.segments.get(1).getPosition(), this.getFirst().getPosition(), this.segments.get(2).getPosition(), this.getReferencePoint());
        return MathUtil.rotatePointOnAPlaneAround(this.segments.get(1).getPosition(), this.getFirst().getPosition(), angleDelta, normal);
    }

    public Vec3 getConstrainedPositions(Vec3 reference, Segment middle, Vec3 endpoint) {
        Vec3 normal = MathUtil.getNormalClosestTo(endpoint, middle.getPosition(), reference, this.getReferencePoint());

        Vec3 referencePoint = MathUtil.rotatePointOnAPlaneAround(reference, middle.getPosition(), middle.angleOffset, normal);

        double angle = Math.toDegrees(MathUtil.calculateAngle(middle.getPosition(), endpoint, referencePoint));
        double clampedAngle = Math.min(middle.angleSize, angle);

        if (clampedAngle == angle) return endpoint;

        double angleDelta = clampedAngle - angle;

        return MathUtil.rotatePointOnAPlaneAround(endpoint, middle.getPosition(), angleDelta, normal);
    }
}
