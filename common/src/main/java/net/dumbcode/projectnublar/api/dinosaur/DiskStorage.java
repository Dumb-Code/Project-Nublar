package net.dumbcode.projectnublar.api.dinosaur;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * What a storage drive (hard drive, SSD) holds: one {@link DNAData} sample per storage key, the
 * key being {@link DNAData#createStorageKey} of the sample's entity type and variant. The
 * sequencer adds DNA to a drive; the sequencer screen and the drive tooltip read it.
 *
 * <p>The drive's {@link DataComponentInit#DISK_STORAGE} component (ruling P118). A storage is
 * immutable: {@link #with} returns a new one. The samples it holds are read only: like the storage,
 * they are shared between every copy of the stack (ruling P120). The keys keep a {@code HashMap}'s
 * order, the order of the 1.20.1 compound the samples were stored in. Equality compares the samples
 * only, not the {@code version} read from a save (ruling P122).
 */
public final class DiskStorage {
    private static final String ENTRIES_KEY = "entries";

    /** The saved form: {@code entries} (storage key to sample) and the root {@code version} key (0 when missing). */
    public static final Codec<DiskStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf(SaveVersion.KEY, 0).forGetter(storage -> SaveVersion.CURRENT),
            Codec.unboundedMap(Codec.STRING, DNAData.CODEC).optionalFieldOf(ENTRIES_KEY, Map.of())
                    .forGetter(storage -> storage.entries)
    ).apply(instance, DiskStorage::new));

    /** {@link #CODEC} on the network, as an NBT tag. */
    public static final StreamCodec<RegistryFriendlyByteBuf, DiskStorage> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    /** A drive with no sample: what a drive without the component holds. */
    public static final DiskStorage EMPTY = new DiskStorage(SaveVersion.CURRENT, Map.of());

    private final int version;
    private final Map<String, DNAData> entries;

    private DiskStorage(int version, Map<String, DNAData> entries) {
        this.version = version;
        this.entries = Collections.unmodifiableMap(new HashMap<>(entries));
    }

    /** The drive's storage; {@link #EMPTY} when the stack has no {@link DataComponentInit#DISK_STORAGE}. */
    public static DiskStorage fromStack(ItemStack stack) {
        return stack.getOrDefault(DataComponentInit.DISK_STORAGE.get(), EMPTY);
    }

    /** Stores this storage as the stack's {@link DataComponentInit#DISK_STORAGE}; it is immutable, so no copy is needed. */
    public void saveTo(ItemStack stack) {
        stack.set(DataComponentInit.DISK_STORAGE.get(), this);
    }

    /**
     * The sample under {@code key}, or null. Read only: the sample is shared with every copy of
     * the stack, so change a {@link DNAData#copy} and store it with {@link #with}.
     */
    public @Nullable DNAData get(String key) {
        return entries.get(key);
    }

    /**
     * The sample under {@code key}, read only as with {@link #get}. When the drive has none, what
     * 1.20.1 read for a missing key: a new pig sample with no DNA ({@link DNAData#missing()}).
     */
    public DNAData getOrMissing(String key) {
        DNAData sample = entries.get(key);
        return sample != null ? sample : DNAData.missing();
    }

    /** True when the drive holds a sample under {@code key}. */
    public boolean contains(String key) {
        return entries.containsKey(key);
    }

    /** The storage keys, read only. */
    public Set<String> keys() {
        return entries.keySet();
    }

    /** A new storage with a copy of {@code sample} under {@code key}; this storage does not change. */
    public DiskStorage with(String key, DNAData sample) {
        Map<String, DNAData> changed = new HashMap<>(entries);
        changed.put(key, sample.copy());
        return new DiskStorage(SaveVersion.CURRENT, changed);
    }

    /** The version the data was saved with: {@code 0} for a save without the key, {@link SaveVersion#CURRENT} when new. */
    public int getVersion() {
        return version;
    }

    /** Equal when the samples are equal; the version is ignored. */
    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof DiskStorage other && entries.equals(other.entries);
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }
}
