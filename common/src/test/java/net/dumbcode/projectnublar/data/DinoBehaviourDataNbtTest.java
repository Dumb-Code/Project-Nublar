package net.dumbcode.projectnublar.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData;
import net.minecraft.util.GsonHelper;
import org.junit.jupiter.api.Test;

/**
 * A saved behaviour profile reads back what it wrote (Task 38). A bred baby copies its mother's profile through
 * NBT and every saved animal loads it, so a key that is written under one name and read under another sets that
 * stat to 0: the stamina maximum did, on 1.20.1 too.
 */
class DinoBehaviourDataNbtTest {

    @Test
    void theStaminaMaximumSurvivesASave() throws IOException {
        InputStream stream = DinoBehaviourDataNbtTest.class
                .getResourceAsStream("/data/projectnublar/config/dinosaurs/tyrannosaurus_rex_config.json");
        assertNotNull(stream, "T. rex config is not on the test class path");
        DinoBehaviourData rex;
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            rex = BehaviourDataReloadListener.parseBehaviourData(GsonHelper.parse(reader));
        }
        // The shipped value is non-zero, so a lost key shows as 0 here.
        assertEquals(300.0, rex.maxStamina());
        assertEquals(rex.maxStamina(), DinoBehaviourData.fromNBT(rex.toNBT(rex)).maxStamina());
    }
}
