package net.dumbcode.projectnublar.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData;
import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData.ActivityWindow;
import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData.SleepWindow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import org.junit.jupiter.api.Test;

/**
 * When each species sleeps (Task 32): the values of engineering backlog F2.0 "Sleep windows" and its per-species
 * table, read from the shipped species configs, so a config edit that moves a window fails here. In its window an
 * animal sleeps on at full stamina ({@code Rest}) and its herd lies down; outside it, it gets up once rested.
 */
class SleepWindowsTest {

    private static final String FOLDER = "/data/projectnublar/config/dinosaurs/";
    private static final List<String> CARNIVORES = List.of("tyrannosaurus_rex", "velociraptor", "dilophosaurus");
    private static final List<String> HERBIVORES = List.of("gallimimus", "triceratops", "brachiosaurus");

    private static JsonObject config(String species) throws IOException {
        InputStream stream = SleepWindowsTest.class.getResourceAsStream(FOLDER + species + "_config.json");
        assertNotNull(stream, species + " config is not on the test class path");
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return GsonHelper.parse(reader);
        }
    }

    private static DinoBehaviourData species(String species) throws IOException {
        return BehaviourDataReloadListener.parseBehaviourData(config(species));
    }

    /** Asleep from {@code start} to the tick before {@code end}; awake just before {@code start} and at {@code end}. */
    private static void assertSleeps(DinoBehaviourData behaviour, int start, int end) {
        String who = behaviour.speciesID() + " ";
        assertFalse(behaviour.inSleepWindow(start - 1), who + "awake at " + (start - 1));
        assertTrue(behaviour.inSleepWindow(start), who + "asleep at " + start);
        assertTrue(behaviour.inSleepWindow(end - 1), who + "asleep at " + (end - 1));
        assertFalse(behaviour.inSleepWindow(end), who + "awake at " + end);
    }

    @Test
    void theCarnivoresAreCathemeralWithTwoSleepsADay() throws IOException {
        DinoBehaviourData rex = species("tyrannosaurus_rex");
        assertSleeps(rex, 16000, 21000);
        assertSleeps(rex, 7000, 9000);
        DinoBehaviourData raptor = species("velociraptor");
        assertSleeps(raptor, 20000, 23000);
        assertSleeps(raptor, 6000, 8500);
        DinoBehaviourData dilophosaurus = species("dilophosaurus");
        assertSleeps(dilophosaurus, 2000, 5000);
        assertSleeps(dilophosaurus, 8000, 10000);
        for (String carnivore : CARNIVORES) {
            DinoBehaviourData behaviour = species(carnivore);
            assertEquals(ActivityWindow.CATHEMERAL, behaviour.activityWindow(), carnivore);
            assertEquals(2, behaviour.sleepWindows().size(), carnivore + ": two windows, no others");
        }
    }

    @Test
    void theHerbivoresAreDiurnalAndSleepFrom16000UntilDawn() throws IOException {
        for (String herbivore : HERBIVORES) {
            DinoBehaviourData behaviour = species(herbivore);
            assertEquals(ActivityWindow.DIURNAL, behaviour.activityWindow(), herbivore);
            // The window ends at 24,000, which is 0 of the next day: awake at dawn.
            assertSleeps(behaviour, 16000, 24000);
            assertFalse(behaviour.inSleepWindow(12500L), herbivore + " is still awake after sunset");
        }
    }

    @Test
    void theLeadsInGameCheckTimes() throws IOException {
        // 26.2 "/time set day" moves the clock to 1,000: every species is awake, so a resting animal gets up rested.
        for (List<String> group : List.of(CARNIVORES, HERBIVORES)) {
            for (String species : group) {
                assertFalse(species(species).inSleepWindow(1000L), species + " awake at 1,000");
            }
        }
        // At 17,000 the T. rex sleeps; the Velociraptor's night sleep starts only at 20,000.
        assertTrue(species("tyrannosaurus_rex").inSleepWindow(17000L));
        assertFalse(species("velociraptor").inSleepWindow(17000L));
        assertTrue(species("tyrannosaurus_rex").inSleepWindow(10L * 24000L + 17000L), "on any day");
    }

    @Test
    void theSavedProfileKeepsTheWindows() throws IOException {
        DinoBehaviourData rex = species("tyrannosaurus_rex");
        DinoBehaviourData loaded = DinoBehaviourData.fromNBT(rex.toNBT(rex));
        assertEquals(ActivityWindow.CATHEMERAL, loaded.activityWindow());
        assertEquals(List.of(new SleepWindow(16000, 21000), new SleepWindow(7000, 9000)), loaded.sleepWindows());
    }

    @Test
    void anAnimalSavedWithTheOldNocturnalFlagSleeps4000To12000() {
        CompoundTag nocturnal = new CompoundTag();
        nocturnal.putBoolean("nocturnal", true);
        DinoBehaviourData behaviour = DinoBehaviourData.fromNBT(nocturnal);
        assertEquals(ActivityWindow.NOCTURNAL, behaviour.activityWindow());
        assertSleeps(behaviour, 4000, 12000);

        CompoundTag diurnal = new CompoundTag();
        diurnal.putBoolean("nocturnal", false);
        assertEquals(ActivityWindow.DIURNAL, DinoBehaviourData.fromNBT(diurnal).activityWindow());
        assertEquals(ActivityWindow.DIURNAL, DinoBehaviourData.fromNBT(new CompoundTag()).activityWindow(),
                "no saved profile: not nocturnal, as before");
    }

    @Test
    void aConfigThatWouldNeverSleepIsRejected() throws IOException {
        JsonObject noWindows = config("tyrannosaurus_rex");
        noWindows.remove(DinoBehaviourData.SLEEP_WINDOWS_KEY);
        assertThrows(JsonSyntaxException.class, () -> BehaviourDataReloadListener.parseBehaviourData(noWindows),
                "a cathemeral species without windows");

        JsonObject backwards = config("tyrannosaurus_rex");
        JsonArray windows = JsonParser.parseString("[{\"start\": 21000, \"end\": 16000}]").getAsJsonArray();
        backwards.add(DinoBehaviourData.SLEEP_WINDOWS_KEY, windows);
        assertThrows(JsonSyntaxException.class, () -> BehaviourDataReloadListener.parseBehaviourData(backwards),
                "a window that ends before it starts");
    }
}
