package net.dumbcode.projectnublar.mixin;

import net.dumbcode.projectnublar.block.api.fence.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFenceBase;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.client.ProjectNublarModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntityElectricFence.class)
public abstract class BlockEntityElectricFenceMixin extends BlockEntityElectricFenceBase{


    public BlockEntityElectricFenceMixin(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void requestModelDataUpdate() {
        super.requestModelDataUpdate();
        if(this.level != null) {
          this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
        this.resetCollidableCache();
    }

    @Override
    public void triggerModelUpdate() {
        this.requestModelDataUpdate();
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(ProjectNublarModelData.CONNECTIONS, this.compiledRenderData())
                .build();
    }
}
