package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.minecraft.world.phys.Vec3;

public class StretchingIKChain<D extends StretchingIKChain.StretchDirection> extends IKChain {
    private final D stretchDirection;

    public StretchingIKChain(D stretchDirection, double... lengths) {
        super(lengths);
        this.stretchDirection = stretchDirection;
    }

    public StretchingIKChain(D stretchDirection, Segment... segments) {
        super(segments);
        this.stretchDirection = stretchDirection;
    }

    public D getStretchDirection() {
        return this.stretchDirection;
    }

    @Override
    public void solve(Vec3 target, Vec3 base) {
        this.stretch(target, base);
        super.solve(target, base);
    }

    public void stretch(Vec3 target, Vec3 base) {
        this.extendFully(this.stretchDirection.onStretch(target, this), base);
    }

    public static Vec3 stretchToTarget(Vec3 target, StretchingIKChain<? extends StretchDirection> chain) {
        Vec3 direction = target.subtract(chain.getFirst().position);
        return chain.getFirst().position.add(direction.scale(chain.maxLength() * 2));
    }

    public interface StretchDirection {
        Vec3 onStretch(Vec3 target, StretchingIKChain<? extends StretchDirection> IKChain);
    }
}
