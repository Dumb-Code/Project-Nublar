package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.world.phys.Vec3;

public class AngleConstraintIKChain extends EntityStretchingIKChain {
    public AngleConstraintIKChain(double... lengths) {
        super(lengths);
    }

    public AngleConstraintIKChain(Segment... segments) {
        super(segments);
    }

    @Override
    public void reachBackwards(Vec3 base) {
        this.getFirst().position = base;
        this.segments.get(1).position = this.getConstrainedPosForRootSegment();

        for (int i = 0; i < this.segments.size() - 1; i++) {
            Segment currentSegment = this.get(i);
            Segment nextSegment = this.get(i + 1);

            nextSegment.position = moveSegment(nextSegment.position, currentSegment.position, currentSegment.length);

            if (i < this.segments.size() - 2) {
                Segment nextNextSegment = this.get(i + 2);
                nextNextSegment.position = this.getConstrainedPos(currentSegment.position, nextSegment, nextNextSegment.position);
            }
        }

        this.endJoint = moveSegment(this.endJoint, this.getLast().position, this.getLast().length);
        //this.endJoint = this.getConstrainedPos(this.get(this.segments.size() - 2).position, this.getLast(), this.endJoint);
    }

    public Vec3 getConstrainedPosForRootSegment() {
        Vec3 C = this.getFirst().position.subtract(0, 1, 0);

        double angle = Math.toDegrees(MathUtil.calculateAngle(this.getFirst().position, this.segments.get(1).position, C));
        double clampedAngle = Math.min(this.getFirst().angleSize, angle);
        double angleDelta = clampedAngle - angle;

        Vec3 normal = MathUtil.getClosestNormalRelativeToEntity(this.segments.get(1).position, this.getFirst().position, this.segments.get(2).position, this.entity);
        return MathUtil.rotatePointOnAPlainAround(this.segments.get(1).position, this.getFirst().position, angleDelta, normal);
    }

    public Vec3 getConstrainedPos(Vec3 reference, Segment middle, Vec3 endpoint) {
        Vec3 normal = MathUtil.getClosestNormalRelativeToEntity(endpoint, middle.position, reference, this.entity);

        Vec3 referencePoint = MathUtil.rotatePointOnAPlainAround(reference, middle.position, middle.angleOffset, normal);

        double angle = Math.toDegrees(MathUtil.calculateAngle(middle.position, endpoint, referencePoint));
        double clampedAngle = Math.min(middle.angleSize, angle);
        double angleDelta = clampedAngle - angle;

        return MathUtil.rotatePointOnAPlainAround(endpoint, middle.position, angleDelta, normal);
    }
}
