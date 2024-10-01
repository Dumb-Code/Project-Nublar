package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.WorldCollidingSegment;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.world.phys.Vec3;

public class EntityLegWithFoot extends EntityLeg {
    public final WorldCollidingSegment foot;
    private double footAngel = 90;

    public EntityLegWithFoot(WorldCollidingSegment foot, double... lengths) {
        super(lengths);
        this.foot = foot;
    }

    public EntityLegWithFoot(WorldCollidingSegment foot, Segment... segments) {
        super(segments);
        this.foot = foot;
    }

    public double getFootAngel() {
        return this.footAngel;
    }

    @Override
    public void solve(Vec3 target, Vec3 base) {
        super.solve(target, base);
        if (this.foot.getLevel() == null) {
            this.foot.setLevel(this.entity.level());
        }

        Vec3 normal = MathUtil.getNormalClosestTo(this.endJoint, this.getLast().getPosition(), this.get(this.segments.size() - 2).getPosition(), this.getReferencePoint());

        this.foot.move(this.getFootPosition().subtract(0,0.01,0));

        Vec3 referencePoint = MathUtil.rotatePointOnAPlaneAround(this.endJoint.add(this.getDownNormalOnLegPlane()), this.endJoint, this.foot.angleOffset, normal);
        this.footAngel = Math.toDegrees(MathUtil.calculateAngle(this.endJoint, this.foot.getPosition(), referencePoint));

        double clampedAngle = Math.max(Math.min(this.foot.angleSize, this.footAngel), 0);

        this.footAngel = clampedAngle;

        Vec3 newFootPosition = this.getFootPosition(clampedAngle);

        this.foot.move(newFootPosition,false);
    }

    public Vec3 getFootPosition() {
        return this.getFootPosition(this.footAngel);
    }

    private Vec3 getDownNormalOnLegPlane() {
        Vec3 baseRotated = this.getFirst().getPosition().yRot(-this.entity.getYRot());
        Vec3 targetRotated = this.endJoint.yRot(-this.entity.getYRot());

        Vec3 flatRotatedBase = new Vec3(baseRotated.x(), baseRotated.y(), 0);
        Vec3 flatRotatedTarget = new Vec3(targetRotated.x(), targetRotated.y(), 0);

        Vec3 flatBase = flatRotatedBase.yRot(this.entity.getYRot());
        Vec3 flatTarget = flatRotatedTarget.yRot(this.entity.getYRot());

        return flatTarget.subtract(flatBase).normalize();
    }

    public Vec3 getFootPosition(double angle) {
        Vec3 normal = MathUtil.getNormalClosestTo(this.endJoint, this.getLast().getPosition(), this.get(this.segments.size() - 2).getPosition(), this.getReferencePoint());

        return MathUtil.rotatePointOnAPlaneAround(this.endJoint.add(this.getDownNormalOnLegPlane().scale(this.foot.length)), this.endJoint, angle + this.foot.angleOffset, normal);
    }
}
