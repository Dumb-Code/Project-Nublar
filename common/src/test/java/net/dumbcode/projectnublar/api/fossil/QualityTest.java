package net.dumbcode.projectnublar.api.fossil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/** Quality is a block state value now: four names, an order, an art folder, and no "none". */
class QualityTest {

    @Test
    void fourQualitiesInOrder() {
        assertEquals(4, Quality.values().length);
        assertEquals("fragmented", Quality.values()[0].getSerializedName());
        assertEquals("pristine", Quality.values()[3].getSerializedName());
    }

    @Test
    void artFoldersFollowTheBranchMapping() {
        assertEquals("fragmented", Quality.FRAGMENTED.artFolder());
        assertEquals("fragmented", Quality.POOR.artFolder());
        assertEquals("fossilized", Quality.COMMON.artFolder());
        assertEquals("fresh", Quality.PRISTINE.artFolder());
    }

    @Test
    void byNameFindsEachQuality() {
        assertSame(Quality.PRISTINE, Quality.byName("pristine"));
        assertSame(Quality.COMMON, Quality.byName("common"));
    }

    @Test
    void unknownNameIsNull() {
        assertNull(Quality.byName("none"));
        assertNull(Quality.byName(""));
    }
}
