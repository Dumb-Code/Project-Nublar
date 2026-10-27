package net.dumbcode.projectnublar.util;

import java.util.HashMap;
import java.util.Map;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.network.syncher.EntityDataAccessor;

import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_EATING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_DRINKING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_NESTING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_ROARING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_SPEAKING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_SITTING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_RESTING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_RISING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_ATTACKING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_FLINCHING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_DEAD_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.IS_SWIMMING_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.LOOKING_LEFT_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.LOOKING_RIGHT_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.TURNING_LEFT_STATE;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.TURNING_RIGHT_STATE;

/**
 * Synched animation flags for dinosaurs. Animation name
 * strings are frozen resource contracts (including the {@code dino_secondary_Controller} typo
 * registered in {@code Dinosaur}).
 *
 * <p>The animation state flags themselves ({@code IS_EATING_STATE} and the others) are defined in
 * {@link Dinosaur}, which keeps every dinosaur synced value in one fixed order.
 */
public class DinoAnimationUtils {

    private static final Map<String, EntityDataAccessor<Boolean>> ANIMATION_STATES_BY_NAME =
            createAnimationStateMap();

    public static HashMap<String, EntityDataAccessor<Boolean>> dinoAnims() {
        return new HashMap<>(ANIMATION_STATES_BY_NAME);
    }

    public static void setAnimationState(Dinosaur dinosaur, String animationState, boolean state) {
        EntityDataAccessor<Boolean> animState = ANIMATION_STATES_BY_NAME.get(animationState);
        dinosaur.getEntityData().set(animState, state);
    }

    private static Map<String, EntityDataAccessor<Boolean>> createAnimationStateMap() {
        Map<String, EntityDataAccessor<Boolean>> animationMap = new HashMap<>();
        animationMap.put("eat", IS_EATING_STATE);
        animationMap.put("drink", IS_DRINKING_STATE);
        animationMap.put("nesting", IS_NESTING_STATE);
        animationMap.put("roar", IS_ROARING_STATE);
        animationMap.put("speak", IS_SPEAKING_STATE);
        animationMap.put("sit", IS_SITTING_STATE);
        animationMap.put("rest", IS_RESTING_STATE);
        animationMap.put("getup", IS_RISING_STATE);
        animationMap.put("attack", IS_ATTACKING_STATE);
        animationMap.put("flinch", IS_FLINCHING_STATE);
        animationMap.put("dead", IS_DEAD_STATE);
        animationMap.put("swim", IS_SWIMMING_STATE);
        animationMap.put("look_left", LOOKING_LEFT_STATE);
        animationMap.put("look_right", LOOKING_RIGHT_STATE);
        animationMap.put("turn_left", TURNING_LEFT_STATE);
        animationMap.put("turn_right", TURNING_RIGHT_STATE);
        return animationMap;
    }

    // The animations each state plays are in AnimationChoice (fix P10): candidate names per state for all species.
}
