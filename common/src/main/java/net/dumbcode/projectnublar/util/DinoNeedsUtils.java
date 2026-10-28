package net.dumbcode.projectnublar.util;

import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.RunGait;
import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.HUNGER;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.THIRST;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.STAMINA;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.SOCIAL;
import static net.dumbcode.projectnublar.entity.dinosaur.Dinosaur.AGGRESSION;

/**
 * Static helpers for the dinosaur need bars (hunger/thirst/stamina/social) and threat scoring.
 *
 * <p>The synced need values these helpers read and write ({@code HUNGER} and the others) are
 * defined in {@link Dinosaur}, which keeps every dinosaur synced value in one fixed order.
 */
public class DinoNeedsUtils {

    /**
     * Below this share of its hunger maximum an animal wants food: the {@code IS_HUNGRY} memory is set, so it eats
     * food in reach, walks to food and feeders, and a carnivore hunts (lead, 2026-10-27: "Start below 75 %").
     */
    public static final double WANTS_FOOD_BELOW = 0.75D;
    /**
     * Above this share of its hunger maximum an animal stops eating: {@code IS_HUNGRY} is cleared and a hunter stops
     * hunting (lead, 2026-10-27: "stop at 90 %"). Between the two lines the memory keeps its state, so an eater goes on
     * eating the food in reach and a fed animal does not want food again at once.
     */
    public static final double FULL_ABOVE = 0.90D;

