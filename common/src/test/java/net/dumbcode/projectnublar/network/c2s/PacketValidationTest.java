package net.dumbcode.projectnublar.network.c2s;

import static net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket.MAX_GENE_VALUE;
import static net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket.MAX_PERCENTAGE;
import static net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket.MIN_GENE_VALUE;
import static net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket.MIN_PERCENTAGE;
import static net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket.EGG_AREA_END_X;
import static net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket.EGG_AREA_END_Y;
import static net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket.EGG_AREA_START;
import static net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket.FIRST_EGG_MENU_INDEX;
import static net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket.LAST_EGG_MENU_INDEX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.dumbcode.projectnublar.TestBootstrap;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.client.screen.IncubatorScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The server must refuse client packets whose values the game's own screens cannot produce,
 * and must accept every value they can produce (a refusal there would be a gameplay change).
 * Each test puts a value one step outside a bound and one step inside it.
 *
 * <p>The bounds come from the screens: the DNA sliders of {@code SequencerScreen} (0 to 100),
 * the gene sliders of {@code CommonClientClass} (-100 to 100), the colour pickers (opaque ARGB)
 * and the egg bed of {@link IncubatorScreen}.
 */
class PacketValidationTest {
    private static final Genes.Gene GENE = new Genes.Gene("aggression");
    /** Set after the bootstrap: reading {@code EntityType.PIG} before it fails. */
    private static DinoData.EntityInfo pig;

    @BeforeAll
    static void bootstrap() {
        TestBootstrap.ensure();
        pig = new DinoData.EntityInfo(EntityType.PIG, null);
    }

    @Test
    void editInfoWithPercentageAbove100IsRejected() {
        DinoData entity = new DinoData();
        entity.addEntity(pig, MAX_PERCENTAGE + 1);
        assertFalse(UpdateEditInfoPacket.isValid(entity));

        DinoData base = new DinoData();
        base.setBasePercentage(MAX_PERCENTAGE + 1);
        assertFalse(UpdateEditInfoPacket.isValid(base));
    }

    @Test
    void editInfoWithNegativePercentageIsRejected() {
        DinoData entity = new DinoData();
        entity.addEntity(pig, MIN_PERCENTAGE - 1);
        assertFalse(UpdateEditInfoPacket.isValid(entity));

        DinoData base = new DinoData();
        base.setBasePercentage(MIN_PERCENTAGE - 1);
        assertFalse(UpdateEditInfoPacket.isValid(base));
    }

    /** The gene sliders go from -100 to 100, so a negative gene is valid down to -100. */
    @Test
    void editInfoWithGeneOutsideSliderRangeIsRejected() {
        DinoData below = new DinoData();
        below.setGeneValue(GENE, MIN_GENE_VALUE - 1);
        assertFalse(UpdateEditInfoPacket.isValid(below));

        DinoData above = new DinoData();
        above.setGeneValue(GENE, MAX_GENE_VALUE + 1);
        assertFalse(UpdateEditInfoPacket.isValid(above));
    }

    /** An unknown gene name decodes to a null gene, which would crash the sequencer's save. */
    @Test
    void editInfoWithUnknownGeneIsRejected() {
        DinoData data = new DinoData();
        data.setGeneValue(null, 0);
        assertFalse(UpdateEditInfoPacket.isValid(data));
    }

    /** The pickers send opaque colours (alpha 0xFF); the default layer colour has alpha 0. */
    @Test
    void editInfoWithTranslucentLayerColourIsRejected() {
        DinoData data = new DinoData();
        data.getLayerColors().set(0, 0x80FF0000);
        assertFalse(UpdateEditInfoPacket.isValid(data));
    }

