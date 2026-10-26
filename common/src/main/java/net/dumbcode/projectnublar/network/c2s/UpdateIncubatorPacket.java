package net.dumbcode.projectnublar.network.c2s;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client-to-server: asks the server to sync the incubator's block entity to clients. Wire
 * format: the BlockPos (as on 1.20.1). No client code sends it today.
 *
 * <p>The server checks that the sender has this incubator's menu open and that the block is an
 * incubator. There is no value to range-check.
 */
public record UpdateIncubatorPacket(BlockPos pos) implements CustomPacketPayload {
    /** The payload id; the same id as on 1.20.1. */
    public static final Type<UpdateIncubatorPacket> TYPE = new Type<>(Constants.modLoc("update_incubator"));

    /** The position only. */
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateIncubatorPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateIncubatorPacket::pos,
            UpdateIncubatorPacket::new);

    @Override
    public Type<UpdateIncubatorPacket> type() {
        return TYPE;
    }

    /**
     * Syncs the incubator on the server thread (the loader adapter enqueues it). Does nothing and
     * logs one debug line when a check fails; on 1.20.1 a missing block entity threw a
     * {@code NullPointerException} here.
     */
    public void handle(ServerPlayer sender) {
        if (!(sender.containerMenu instanceof IncubatorMenu menu) || !pos.equals(menu.pos())) {
            Constants.LOG.debug("Ignored update_incubator from {}: no incubator menu open at {}", sender.getName().getString(), pos);
            return;
        }
        if (!(sender.level().getBlockEntity(pos) instanceof IncubatorBlockEntity incubator)) {
            Constants.LOG.debug("Ignored update_incubator from {}: no incubator at {}", sender.getName().getString(), pos);
            return;
        }
        incubator.updateBlock();
    }
}
