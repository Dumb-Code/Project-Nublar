package net.dumbcode.projectnublar.mixin;

import net.dumbcode.projectnublar.block.ElectricFencePostBlock;
import net.dumbcode.projectnublar.block.api.fence.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.fence.ConnectionType;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.client.ProjectNublarModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(BlockEntityElectricFencePole.class)
public abstract class BlockEntityElectricFencePoleMixin extends BlockEntityElectricFence {

    @Shadow public abstract double getCachedRotation();

    @Shadow public abstract double computeRotation();

    @Shadow private double cachedRotation;

    @Shadow public VoxelShape cachedShape;

    @Shadow public boolean shouldRefreshNextTick;

    @Shadow public boolean flippedAround;

    public BlockEntityElectricFencePoleMixin(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void requestModelDataUpdate() {
        this.cachedRotation = this.computeRotation();

        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof ElectricFencePostBlock) {
            ConnectionType type = ((ElectricFencePostBlock) state.getBlock()).getType();

            float t = type.getHalfSize();
            double x = Math.sin(Math.toRadians(this.cachedRotation + 90F - type.getRotationOffset())) * type.getRadius();
            double z = Math.cos(Math.toRadians(this.cachedRotation + 90F - type.getRotationOffset())) * type.getRadius();
            this.cachedShape = Shapes.box(x-t, 0, z-t, x+t, 1, z+t).move(0.5, 0, 0.5);
        }

        super.requestModelDataUpdate();

    }

    @Override
    public void triggerModelUpdate() {
        this.requestModelDataUpdate();
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(ProjectNublarModelData.CONNECTIONS, this.compiledRenderData())
                .with(ProjectNublarModelData.FENCE_POLE_ROTATION_DEGS, this.getCachedRotation())
                .build();
    }
    @Override
    public void onLoad() {
        this.shouldRefreshNextTick = true;
    }


}
