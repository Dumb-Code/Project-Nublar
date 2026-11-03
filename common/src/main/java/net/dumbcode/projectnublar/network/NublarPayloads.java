package net.dumbcode.projectnublar.network;

import java.util.List;
import java.util.function.BiConsumer;
import net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorPacket;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * The mod's payloads, for the loader modules to register. Each loader registers every
 * {@link #SERVERBOUND} entry and runs its handler on the server thread. The mod has no
 * server-to-client payload today, so there is no clientbound list.
 */
public final class NublarPayloads {

    /**
     * One payload: its id, its stream codec and the server-side handler.
     *
     * @param type    the payload id
     * @param codec   writes and reads the payload
     * @param handler runs the payload on the server thread for the player who sent it
     * @param <T>     the payload record
     */
    public record Entry<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            BiConsumer<T, ServerPlayer> handler) {
    }

    /** The client-to-server payloads, in the order the 1.20.1 code registered them. */
    public static final List<Entry<?>> SERVERBOUND = List.of(
            new Entry<>(UpdateEditInfoPacket.TYPE, UpdateEditInfoPacket.STREAM_CODEC, UpdateEditInfoPacket::handle),
            new Entry<>(UpdateIncubatorSlotPacket.TYPE, UpdateIncubatorSlotPacket.STREAM_CODEC, UpdateIncubatorSlotPacket::handle),
            new Entry<>(UpdateIncubatorPacket.TYPE, UpdateIncubatorPacket.STREAM_CODEC, UpdateIncubatorPacket::handle));

    private NublarPayloads() {
    }
}
