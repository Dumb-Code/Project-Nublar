package net.dumbcode.projectnublar.api.fossil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The piece registry of the ported fossil design and the overlay texture path it implies. */
class FossilPiecesTest {

    @Test
    void twelvePiecesAreRegistered() {
        assertEquals(12, FossilPieces.all().size());
    }

    @Test
    void tyrannosaurusHasNinePiecesSpeciesFirst() {
        assertEquals(9, FossilPieces.TYRANNOSAURUS_PIECES.size());
        assertSame(FossilPieces.TYRANNOSAURUS_HAND, FossilPieces.TYRANNOSAURUS_PIECES.get(0));
        assertTrue(FossilPieces.TYRANNOSAURUS_PIECES.containsAll(FossilPieces.BIPED_PIECES));
    }

    @Test
    void texturePathUsesFolderArtFolderAndName() {
        assertEquals("block/fossil_overlay/tyrannosaurus_rex/fresh/tyrannosaurus_skull",
                FossilPieces.TYRANNOSAURUS_SKULL.texturePath(Quality.PRISTINE));
        assertEquals("block/fossil_overlay/common/biped/fragmented/bipedal_arm",
                FossilPieces.BIPEDAL_ARM.texturePath(Quality.POOR));
    }

    @Test
    void byNameFindsAPiece() {
        assertSame(FossilPieces.NECK, FossilPieces.byName("neck"));
    }

    @Test
    void unknownNameIsNull() {
        assertNull(FossilPieces.byName("rex_skull"));
    }
}
