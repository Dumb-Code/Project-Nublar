package net.dumbcode.projectnublar.block.api.geometry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DelegateBlockHitResult extends BlockHitResult {
    private final BlockHitResult delegate;
    public Object hitInfo;

    public static DelegateBlockHitResult of(BlockHitResult delegate) {
        return delegate == null ? null : new DelegateBlockHitResult(delegate);
    }

    private DelegateBlockHitResult(BlockHitResult delegate) {
        super(delegate.getLocation(), delegate.getDirection(), delegate.getBlockPos(), delegate.isInside());
        this.delegate = delegate;
    }

    public BlockHitResult withDirection(Direction p_216351_1_) {
        return DelegateBlockHitResult.of(this.delegate.withDirection(p_216351_1_));
    }

    public BlockHitResult withPosition(BlockPos p_237485_1_) {
        return DelegateBlockHitResult.of(this.delegate.withPosition(p_237485_1_));
    }

    public BlockPos getBlockPos() {
        return this.delegate.getBlockPos();
    }

    public Direction getDirection() {
        return this.delegate.getDirection();
    }

    public Type getType() {
        return this.delegate.getType();
    }

    public boolean isInside() {
        return this.delegate.isInside();
    }

    public double distanceTo(Entity p_237486_1_) {
        return this.delegate.distanceTo(p_237486_1_);
    }

    public Vec3 getLocation() {
        return this.delegate.getLocation();
    }
}
