package net.dumbcode.projectnublar.util;

import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.dumbcode.projectnublar.init.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

public class DinoNeedsUtils {
    public static final EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> THIRST = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> STAMINA = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> SOCIAL = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> AGGRESSION = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> FERTILITY = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DOMESTICITY = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> INTELLIGENCE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> VISION = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> IMMUNITY = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> TAMING_SCORE = SynchedEntityData.defineId(AbstractDinosaur.class, EntityDataSerializers.FLOAT);

    public static float getMaxHunger(AbstractDinosaur dinosaur){return (float) dinosaur.getAttributeValue(AttributesInit.DINO_HUNGER_NEED.get());}
    public static float getMaxThirst(AbstractDinosaur dinosaur){return (float) dinosaur.getAttributeValue(AttributesInit.DINO_THIRST_NEED.get());}
    public static float getMaxStamina(AbstractDinosaur dinosaur){return (float) dinosaur.getAttributeValue(AttributesInit.DINO_ENERGY_NEED.get());}
    public static float getMaxSocial(AbstractDinosaur dinosaur){return (float) dinosaur.getAttributeValue(AttributesInit.DINO_SOCIAL_NEED.get());}

    public static float getAggressionScoreFromStats(AbstractDinosaur dinosaur){
        double baseAggression = dinosaur.getAttributeValue(AttributesInit.DINO_AGGRESSION.get());
        double multiplier = dinosaur.getDinoData().getGeneValue(GeneInit.AGGRESSION.get());
        if(multiplier != 0.0D){
            double finalScore = baseAggression * (1.0D + (multiplier/100));
            return (float) finalScore;
        } else return (float) baseAggression;
    }

    public static void setAggressionScoreFromStats(AbstractDinosaur dinosaur){
        float aggressionScore = DinoNeedsUtils.getAggressionScoreFromStats(dinosaur);
        dinosaur.getEntityData().set(AGGRESSION, aggressionScore);
    }

