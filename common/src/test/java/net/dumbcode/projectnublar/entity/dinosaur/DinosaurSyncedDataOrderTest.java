package net.dumbcode.projectnublar.entity.dinosaur;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import net.dumbcode.projectnublar.TestBootstrap;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the order of a dinosaur's synced values. The game numbers them in the order {@link Dinosaur} defines them,
 * and the server and every client must give each value the same number, so a value that is reordered or moved out
 * of {@link Dinosaur} fails here. A new value goes after the last one, here and in {@link Dinosaur}.
 */
class DinosaurSyncedDataOrderTest {

    /** Reading the fields loads {@link Dinosaur}, whose entity superclasses need vanilla's registries. */
    @BeforeAll
    static void bootstrap() {
        TestBootstrap.ensure();
    }

    @Test
    void syncedValuesKeepTheirDefinitionOrder() {
        List<EntityDataAccessor<?>> order = order();
        int first = order.get(0).id();
        for (int i = 0; i < order.size(); i++) {
            assertEquals(first + i, order.get(i).id(), "synced value at position " + i);
        }
    }

    /** A synced value added to {@link Dinosaur} but not to {@link #order()} fails here, not silently. */
    @Test
    void everyDeclaredSyncedValueIsListed() {
        long declared = Arrays.stream(Dinosaur.class.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()) && field.getType() == EntityDataAccessor.class)
                .count();
        assertEquals(order().size(), declared);
    }

    /** The synced values in the order {@link Dinosaur} defines them. A method, so the class loads after the bootstrap. */
    private static List<EntityDataAccessor<?>> order() {
        return List.of(
                Dinosaur.DINO_DATA, Dinosaur.DINO_BEHAVIOUR, Dinosaur.DINO_FAMILY_UUID, Dinosaur.DINO_MATE,
                Dinosaur.BABY_DATA_ID, Dinosaur.JUVENILE_DATA_ID, Dinosaur.SUB_ADULT_DATA_ID, Dinosaur.ADULT_DATA_ID,
                Dinosaur.SHOULD_TICK_STAMINA, Dinosaur.GROUP_LEADER, Dinosaur.DINOSAUR_HEAD_POS, Dinosaur.ONE_SHOT,
                Dinosaur.HERD_TRAVELS, Dinosaur.SCALE_MODE,
                Dinosaur.HUNGER, Dinosaur.THIRST, Dinosaur.STAMINA, Dinosaur.SOCIAL, Dinosaur.AGGRESSION,
                Dinosaur.FERTILITY, Dinosaur.DOMESTICITY, Dinosaur.SIZE, Dinosaur.INTELLIGENCE, Dinosaur.VISION,
                Dinosaur.IMMUNITY, Dinosaur.TAMING_SCORE,
                Dinosaur.IS_EATING_STATE, Dinosaur.IS_DRINKING_STATE, Dinosaur.IS_NESTING_STATE,
                Dinosaur.IS_ROARING_STATE, Dinosaur.IS_SPEAKING_STATE, Dinosaur.IS_SITTING_STATE,
                Dinosaur.IS_RESTING_STATE, Dinosaur.IS_RISING_STATE, Dinosaur.IS_ATTACKING_STATE,
                Dinosaur.IS_FLINCHING_STATE, Dinosaur.IS_DEAD_STATE, Dinosaur.IS_SWIMMING_STATE,
                Dinosaur.LOOKING_LEFT_STATE, Dinosaur.LOOKING_RIGHT_STATE, Dinosaur.TURNING_LEFT_STATE,
                Dinosaur.TURNING_RIGHT_STATE);
    }
}
