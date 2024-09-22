package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class EntityStretchingIKChain extends StretchingIKChain<StretchingIKChain.StretchDirection> {
    public PathfinderMob entity;

    public EntityStretchingIKChain(double... lengths) {
        super(null, lengths);
    }

    public EntityStretchingIKChain(Segment... segments) {
        super(null, segments);
    }

    @Override
    public void stretch(Vec3 target, Vec3 base) {
        this.extendFully(base.add(MathUtil.getFlatRotationVector(this.entity).scale(2)), base.add(MathUtil.getFlatRotationVector(this.entity)));
        //this.reachBackwards(base);
        //this.iterate(MathUtil.getFlatRotationVector(this.entity).scale(2).add(base), base);
    }
}
