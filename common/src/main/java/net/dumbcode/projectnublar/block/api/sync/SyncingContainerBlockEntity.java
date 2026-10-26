package net.dumbcode.projectnublar.block.api.sync;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * The container form of {@link SyncingBlockEntity}: the saved data ({@link #saveData}) is also the
 * client sync data, and the update tag is built the same way
 * ({@code TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries)}, then
 * {@code buildResult()}).
 */
public abstract class SyncingContainerBlockEntity extends BaseContainerBlockEntity {

    protected SyncingContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract void saveData(ValueOutput output);

    protected abstract void loadData(ValueInput input);

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        saveData(output);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        loadData(input);
        super.loadAdditional(input);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        saveData(output);
        return output.buildResult();
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
