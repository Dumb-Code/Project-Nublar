package net.dumbcode.projectnublar.entity.dinosaur;

import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.dinosaur.DinoDietData;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.entity.dinosaur.ai.util.FenceAwareNavigation;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action.BreakFenceBehaviour;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action.DinosaurLookAtTarget;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action.GettingUpFromRestBehaviour;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.need.SoloHuntRoamBehaviour;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.need.SoloHuntingBehaviour;
import net.dumbcode.projectnublar.entity.dinosaur.ai.task.*;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.dinosaur.ai.sensor.NearestWaterSourceSensor;
import net.dumbcode.projectnublar.registry.*;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowParent;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.NearbyBlocksSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearestItemSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

import static net.dumbcode.projectnublar.util.DinoAnimationUtils.IS_ROARING_STATE;

/**
 * Base class of every dinosaur entity: genetics ({@link DinoData}), datapack behaviour profile,
 * needs (hunger/thirst/stamina/social), breeding, growth stages, multipart support, and the
 * SmartBrainLib brain definition.
 *
 * <p><b>Frozen invariants:</b> the {@link EntityDataAccessor} declarations below must stay in
 * this class and in this order (their ids depend on class-load order together with
 * {@code DinoAnimationUtils} and {@code DinoNeedsUtils}); the {@code defineSynchedData} call
 * order is a network contract; every NBT tag name is a save contract; all numeric constants are
 * behavioral contracts.
 *
 * <p>Note: {@link #getLayers()} calls into the client-only {@code CommonClientClass}
 * (pre-existing client/server mixing).
 */
public abstract class Dinosaur extends TamableAnimal implements FossilRevived, GeoEntity, SmartBrainOwner<Dinosaur>,GeoAnimatable{

    // Growth-stage age thresholds (age counts up from negative to 0 = adult).
    public static final int BABY_MAX_AGE = -18000;
    public static final int JUVENILE_MAX_AGE = -12000;
    public static final int SUB_ADULT_MAX_AGE = -6000;

    // Breeding cooldown starts at 500, retries above a 50% roll, resets at 2000.
    private static final int INITIAL_BREEDING_COOLDOWN = 500;
    private static final int BREEDING_COOLDOWN_RESET = 2000;
    private static final int BREEDING_ATTEMPT_THRESHOLD = 50;

