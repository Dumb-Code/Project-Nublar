package net.dumbcode.projectnublar.api.dinosaur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.netty.buffer.Unpooled;
import java.util.Set;
import net.dumbcode.projectnublar.TestBootstrap;
import net.dumbcode.projectnublar.api.fossil.FossilPieces;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** {@link DNAData#CODEC} must save and load what 1.20.1's {@code saveToNBT}/{@code loadFromNBT} did (ruling P127). */
class DNADataCodecTest {

    @BeforeAll
    static void bootstrap() {
        TestBootstrap.ensure();
    }

    /** Every saved field set to a value other than its default. */
    private static DNAData full() {
        DNAData data = new DNAData();
        data.setEntityType(EntityTypes.COW);
        data.setDnaPercentage(0.75);
        data.setVariant("minecraft:temperate");
        data.setFossilPiece(FossilPieces.TYRANNOSAURUS_SKULL);
        data.setQuality(Quality.PRISTINE);
        data.setEmbryo(true);
        return data;
    }

    private static CompoundTag encode(DNAData data) {
        return (CompoundTag) DNAData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
    }

    private static DNAData parse(CompoundTag tag) {
        return DNAData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    @Test
    void roundTripThroughNbtKeepsEveryField() {
        DNAData decoded = parse(encode(full()));
        assertEquals(full(), decoded);
        assertEquals(EntityTypes.COW, decoded.getEntityType());
        assertEquals("minecraft:temperate", decoded.getVariant());
        assertEquals(FossilPieces.TYRANNOSAURUS_SKULL, decoded.getFossilPiece());
        assertEquals(Quality.PRISTINE, decoded.getQuality());
    }

    @Test
    void encodedCompoundHasVersionAtRoot() {
        assertEquals(SaveVersion.CURRENT, encode(full()).getIntOr(SaveVersion.KEY, -1));
    }

    @Test
    void missingVersionDecodesAsZero() {
        CompoundTag tag = encode(full());
        tag.remove(SaveVersion.KEY);
        DNAData decoded = parse(tag);
        assertEquals(0, decoded.getVersion());
        assertEquals(full(), decoded, "equals ignores the version");
    }

    @Test
    void equalsSeesEverySavedField() {
        DNAData changed = full();
        changed.setEntityType(EntityTypes.PIG);
        assertNotEquals(full(), changed);
        changed = full();
        changed.setDnaPercentage(0.5);
        assertNotEquals(full(), changed);
        changed = full();
        changed.setVariant(null);
        assertNotEquals(full(), changed);
        changed = full();
        changed.setFossilPiece(FossilPieces.TAIL);
        assertNotEquals(full(), changed);
        changed = full();
        changed.setQuality(Quality.POOR);
        assertNotEquals(full(), changed);
        changed = full();
        changed.setEmbryo(false);
        assertNotEquals(full(), changed);
        assertEquals(full().hashCode(), full().hashCode());
    }

    @Test
    void streamCodecRoundTrip() {
        RegistryFriendlyByteBuf buf = RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY)
                .apply(new FriendlyByteBuf(Unpooled.buffer()));
        DNAData.STREAM_CODEC.encode(buf, full());
        assertEquals(full(), DNAData.STREAM_CODEC.decode(buf));
        assertEquals(0, buf.readableBytes(), "the decoder must consume every byte the encoder wrote");
    }

    /** A stack without the component reads like loadFromNBT of an empty compound: a pig sample (P131). */
    @Test
    void emptyCompoundDecodesAsPigSample() {
        DNAData decoded = parse(new CompoundTag());
        assertEquals(EntityTypes.PIG, decoded.getEntityType());
        assertNull(decoded.getVariant());
        assertNull(decoded.getFossilPiece());
        assertNull(decoded.getQuality());
        assertFalse(decoded.isEmbryo());
    }

    /** As saveToNBT: a null entity type writes the entity registry's default key (P131). */
    @Test
    void nullEntityTypeEncodesAsPig() {
        assertEquals("minecraft:pig", encode(new DNAData()).getStringOr("entityType", ""));
    }

    /** copy() keeps every saved field and the version (P120). */
    @Test
    void copyKeepsEverySavedFieldAndTheVersion() {
        CompoundTag tag = encode(full());
        tag.remove(SaveVersion.KEY);
        DNAData original = parse(tag);
        DNAData copy = original.copy();
        assertEquals(original, copy, "equals compares every saved field");
        assertEquals(0, copy.getVersion());
        assertEquals(Quality.PRISTINE, copy.getQuality());
        assertEquals(FossilPieces.TYRANNOSAURUS_SKULL, copy.getFossilPiece());
        copy.setDnaPercentage(0.1);
        assertEquals(full(), original, "the copy is a separate object");
    }

    /** The saved key names are the 1.20.1 saveToNBT names; nothing else ties them once the NBT methods are gone. */
    @Test
    void encodedKeyNamesAreThe1201Names() {
        assertEquals(Set.of("version", "entityType", "dnaPercentage", "variant", "fossilPiece", "quality", "isEmbryo"),
                encode(full()).keySet());
    }

    /**
     * saveTo stores a null entity type as pig, so the stored sample equals what the 1.20.1 NBT save
     * and load gave (P133).
     */
    @Test
    void storedCopyStoresANullEntityTypeAsPig() {
        DNAData data = new DNAData();
        data.setDnaPercentage(0.5);
        DNAData stored = data.storedCopy();
        assertEquals(EntityTypes.PIG, stored.getEntityType());
        assertEquals(parse(encode(data)), stored);
        assertNull(data.getEntityType(), "the sample saveTo is called on does not change");
    }
}