    /** An incubated egg casts its base species to {@code Dinosaur}; a cow there would throw on the server. */
    @Test
    void editInfoWithNonDinosaurBaseIsRejected() {
        DinoData data = new DinoData();
        data.setBaseDino(EntityType.COW);
        assertFalse(UpdateEditInfoPacket.isValid(data));

        assertFalse(UpdateEditInfoPacket.isDinosaurId(Identifier.fromNamespaceAndPath("minecraft", "cow")));
        // The mod's hitbox helper entity is registered by the mod but is not a dinosaur.
        assertFalse(UpdateEditInfoPacket.isDinosaurId(Identifier.fromNamespaceAndPath("projectnublar", "dinosaur_part_entity")));
    }

    /**
     * The mod's entity types are not in the registry during a unit test, so the dinosaur case is
     * checked on the id. A DinoData with no base yet is what the screen sends first.
     */
    @Test
    void editInfoWithDinosaurOrNoBaseIsAccepted() {
        assertTrue(UpdateEditInfoPacket.isDinosaurId(Identifier.fromNamespaceAndPath("projectnublar", "tyrannosaurus_rex")));
        assertTrue(UpdateEditInfoPacket.isDinosaurId(Identifier.fromNamespaceAndPath("projectnublar", "gallimimus")));
        assertTrue(UpdateEditInfoPacket.isValid(new DinoData()));
    }

    @Test
    void editInfoInRangeIsAccepted() {
        assertTrue(UpdateEditInfoPacket.isValid(new DinoData()), "a fresh DinoData is what the screen starts from");

        DinoData data = new DinoData();
        data.addEntity(pig, MAX_PERCENTAGE);
        data.addEntity(new DinoData.EntityInfo(EntityType.COW, null), MIN_PERCENTAGE);
        data.setBasePercentage(MAX_PERCENTAGE);
        data.setGeneValue(GENE, MIN_GENE_VALUE);
        data.setGeneValue(new Genes.Gene("speed"), MAX_GENE_VALUE);
        data.getLayerColors().set(0, 0xFF123456);
        assertTrue(UpdateEditInfoPacket.isValid(data));
    }

    @Test
    void slotIndexOutOfRangeIsRejected() {
        // Menu slot 0 is the plant-matter input, so the egg slots start at 1.
        assertFalse(UpdateIncubatorSlotPacket.isValid(FIRST_EGG_MENU_INDEX - 1, EGG_AREA_START, EGG_AREA_START));
        assertFalse(UpdateIncubatorSlotPacket.isValid(LAST_EGG_MENU_INDEX + 1, EGG_AREA_START, EGG_AREA_START));
    }

    @Test
    void slotPositionOutsideScreenIsRejected() {
        assertFalse(UpdateIncubatorSlotPacket.isValid(FIRST_EGG_MENU_INDEX, EGG_AREA_START - 1, EGG_AREA_START));
        assertFalse(UpdateIncubatorSlotPacket.isValid(FIRST_EGG_MENU_INDEX, EGG_AREA_END_X, EGG_AREA_START));
        assertFalse(UpdateIncubatorSlotPacket.isValid(FIRST_EGG_MENU_INDEX, EGG_AREA_START, EGG_AREA_START - 1));
        assertFalse(UpdateIncubatorSlotPacket.isValid(FIRST_EGG_MENU_INDEX, EGG_AREA_START, EGG_AREA_END_Y));
    }

    @Test
    void slotInRangeIsAccepted() {
        assertEquals(1, FIRST_EGG_MENU_INDEX);
        assertEquals(IncubatorBlockEntity.EGG_SLOT_COUNT, LAST_EGG_MENU_INDEX);
        assertTrue(UpdateIncubatorSlotPacket.isValid(FIRST_EGG_MENU_INDEX, EGG_AREA_START, EGG_AREA_START));
        assertTrue(UpdateIncubatorSlotPacket.isValid(LAST_EGG_MENU_INDEX, EGG_AREA_END_X - 1, EGG_AREA_END_Y - 1));
        // The screen parks a taken-out egg slot at the empty slot's position.
        assertTrue(UpdateIncubatorSlotPacket.isValid(FIRST_EGG_MENU_INDEX,
                IncubatorBlockEntity.Slot.EMPTY.x(), IncubatorBlockEntity.Slot.EMPTY.y()));
    }
}
