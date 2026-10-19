package net.dumbcode.projectnublar.network.c2s;

import commonnetwork.networking.data.PacketContext;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client-to-server: requests an incubator block sync. Wire format (BlockPos) is the same as before. */
public record UpdateIncubatorPacket(BlockPos pos) {
    public static ResourceLocation ID = Constants.modLoc("update_incubator");

    public static UpdateIncubatorPacket decode(FriendlyByteBuf buf) {
        return new UpdateIncubatorPacket(buf.readBlockPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos());
    }

    public static void handle(PacketContext<UpdateIncubatorPacket> context) {
        context.sender().getServer().execute(() -> {
            BlockPos pos = context.message().pos();
            // Note: no null check - a missing block entity would NPE.
            IncubatorBlockEntity entity =
                    (IncubatorBlockEntity) context.sender().level().getBlockEntity(pos);
            entity.updateBlock();
        });
    }
}
