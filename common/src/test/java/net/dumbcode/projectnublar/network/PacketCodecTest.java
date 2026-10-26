package net.dumbcode.projectnublar.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import net.dumbcode.projectnublar.TestBootstrap;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorPacket;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Each payload's stream codec must read back exactly what it wrote, and nothing more: a codec
 * that leaves bytes behind or reads too many breaks the connection on the receiving side.
 * The {@code UpdateEditInfoPacket} round trip is added in Task 16 with {@code DinoData.STREAM_CODEC}.
 */
class PacketCodecTest {

    @BeforeAll
    static void bootstrap() {
        TestBootstrap.ensure();
    }

    @Test
    void updateIncubatorRoundTrips() {
        assertRoundTrip(UpdateIncubatorPacket.STREAM_CODEC, new UpdateIncubatorPacket(new BlockPos(12, -40, 300)));
    }

    @Test
    void updateIncubatorSlotRoundTrips() {
        // y = -100 is the "egg taken out" position IncubatorScreen sends; it must survive the var-int.
        assertRoundTrip(UpdateIncubatorSlotPacket.STREAM_CODEC,
                new UpdateIncubatorSlotPacket(new BlockPos(-5, 64, 7), 3, 0, -100));
    }

    private static <T> void assertRoundTrip(StreamCodec<RegistryFriendlyByteBuf, T> codec, T payload) {
        RegistryFriendlyByteBuf buf = RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY)
                .apply(new FriendlyByteBuf(Unpooled.buffer()));
        codec.encode(buf, payload);
        T decoded = codec.decode(buf);
        assertEquals(payload, decoded);
        assertEquals(0, buf.readableBytes(), "the decoder must consume every byte the encoder wrote");
    }
}
