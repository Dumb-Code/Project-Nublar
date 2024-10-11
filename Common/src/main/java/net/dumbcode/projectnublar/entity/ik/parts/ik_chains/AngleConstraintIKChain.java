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

            nextSegment.move(this.moveSegment(nextSegment.getPosition(), currentSegment.getPosition(), currentSegment.length));

            if (i < this.segments.size() - 2) {
                Segment nextNextSegment = this.get(i + 2);
                nextNextSegment.move(this.getConstrainedPositions(currentSegment.getPosition(), nextSegment, nextNextSegment.getPosition()));
            }
        }

        this.endJoint = this.moveSegment(this.endJoint, this.getLast().getPosition(), this.getLast().length);
        this.endJoint = this.getConstrainedPositions(this.get(this.segments.size() - 2).getPosition(), this.getLast(), this.endJoint);
    }

    /*
    @Override
    public void reachForwards(Vec3 target) {
        this.endJoint = target;

        this.getLast().move(moveSegment(this.getLast().getPosition(), this.endJoint, this.getLast().length));
        this.getLast().move(this.getConstrainedPositions(this.get(this.segments.size() - 3).getPosition(), this.get(this.segments.size() - 2), this.getLast().getPosition()));

        for (int i = this.segments.size() - 1; i > 0; i--) {
            Segment currentSegment = this.segments.get(i);
            Segment nextSegment = this.segments.get(i - 1);

            nextSegment.move(moveSegment(nextSegment.getPosition(), currentSegment.getPosition(), nextSegment.length));
            if (i > 2) {
                nextSegment.move(this.getConstrainedPositions(this.segments.get(i - 3).getPosition(), this.segments.get(i - 2), nextSegment.getPosition()));
            }
        }
    }
    */

    public Vec3 getLegPlane() {
        return MathUtil.getNormalClosestTo(this.getFirst().getPosition(), this.endJoint, this.getStretchingPos(this.endJoint, this.getFirst().getPosition()), this.getReferencePoint());
    }

    public abstract Vec3 getReferencePoint();

    public Vec3 getConstrainedPosForRootSegment() {
        Vec3 C = new Vec3(0, 1, 0);
        return this.getConstrainedPosForRootSegment(C);
    }

    public Vec3 getConstrainedPosForRootSegment(Vec3 downVector) {
        double angle = Math.toDegrees(MathUtil.calculateAngle(this.getFirst().getPosition(), this.segments.get(1).getPosition(), this.getFirst().getPosition().add(downVector)));
        double clampedAngle = Math.min(this.getFirst().angleSize, angle);

        if (clampedAngle == angle) return this.segments.get(1).getPosition();

        double angleDelta = clampedAngle - angle;

        //Vec3 normal = MathUtil.getNormalClosestTo(this.segments.get(1).getPosition(), this.getFirst().getPosition(), this.segments.get(2).getPosition(), this.getReferencePoint());
        return MathUtil.rotatePointOnAPlaneAround(this.segments.get(1).getPosition(), this.getFirst().getPosition(), angleDelta, this.getLegPlane());
    }

    public Vec3 getConstrainedPositions(Vec3 reference, Segment middle, Vec3 endpoint) {
        //Vec3 normal = MathUtil.getNormalClosestTo(endpoint, middle.getPosition(), reference, this.getReferencePoint());

        Vec3 referencePoint = MathUtil.rotatePointOnAPlaneAround(reference, middle.getPosition(), middle.angleOffset, this.getLegPlane());

        double angle = Math.toDegrees(MathUtil.calculateAngle(middle.getPosition(), endpoint, referencePoint));
        double clampedAngle = Math.min(middle.angleSize, angle);

        if (clampedAngle == angle) return endpoint;

        double angleDelta = clampedAngle - angle;

        return MathUtil.rotatePointOnAPlaneAround(endpoint, middle.getPosition(), angleDelta, this.getLegPlane());
    }
}
