package net.dumbcode.projectnublar.util;

import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.HashMap;
import java.util.Random;

public class DinoAnimationUtils {



    //ANIMATION-STATES
    public static EntityDataAccessor<Boolean> IS_EATING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_DRINKING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_NESTING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_ROARING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_SPEAKING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_SITTING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_RESTING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_RISING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_ATTACKING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_FLINCHING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_DEAD_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_SWIMMING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_RUNNING_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> LOOKING_LEFT_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> LOOKING_RIGHT_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> TURNING_LEFT_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> TURNING_RIGHT_STATE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.BOOLEAN);

    public static HashMap<String,EntityDataAccessor<Boolean>> dinoAnims(){
        HashMap<String,EntityDataAccessor<Boolean>> animationMap = new HashMap<String,EntityDataAccessor<Boolean>>();
        animationMap.put("eat",IS_EATING_STATE);
        animationMap.put("drink",IS_DRINKING_STATE);
        animationMap.put("nesting",IS_NESTING_STATE);
        animationMap.put("roar",IS_ROARING_STATE);
        animationMap.put("speak",IS_SPEAKING_STATE);
        animationMap.put("sit",IS_SITTING_STATE);
        animationMap.put("rest",IS_RESTING_STATE);
        animationMap.put("getup",IS_RISING_STATE);
        animationMap.put("attack",IS_ATTACKING_STATE);
        animationMap.put("flinch",IS_FLINCHING_STATE);
        animationMap.put("dead",IS_DEAD_STATE);
        animationMap.put("swim",IS_SWIMMING_STATE);
        animationMap.put("run",IS_RUNNING_STATE);
        animationMap.put("look_left",LOOKING_LEFT_STATE);
        animationMap.put("look_right",LOOKING_RIGHT_STATE);
        animationMap.put("turn_left",TURNING_LEFT_STATE);
        animationMap.put("turn_right",TURNING_RIGHT_STATE);
    return animationMap;
    }

    public static void setAnimationState(AbstractDinosaur dinosaur, String animationState, boolean state){
        EntityDataAccessor<Boolean> animState = DinoAnimationUtils.dinoAnims().get(animationState);
        dinosaur.getEntityData().set(animState,state);
    }
    //ACTIONS
    public static final RawAnimation DRINKING_ANIM = RawAnimation.begin().thenLoop("drink");
    public static final RawAnimation EATING_ANIM = RawAnimation.begin().thenLoop("eat1");
    public static final RawAnimation NESTING_ANIM = RawAnimation.begin().thenLoop("nesting");
    public static final RawAnimation ROARING_ANIM = RawAnimation.begin().thenLoop("roar");
    public static final RawAnimation SPEAK_ANIM = RawAnimation.begin().thenLoop("speak");

    //RESTING
    public static final RawAnimation REST_ANIM = RawAnimation.begin().thenLoop("rest");
    public static final RawAnimation REST_IDLE_ANIM = RawAnimation.begin().thenPlay("restidle");
    public static final RawAnimation GETTING_UP_ANIM = RawAnimation.begin().thenPlay("getup");

    //MOVEMENT
    public static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("move.walk");
    public static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
    public static final RawAnimation SWIM_ANIM = RawAnimation.begin().thenLoop("swim");
    public static final RawAnimation LOOK_LEFT_ANIM = RawAnimation.begin().thenLoop("lookleft");
    public static final RawAnimation LOOK_RIGHT_ANIM = RawAnimation.begin().thenLoop("lookright");
    public static final RawAnimation TURN_LEFT_ANIM = RawAnimation.begin().thenPlayAndHold("turnleft");
    public static final RawAnimation TURN_RIGHT_ANIM = RawAnimation.begin().thenPlayAndHold("turnright");
    public static final RawAnimation FALL_ANIM = RawAnimation.begin().thenPlayAndHold("jump/fall");

    //IDLES
    public static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("misc.idle");
    public static final RawAnimation SNIFF_AIR_IDLE_ANIM = RawAnimation.begin().thenLoop("sniffair");
    public static final RawAnimation SNIFF_GROUND_IDLE_ANIM = RawAnimation.begin().thenLoop("sniffground");
    public static final RawAnimation SHAKE_BODY_IDLE_ANIM = RawAnimation.begin().thenLoop("shakebody");
    public static final RawAnimation SHAKE_HEAD_IDLE_ANIM = RawAnimation.begin().thenLoop("shakehead");
    public static final RawAnimation SCRATCH_IDLE_ANIM = RawAnimation.begin().thenLoop("scratch");

    //FIGHT
    public static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenLoop("attack1");
    public static final RawAnimation FLINCH_ANIM = RawAnimation.begin().thenLoop("flinch");
    public static final RawAnimation DEAD_ANIM = RawAnimation.begin().thenPlayAndHold("dead");

     public static RawAnimation playRandomIdle(){
         Random idleAnim = new Random();
         int idleToPlay = idleAnim.nextInt(1,6);
         RawAnimation animation;

         switch (idleToPlay) {

            case 1: animation = IDLE_ANIM;
            break;
            case 2: animation = SNIFF_AIR_IDLE_ANIM;
            break;
            case 3: animation = SNIFF_GROUND_IDLE_ANIM;
            break;
            case 4: animation = SHAKE_BODY_IDLE_ANIM;
            break;
            case 5: animation = SHAKE_HEAD_IDLE_ANIM;
            break;
            case 6: animation = SCRATCH_IDLE_ANIM;
            break;
            default: animation = IDLE_ANIM;
            break;
        }
        return animation;
    }

}
