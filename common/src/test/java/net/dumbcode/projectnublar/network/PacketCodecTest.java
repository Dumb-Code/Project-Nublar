package net.dumbcode.projectnublar.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import net.dumbcode.projectnublar.TestBootstrap;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorPacket;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Each payload's stream codec must read back exactly what it wrote, and nothing more: a codec
 * that leaves bytes behind or reads too many breaks the connection on the receiving side.
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

    /**
     * The edit-tab DinoData with every saved field but genes (gene names decode through the gene
     * registry, which a unit test does not have; ruling P129).
     */
    @Test
    void editInfoRoundTrip() {
        DinoData info = new DinoData();
        info.setBasePercentage(42.5);
        info.addEntity(new DinoData.EntityInfo(EntityTypes.PIG, null), 25);
        info.addEntity(new DinoData.EntityInfo(EntityTypes.CAT, "minecraft:tabby"), 30);
        info.setBaseDino(EntityTypes.COW);
        info.setIncubationProgress(0.5);
        info.setIncubationTimeLeft(1200);
        assertRoundTrip(UpdateEditInfoPacket.STREAM_CODEC, new UpdateEditInfoPacket(info, new BlockPos(3, -20, 900)));
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
