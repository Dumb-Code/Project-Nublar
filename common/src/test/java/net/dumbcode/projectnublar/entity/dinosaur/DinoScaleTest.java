package net.dumbcode.projectnublar.entity.dinosaur;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The size of an animal ({@link Dinosaur#scaleFactor(double, int, float)}): the 1.20.1 render scale, which the
 * renderer applies by hand with the scale switch off and the {@code SCALE} attribute holds with it on. An adult is
 * {@code SIZE} gene / 100 plus the species offset; the growth stages 1 to 3 are a quarter, a half and three quarters
 * of that.
 */
class DinoScaleTest {

    @Test
    void adultFactorIsGeneOverHundredPlusOffset() {
        assertEquals(1.5F, Dinosaur.scaleFactor(50, 4, 1.0F));
    }

    @Test
    void babyIsAQuarterOfAdult() {
        assertEquals(0.375F, Dinosaur.scaleFactor(50, 1, 1.0F));
    }

    @Test
    void juvenileIsHalf() {
        assertEquals(0.75F, Dinosaur.scaleFactor(50, 2, 1.0F));
    }

    @Test
    void subAdultIsThreeQuarters() {
        assertEquals(1.125F, Dinosaur.scaleFactor(50, 3, 1.0F));
    }

    @Test
    void zeroGeneGivesTheSpeciesOffset() {
        assertEquals(2.5F, Dinosaur.scaleFactor(0, 4, 2.5F));
    }
}
