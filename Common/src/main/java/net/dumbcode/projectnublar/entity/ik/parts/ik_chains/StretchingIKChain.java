package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.minecraft.world.phys.Vec3;

public abstract class StretchingIKChain extends IKChain {


    public StretchingIKChain(double... lengths) {
        super(lengths);
    }

    public StretchingIKChain(Segment... segments) {
        super(segments);
    }

    @Override
    public void solve(Vec3 target, Vec3 base) {
        this.stretch(target, base);
        super.solve(target, base);
    }

    public abstract void stretch(Vec3 target, Vec3 base);

    public static Vec3 stretchToTarget(Vec3 target, StretchingIKChain chain) {
        Vec3 direction = target.subtract(chain.getFirst().getPosition());
        return chain.getFirst().getPosition().add(direction.scale(chain.maxLength() * 2));
    }
}
