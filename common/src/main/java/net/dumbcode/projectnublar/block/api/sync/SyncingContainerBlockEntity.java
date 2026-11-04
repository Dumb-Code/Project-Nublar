package net.dumbcode.projectnublar.block.api.sync;

import net.dumbcode.projectnublar.block.api.multiblock.MultiBlock;
import net.dumbcode.projectnublar.block.api.multiblock.MultiEntityBlock;
import net.dumbcode.projectnublar.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
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

    /**
     * Marks the block entity removed (its block broken or its chunk unloaded); a machine core then
     * refreshes the capability caches of its whole machine ({@code refreshMachineCaches}).
     */
    @Override
    public void setRemoved() {
        super.setRemoved();
        refreshMachineCaches();
    }

    /**
     * Marks the block entity in the level again (its block placed or its chunk loaded); a machine
     * core then refreshes the capability caches of its whole machine
     * ({@code refreshMachineCaches}).
     */
    @Override
    public void clearRemoved() {
        super.clearRemoved();
        refreshMachineCaches();
    }

    /**
     * On the server, when this is the core of a {@link MultiEntityBlock} machine: refreshes the
     * loader's capability caches at every block of the machine
     * ({@code IPlatformHelper.invalidateCapabilities}). Every block of a machine answers an energy
     * lookup with the core's storage, but a loader refreshes on its own only at the position and
     * chunk where a block entity comes or goes. A machine can stand across a chunk border, so a
     * cable at a block in the other chunk would keep the storage of a core block entity that its
     * chunk unloaded, or keep "no energy" after the chunk loads again. Vanilla calls
     * {@code setRemoved} and {@code clearRemoved} on the server thread, with the level set, when a
     * chunk unloads and loads and when the block is broken and placed.
     */
    private void refreshMachineCaches() {
        BlockState state = getBlockState();
        if (!(level instanceof ServerLevel serverLevel)
                || !(state.getBlock() instanceof MultiEntityBlock machine)
                || !MultiBlock.getCorePos(state, worldPosition).equals(worldPosition)) {
            return;
        }
        AABB footprint = machine.getFootprint(worldPosition, state);
        BlockPos min = BlockPos.containing(footprint.minX, footprint.minY, footprint.minZ);
        BlockPos max = BlockPos.containing(footprint.maxX - 1, footprint.maxY - 1,
                footprint.maxZ - 1);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            Services.PLATFORM.invalidateCapabilities(serverLevel, pos.immutable());
        }
    }

    /**
     * Always empty: every subclass keeps its slots in its own fields (fuel, food, machine inputs
     * and outputs) and overrides each container method to read them. 26.2 copies this list into an
     * item's container component on pick-block and back on placement; the empty list keeps that
     * component empty, as 1.20.1 had no such component (P85).
     */
    @Override
    protected NonNullList<ItemStack> getItems() {
        return NonNullList.create();
    }

    /** Ignored, see {@link #getItems()}. */
    @Override
    protected void setItems(NonNullList<ItemStack> items) {
    }
}
