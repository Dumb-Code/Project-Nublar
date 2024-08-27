package net.dumbcode.projectnublar.network.c2s;

import commonnetwork.networking.data.PacketContext;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public record UpdateIncubatorSlotPacket(BlockPos pos, int index, int x, int y) {
    public static ResourceLocation ID = Constants.modLoc("update_incubator_slot");
    public static UpdateIncubatorSlotPacket decode(FriendlyByteBuf buf) {
        return new UpdateIncubatorSlotPacket(buf.readBlockPos(),buf.readInt(), buf.readInt(), buf.readInt());
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
            IncubatorBlockEntity entity = (IncubatorBlockEntity) context.sender().level().getBlockEntity(pos);
            if(entity != null) {
                entity.updateSlot(context.message().index(), context.message().x(), context.message().y());
            }
            entity.updateBlock();
        });
    }
}
