package net.dumbcode.projectnublar.block.api.geometry;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class DelegateVoxelShape extends VoxelShape {
    private final VoxelShape delegate;
    private final Callback callback;

    public DelegateVoxelShape( VoxelShape delegate, Callback callback) {
        super(delegate.shape);
        this.delegate = delegate;
        this.callback = callback;
    }

    public BlockHitResult clip(Vec3 from, Vec3 to, BlockPos offset) {
        return DelegateBlockHitResult.of(this.callback.getRaytrace(from, to, offset, () -> this.delegate.clip(from, to, offset)));
    }


    @Override
    public DoubleList getCoords(Direction.Axis axis) {
        return this.delegate.getCoords(axis);
    }

    public interface Callback {
        BlockHitResult getRaytrace(Vec3 from, Vec3 to, BlockPos offset, Supplier<BlockHitResult> fallback);
    }
}