    public static float getThreatScore(LivingEntity entity){
        AttributeInstance attackAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armorAttribute = entity.getAttribute(Attributes.ARMOR);
        double attack = 1;
        if(attackAttribute != null){
            attack = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
        double defence = 1;
        if(armorAttribute != null) {
            defence = entity.getAttributeValue(Attributes.ARMOR);
        }
        double health = entity.getHealth();
        double speed = entity.getSpeed();

        int groupsize = 1;

        if(entity instanceof AbstractDinosaur dinosaur && dinosaur.hasGroup()){
            ///TO-DO : Multiply threat by group size
        }
        return (float) ((attack * 2) + (defence * 1.7) + (health * 1.4) + (speed * 0.3) ) * groupsize;
    }
    public static boolean isTargetInsideEnclosure(LivingEntity target, AbstractDinosaur dinosaur) {
        BlockPos targetPos = target.blockPosition();
        BlockPos mobPos = dinosaur.blockPosition();

        // Simple: check if there’s a fence in a straight line between mob and target
        Vec3 dir = target.position().subtract(dinosaur.position());
        int steps = (int) dir.length();
        for (int i = 0; i <= steps; i++) {
            Vec3 pCheckPos = dinosaur.position().add(dir.scale(i / (double) steps));
            BlockPos pos = new BlockPos((int) pCheckPos.x,(int) pCheckPos.y,(int) pCheckPos.z);
            BlockState state = dinosaur.level().getBlockState(pos);
            @Nullable BlockEntity entity = null;
            if(dinosaur.level().getBlockEntity(pos) != null){
            entity = dinosaur.level().getBlockEntity(pos);
            }
            if (state.is(BlockInit.ELECTRIC_FENCE.get())||state.is(BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get()) || state.is(BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get())||
                    (entity != null && entity.equals(BlockInit.ELECTRIC_FENCE_BLOCK_ENTITY.get()))) {
                return false; // target is outside
            }
        }
        return true; // no fence in the way → inside
    }

    public static double getHuntTargetValue(LivingEntity huntTarget){
        double targetFoodValue = 50.0D; //Need to set up config for this

        if(huntTarget instanceof Monster){
            return 0.0;
        }
        if(huntTarget instanceof ServerPlayer){
            targetFoodValue = 200.0D;
        }
        double multiplyerFromRisk = 100.0D / (double) DinoNeedsUtils.getThreatScore(huntTarget);
        return targetFoodValue * multiplyerFromRisk;
    }

    public static boolean isStaminaFull(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) == DinoNeedsUtils.getMaxStamina(dinosaur);}
    public static boolean isHungerFull(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(HUNGER) == DinoNeedsUtils.getMaxHunger(dinosaur);}
    public static boolean isThirstFull(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) == DinoNeedsUtils.getMaxThirst(dinosaur);}
    public static boolean isSocialFull(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(SOCIAL) == DinoNeedsUtils.getMaxSocial(dinosaur);}


    public static boolean isHungry(AbstractDinosaur dinosaur){
        float currentHunger = dinosaur.getEntityData().get(HUNGER);
        float maxhunger = DinoNeedsUtils.getMaxHunger(dinosaur);
        float lowRiskThreshold = (float) dinosaur.getDinoBehaviour().happyThreshold();
        float stomachThreshold = maxhunger * lowRiskThreshold;

        return currentHunger < stomachThreshold;
    }
    public static boolean isThirsty(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) < DinoNeedsUtils.getMaxThirst(dinosaur) * dinosaur.getDinoBehaviour().uncomfortableThreshold();}
    public static boolean isTired(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) < DinoNeedsUtils.getMaxStamina(dinosaur) * dinosaur.getDinoBehaviour().rageThreshold();}
    public static boolean isSociallyLow(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(SOCIAL) == 0.0F;}

    public static boolean allNeedsAtZero(AbstractDinosaur dinosaur){
        return dinosaur.getEntityData().get(THIRST) == 0.0 && dinosaur.getEntityData().get(HUNGER) == 0.0F
                && dinosaur.getEntityData().get(SOCIAL) == 0.0 && dinosaur.getEntityData().get(STAMINA) == 0.0F;
    }
    public static boolean isDehydratedOrStarving(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) == 0.0F || dinosaur.getEntityData().get(HUNGER) == 0.0F;}
    public static boolean starving(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(HUNGER) == 0.0F;}
    public static boolean dehyrdrated(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) == 0.0F;}
    public static boolean isExhausted(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) == 0.0F;}
    public static boolean isSociallyDrained(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) == 0.0F;}


    public static void setCurrentHunger(AbstractDinosaur dinosaur, float pHunger){dinosaur.getEntityData().set(HUNGER, pHunger);}
    public static void setCurrentThirst(AbstractDinosaur dinosaur, float pThirst){dinosaur.getEntityData().set(THIRST, pThirst);}
    public static void setCurrentSocial(AbstractDinosaur dinosaur, float pSocial){dinosaur.getEntityData().set(SOCIAL, pSocial);}
    public static void setCurrentStamina(AbstractDinosaur dinosaur, float pStamina){dinosaur.getEntityData().set(STAMINA, pStamina);}

    public static float getCurrentHunger(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(HUNGER);}
    public static float getCurrentThirst(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(THIRST);}
    public static float getCurrentSocial(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(SOCIAL);}
    public static float getCurrentStamina(AbstractDinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA);}

    public static void tickHunger(AbstractDinosaur dinosaur, int timeSinceLastMeal, int starvationTime){
        float currentHunger = dinosaur.getEntityData().get(HUNGER);
        double eatRate = dinosaur.getDinoBehaviour().eatRate();
        double stomachCapacity = 100D;

        int days = timeSinceLastMeal;
        int maxDays = starvationTime;

        if( (days >= maxDays) || (currentHunger <= 0)){
            if(days >= maxDays){
                System.err.println("Days without food: " + days);
                System.err.println("Starvation time exceeds maximum of days!");
            }
            if(currentHunger <= 0){
                System.err.println("Hunger reached zero");
            }
            dinosaur.die(dinosaur.damageSources().starve());
            return;
        }

        //get how much hunger can decrease per day
        double dailyFoodDecrease = (stomachCapacity / maxDays);
        //get how much hunger is lost per hunger tick
        double hungerDecrease = dailyFoodDecrease / eatRate;
        //set new hunger
        float newCurrentHunger = currentHunger - (float) hungerDecrease;

        if(newCurrentHunger <= 0){
            newCurrentHunger = 0;
        }

        dinosaur.getEntityData().set(HUNGER, newCurrentHunger);
        //Let brain know dinosaur is hungry
        BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.IS_HUNGRY.get(), true);
    }
    public static void tickThirst(AbstractDinosaur dinosaur, int timeSinceLastDrink, int dehydrationTime){
        float currentThirst = dinosaur.getEntityData().get(THIRST);
        double drinkRate = dinosaur.getDinoBehaviour().drinkRate();
        double stomachCapacity = 100D;
        int days = timeSinceLastDrink;
        int maxDays = dehydrationTime;

        if( (days >= maxDays) || (currentThirst <= 0)){
            dinosaur.die(dinosaur.damageSources().starve());
            return;
        }
        double dailyThirstDecrease = (stomachCapacity / maxDays);
        double thirstDecrease = dailyThirstDecrease / drinkRate;
        float newCurrentThirst = currentThirst - (float) thirstDecrease;
        if(newCurrentThirst <= 0){
            newCurrentThirst = 0;
        }

        dinosaur.getEntityData().set(THIRST, newCurrentThirst);
        BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.IS_THIRSTY.get(), true);
    }
    public static void setDinoBaseNeeds(AbstractDinosaur dinosaur, DinoBehaviourData data){
        dinosaur.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.maxHealth());
  //      dinosaur.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(data.speed());
        dinosaur.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.attackDamage());
        dinosaur.getAttribute(Attributes.ARMOR).setBaseValue(data.resistance());
     //   dinosaur.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(data.visionQuality());
        dinosaur.getAttribute(AttributesInit.DINO_THIRST_NEED.get()).setBaseValue(100);
        dinosaur.getAttribute(AttributesInit.DINO_HUNGER_NEED.get()).setBaseValue(100);
        dinosaur.getAttribute(AttributesInit.DINO_ENERGY_NEED.get()).setBaseValue(data.maxStamina());
        dinosaur.getAttribute(AttributesInit.DINO_SOCIAL_NEED.get()).setBaseValue(data.socialNeed());
        dinosaur.getAttribute(AttributesInit.TRUST_SCORE.get()).setBaseValue(data.trustThreshold());
        dinosaur.getAttribute(AttributesInit.DINO_VISION.get()).setBaseValue(data.visionQuality());
        dinosaur.getAttribute(AttributesInit.DINO_AGGRESSION.get()).setBaseValue(data.aggressionLevel());
        dinosaur.getAttribute(AttributesInit.DINO_INTELLIGENCE.get()).setBaseValue(data.intelligence());
        dinosaur.getAttribute(AttributesInit.DINO_FERTILITY.get()).setBaseValue(data.fertility());
        dinosaur.getAttribute(AttributesInit.DINO_IMMUNITY.get()).setBaseValue(data.immunity());
        DinoNeedsUtils.setAggressionScoreFromStats(dinosaur);
    }

    public static void tickStamina(AbstractDinosaur dinosaur){
        float currentStamina = dinosaur.getEntityData().get(STAMINA);
        float staminaDecrease = (float) dinosaur.getDinoBehaviour().staminaDrain();
        float newCurrentValue;

        if(dinosaur.isRunning()){
            staminaDecrease = staminaDecrease * 2;
        }

        newCurrentValue = currentStamina - staminaDecrease;
        if (newCurrentValue <= 0) {
            dinosaur.getEntityData().set(STAMINA, 0.0F);
        } else {
            dinosaur.getEntityData().set(STAMINA, newCurrentValue);
        }

    }

    public static void tickSocial(){
    }

    public static void feed(AbstractDinosaur dinosaur, String foodItem){
        float currentHunger = dinosaur.getEntityData().get(HUNGER);
        float maxHunger = 100;
        double hungerIncrease;
        System.err.println(foodItem);

        if(dinosaur.getDinoDiet() != null) {
            hungerIncrease = dinosaur.getDinoDiet().foodMap().get(foodItem);
        } else hungerIncrease = 20F;

        float pCurrentHunger = currentHunger + (float) hungerIncrease;
        int eatCount;

        if(pCurrentHunger >= maxHunger) {
            if (BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.MEAL_COUNTER.get())) {
                eatCount = BrainUtils.getMemory(dinosaur, MemoryModuleTypeInit.MEAL_COUNTER.get());
                eatCount++;
            } else eatCount = 1;
            BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.MEAL_COUNTER.get(), eatCount);
        }

        BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get(), 0);

        DinoNeedsUtils.setCurrentHunger(dinosaur, Math.min(pCurrentHunger, maxHunger));

    }

    public static void drink(AbstractDinosaur dinosaur){
        DinoNeedsUtils.setCurrentThirst(dinosaur, getMaxThirst(dinosaur));
    }

}