    // Need-ticking intervals/amounts.
    private static final int STAMINA_DRAIN_INTERVAL_TICKS = 20;
    private static final int REST_RECOVERY_INTERVAL_TICKS = 20;
    private static final float REST_STAMINA_RECOVERY = 10F;
    private static final int SOCIAL_DRAIN_INTERVAL_TICKS = 300;
    private static final long DAY_LENGTH = 24000L;
    private static final long NOCTURNAL_ACTIVE_START = 12000;
    private static final long NOCTURNAL_ACTIVE_END = 23999;
    private static final long DIURNAL_ACTIVE_START = 0;
    private static final long DIURNAL_ACTIVE_END = 12000;

    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public static EntityDataAccessor<CompoundTag> DINO_BEHAVIOUR = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.COMPOUND_TAG);

    public static EntityDataAccessor<Optional<UUID>> DINO_FAMILY_UUID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.OPTIONAL_UUID);
    public static EntityDataAccessor<Optional<UUID>> DINO_MATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.OPTIONAL_UUID);
    public static EntityDataAccessor<Boolean> BABY_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> JUVENILE_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> SUB_ADULT_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> ADULT_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> SHOULD_TICK_STAMINA = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Vector3f> DINOSAUR_HEAD_POS = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.VECTOR3);

    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected @Nullable DinoBehaviourData cachedBehaviourData;
    private DinoDietData dietData;
    public  DinosaurPart head;
    public @Nullable Vec3 headBonePos;
    private int staminaDrainTick;
    private int lastEatTime;
    private int daysSincelastAte;
    private int lastDrinkTime;
    private int daysSincelastDrink;
    private boolean eatenToday;
    private int socialDrainTick;
    private int breedingCoolDown = INITIAL_BREEDING_COOLDOWN;
    private int flinchAnimLength;
    public DinosaurPart[] subEntities;
    private int cachedDayTime;
    public boolean isNewDay;
    List<Long> hungerSchedule = new ArrayList<>();
    List<Long> thirstSchedule = new ArrayList<>();

    public Dinosaur(EntityType<? extends Dinosaur> $$0, Level $$1, int flinchAnimLength) {
        super($$0, $$1);
        this.flinchAnimLength = flinchAnimLength;
    }
    //ANIMATION
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkController(this));
        controllers.add(new AnimationController<GeoAnimatable>(this, "dino_controller",0,this::animationPredicate));
        controllers.add(new AnimationController<GeoAnimatable>(this, "dino_secondary_Controller",0,this::animationPredicateAmbient));
    }
    private <T extends GeoAnimatable> PlayState animationPredicateAmbient(AnimationState<T> state) {

    return PlayState.STOP;

    }

    // TODO(BUG): overridden empty (original intent: "stop the game despawning after death"), so
    // dead dinosaurs never complete the vanilla death pipeline (death timer, removal, poof).
    @Override
    protected void tickDeath() {
    }

    public List<DinoLayer> getLayers() {
        return CommonClientClass.getDinoLayers(this.getType());
    };

    public boolean isRunning(){
        return this.entityData.get(DinoAnimationUtils.IS_RUNNING_STATE);
    }
    public boolean isFlinching(){
        return this.entityData.get(DinoAnimationUtils.IS_FLINCHING_STATE);
    }

    public void resetParts(float scale) {}
    public void removeParts() {}
    public void updateParts(){}
    public void updatePart(@Nullable final DinosaurPart part, @NotNull final Dinosaur parent) {}

    private <T extends GeoAnimatable> PlayState animationPredicate(AnimationState<T> state) {
        if(this.isDeadOrDying()){
            return state.setAndContinue(DinoAnimationUtils.DEAD_ANIM);
        }
        if(this.isFlinching()){
            return state.setAndContinue(DinoAnimationUtils.FLINCH_ANIM);
        }

        /// TO-DO: Fix running animation

        if(!this.isResting() && !this.shouldTickStamina() && (this.isRunning() || this.isSwimming() || state.isMoving())){
            this.entityData.set(SHOULD_TICK_STAMINA, true);
        } else if (this.shouldTickStamina() && !state.isMoving() && !this.isRunning() && !this.isSwimming()) {
            this.entityData.set(SHOULD_TICK_STAMINA, false);
        }
        if(this.isRoaring()){
            return state.setAndContinue(DinoAnimationUtils.ROARING_ANIM);
        }

        if(this.isAttacking()){
            return state.setAndContinue(DinoAnimationUtils.ATTACK_ANIM);
        }
        if(this.isSitting()){
            return state.setAndContinue(DinoAnimationUtils.REST_ANIM);
        }
        if(this.isRising()){
            return state.setAndContinue(DinoAnimationUtils.GETTING_UP_ANIM);
        }
        if(this.isResting()) {
            return state.setAndContinue(DinoAnimationUtils.REST_IDLE_ANIM);
        }
        if(this.isDrinking()) {
            return state.setAndContinue(DinoAnimationUtils.DRINKING_ANIM);
        }
        if (this.isEating()) {
            return  state.setAndContinue(DinoAnimationUtils.EATING_ANIM);
        }
        if (this.isInWater() && state.isMoving()){
            return state.setAndContinue(DinoAnimationUtils.SWIM_ANIM);
        }
        if(!state.isMoving() && this.isIdle()){
            return state.setAndContinue(DinoAnimationUtils.IDLE_ANIM);
        }

        return PlayState.STOP;
    }

    //MAIN DATA GETTERS
    public DinoData getDinoData() {
        return this.entityData.get(DINO_DATA);
    }

    public DinoBehaviourData getDinoBehaviour(){
        if(this.cachedBehaviourData == null){
            this.cachedBehaviourData = DinoBehaviourData.fromNBT(this.entityData.get(DINO_BEHAVIOUR));
        }
        return this.cachedBehaviourData;
    }

    public DinoDietData getDinoDiet(){
        if(this.dietData == null){
            this.dietData = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        }
        return this.dietData;
    }
    //DATA SYNC
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DINO_DATA, new DinoData());
        this.entityData.define(DINO_BEHAVIOUR, new CompoundTag());
        this.entityData.define(DINO_FAMILY_UUID, Optional.empty());
        this.entityData.define(DINO_MATE, Optional.empty());

        this.entityData.define(DinoNeedsUtils.HUNGER, 100.0F);
        this.entityData.define(DinoNeedsUtils.THIRST, 100.0F);
        this.entityData.define(DinoNeedsUtils.STAMINA, 100.0F);
        this.entityData.define(DinoNeedsUtils.SOCIAL, 100.0F);
        this.entityData.define(DinoNeedsUtils.AGGRESSION, 100.0F);
        this.entityData.define(DinoNeedsUtils.DOMESTICITY, 100.0F);
        this.entityData.define(DinoNeedsUtils.FERTILITY, 100.0F);
        this.entityData.define(DinoNeedsUtils.IMMUNITY, 100.0F);
        this.entityData.define(DinoNeedsUtils.INTELLIGENCE, 100.0F);
        this.entityData.define(DinoNeedsUtils.SIZE, 1F);
        this.entityData.define(DinoNeedsUtils.TAMING_SCORE, 100.0F);
        this.entityData.define(DinoNeedsUtils.VISION, 100.0F);

        this.entityData.define(DINOSAUR_HEAD_POS, new Vector3f());

        this.entityData.define(BABY_DATA_ID, false);
        this.entityData.define(JUVENILE_DATA_ID, false);
        this.entityData.define(SUB_ADULT_DATA_ID, false);
        this.entityData.define(ADULT_DATA_ID, true);
        this.entityData.define(SHOULD_TICK_STAMINA, true);

        this.entityData.define(DinoAnimationUtils.IS_EATING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_DRINKING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_NESTING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_RESTING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_RISING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_SITTING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_ATTACKING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_FLINCHING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_DEAD_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_SWIMMING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_RUNNING_STATE, false);
        this.entityData.define(DinoAnimationUtils.LOOKING_LEFT_STATE, false);
        this.entityData.define(DinoAnimationUtils.LOOKING_RIGHT_STATE, false);
        this.entityData.define(DinoAnimationUtils.TURNING_RIGHT_STATE, false);
        this.entityData.define(DinoAnimationUtils.TURNING_LEFT_STATE, false);
        this.entityData.define(IS_ROARING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_SPEAKING_STATE, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("dino_data", this.getDinoData().toNBT());
        tag.put("behaviour_profile", this.entityData.get(DINO_BEHAVIOUR));
        tag.putFloat("hunger_bar", this.entityData.get(DinoNeedsUtils.HUNGER));
        tag.putFloat("thirst_bar", this.entityData.get(DinoNeedsUtils.THIRST));
        tag.putFloat("stamina_bar", this.entityData.get(DinoNeedsUtils.STAMINA));
        tag.putFloat("social_bar", this.entityData.get(DinoNeedsUtils.SOCIAL));
        tag.putFloat("trust_threshold", this.entityData.get(DinoNeedsUtils.TAMING_SCORE));
        tag.putFloat("dino_vision", this.entityData.get(DinoNeedsUtils.VISION));
        tag.putFloat("dino_aggression", this.entityData.get(DinoNeedsUtils.AGGRESSION));
        tag.putFloat("dino_fertility", this.entityData.get(DinoNeedsUtils.FERTILITY));
        tag.putFloat("dino_domesticity", this.entityData.get(DinoNeedsUtils.DOMESTICITY));
        tag.putFloat("dino_size", this.entityData.get(DinoNeedsUtils.SIZE));
        tag.putFloat("dino_intelligence", this.entityData.get(DinoNeedsUtils.INTELLIGENCE));
        tag.putFloat("dino_immunity", this.entityData.get(DinoNeedsUtils.IMMUNITY));
        tag.putBoolean("baby_age_boolean", this.entityData.get(BABY_DATA_ID));
        tag.putBoolean("juvenile_age_boolean", this.entityData.get(JUVENILE_DATA_ID));
        tag.putBoolean("sub_adult_age_boolean", this.entityData.get(SUB_ADULT_DATA_ID));
        tag.putBoolean("adult_age_boolean", this.entityData.get(ADULT_DATA_ID));


        if(headBonePos != null){
            tag.putDouble("headx",headBonePos.x);
            tag.putDouble("heady",headBonePos.y);
            tag.putDouble("headz",headBonePos.z);
        }

        if(this.entityData.get(DINO_MATE).isPresent()) {
            tag.putUUID("mate_uuid",this.entityData.get(DINO_MATE).get());
        }
        if(this.entityData.get(DINO_FAMILY_UUID).isPresent()) {
            tag.putUUID("family_uuid",this.entityData.get(DINO_FAMILY_UUID).get());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pTag) {
        super.readAdditionalSaveData(pTag);
        entityData.set(DINO_DATA, DinoData.fromNBT(pTag.getCompound("dino_data")));
        this.entityData.set(DINO_BEHAVIOUR, pTag.getCompound("behaviour_profile"));
        this.entityData.set(DinoNeedsUtils.HUNGER, pTag.getFloat("hunger_bar"));
        this.entityData.set(DinoNeedsUtils.THIRST, pTag.getFloat("thirst_bar"));
        this.entityData.set(DinoNeedsUtils.STAMINA, pTag.getFloat("stamina_bar"));
        this.entityData.set(DinoNeedsUtils.SOCIAL, pTag.getFloat("social_bar"));
        // TODO(BUG): all four age flags are read from "baby_age_boolean"; the dedicated
        // juvenile/sub_adult/adult keys are written in addAdditionalSaveData but never read.
        this.entityData.set(BABY_DATA_ID, pTag.getBoolean("baby_age_boolean"));
        this.entityData.set(JUVENILE_DATA_ID, pTag.getBoolean("baby_age_boolean"));
        this.entityData.set(SUB_ADULT_DATA_ID, pTag.getBoolean("baby_age_boolean"));
        this.entityData.set(ADULT_DATA_ID, pTag.getBoolean("baby_age_boolean"));
        this.entityData.set(DinoNeedsUtils.TAMING_SCORE,pTag.getFloat("trust_threshold"));
        this.entityData.set(DinoNeedsUtils.VISION,pTag.getFloat("dino_vision"));
        this.entityData.set(DinoNeedsUtils.AGGRESSION,pTag.getFloat("dino_aggression"));
        this.entityData.set(DinoNeedsUtils.FERTILITY,pTag.getFloat("dino_fertility"));
        this.entityData.set(DinoNeedsUtils.DOMESTICITY,pTag.getFloat("dino_domesticity"));
        this.entityData.set(DinoNeedsUtils.SIZE,pTag.getFloat("dino_size"));
        this.entityData.set(DinoNeedsUtils.INTELLIGENCE,pTag.getFloat("dino_intelligence"));
        this.entityData.set(DinoNeedsUtils.IMMUNITY,pTag.getFloat("dino_immunity"));

        if(pTag.contains("headx") && pTag.contains("heady") && pTag.contains("headz")){
           headBonePos = new Vec3(pTag.getDouble("headx"),pTag.getDouble("heady"),pTag.getDouble("headz"));
        }

        if(pTag.contains("mate_uuid")){
            Optional<UUID> mate_uuid = Optional.of(pTag.getUUID("mate_uuid"));
            this.entityData.set(DINO_MATE, mate_uuid);
        } else this.entityData.set(DINO_MATE, Optional.empty());

        if(pTag.contains("family_uuid")){
            Optional<UUID> mate_uuid = Optional.of(pTag.getUUID("family_uuid"));
            this.entityData.set(DINO_FAMILY_UUID, mate_uuid);
        } else this.entityData.set(DINO_FAMILY_UUID, Optional.empty());

    }

    public void setHeadPositon(Vec3 worldPos){
      Vector3f pos = worldPos.toVector3f();
      this.entityData.set(DINOSAUR_HEAD_POS, pos);
    }
    public @Nullable Vec3 getHeadBonePos(){
        return new Vec3(this.entityData.get(DINOSAUR_HEAD_POS));
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    //MAIN DATA SETTERS
    public void setDinoData(DinoData dinoData) {
        this.entityData.set(DINO_DATA, dinoData);
    }

    public void setDinoBehaviour(CompoundTag behaviourData){
        this.entityData.set(DINO_BEHAVIOUR, behaviourData);
    }

    //TARGETING BOOLEANS FOR BRAIN
    public boolean canTargetWaterSource(BlockState entity){
        if(this.isSleeping()){
            return false;
        }

           return entity.is(Blocks.WATER);
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        if(this.isSleeping()){
            return false;
        }
        DinoDietData validfood = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        return validfood.foodMap().containsKey(stack.getDescriptionId());
    }

    // TODO(BUG): always applies a generic damage source, discarding the real source (attacker,
    // armor bypass flags, etc.) passed in by the part entity.
    public boolean hurtFromPart(DinosaurPart part, DamageSource source, float amount) {
        return this.hurt(this.damageSources().generic(), amount);
    }

    public boolean canTarget(LivingEntity target) {
       return target.getVehicle() != this;
    }

    public boolean isChild(){
        return this.isBaby() || this.isJuvanile();
    }

    public boolean isHuntingBlocked(){
        return BrainUtils.hasMemory(this, MemoryModuleTypeInit.IS_RESTING.get()) ||BrainUtils.hasMemory(this, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM) || BrainUtils.hasMemory(this, MemoryModuleTypeInit.IS_EATING.get()) ||
                BrainUtils.hasMemory(this, MemoryModuleTypeInit.IS_DRINKING.get());
    }

    public boolean canTargetFoodItem(ItemEntity target) {
        if(this.isSleeping()){
            return false;
        }
        if(target.getItem().is(Items.AIR)){
            return false;
        }
        if(this.getDinoDiet() == null){
            return false;
        }
        return this.getDinoDiet().foodMap().containsKey(target.getItem().getDescriptionId());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        DinoDietData validfood = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        return validfood.foodMap().containsKey(stack.getDescriptionId());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FenceAwareNavigation(this,level);
    }


    @Override
    public int getMaxHeadYRot() {
        return 2;
    }

    @Override
    public int getHeadRotSpeed() {
        return 2;
    }

    public boolean hasWantedLookTarget(){
        double x;
        double z;

        if(this.getLookControl() != null) {
            x = this.getLookControl().getWantedX();
            z = this.getLookControl().getWantedZ();
        } else{
            x = 0.0;
            z = 0.0;
        }
    return x != 0.0 && z != 0.0;
    }


    @Override
    public void setYRot(float yRot) {
        float currentYaw = super.getYRot();
        float delta = Mth.wrapDegrees(yRot - currentYaw);

        float maxTurn = 10.0F;

            if (delta > maxTurn) {
                delta = maxTurn;
                super.setYRot(currentYaw + delta);
            } else if (delta < -maxTurn) {
                delta = -maxTurn;
                super.setYRot(currentYaw + delta);
            } else {
                super.setYRot(yRot);
            }
    }
    //BRAIN

    @Override
    protected Brain.Provider<Dinosaur> brainProvider() {
        return new SmartBrainProvider<>(this);
    }



    @Override
    public double getMeleeAttackRangeSqr(LivingEntity entity) {
        return (double)(this.getBbWidth() * 3.0F * this.getBbWidth() * 3.0F + entity.getBbWidth());
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.tickBrain(this);

    }

    @Override
    public List<? extends ExtendedSensor<? extends Dinosaur>> getSensors() {
        NearestWaterSourceSensor<Dinosaur> waterSourceSensor = new NearestWaterSourceSensor<>();
        waterSourceSensor.setPredicate((block, dinosaur) -> dinosaur.canTargetWaterSource(block));
        waterSourceSensor.setRadius(20);
        NearbyBlocksSensor<Dinosaur> fenceProximinitySensor = new NearbyBlocksSensor<>();
        fenceProximinitySensor.setRadius(10.0);
        fenceProximinitySensor.setPredicate((block, dinosaur) -> block.is(BlockInit.ELECTRIC_FENCE.get()) && DinoNeedsUtils.starving(dinosaur));
        NearestItemSensor<Dinosaur> foodItemSensor = new NearestItemSensor<>();
        foodItemSensor.setPredicate((item, dinosaur) -> dinosaur.canTargetFoodItem(item)) ;
        foodItemSensor.setRadius(20);
        NearbyLivingEntitySensor<Dinosaur> nearbyLivingEntitySensor = new NearbyLivingEntitySensor<>();
        return List.of(
                waterSourceSensor,
                nearbyLivingEntitySensor,
                foodItemSensor,
                fenceProximinitySensor
        );
    }


    @Override
    public BrainActivityGroup<? extends Dinosaur> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new DinosaurLookAtTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dinosaur) && (dinosaur.isResting() || dinosaur.isDrinking() || dinosaur.isDeadOrDying())),
                // TODO(DEAD): ThreatDisplay(34) was commented out of the core tasks ("needs to be
                // made more situational"); intentionally left unwired.
                new MoveToWalkTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dinosaur) && (dinosaur.isResting() || dinosaur.isDrinking() || dinosaur.isDeadOrDying())) ,
                new SetHunting<>(),
                new SetWalkTargetToWaterSource<>().closeEnoughWhen((entity, pos)-> 3),
                new SetWalkTargetToFoodItem<>().predicate(Dinosaur::canTargetFoodItem),
                new FollowParent<>().parentPredicate((baby, parent)-> baby instanceof Dinosaur dino && dino.isFamily(parent) && !parent.isBaby())
        );
    }


    @Override
    public BrainActivityGroup<? extends Dinosaur> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour(
                        // TODO(DEAD): a Panic behaviour was commented out here; left unwired.
                        new Drink<>(100)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"drink",true))
                                .whenStopping(dinosaur ->  DinoAnimationUtils.setAnimationState(dinosaur,"drink",false)),
                        new Eat<>(69)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"eat",true))
                                .whenStopping(dinosaur ->  DinoAnimationUtils.setAnimationState(dinosaur,"eat",false)),
                        new Rest<>(69)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"sit",true))
                                .whenStopping(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"rest",false)),
                        new GettingUpFromRestBehaviour<>(69)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"getup",true))
                                .whenStopping(dinosaur ->DinoAnimationUtils.setAnimationState(dinosaur,"getup",false)),
                       new SoloHuntingBehaviour<>()
                                .attackablePredicate(this::canTarget)
                                .startCondition(dinosaur -> dinosaur instanceof CarnivoreDinosaur),
                        new SoloHuntRoamBehaviour<>()
                                .dontAvoidWater()
                                .setRadius(40.0D)
                                .stopIf(dino -> BrainUtils.hasMemory(dino, MemoryModuleType.ATTACK_TARGET)),
                new OneRandomBehaviour<>(
                        new SetRandomWalkTarget<>().dontAvoidWater().setRadius(10.0, 4.0).walkTargetPredicate((dinosaur, pos)-> !BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.BRAIN_OVERRIDE.get()))
                )));
    }

    @Override
    public BrainActivityGroup<? extends Dinosaur> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>()
                        .invalidateIf((entity, target) -> (target instanceof Player pl && (pl.isCreative() || pl.isSpectator())) || target.isDeadOrDying()),
                new SetWalkTargetToAttackTarget<>().speedMod((owner, target) -> 1.5f)
                        .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState((Dinosaur) dinosaur,"run",true)),
                new BreakFenceBehaviour<>(20),
                new AnimatableMeleeAttack<>(20)
                        .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState((Dinosaur) dinosaur,"attack",true))
                        .whenStopping(dinosaur -> DinoAnimationUtils.setAnimationState((Dinosaur) dinosaur,"attack",false))
        );
    }
    private int flinchAnimTicks;
    private int restTicks;

    /**
     * The server-side tick is a fixed sequence of steps; the call order below matches the
     * original inline code exactly and must not change.
     */
    @Override
    public void tick() {
        super.tick();

        tickFlinchAnimation();

        if(!level().isClientSide() && !this.isDeadOrDying()) {
            // TODO(BUG): the socialDrainTick++ increment was commented out in the original, so
            // the social-drain branch below never fires.
            if(this.isResting()) {
                this.restTicks++;
            }
            if(this.shouldTickStamina()) {
                this.staminaDrainTick++;
            }

            syncGrowthStageFlags();
            tickBreedingCooldown();
            tickStaminaDrain();
            tickRestRecovery();
            tickSocialDrain();
            detectNewDay();
            if(this.isNewDay){
                processNewDay();
            }
            processThirstSchedule();
            processHungerSchedule();
            if(!DinoNeedsUtils.isHungry(this) && BrainUtils.hasMemory(this, MemoryModuleTypeInit.HUNTING.get())){
                BrainUtils.clearMemory(this, MemoryModuleTypeInit.HUNTING.get());
            }
        }
    }

    /** Counts down the flinch animation and clears the flag when it has played out. */
    private void tickFlinchAnimation() {
        if(this.entityData.get(DinoAnimationUtils.IS_FLINCHING_STATE)){
            flinchAnimTicks++;
            if(flinchAnimTicks > this.flinchAnimLength){
                this.entityData.set(DinoAnimationUtils.IS_FLINCHING_STATE, false);
                flinchAnimTicks = 0;
            }
        }
    }

    /** Mirrors the age-derived growth stage into the synched boolean flags. */
    private void syncGrowthStageFlags() {
        if(this.isBaby() && !this.entityData.get(BABY_DATA_ID)){
            this.entityData.set(BABY_DATA_ID, true);
        } else if (!this.isBaby() && this.entityData.get(BABY_DATA_ID)){
            this.entityData.set(BABY_DATA_ID, false);
        }
        if(this.isJuvanile() && !this.entityData.get(JUVENILE_DATA_ID)){
            this.entityData.set(JUVENILE_DATA_ID, true);
        } else if (!this.isJuvanile() && this.entityData.get(JUVENILE_DATA_ID)){
            this.entityData.set(JUVENILE_DATA_ID, false);
        }
        if(this.isSubAdult() && !this.entityData.get(SUB_ADULT_DATA_ID)){
            this.entityData.set(SUB_ADULT_DATA_ID, true);
        } else if (!this.isSubAdult() && this.entityData.get(SUB_ADULT_DATA_ID)){
            this.entityData.set(SUB_ADULT_DATA_ID, false);
        }
        if(this.age >= 0 && !this.entityData.get(ADULT_DATA_ID)){
            this.entityData.set(ADULT_DATA_ID, true);
        } else if (this.age < 0 && this.entityData.get(ADULT_DATA_ID)){
            this.entityData.set(ADULT_DATA_ID, false);
        }
    }

    /**
     * At cooldown 0 the (gender == 1) partner with a mate attempts breeding; the counter then
     * counts up to 2000 before resetting to 0.
     *
     * <p>TODO(BUG): the initiator check uses {@code getDinoGender() == 1.0F} while other code
     * treats 2.0 as male and offspring can only roll gender 1.
     */
    private void tickBreedingCooldown() {
        if(this.breedingCoolDown == 0){
            if(this.getDinoGender() == 1.0F && this.hasMate()) {
                this.tryBreedWithMate();
                this.breedingCoolDown++;
            }
        }
        if(this.breedingCoolDown >= 1){
            this.breedingCoolDown++;
        }
        if(this.breedingCoolDown > BREEDING_COOLDOWN_RESET){
            this.breedingCoolDown = 0;
        }
    }

    private void tickStaminaDrain() {
        if(this.staminaDrainTick >= STAMINA_DRAIN_INTERVAL_TICKS && this.shouldTickStamina()){
            DinoNeedsUtils.tickStamina(this);
            if(DinoNeedsUtils.isTired(this) && !BrainUtils.hasMemory(this, MemoryModuleTypeInit.IS_TIRED.get())){
                BrainUtils.setMemory(this, MemoryModuleTypeInit.IS_TIRED.get(), true);
            }
            this.staminaDrainTick = 0;
        }
    }

    private void tickRestRecovery() {
        if(this.isResting() && restTicks >= REST_RECOVERY_INTERVAL_TICKS){
            if(DinoNeedsUtils.getMaxStamina(this) > DinoNeedsUtils.getCurrentStamina(this)) {
                float stamina = DinoNeedsUtils.getCurrentStamina(this);
                float newStamina = stamina + REST_STAMINA_RECOVERY;
                DinoNeedsUtils.setCurrentStamina(this, newStamina);
            }
            restTicks = 0;
        }
    }

    // TODO(DEAD): dead branch - socialDrainTick is never incremented (see tick()) and
    // DinoNeedsUtils.tickSocial() is an empty stub.
    private void tickSocialDrain() {
        if(this.socialDrainTick >= SOCIAL_DRAIN_INTERVAL_TICKS){
            DinoNeedsUtils.tickSocial();
            this.socialDrainTick = 0;
        }
    }

    private void detectNewDay() {
        if(cachedDayTime != (int) this.level().getDayTime() / DAY_LENGTH){
            cachedDayTime = (int) (this.level().getDayTime() / DAY_LENGTH);
            this.isNewDay = true;
        }
    }

    /**
     * Day rollover: bumps the days-without-food/water counters, resets the daily memories,
     * applies one hunger/thirst tick, and rolls a fresh random hunger/thirst schedule inside the
     * species' active hours.
     */
    private void processNewDay() {
        boolean eatenToday = Boolean.TRUE.equals(BrainUtils.getMemory(this, MemoryModuleTypeInit.EATEN_TODAY.get()));
        boolean drankToday = Boolean.TRUE.equals(BrainUtils.getMemory(this, MemoryModuleTypeInit.DRANK_TODAY.get()));
        if(!eatenToday){
            if(BrainUtils.hasMemory(this,MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get())) {
                daysSincelastAte = BrainUtils.getMemory(this,MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get());
            } else { daysSincelastAte = 0; }
            daysSincelastAte++;

            BrainUtils.setMemory(this, MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get(), daysSincelastAte);
        }
        if(!drankToday){
            if(BrainUtils.hasMemory(this,MemoryModuleTypeInit.DAYS_SINCE_LAST_DRANK.get())) {
                daysSincelastDrink = BrainUtils.getMemory(this,MemoryModuleTypeInit.DAYS_SINCE_LAST_DRANK.get());
            } else { daysSincelastDrink = 0;}

            daysSincelastDrink++;

            BrainUtils.setMemory(this, MemoryModuleTypeInit.DAYS_SINCE_LAST_DRANK.get(), daysSincelastDrink);
        }

        this.isNewDay = false;
        BrainUtils.setMemory(this,MemoryModuleTypeInit.MEAL_COUNTER.get(), 0);
        BrainUtils.setMemory(this, MemoryModuleTypeInit.DRANK_TODAY.get(), false);
        BrainUtils.setMemory(this, MemoryModuleTypeInit.EATEN_TODAY.get(), false);
        DinoNeedsUtils.tickThirst(this,this.daysSincelastDrink,this.getDinoBehaviour().dehydrationLimit());
        DinoNeedsUtils.tickHunger(this,this.daysSincelastAte,this.getDinoBehaviour().starvationLimit());

        long activeStart;
        long activeEnd;

        if(this.getDinoBehaviour().isNocturnal()){
            activeStart = NOCTURNAL_ACTIVE_START;
            activeEnd = NOCTURNAL_ACTIVE_END;
        } else {
            activeStart = DIURNAL_ACTIVE_START;
            activeEnd = DIURNAL_ACTIVE_END;
        }
        Random random = new Random();

        for (int i = 0; i < this.getDinoBehaviour().eatRate() - 1; i++) {
            long hungerTime = random.nextInt((int)activeStart,(int) activeEnd);
            hungerSchedule.add(hungerTime);
        }
        for (int i = 0; i < this.getDinoBehaviour().drinkRate() - 1; i++) {
            long thirstTime = random.nextInt((int)activeStart,(int) activeEnd);
            thirstSchedule.add(thirstTime);
        }
    }

    /**
     * TODO(BUG): only one due entry is removed per tick (the last matching index), even though
     * every due entry triggers a thirst tick on each pass. Same for hunger.
     */
    private void processThirstSchedule() {
        if(!thirstSchedule.isEmpty()) {
            int toRemove = -1;
            int i = 0;

            for (long thirstTime : thirstSchedule) {
                if (this.level().getDayTime() % DAY_LENGTH >= thirstTime) {
                    toRemove = i;
                    DinoNeedsUtils.tickThirst(this, this.daysSincelastDrink, this.getDinoBehaviour().dehydrationLimit());
                }
                i++;
            }
            if(toRemove != -1) {
                thirstSchedule.remove(toRemove);
            }
        }
    }

    private void processHungerSchedule() {
        if(!hungerSchedule.isEmpty()) {
            int toRemove = -1;
            int i = 0;

            for (long hungerTime : hungerSchedule) {
                if (this.level().getDayTime() % DAY_LENGTH >= hungerTime) {
                    toRemove = i;
                    DinoNeedsUtils.tickHunger(this, this.daysSincelastAte, this.getDinoBehaviour().starvationLimit());
                }
                i++;
            }
            if(toRemove != -1) {
                hungerSchedule.remove(toRemove);
            }
        }
    }

    Random random = new Random();

    /** Rolls 0–99; above {@value #BREEDING_ATTEMPT_THRESHOLD} both partners enter love mode. */
    public void tryBreedWithMate(){
        int attempt = random.nextInt(0,100);
        if(attempt > BREEDING_ATTEMPT_THRESHOLD) {
            this.setInLove(null);
            @Nullable Dinosaur mate = BrainUtils.getMemory(this, MemoryModuleTypeInit.MATE.get());

            if(mate != null) {
                mate.setInLove(null);
            } else {
                this.clearDinoMate();
            }
        }
    }

    public boolean isNight(){
        int day = (int) this.level().getDayTime() % 24000;
        return day > 12000;
    }
    public boolean isDay(){
        return !this.isNight() ;
    }


    //ATTRIBUTES
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35)
                .add(Attributes.MOVEMENT_SPEED, .25)
                .add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                .add(AttributesInit.DINO_ENERGY_NEED.get(),100)
                .add(AttributesInit.DINO_THIRST_NEED.get(), 100)
                .add(AttributesInit.DINO_HUNGER_NEED.get(),100)
                .add(AttributesInit.DINO_SOCIAL_NEED.get(),100)
                .add(AttributesInit.TRUST_SCORE.get(),1000)
                .add(AttributesInit.DINO_VISION.get(),100)
                .add(AttributesInit.DINO_AGGRESSION.get(), 0)
                .add(AttributesInit.DINO_INTELLIGENCE.get(), 50)
                .add(AttributesInit.DINO_FERTILITY.get(),50)
                .add(AttributesInit.DINO_IMMUNITY.get(),0);
    }

    public boolean isDrinking() {
        return this.entityData.get(DinoAnimationUtils.IS_DRINKING_STATE);
    }
    public boolean isEating() {return this.entityData.get(DinoAnimationUtils.IS_EATING_STATE);}
    public boolean isSitting() {return this.entityData.get(DinoAnimationUtils.IS_SITTING_STATE);}
    public boolean isRising() {
        return this.entityData.get(DinoAnimationUtils.IS_RISING_STATE);
    }
    public boolean isResting() {
        return this.entityData.get(DinoAnimationUtils.IS_RESTING_STATE);
    }
    public boolean isRoaring() {
        return this.entityData.get(IS_ROARING_STATE);
    }
    public boolean isAttacking() {return this.entityData.get(DinoAnimationUtils.IS_ATTACKING_STATE);}
    public boolean isIdle(){return !this.isDrinking() && !this.isEating() && !this.isResting() && !this.isRoaring() && !this.isAttacking();}

    /**
     * Gets the gender; if none has been set, returns female.
     *
     * <p>TODO(BUG): gender encoding is inconsistent across the codebase: here 2.0 = male, the
     * breeding initiator checks {@code == 1.0F}, mother selection checks {@code == 1}, T-Rex
     * male layers check {@code == 2D}, and offspring can only ever roll gender 1.
     */
    public String getStringDinoGender() {
        double geneGender = this.getDinoData().getGeneValue(GeneInit.GENDER.get());
        if(geneGender == 2.0D){
            return "male";
        } else return "female";
    }
    public double getDinoGender(){
      return this.getDinoData().getGeneValue(GeneInit.GENDER.get());
    }
    // TODO(BUG): nothing ever populates the PLAYER_REPUTATION memory, and the mutators below
    // return early when the player is *not* in the map - so reputation is permanently inert.
    @Nullable
    public Map<Player,Integer> getPlayerReputationMap(){
        if(BrainUtils.hasMemory(this, MemoryModuleTypeInit.PLAYER_REPUTATION.get())){
            return BrainUtils.getMemory(this, MemoryModuleTypeInit.PLAYER_REPUTATION.get());
        } else return null;
    }

    public int getReputationForPlayer(Player player){
        if(this.getPlayerReputationMap() != null){
            return this.getPlayerReputationMap().getOrDefault(player, 0);
        } else return 0;
    }
    public void increaseReputationForPlayer(Player player, int increase){
        if(this.getPlayerReputationMap() == null || !this.getPlayerReputationMap().containsKey(player)){
            return;
        }
        Map<Player,Integer> reputationMap = this.getPlayerReputationMap();
        int currentReputation = reputationMap.get(player);
        reputationMap.remove(player);
        reputationMap.put(player,currentReputation + increase);
    }
    public void decreaseReputationForPlayer(Player player, int decrease){
        if(this.getPlayerReputationMap() == null || !this.getPlayerReputationMap().containsKey(player)){
            return;
        }
        Map<Player,Integer> reputationMap = this.getPlayerReputationMap();
        int currentReputation = reputationMap.get(player);
        reputationMap.remove(player);
        reputationMap.put(player,currentReputation - decrease);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        EntityType<?> babyType = this.getDinoData().getBaseDino();
        return (Dinosaur) babyType.create(serverLevel);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        @Nullable Dinosaur dinosaur = (Dinosaur) this.getBreedOffspring(level, mate);
        Dinosaur mother;
        // TODO(BUG): part of the inconsistent gender encoding (see getStringDinoGender).
        if(this.getDinoGender() == 1){
            mother = this;
        } else mother = (Dinosaur) mate;

        if(mate instanceof Dinosaur) {
            if (dinosaur != null) {
                dinosaur.setDinoData(mother.getDinoData());
                DinoNeedsUtils.setDinoBaseNeeds(dinosaur, mother.getDinoBehaviour());
                dinosaur.setDinoBehaviour(mother.getDinoBehaviour().toNBT(mother.getDinoBehaviour()));
                // TODO(BUG): nextInt(1, 2) can only ever return 1, so every offspring gets the
                // same gender value.
                dinosaur.getDinoData().setGeneValue(GeneInit.GENDER.get(), random.nextInt(1,2));
                dinosaur.setBaby(true);
                dinosaur.setDinoFamilyUuid(this.getFamilyId());
                dinosaur.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                this.finalizeSpawnChildFromBreeding(level, mate, dinosaur);
                level.addFreshEntityWithPassengers(dinosaur);
            }
        }
    }

    @Override
    public boolean isBaby() {
        int age = this.age;
        return age <= BABY_MAX_AGE;
    }

    public boolean isJuvanile(){
        int age = this.age;
        return age <= JUVENILE_MAX_AGE && age > BABY_MAX_AGE;
    }

    public boolean isSubAdult(){
        int age = this.age;
        return age <= SUB_ADULT_MAX_AGE && age > JUVENILE_MAX_AGE;
    }

    public int getGrowthStage(){
        if(this.entityData.get(BABY_DATA_ID)){
            return 1;
        }
        else if(this.entityData.get(JUVENILE_DATA_ID)){
            return 2;
        }
        else if(this.entityData.get(SUB_ADULT_DATA_ID)){
            return 3;

        } else return 4;
    }

    public boolean shouldTickStamina(){
        return this.entityData.get(SHOULD_TICK_STAMINA) && !this.isResting();
    }


    public void createDinosaurFamily(Dinosaur mate){
        UUID newFamilyId = UUID.randomUUID();
        this.setDinoFamilyUuid(newFamilyId);
        mate.setDinoFamilyUuid(newFamilyId);
    }
    public void setDinoFamilyUuid(UUID familyUuid){
        this.entityData.set(DINO_FAMILY_UUID,Optional.of(familyUuid));
    }
    public @Nullable UUID getFamilyId(){
        if(this.entityData.get(DINO_FAMILY_UUID).isPresent()) {
            return this.entityData.get(DINO_FAMILY_UUID).get();
        } else return null;
    }

    public @Nullable UUID getGroupId() {
        if(BrainUtils.hasMemory(this, MemoryModuleTypeInit.GROUP_UUID.get())){
            return BrainUtils.getMemory(this, MemoryModuleTypeInit.GROUP_UUID.get());
        } else return null;
    }
    public boolean hasGroup() {
        if(BrainUtils.hasMemory(this,MemoryModuleTypeInit.HAS_GROUP.get())) {
            return Boolean.TRUE.equals(BrainUtils.getMemory(this, MemoryModuleTypeInit.HAS_GROUP.get()));
        } else return false;
    }
    public boolean isGroupLeader() {
        if(BrainUtils.hasMemory(this, MemoryModuleTypeInit.IS_GROUP_LEADER.get())){
            return Boolean.TRUE.equals(BrainUtils.getMemory(this, MemoryModuleTypeInit.IS_GROUP_LEADER.get()));
        } else return false;
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return this.isMate(otherAnimal.getUUID());
    }

    @Override
    public boolean canBreed() {
        return !this.isBaby() && !this.isJuvanile() && !this.isSubAdult();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();

        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemStack.is(Items.STICK) && !(this.getLastHurtByMob() != null && this.getLastAttacker().is(player));
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else if (this.isTame()) {
            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                this.heal((float) item.getFoodProperties().getNutrition());
                return InteractionResult.SUCCESS;
            } else {
                InteractionResult interactionresult = super.mobInteract(player, hand);
                if ((!interactionresult.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget((LivingEntity) null);
                    return InteractionResult.SUCCESS;
                } else {
                    return interactionresult;
                }
            }
        } else if (itemStack.is(Items.STICK)) {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }

            if (this.random.nextInt(3) == 0) {
                this.increaseReputationForPlayer(player,10);

                if(!this.isTame() && this.getReputationForPlayer(player) > 100){
                    this.tame(player);
                }
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(false);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;


        } else {
            return super.mobInteract(player, hand);
        }
    }

    public boolean canMateWith(Dinosaur pDinosaur, Dinosaur pMate){

        if(pDinosaur.isBaby() || pMate.isBaby()){
            return false;
        }if(pDinosaur.isJuvanile() || pMate.isJuvanile()){
            return false;
        }if(pDinosaur.isSubAdult() || pMate.isSubAdult()) {
            return false;
        }
        if(!pDinosaur.isAlive() || !pMate.isAlive()){
            return false;
        }
        if(pDinosaur.hasMate() || pMate.hasMate()){
            return false;
        }

        return pDinosaur.getDinoGender() != pMate.getDinoGender();
    }

    public boolean isMate(UUID mateId){
        if(this.entityData.get(DINO_MATE).isPresent()) {
            return mateId == this.entityData.get(DINO_MATE).get();
        } else return false;
    }
    public boolean isFamily(LivingEntity pMob){
        if(pMob instanceof Dinosaur mob){
            if(mob.getFamilyId() != null && this.getFamilyId() != null){
            return mob.getFamilyId().equals(this.getFamilyId());
            } else return false;
        } else return false;
    }

    public boolean hasMate(){
        return !this.entityData.get(DINO_MATE).isEmpty();
    }
    public void registerDinoMate(UUID pMateId){
        this.entityData.set(DINO_MATE,Optional.of(pMateId));
    }
    public void clearDinoMate(){
        this.entityData.set(DINO_MATE, Optional.empty());
    }

    //SKIN SETTER
    public Color layerColor(int layer, DinoLayer dinoLayer) {
        if (dinoLayer != null && dinoLayer.getBasicLayer() == -1) {
            return Color.WHITE;
        }
        if (layer >= this.getDinoData().getLayerColors().stream().count()) {
            return new Color(Mth.floor(this.getDinoData().getLayerColor(dinoLayer.getBasicLayer())));
        }
        return new Color(Mth.floor(this.getDinoData().getLayerColor(layer)));
    }

    public @Nullable SoundEvent getRoarSound(){
        return null;
    }
    public @Nullable SoundEvent getAttackGrowlSound(){
        return null;
    }
    public @Nullable SoundEvent getAttackSound(){
        return null;
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
        if(damageSource.getEntity() instanceof Player player){
            this.decreaseReputationForPlayer(player, 20);
            if(this.isTame() && this.getOwner().is(player) && this.getReputationForPlayer(player) < 40){
                this.setTame(false);
                this.setOwnerUUID(null);
            }
        }
        DinoAnimationUtils.setAnimationState(this,"flinch", true);
        super.actuallyHurt(damageSource, damageAmount);

    }
}
