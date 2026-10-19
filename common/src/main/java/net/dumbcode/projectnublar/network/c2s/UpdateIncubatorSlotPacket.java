package net.dumbcode.projectnublar.network.c2s;

import commonnetwork.networking.data.PacketContext;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-to-server: moves an incubator egg slot. {@code index} is the menu slot index;
 * the block entity subtracts 1 (frozen handshake). The encode/decode field order
 * (BlockPos, index, x, y) is a frozen wire contract.
 */
public record UpdateIncubatorSlotPacket(BlockPos pos, int index, int x, int y) {
    public static ResourceLocation ID = Constants.modLoc("update_incubator_slot");

    public static UpdateIncubatorSlotPacket decode(FriendlyByteBuf buf) {
        return new UpdateIncubatorSlotPacket(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos());
        buf.writeInt(index());
        buf.writeInt(x());
        buf.writeInt(y());
    }

    public static void handle(PacketContext<UpdateIncubatorSlotPacket> context) {
        context.sender().getServer().execute(() -> {
            BlockPos pos = context.message().pos();
            IncubatorBlockEntity entity =
                    (IncubatorBlockEntity) context.sender().level().getBlockEntity(pos);
            if (entity != null) {
                entity.updateSlot(context.message().index(), context.message().x(), context.message().y());
            }
            // TODO(BUG): this call sits outside the null check above, so a missing block entity
            // throws a NullPointerException here.
            entity.updateBlock();
        });
    }
}
