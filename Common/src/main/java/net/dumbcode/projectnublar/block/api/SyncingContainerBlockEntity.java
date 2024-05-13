package net.dumbcode.projectnublar.block.api;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class SyncingContainerBlockEntity extends BaseContainerBlockEntity {

    protected SyncingContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract void saveData(CompoundTag tag);

    protected abstract void loadData(CompoundTag tag);

    public void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        saveData(pTag);
    }

    public void load(CompoundTag tag) {
        loadData(tag);
        super.load(tag);
    }

    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveData(tag);
        return tag;
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity) this);
    }

    public void updateBlock() {
        BlockState blockState = this.getLevel().getBlockState(this.getBlockPos());
        this.getLevel().sendBlockUpdated(this.getBlockPos(), blockState, blockState, 3);
        this.setChanged();
    }
}
