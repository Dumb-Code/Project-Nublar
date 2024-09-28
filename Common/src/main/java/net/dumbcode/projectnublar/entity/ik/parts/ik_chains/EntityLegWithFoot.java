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

        Vec3 referencePoint = MathUtil.rotatePointOnAPlaneAround(this.endJoint.subtract(0, 1,0), this.endJoint, this.foot.angleOffset, normal);
        this.footAngel = Math.toDegrees(MathUtil.calculateAngle(this.endJoint, this.foot.getPosition(), referencePoint));

        double clampedAngle = Math.max(Math.min(this.foot.angleSize, this.footAngel), 0);

        this.footAngel = clampedAngle;

        Vec3 newFootPosition = this.getFootPosition(clampedAngle);

        this.foot.move(newFootPosition,false);

        System.out.println("Foot Angle: " + this.footAngel);
        System.out.println("Clamped Foot Angle: " + clampedAngle);
    }

    public Vec3 getFootPosition() {
        return this.getFootPosition(this.footAngel);
    }

    public Vec3 getFootPosition(double angle) {
        Vec3 normal = MathUtil.getNormalClosestTo(this.endJoint, this.getLast().getPosition(), this.get(this.segments.size() - 2).getPosition(), this.getReferencePoint());

        return MathUtil.rotatePointOnAPlaneAround(this.endJoint.subtract(new Vec3(0, 1, 0).scale(this.foot.length)), this.endJoint, angle + this.foot.angleOffset, normal);
    }
}
