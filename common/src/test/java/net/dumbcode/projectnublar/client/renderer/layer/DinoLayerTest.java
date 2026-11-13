package net.dumbcode.projectnublar.client.renderer.layer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DinoLayerTest {

    @Test
    void layersAreDrawnInListOrderByDefault() {
        List<DinoLayer> layers = List.of(new DinoLayer("belly", 2), new DinoLayer("back", 1),
                new DinoLayer("claws", -1));

        assertArrayEquals(new int[] {0, 1, 2}, DinoLayer.drawOrder(layers));
    }

    @Test
    void aLayerSetBelowAnotherIsDrawnDirectlyUnderItAndKeepsItsPlaceInTheList() {
        // The male Tyrannosaurus: the feet art lies under the claws, the yellow under the back.
        List<DinoLayer> layers = List.of(new DinoLayer("belly", 2), new DinoLayer("back", 1),
                new DinoLayer("mouth", -1), new DinoLayer("claws", -1), new DinoLayer("feet", 3).below("mouth"),
                new DinoLayer("yellow", 2).below("back"));

        // Drawn bottom to top: belly, yellow, back, feet, mouth, claws.
        assertArrayEquals(new int[] {0, 2, 4, 5, 3, 1}, DinoLayer.drawOrder(layers));
    }

    @Test
    void layersSetBelowTheSameLayerKeepTheirListOrder() {
        // The male Gallimimus: the belly art lies on top of the tail, the pattern and the stripes.
        List<DinoLayer> layers = List.of(new DinoLayer("belly", 2), new DinoLayer("tail", 1).below("belly"),
                new DinoLayer("pattern", 2).below("belly"), new DinoLayer("body-stripes", 3).below("belly"));

        assertArrayEquals(new int[] {3, 0, 1, 2}, DinoLayer.drawOrder(layers));
    }

    @Test
    void aLayerSetBelowANameThatIsNotInTheListIsRefused() {
        List<DinoLayer> layers = List.of(new DinoLayer("belly", 2), new DinoLayer("feet", 3).below("claws"));

        assertThrows(IllegalArgumentException.class, () -> DinoLayer.drawOrder(layers));
    }
}
