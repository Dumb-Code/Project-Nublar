package net.dumbcode.projectnublar.client;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Each Triceratops shows one of the four horn sets in one of the three horn colours, picked from its UUID. The UUID
 * is the only input and is saved with the animal, so an animal keeps its horns after a save and reload, and after an
 * update of the mod. Every set and every colour can come up, so all of the horn art shows in a herd.
 */
class TriceratopsHornsTest {

    /** Random UUIDs, the same ones on every run. */
    private static Set<UUID> sampleUuids() {
        Random random = new Random(46);
        Set<UUID> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(new UUID(random.nextLong(), random.nextLong()));
        }
        return ids;
    }

    @Test
    void theSameUuidAlwaysGivesTheSameHorns() {
        for (UUID id : sampleUuids()) {
            // A reload reads the UUID back from the save as a new object.
            UUID reloaded = UUID.fromString(id.toString());
            assertEquals(TriceratopsHorns.of(id), TriceratopsHorns.of(reloaded), id.toString());
        }
        // Pinned picks: a changed formula would give every saved animal new horns. The last UUID has a negative hash.
        assertEquals(new TriceratopsHorns(1, 0), TriceratopsHorns.of(UUID.fromString("00000000-0000-0000-0000-000000000000")));
        assertEquals(new TriceratopsHorns(4, 1), TriceratopsHorns.of(UUID.fromString("00000000-0000-0000-0000-000000000007")));
        assertEquals(new TriceratopsHorns(3, 2), TriceratopsHorns.of(UUID.fromString("00000000-0000-0000-0000-00000000000a")));
        assertEquals(new TriceratopsHorns(4, 0), TriceratopsHorns.of(UUID.fromString("00000000-0000-0000-0000-0000ffffffff")));
    }

    @Test
    void everySetAndEveryColourIsReachable() {
        Set<TriceratopsHorns> expected = new HashSet<>();
        for (int set = 1; set <= 4; set++) {
            for (int colour = 0; colour < 3; colour++) {
                expected.add(new TriceratopsHorns(set, colour));
            }
        }
        Set<TriceratopsHorns> picked = new HashSet<>();
        for (UUID id : sampleUuids()) {
            picked.add(TriceratopsHorns.of(id));
        }
        assertEquals(expected, picked);
    }

    @Test
    void everyPickHasItsTextureInBothGenderFolders() {
        for (int set = 1; set <= 4; set++) {
            for (int colour = 0; colour < 3; colour++) {
                String layer = new TriceratopsHorns(set, colour).layerName();
                for (String gender : new String[] {"male", "female"}) {
                    String path = "/assets/projectnublar/textures/entity/triceratops/" + gender + "/" + layer + ".png";
                    assertNotNull(TriceratopsHornsTest.class.getResource(path), path + " is missing");
                }
            }
        }
    }
}
