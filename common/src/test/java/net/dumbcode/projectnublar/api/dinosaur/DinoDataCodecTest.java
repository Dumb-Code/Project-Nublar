package net.dumbcode.projectnublar.api.dinosaur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.netty.buffer.Unpooled;
import java.util.Set;
import net.dumbcode.projectnublar.TestBootstrap;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * {@link DinoData#CODEC} must save and load what 1.20.1's {@code toNBT}/{@code fromNBT} saved and
 * loaded (ruling P127). Gene names resolve through the gene registry, which a unit test does not
 * have, so the round trips leave genes out and {@link #genesEncodeByName} checks the written form
 * (ruling P129).
 */
class DinoDataCodecTest {
    private static final Genes.Gene AGGRESSION = new Genes.Gene("aggression");
    private static final Genes.Gene SPEED = new Genes.Gene("speed");
    private static final Genes.Gene SIZE = new Genes.Gene("size");

    @BeforeAll
    static void bootstrap() {
        TestBootstrap.ensure();
    }

    /** Every saved field except genes set to a value other than its default. */
    private static DinoData full() {
        DinoData data = new DinoData();
        data.setBasePercentage(42.5);
        data.addEntity(new DinoData.EntityInfo(EntityTypes.PIG, null), 25);
        data.addEntity(new DinoData.EntityInfo(EntityTypes.CAT, "minecraft:tabby"), 30);
        data.setBaseDino(EntityTypes.COW);
        data.setIncubationProgress(0.5);
        data.setIncubationTimeLeft(1200);
        return data;
    }

    private static CompoundTag encode(DinoData data) {
        return (CompoundTag) DinoData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
    }

    private static DinoData parse(CompoundTag tag) {
        return DinoData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    @Test
    void roundTripThroughNbtKeepsEveryField() {
        DinoData decoded = parse(encode(full()));
        assertEquals(full(), decoded);
        assertEquals(42.5, decoded.getBasePercentage());
        assertEquals(30, decoded.getEntityPercentage(new DinoData.EntityInfo(EntityTypes.CAT, "minecraft:tabby")));
        assertEquals(EntityTypes.COW, decoded.getBaseDino());
        assertEquals(0.5, decoded.getIncubationProgress());
    }

    @Test
    void encodedCompoundHasVersionAtRoot() {
        assertNotEquals(0, SaveVersion.CURRENT, "the key is written only while CURRENT is not the missing-key default 0");
        assertEquals(SaveVersion.CURRENT, encode(full()).getIntOr(SaveVersion.KEY, -1));
    }

    @Test
    void missingVersionDecodesAsZero() {
        CompoundTag tag = encode(full());
        tag.remove(SaveVersion.KEY);
        DinoData decoded = parse(tag);
        assertEquals(0, decoded.getVersion());
        assertEquals(full(), decoded, "equals ignores the version");
    }

    @Test
    void equalsIgnoresMapOrder() {
        DinoData first = new DinoData();
        first.addEntity(new DinoData.EntityInfo(EntityTypes.PIG, null), 10);
        first.addEntity(new DinoData.EntityInfo(EntityTypes.COW, null), 20);
        first.setGeneValue(AGGRESSION, 5);
        first.setGeneValue(SPEED, -5);
        DinoData second = new DinoData();
        second.setGeneValue(SPEED, -5);
        second.setGeneValue(AGGRESSION, 5);
        second.addEntity(new DinoData.EntityInfo(EntityTypes.COW, null), 20);
        second.addEntity(new DinoData.EntityInfo(EntityTypes.PIG, null), 10);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    /** Each saved field takes part in equals; the unsaved layer colours do not and are not written (P128). */
    @Test
    void equalsSeesEverySavedFieldButNotLayerColors() {
        DinoData base = full();
        DinoData changed = full();
        changed.setBasePercentage(1);
        assertNotEquals(base, changed);
        changed = full();
        changed.addEntity(new DinoData.EntityInfo(EntityTypes.PIG, null), 26);
        assertNotEquals(base, changed);
        changed = full();
        changed.setGeneValue(AGGRESSION, 1);
        assertNotEquals(base, changed);
        changed = full();
        changed.setBaseDino(EntityTypes.PIG);
        assertNotEquals(base, changed);
        changed = full();
        changed.setIncubationProgress(0.75);
        assertNotEquals(base, changed);
        changed = full();
        changed.setIncubationTimeLeft(1);
        assertNotEquals(base, changed);

        changed = full();
        changed.getLayerColors().set(0, 0xFF123456);
        assertEquals(base, changed);
        assertFalse(encode(changed).contains("layerColors"));
    }

    @Test
    void streamCodecRoundTrip() {
        RegistryFriendlyByteBuf buf = RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY)
                .apply(new FriendlyByteBuf(Unpooled.buffer()));
        DinoData.STREAM_CODEC.encode(buf, full());
        assertEquals(full(), DinoData.STREAM_CODEC.decode(buf));
        assertEquals(0, buf.readableBytes(), "the decoder must consume every byte the encoder wrote");
    }

    @Test
    void genesEncodeByName() {
        DinoData data = new DinoData();
        data.setGeneValue(AGGRESSION, 10);
        data.setGeneValue(SPEED, -20);
        data.setGeneValue(SIZE, 35.5);
        CompoundTag genes = encode(data).getCompoundOrEmpty("genes");
        assertEquals(3, genes.size());
        assertEquals(10, genes.getDoubleOr("aggression", 0));
        assertEquals(-20, genes.getDoubleOr("speed", 0));
        assertEquals(35.5, genes.getDoubleOr("size", 0));
    }

    /** As toNBT/fromNBT: a null base writes the entity registry's default key, a missing key reads as null (P131). */
    @Test
    void nullBaseDinoEncodesAsPigAndMissingKeyDecodesAsNull() {
        CompoundTag tag = encode(new DinoData());
        assertEquals("minecraft:pig", tag.getStringOr("baseDino", ""));
        tag.remove("baseDino");
        assertNull(parse(tag).getBaseDino());
    }

    /** A stack without the component reads like fromNBT of an empty compound. */
    @Test
    void emptyCompoundDecodesLikeANewDinoData() {
        DinoData decoded = parse(new CompoundTag());
        assertEquals(new DinoData(), decoded);
        assertNull(decoded.getBaseDino());
        assertEquals(-1, decoded.getIncubationProgress());
    }

    /** copy() keeps every saved field and the version, in maps of its own (P120, P130). */
    @Test
    void copyKeepsEverySavedFieldAndTheVersion() {
        CompoundTag tag = encode(full());
        tag.remove(SaveVersion.KEY);
        DinoData original = parse(tag);
        original.setGeneValue(AGGRESSION, 7);
        DinoData copy = original.copy();
        assertEquals(original, copy, "equals compares every saved field");
        assertEquals(0, copy.getVersion());
        assertEquals(EntityTypes.COW, copy.getBaseDino());
        assertEquals(30, copy.getEntityPercentage(new DinoData.EntityInfo(EntityTypes.CAT, "minecraft:tabby")));
        copy.setGeneValue(SPEED, 1);
        copy.addEntity(new DinoData.EntityInfo(EntityTypes.COW, null), 5);
        assertFalse(original.getAdvancedGenes().containsKey(SPEED));
        assertEquals(0, original.getEntityPercentage(new DinoData.EntityInfo(EntityTypes.COW, null)));
    }

    /**
     * The saved key names are the 1.20.1 toNBT names, with {@code entityPercentages} as a compound of
     * {@code entity_0 .. entity_{n-1}} entries (P146); nothing else ties them once the NBT methods are gone.
     */
    @Test
    void encodedKeyNamesAreThe1201Names() {
        DinoData data = full();
        data.setGeneValue(AGGRESSION, 7);
        CompoundTag tag = encode(data);
        assertEquals(Set.of("version", "basePercentage", "entityPercentages", "genes", "baseDino",
                "incubationProgress", "incubationTimeLeft"), tag.keySet());
        CompoundTag entities = tag.getCompoundOrEmpty("entityPercentages");
        assertEquals(Set.of("entity_0", "entity_1"), entities.keySet());
        CompoundTag cat = entities.keySet().stream()
                .map(key -> entities.getCompoundOrEmpty(key))
                .filter(entry -> entry.contains("variant"))
                .findFirst().orElseThrow();
        assertEquals(Set.of("type", "variant", "percentage"), cat.keySet());
        assertEquals("minecraft:cat", cat.getStringOr("type", ""));
        assertEquals("minecraft:tabby", cat.getStringOr("variant", ""));
        assertEquals(30, cat.getDoubleOr("percentage", 0));
    }

    /**
     * A 1.20.1 dinosaur or Sequencer saved {@code entityPercentages} as a compound of {@code entity_N}
     * entries; it loads with every entry, whatever the entry's key (P146).
     */
    @Test
    void decodesThe1201EntityPercentagesShape() {
        CompoundTag pig = new CompoundTag();
        pig.putString("type", "minecraft:pig");
        pig.putDouble("percentage", 25);
        CompoundTag cat = new CompoundTag();
        cat.putString("type", "minecraft:cat");
        cat.putString("variant", "minecraft:tabby");
        cat.putDouble("percentage", 30);
        CompoundTag entities = new CompoundTag();
        entities.put("entity_0", pig);
        entities.put("entity_7", cat);
        CompoundTag tag = new CompoundTag();
        tag.putDouble("basePercentage", 42.5);
        tag.put("entityPercentages", entities);
        tag.putString("baseDino", "minecraft:cow");

        DinoData decoded = parse(tag);
        assertEquals(25, decoded.getEntityPercentage(new DinoData.EntityInfo(EntityTypes.PIG, null)));
        assertEquals(30, decoded.getEntityPercentage(new DinoData.EntityInfo(EntityTypes.CAT, "minecraft:tabby")));
        assertEquals(2, decoded.getEntityPercentages().size());
    }

    /**
     * saveTo stores null entity types as pig, so the stored data equals what the 1.20.1 NBT save and
     * load gave (P133).
     */
    @Test
    void storedCopyStoresNullEntityTypesAsPig() {
        DinoData data = new DinoData();
        data.setBasePercentage(50);
        data.addEntity(new DinoData.EntityInfo(null, "minecraft:tabby"), 20);
        DinoData stored = data.storedCopy();
        assertEquals(EntityTypes.PIG, stored.getBaseDino());
        assertEquals(20, stored.getEntityPercentage(new DinoData.EntityInfo(EntityTypes.PIG, "minecraft:tabby")));
        assertEquals(parse(encode(data)), stored);
        assertNull(data.getBaseDino(), "the data saveTo is called on does not change");
    }
}