    /** The registered holder of a mod attribute: 26.2 reads and sets attributes by {@code Holder<Attribute>}. */
    private static Holder<Attribute> holder(Supplier<Attribute> attribute) {
        return BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute.get());
    }

    public static float getMaxHunger(Dinosaur dinosaur){return (float) dinosaur.getAttributeValue(holder(AttributesInit.DINO_HUNGER_NEED));}
    public static float getMaxThirst(Dinosaur dinosaur){return (float) dinosaur.getAttributeValue(holder(AttributesInit.DINO_THIRST_NEED));}
    public static float getMaxStamina(Dinosaur dinosaur){return (float) dinosaur.getAttributeValue(holder(AttributesInit.DINO_ENERGY_NEED));}
    public static float getMaxSocial(Dinosaur dinosaur){return (float) dinosaur.getAttributeValue(holder(AttributesInit.DINO_SOCIAL_NEED));}

    public static float getAggressionScoreFromStats(Dinosaur dinosaur){
        double baseAggression = dinosaur.getAttributeValue(holder(AttributesInit.DINO_AGGRESSION));
        double multiplier = dinosaur.getDinoData().getGeneValue(GeneInit.AGGRESSION.get());
        if(multiplier != 0.0D){
            double finalScore = baseAggression * (1.0D + (multiplier/100));
            return (float) finalScore;
        } else return (float) baseAggression;
    }

    public static void setAggressionScoreFromStats(Dinosaur dinosaur){
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

        if(entity instanceof Dinosaur dinosaur && dinosaur.hasGroup()){
            ///TO-DO : Multiply threat by group size
        }
        return (float) ((attack * 2) + (defence * 1.7) + (health * 1.4) + (speed * 0.3) ) * groupsize;
    }
    public static boolean isTargetInsideEnclosure(LivingEntity target, Dinosaur dinosaur) {
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

    public static boolean isStaminaFull(Dinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) == DinoNeedsUtils.getMaxStamina(dinosaur);}
    public static boolean isHungerFull(Dinosaur dinosaur){return dinosaur.getEntityData().get(HUNGER) == DinoNeedsUtils.getMaxHunger(dinosaur);}
    public static boolean isThirstFull(Dinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) == DinoNeedsUtils.getMaxThirst(dinosaur);}
    public static boolean isSocialFull(Dinosaur dinosaur){return dinosaur.getEntityData().get(SOCIAL) == DinoNeedsUtils.getMaxSocial(dinosaur);}


    /**
     * Whether the animal wants food: the {@code IS_HUNGRY} memory, which follows the hunger bar ({@link #wantsFood}).
     * Eating, walks to food and feeders, and hunting all read this one flag.
     */
    public static boolean isHungry(Dinosaur dinosaur){
        return BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.IS_HUNGRY.get());
    }

    /**
     * Whether an animal wants food at this hunger: yes below {@link #WANTS_FOOD_BELOW} of the maximum, no above
     * {@link #FULL_ABOVE}, and as before ({@code wantedFood}) between the two lines.
     */
    public static boolean wantsFood(boolean wantedFood, float hunger, float maxHunger) {
        if (hunger < maxHunger * WANTS_FOOD_BELOW) {
            return true;
        }
        if (hunger > maxHunger * FULL_ABOVE) {
            return false;
        }
        return wantedFood;
    }

    /**
     * Sets or clears the {@code IS_HUNGRY} memory by the bar ({@link #wantsFood}). The hunger tick and {@link #feed}
     * call it when the bar changes, and the dinosaur's server tick calls it every tick, which also covers a bar set by a
     * command and an animal just loaded (the memory is not saved).
     */
    public static void updateHungryFlag(Dinosaur dinosaur) {
        boolean wasHungry = isHungry(dinosaur);
        boolean hungry = wantsFood(wasHungry, getCurrentHunger(dinosaur), getMaxHunger(dinosaur));
        if (hungry && !wasHungry) {
            BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.IS_HUNGRY.get(), true);
        } else if (!hungry && wasHungry) {
            BrainUtil.clearMemory(dinosaur, MemoryModuleTypeInit.IS_HUNGRY.get());
        }
    }
    public static boolean isThirsty(Dinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) < DinoNeedsUtils.getMaxThirst(dinosaur) * dinosaur.getDinoBehaviour().uncomfortableThreshold();}
    public static boolean isTired(Dinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) < DinoNeedsUtils.getMaxStamina(dinosaur) * dinosaur.getDinoBehaviour().rageThreshold();}
    public static boolean isSociallyLow(Dinosaur dinosaur){return dinosaur.getEntityData().get(SOCIAL) == 0.0F;}

    public static boolean allNeedsAtZero(Dinosaur dinosaur){
        return dinosaur.getEntityData().get(THIRST) == 0.0 && dinosaur.getEntityData().get(HUNGER) == 0.0F
                && dinosaur.getEntityData().get(SOCIAL) == 0.0 && dinosaur.getEntityData().get(STAMINA) == 0.0F;
    }
    public static boolean isDehydratedOrStarving(Dinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) == 0.0F || dinosaur.getEntityData().get(HUNGER) == 0.0F;}
    public static boolean starving(Dinosaur dinosaur){return dinosaur.getEntityData().get(HUNGER) == 0.0F;}
    public static boolean dehyrdrated(Dinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) == 0.0F;}
    public static boolean isExhausted(Dinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) == 0.0F;}

    // TODO(BUG): checks STAMINA instead of SOCIAL (copy-paste from isExhausted);
    public static boolean isSociallyDrained(Dinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) == 0.0F;}


    public static void setCurrentHunger(Dinosaur dinosaur, float pHunger){dinosaur.getEntityData().set(HUNGER, pHunger);}
    public static void setCurrentThirst(Dinosaur dinosaur, float pThirst){dinosaur.getEntityData().set(THIRST, pThirst);}
    public static void setCurrentSocial(Dinosaur dinosaur, float pSocial){dinosaur.getEntityData().set(SOCIAL, pSocial);}
    public static void setCurrentStamina(Dinosaur dinosaur, float pStamina){dinosaur.getEntityData().set(STAMINA, pStamina);}

    public static float getCurrentHunger(Dinosaur dinosaur){return dinosaur.getEntityData().get(HUNGER);}
    public static float getCurrentThirst(Dinosaur dinosaur){return dinosaur.getEntityData().get(THIRST);}
    public static float getCurrentSocial(Dinosaur dinosaur){return dinosaur.getEntityData().get(SOCIAL);}
    public static float getCurrentStamina(Dinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA);}

    public static void tickHunger(Dinosaur dinosaur, int timeSinceLastMeal, int starvationTime){
        float currentHunger = dinosaur.getEntityData().get(HUNGER);
        double eatRate = dinosaur.getDinoBehaviour().eatRate();
        double stomachCapacity = 100D;

        int days = timeSinceLastMeal;
        int maxDays = starvationTime;

        if( (days >= maxDays) || (currentHunger <= 0)){
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
        // Wants food only below the 75 % line, not after every tick (before Task 40 every tick set it, at 97 % too).
        updateHungryFlag(dinosaur);
    }
    public static void tickThirst(Dinosaur dinosaur, int timeSinceLastDrink, int dehydrationTime){
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
        BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.IS_THIRSTY.get(), true);
    }
    public static void setDinoBaseNeeds(Dinosaur dinosaur, DinoBehaviourData data){
        dinosaur.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.maxHealth());
  //      dinosaur.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(data.speed());
        dinosaur.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.attackDamage());
        dinosaur.getAttribute(Attributes.ARMOR).setBaseValue(data.resistance());
     //   dinosaur.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(data.visionQuality());
        dinosaur.getAttribute(holder(AttributesInit.DINO_THIRST_NEED)).setBaseValue(100);
        dinosaur.getAttribute(holder(AttributesInit.DINO_HUNGER_NEED)).setBaseValue(100);
        dinosaur.getAttribute(holder(AttributesInit.DINO_ENERGY_NEED)).setBaseValue(data.maxStamina());
        dinosaur.getAttribute(holder(AttributesInit.DINO_SOCIAL_NEED)).setBaseValue(data.socialNeed());
        dinosaur.getAttribute(holder(AttributesInit.TRUST_SCORE)).setBaseValue(data.trustThreshold());
        dinosaur.getAttribute(holder(AttributesInit.DINO_VISION)).setBaseValue(data.visionQuality());
        dinosaur.getAttribute(holder(AttributesInit.DINO_AGGRESSION)).setBaseValue(data.aggressionLevel());
        dinosaur.getAttribute(holder(AttributesInit.DINO_INTELLIGENCE)).setBaseValue(data.intelligence());
        dinosaur.getAttribute(holder(AttributesInit.DINO_FERTILITY)).setBaseValue(data.fertility());
        dinosaur.getAttribute(holder(AttributesInit.DINO_IMMUNITY)).setBaseValue(data.immunity());
        DinoNeedsUtils.setAggressionScoreFromStats(dinosaur);
    }

    public static void tickStamina(Dinosaur dinosaur){
        float currentStamina = dinosaur.getEntityData().get(STAMINA);
        // P13b (ruling Q3): a walk drains 0.75 of the species' drain, a run twice it (as before).
        float staminaDecrease = (float) RunGait.staminaDrain(dinosaur.getDinoBehaviour().staminaDrain(), dinosaur.isRunning());
        float newCurrentValue;

        newCurrentValue = currentStamina - staminaDecrease;
        if (newCurrentValue <= 0) {
            dinosaur.getEntityData().set(STAMINA, 0.0F);
        } else {
            dinosaur.getEntityData().set(STAMINA, newCurrentValue);
        }

    }

    // TODO(BUG): empty stub - the social bar never drains (and its caller's tick increment was
    // also commented out in Dinosaur.tick());
    public static void tickSocial(){
    }

    public static void feed(Dinosaur dinosaur, String foodItem){
        float currentHunger = dinosaur.getEntityData().get(HUNGER);
        float maxHunger = 100;
        double hungerIncrease;

        if(dinosaur.getDinoDiet() != null) {
            hungerIncrease = dinosaur.getDinoDiet().foodMap().get(foodItem);
        } else hungerIncrease = 20F;

        float pCurrentHunger = currentHunger + (float) hungerIncrease;
        int eatCount;

        if(pCurrentHunger >= maxHunger) {
            if (BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.MEAL_COUNTER.get())) {
                eatCount = BrainUtil.getMemory(dinosaur, MemoryModuleTypeInit.MEAL_COUNTER.get());
                eatCount++;
            } else eatCount = 1;
            BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.MEAL_COUNTER.get(), eatCount);
        }

        BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get(), 0);

        DinoNeedsUtils.setCurrentHunger(dinosaur, Math.min(pCurrentHunger, maxHunger));
        // Above the 90 % line the animal stops eating, so the next behaviour this tick already sees it full.
        updateHungryFlag(dinosaur);
    }

    /** Fills the thirst bar and starts the days-without-water count again ({@link #daysWithoutWater}). */
    public static void drink(Dinosaur dinosaur){
        DinoNeedsUtils.setCurrentThirst(dinosaur, getMaxThirst(dinosaur));
        Integer stored = BrainUtil.getMemory(dinosaur, MemoryModuleTypeInit.DAYS_SINCE_LAST_DRANK.get());
        BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.DAYS_SINCE_LAST_DRANK.get(), daysWithoutWater(stored, true));
    }

    /**
     * The days-without-water count (the {@code DAYS_SINCE_LAST_DRANK} memory) after a drink or a day rollover, from the
     * stored count ({@code null} before the first rollover). A drink starts the count again at 0, as {@link #feed} does
     * for days without food; a rollover adds one day, and {@link #tickThirst} kills the animal when the count reaches
     * {@code max_days_without_water} (3 for every species). Before Task 40 no drink set the count back (the same on
     * 1.20.1), so every animal died of thirst on its third day.
     */
    public static int daysWithoutWater(@Nullable Integer stored, boolean drinks) {
        if (drinks) {
            return 0;
        }
        return stored == null ? 1 : stored + 1;
    }

}
