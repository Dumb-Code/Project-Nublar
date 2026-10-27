package net.dumbcode.projectnublar.api.dinosaur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import java.util.Set;
import net.dumbcode.projectnublar.TestBootstrap;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** {@link DiskStorage#CODEC}: one DNA sample per storage key, as a drive holds them (ruling P118). */
class DiskStorageCodecTest {

    private static final String COW_KEY = "minecraft:cow";
    private static final String CAT_KEY = "minecraft:cat_minecraft:tabby";

    @BeforeAll
    static void bootstrap() {
        TestBootstrap.ensure();
    }

    private static DNAData sample(EntityType<?> type, String variant, double percentage) {
        DNAData data = new DNAData();
        data.setEntityType(type);
        data.setVariant(variant);
        data.setDnaPercentage(percentage);
        return data;
    }

    /** Two samples under their storage keys. */
    private static DiskStorage twoEntries() {
        return DiskStorage.EMPTY
                .with(COW_KEY, sample(EntityTypes.COW, null, 0.25))
                .with(CAT_KEY, sample(EntityTypes.CAT, "minecraft:tabby", 0.5));
    }

    private static CompoundTag encode(DiskStorage storage) {
        return (CompoundTag) DiskStorage.CODEC.encodeStart(NbtOps.INSTANCE, storage).getOrThrow();
    }

    private static DiskStorage parse(CompoundTag tag) {
        return DiskStorage.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    @Test
    void storageKeysAreTheSampleStorageNames() {
        assertEquals(COW_KEY, sample(EntityTypes.COW, null, 0).getStorageName());
        assertEquals(CAT_KEY, sample(EntityTypes.CAT, "minecraft:tabby", 0).getStorageName());
    }

    @Test
    void roundTripThroughNbtKeepsEveryEntry() {
        DiskStorage decoded = parse(encode(twoEntries()));
        assertEquals(twoEntries(), decoded);
        assertEquals(Set.of(COW_KEY, CAT_KEY), decoded.keys());
        assertEquals(sample(EntityTypes.CAT, "minecraft:tabby", 0.5), decoded.get(CAT_KEY));
    }

    @Test
    void encodedCompoundHasVersionAtRoot() {
        assertEquals(SaveVersion.CURRENT, encode(twoEntries()).getIntOr(SaveVersion.KEY, -1));
    }

    @Test
    void missingVersionDecodesAsZero() {
        CompoundTag tag = encode(twoEntries());
        tag.remove(SaveVersion.KEY);
        DiskStorage decoded = parse(tag);
        assertEquals(0, decoded.getVersion());
        assertEquals(twoEntries(), decoded, "equals ignores the version");
    }

    @Test
    void equalsIgnoresMapOrder() {
        DiskStorage reversed = DiskStorage.EMPTY
                .with(CAT_KEY, sample(EntityTypes.CAT, "minecraft:tabby", 0.5))
                .with(COW_KEY, sample(EntityTypes.COW, null, 0.25));
        assertEquals(twoEntries(), reversed);
        assertEquals(twoEntries().hashCode(), reversed.hashCode());
    }

    @Test
    void streamCodecRoundTrip() {
        RegistryFriendlyByteBuf buf = RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY)
                .apply(new FriendlyByteBuf(Unpooled.buffer()));
        DiskStorage.STREAM_CODEC.encode(buf, twoEntries());
        assertEquals(twoEntries(), DiskStorage.STREAM_CODEC.decode(buf));
        assertEquals(0, buf.readableBytes(), "the decoder must consume every byte the encoder wrote");
    }

    /** A component value is shared between stack copies, so {@code with} must not change the storage it is called on. */
    @Test
    void withLeavesTheOriginalUnchanged() {
        DiskStorage one = DiskStorage.EMPTY.with(COW_KEY, sample(EntityTypes.COW, null, 0.25));
        DiskStorage two = one.with(CAT_KEY, sample(EntityTypes.CAT, "minecraft:tabby", 0.5));
        assertFalse(one.contains(CAT_KEY));
        assertTrue(two.contains(CAT_KEY));
        assertTrue(DiskStorage.EMPTY.keys().isEmpty());
    }

    /** {@code with} stores a copy: changing the sample afterwards leaves the storage unchanged (P120). */
    @Test
    void withStoresACopyOfTheSample() {
        DNAData cow = sample(EntityTypes.COW, null, 0.25);
        DiskStorage storage = DiskStorage.EMPTY.with(COW_KEY, cow);
        cow.setDnaPercentage(0.9);
        assertEquals(0.25, storage.get(COW_KEY).getDnaPercentage());
    }

    /** The saved key names: {@code version} and {@code entries}, keyed by the storage key (P118). */
    @Test
    void encodedKeyNames() {
        CompoundTag tag = encode(twoEntries());
        assertEquals(Set.of("version", "entries"), tag.keySet());
        assertEquals(Set.of(COW_KEY, CAT_KEY), tag.getCompoundOrEmpty("entries").keySet());
    }

    /** A key the drive does not hold reads as 1.20.1's missing compound: a pig sample with no DNA. */
    @Test
    void getOrMissingReadsAMissingKeyAsAPigSample() {
        DNAData missing = twoEntries().getOrMissing("minecraft:pig");
        assertEquals(EntityTypes.PIG, missing.getEntityType());
        assertEquals(0, missing.getDnaPercentage());
        assertEquals(sample(EntityTypes.COW, null, 0.25), twoEntries().getOrMissing(COW_KEY));
    }
}
