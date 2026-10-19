package net.dumbcode.projectnublar.util;

import java.util.HashMap;
import java.util.Map;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

/**
 * Synched animation flags for dinosaurs. Animation name
 * strings are frozen resource contracts (including the {@code dino_secondary_Controller} typo
 * registered in {@code Dinosaur}).
 *
 * <p>Frozen invariant: the {@link EntityDataAccessor} definitions below must stay in this
 * class and in this order - their ids depend on class-load order together with {@code Dinosaur}
 * and {@code DinoNeedsUtils}. Do not move or reorder them.
 */
public class DinoAnimationUtils {

    // Animation states
    public static EntityDataAccessor<Boolean> IS_EATING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_DRINKING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_NESTING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_ROARING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_SPEAKING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_SITTING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_RESTING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_RISING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_ATTACKING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_FLINCHING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_DEAD_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_SWIMMING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> LOOKING_LEFT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> LOOKING_RIGHT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> TURNING_LEFT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> TURNING_RIGHT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);

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
