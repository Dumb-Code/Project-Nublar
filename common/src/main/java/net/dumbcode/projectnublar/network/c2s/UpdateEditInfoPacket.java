package net.dumbcode.projectnublar.network.c2s;

import commonnetwork.networking.data.PacketContext;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-to-server: pushes the edit-tab {@link DinoData} into the sequencer. The encode/decode
 * field order (DinoData NBT, then BlockPos) is a frozen wire contract.
 */
public record UpdateEditInfoPacket(DinoData info, BlockPos pos) {
    public static ResourceLocation ID = Constants.modLoc("update_edit_info");

    public static UpdateEditInfoPacket decode(FriendlyByteBuf buf) {
        return new UpdateEditInfoPacket(DinoData.fromNBT(buf.readNbt()), buf.readBlockPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(info.toNBT());
        buf.writeBlockPos(pos);
    }
    public static void handle(PacketContext<UpdateEditInfoPacket> context) {
        context.sender().getServer().execute(() -> {
            BlockPos pos = context.message().pos();
            DinoData info = context.message().info();
            if(context.sender().level().getBlockEntity(pos) instanceof SequencerBlockEntity sequencer) {
                sequencer.setDinoData(info);
            }
        });
    }
}
