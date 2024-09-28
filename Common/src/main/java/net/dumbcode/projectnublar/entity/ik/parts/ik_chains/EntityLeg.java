package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import static net.dumbcode.projectnublar.entity.ik.util.MathUtil.getFlatRotationVector;

public class EntityLeg extends AngleConstraintIKChain {
    public PathfinderMob entity;

    public EntityLeg(double... lengths) {
        super(lengths);
    }

    public EntityLeg(Segment... segments) {
        super(segments);
    }

    @Override
    public Vec3 getReferencePoint() {
        Vec3 referencePoint = getFlatRotationVector(this.entity.getYRot() + 90);
        return this.getFirst().getPosition().add(referencePoint.scale(100));
    }

    @Override
    public void stretch(Vec3 target, Vec3 base) {
        Vec3 rotatedTarget = MathUtil.rotatePointOnAPlaneAround(target, base, 90, MathUtil.getClosestNormalRelativeToEntity(target, base, base.add(MathUtil.getFlatRotationVector(this.entity)), this.entity));

        Vec3 direction = rotatedTarget.subtract(base);
        Vec3 flatDirection = new Vec3(direction.x, 0, direction.z).normalize();

        this.extendFully(base.add(flatDirection.scale(this.maxLength() * 2)), base);
    }
}
