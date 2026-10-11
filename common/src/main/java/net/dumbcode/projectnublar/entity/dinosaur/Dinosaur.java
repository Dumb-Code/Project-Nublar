package net.dumbcode.projectnublar.entity.dinosaur;

import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.api.DinoDietData;
import net.dumbcode.projectnublar.block.DinosaurFeederBlock;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.entity.ai.FenceAwareNavigation;
import net.dumbcode.projectnublar.entity.ai.behaviour.actions.BreakFenceBehaviour;
import net.dumbcode.projectnublar.entity.ai.behaviour.actions.DinosaurLookAtTarget;
import net.dumbcode.projectnublar.entity.ai.behaviour.actions.GettingUpFromRestBehaviour;
import net.dumbcode.projectnublar.entity.ai.behaviour.needs.SoloHuntRoamBehaviour;
import net.dumbcode.projectnublar.entity.ai.behaviour.needs.SoloHuntingBehaviour;
import net.dumbcode.projectnublar.entity.ai.sensors.NearestFeederSensor;
import net.dumbcode.projectnublar.entity.ai.tasks.*;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.ai.sensors.NearestWaterSourceSensor;
import net.dumbcode.projectnublar.init.*;
import net.dumbcode.projectnublar.util.DinoAnimationLengths;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.ai.control.BodyRotationControl;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.ChallengeGroupLeader;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.DinoGroup;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.FollowFormation;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.HerdLeading;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.HerdNeeds;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.GroupManager;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.HerdMoveControl;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.HerdState;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.JoinGroup;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.LeaderJourney;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.RunGait;
import net.dumbcode.projectnublar.entity.ai.behaviour.social.TurnEasing;
import net.dumbcode.projectnublar.entity.ai.behaviour.actions.ThreatDisplay;
import net.dumbcode.projectnublar.entity.dinosaur.animation.AnimationChoice;
import net.dumbcode.projectnublar.entity.dinosaur.animation.DinoAnimator;
import net.dumbcode.projectnublar.entity.dinosaur.animation.DinoOneShots;
import net.dumbcode.projectnublar.entity.dinosaur.animation.OneShotAction;
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
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

import static net.dumbcode.projectnublar.util.DinoAnimationUtils.IS_ROARING_STATE;

public abstract class Dinosaur extends TamableAnimal implements FossilRevived, GeoEntity, SmartBrainOwner<Dinosaur>,GeoAnimatable{

    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public static EntityDataAccessor<CompoundTag> DINO_BEHAVIOUR = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.COMPOUND_TAG);

    public static EntityDataAccessor<Optional<UUID>> DINO_FAMILY_UUID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.OPTIONAL_UUID);
    public static EntityDataAccessor<Optional<UUID>> DINO_MATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.OPTIONAL_UUID);
    public static EntityDataAccessor<Boolean> BABY_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> JUVENILE_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> SUB_ADULT_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> ADULT_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> SHOULD_TICK_STAMINA = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    /** Mirror of {@link GroupManager#isLeader} for the client, which has no group data. Server writes it once a second. */
    public static final EntityDataAccessor<Boolean> GROUP_LEADER = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Vector3f> DINOSAUR_HEAD_POS = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.VECTOR3);
    /** The one-shot action the server plays ({@link OneShotAction#id}); one channel for call, sentry and startled (fix P10). */
    public static final EntityDataAccessor<Byte> ONE_SHOT = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BYTE);
    /**
     * The animal's herd travels: its group is in TRAVEL, the journey's pauses included (P13b, ruling 3b). Set by
     * the server each tick ({@link #customServerAiStep}); the client then plays only the main idle
     * ({@code AnimationChoice.mainIdleOnly}).
     */
    public static final EntityDataAccessor<Boolean> HERD_TRAVELS = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    /**
     * Largest change of body yaw per tick, in degrees ({@link #setYRot}): the top rate of the eased turn
     * ({@code TurnEasing}, playtest 4 fix P12b). A turn from rest starts slower (2, 4, 6, 8 degrees in its first
     * ticks, then this rate; P13b) and slows again near its end. Herd steering uses the same value, so the direction
     * it plans is the direction the body can actually turn to.
     */
    public static final float MAX_TURN_PER_TICK = 10F;
    /**
     * The {@code MOVEMENT_SPEED} attribute every dinosaur spawns with. Herd steering derives its walk floor
     * from it ({@code GroundSpeed}): the ground speed is 0.135 x modifier^2 blocks per tick at this value.
     */
    public static final double BASE_MOVEMENT_SPEED = 0.25D;

    /** Client: chooses the animations (fix P10). Before {@link #cache}: the controllers refer to it. */
    private final DinoAnimator animator = new DinoAnimator(this);
    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Server: the one-shot animation triggers (fix P10). */
    private final DinoOneShots oneShots = new DinoOneShots(this);
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
    private int breedingCoolDown = 500;
    private int flinchAnimLength;
    /** Server, eased turning ({@link #setYRot}): this tick's signed yaw change, and the tick it was made in. */
    private float turnRate;
    private int turnTick = Integer.MIN_VALUE;
    /** Server, eased turning: the rate and the yaw the current tick's turn started from. */
    private float turnRateIn;
    private float turnYawIn;
    public DinosaurPart[] subEntities;
    private int cachedDayTime;
    public boolean isNewDay;
    List<Long> hungerSchedule = new ArrayList<>();
    List<Long> thirstSchedule = new ArrayList<>();

    public Dinosaur(EntityType<? extends Dinosaur> $$0, Level $$1, int flinchAnimLength) {
        super($$0, $$1);
        this.flinchAnimLength = flinchAnimLength;
        // Walk up one-block rises like a horse. With the default 0.6, a body 2 or more blocks wide never
        // gets close enough to its next path point to trigger MoveControl's jump, so it stalls at the step.
        this.setMaxUpStep(1.0F);
        // Herd members steer round each other and never step inside a hard minimum (HerdMoveControl: context
        // steering masks and the guard). Vanilla pushing stays on as a backstop for overlaps from spawning,
        // shoves and teleports (turning it off made members stack in playtest 2).
        this.moveControl = new HerdMoveControl(this);
    }
    //ANIMATION
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Walk or run from the real ground speed (fix P10; GeckoLib's generic walk controller only walks).
        // Blends instead of snaps between animations (fix P12a).
        controllers.add(new AnimationController<GeoAnimatable>(this, "movement", DinoAnimator.MOVEMENT_TRANSITION_TICKS, this.animator::movement));
        controllers.add(new AnimationController<GeoAnimatable>(this, "dino_controller", DinoAnimator.MAIN_TRANSITION_TICKS, this.animator::main));
        controllers.add(new AnimationController<GeoAnimatable>(this, "dino_secondary_Controller",0,this::animationPredicateAmbient));
    }
    private <T extends GeoAnimatable> PlayState animationPredicateAmbient(AnimationState<T> state) {

    return PlayState.STOP;

    }

    //STOP THE GAME DESPAWNING AFTER DEATH
    @Override
    protected void tickDeath() {
    }

    /** The colour layers drawn for this animal. Depends on gender, because a species may register a separate male set. */
    public List<DinoLayer> getLayers() {
        return CommonClientClass.getDinoLayers(this.getType(), isMale());
    };

    /**
     * Whether the body moved at running speed on its last tick: the run animation's threshold ({@link RunGait},
     * without its hysteresis). Fix P10: this replaces the old running flag, which a chase set and nothing cleared.
     */
    public boolean isRunning(){
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        return RunGait.running(false, Math.sqrt(dx * dx + dz * dz), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
    }
    public boolean isFlinching(){
        return this.entityData.get(DinoAnimationUtils.IS_FLINCHING_STATE);
    }

    /**
     * How long the flinch state lasts: a positive species value, else the species' flinch animation length
     * (fix P10; the Gallimimus passes 0, which showed its flinch for one tick).
     */
    private int flinchTicks() {
        return AnimationChoice.flinchTicks(this.flinchAnimLength, DinoAnimationLengths.lengthTicks(this.getType(), AnimationChoice.FLINCH));
    }

    /** The one-shot action (call, sentry, startled) the server plays now, from the synced {@link #ONE_SHOT}. */
    public OneShotAction oneShot() {
        return OneShotAction.byId(this.entityData.get(ONE_SHOT));
    }

    /** The animal's herd travels, from the synced {@link #HERD_TRAVELS} (both sides; the server sets it). */
    public boolean herdTravels() {
        return this.entityData.get(HERD_TRAVELS);
    }

    /** Server side: the one-shot triggers and the journey-start call of this animal. */
    public DinoOneShots oneShots() {
        return this.oneShots;
    }

    public void resetParts(float scale) {}
    public void removeParts() {}
    public void updateParts(){}
    public void updatePart(@Nullable final DinosaurPart part, @NotNull final Dinosaur parent) {}

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
        this.entityData.define(GROUP_LEADER, false);
        this.entityData.define(ONE_SHOT, OneShotAction.NONE.id());
        this.entityData.define(HERD_TRAVELS, false);

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

    public boolean canTargetFeeder(BlockState target){
        return target.getBlock() instanceof DinosaurFeederBlock;
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


    /**
     * Eased turning (playtest 4 fix P12b, motion cause 3): on the server every yaw change ramps in and out
     * ({@link TurnEasing#yawStep}), at most {@link #MAX_TURN_PER_TICK} per tick, instead of a constant 10
     * degrees per tick that started and stopped hard. Several calls in one tick all start from the yaw and the
     * rate the tick began with, so they do not add up. The client takes the yaw as given: it only lerps toward
     * the server's eased yaw, and a clamp there cut merged lerp steps (motion report, section 7). Before the
     * first tick (spawn, load from disk) the yaw is set as given.
     */
    @Override
    public void setYRot(float yRot) {
        if (this.firstTick || this.level().isClientSide()) {
            super.setYRot(yRot);
            return;
        }
        if (this.turnTick != this.tickCount) {
            // The first change this tick: carry last tick's rate if the body turned then, else start from rest.
            this.turnRateIn = this.turnTick == this.tickCount - 1 ? this.turnRate : 0F;
            this.turnYawIn = super.getYRot();
            this.turnTick = this.tickCount;
        }
        this.turnRate = TurnEasing.yawStep(Mth.wrapDegrees(yRot - this.turnYawIn), this.turnRateIn);
        super.setYRot(this.turnYawIn + this.turnRate);
    }

    /** Eased body yaw (playtest 4 fix P12b): {@link DinoBodyRotationControl}. */
    @Override
    protected BodyRotationControl createBodyControl() {
        return new DinoBodyRotationControl(this);
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

    /**
     * Clears the home restriction the leash set. While leashed, vanilla restricts the animal to 5 blocks round
     * the holder every tick ({@code PathfinderMob.tickLeash}), and nothing in vanilla clears it when the leash
     * comes off: it stayed until the animal reloaded, and {@code LandRandomPos} kept every 32-64 block journey
     * target inside it (playtest 1, R1). Nothing in this mod restricts a dinosaur, so the restriction left when
     * a leash comes off is the leash's.
     */
    @Override
    public void dropLeash(boolean broadcastPacket, boolean dropLeash) {
        boolean wasLeashed = this.isLeashed();
        super.dropLeash(broadcastPacket, dropLeash);
        if (wasLeashed && !this.isLeashed()) {
            this.clearRestriction();
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        // Before the brain: no behaviour starts from the walk of a leader that lost or took the lead (L1, L2).
        HerdLeading.settleLeaderChange(this);
        this.tickBrain(this);
        // Stamina drains only while the animal moves under its own power: a navigation path, a wanted
        // position in the move control or herd steering, and the body actually moving (playtest 2 fix P8a: a
        // member the guard holds in a jam, speed 0, does not tire and lie down there). Not velocity alone: a
        // shove from a herd mate is velocity too. The server owns stamina, so it decides here; the animation
        // predicate runs on the client only.
        boolean selfPropelled = this.getMoveControl() instanceof HerdMoveControl herd ? herd.isSelfPropelled()
                : this.getNavigation().isInProgress() || this.getMoveControl().hasWanted();
        this.entityData.set(SHOULD_TICK_STAMINA, selfPropelled || this.isSwimming());
        // Once a second is enough for the client's leader crown.
        if (this.tickCount % 20 == 0) {
            this.entityData.set(GROUP_LEADER, GroupManager.isLeader(this));
        }
        // The leader records its trail and reports whether the herd travels (no-op for non-leaders).
        HerdLeading.tick(this);
        // P13b (ruling 3b): after the leader's tick, so a journey that starts or ends this tick shows at once.
        // One group look-up per animal per tick (two hash look-ups, no allocation); the value is sent only when
        // it changes.
        DinoGroup group = GroupManager.groupOf(this);
        this.entityData.set(HERD_TRAVELS, group != null && group.state() == HerdState.TRAVEL);

    }

    @Override
    public List<? extends ExtendedSensor<? extends Dinosaur>> getSensors() {
        NearestWaterSourceSensor<Dinosaur> waterSourceSensor = new NearestWaterSourceSensor<>();
        waterSourceSensor.setPredicate((block, dinosaur) -> dinosaur.canTargetWaterSource(block));
        waterSourceSensor.setRadius(20);
        NearestFeederSensor<Dinosaur> feederSensor = new NearestFeederSensor<>();
        feederSensor.setPredicate((block, dinosaur) -> dinosaur.canTargetFeeder(block));
        feederSensor.setRadius(20);
        NearbyBlocksSensor<Dinosaur> fenceProximinitySensor = new NearbyBlocksSensor<>();
        fenceProximinitySensor.setRadius(10.0);
        fenceProximinitySensor.setPredicate((block, dinosaur) -> block.is(BlockInit.ELECTRIC_FENCE.get()) && DinoNeedsUtils.starving(dinosaur));
        NearestItemSensor<Dinosaur> foodItemSensor = new NearestItemSensor<>();
        foodItemSensor.setPredicate((item, dinosaur) -> dinosaur.canTargetFoodItem(item)) ;
        foodItemSensor.setRadius(20);
        NearbyLivingEntitySensor<Dinosaur> nearbyLivingEntitySensor = new NearbyLivingEntitySensor<>();
        return List.of(
                waterSourceSensor,
                feederSensor,
                nearbyLivingEntitySensor,
                foodItemSensor,
                fenceProximinitySensor
        );
    }


    @Override
    public BrainActivityGroup<? extends Dinosaur> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new DinosaurLookAtTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dinosaur) && (dinosaur.isResting() || dinosaur.isDrinking() || dinosaur.isDeadOrDying())),
            //   new ThreatDisplay<>(34) //- needs to be made more situational so it happens more
              //      .whenStarting(dinosaur -> dinosaur.entityData.set(IS_ROARING_STATE, true))
                //    .whenStopping(dinosaur -> dinosaur.entityData.set(IS_ROARING_STATE,false)),
                new MoveToWalkTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dinosaur) && (dinosaur.isResting() || dinosaur.isDrinking() || dinosaur.isDeadOrDying())) ,
                new SetHunting<>(),
                // Lead decision 8: a travelling follower leaves the line only for a critical need (HerdNeeds).
                new SetWalkTargetToWaterSource<>().closeEnoughWhen((entity, pos)-> 3).startCondition(HerdNeeds::mayLeaveForNeeds),
                new WalkToNearestFeeder<>().closeEnoughWhen((entity, pos) -> 3).startCondition(HerdNeeds::mayLeaveForNeeds),
                new SetWalkTargetToFoodItem<>().predicate(Dinosaur::canTargetFoodItem).startCondition(HerdNeeds::mayLeaveForNeeds),
                new FollowParent<>().parentPredicate((baby, parent)-> baby instanceof Dinosaur dino && dino.isFamily(parent) && !parent.isBaby()),
                new JoinGroup<>(),
                new ThreatDisplay<>(34),
                new FollowFormation<>()
        );
    }


    @Override
    public BrainActivityGroup<? extends Dinosaur> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour(
                //        new Panic<>(),
                        // Lead decision 8: a travelling follower stops to drink or eat only for a critical need (HerdNeeds).
                        new Drink<>(100).startCondition(HerdNeeds::mayLeaveForNeeds)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"drink",true))
                                .whenStopping(dinosaur ->  DinoAnimationUtils.setAnimationState(dinosaur,"drink",false)),
                        new EatFromMeatFeeder<>(20).startCondition(HerdNeeds::mayLeaveForNeeds)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"eat",true))
                                .whenStopping(dinosaur ->  DinoAnimationUtils.setAnimationState(dinosaur,"eat",false)),
                        new Eat<>(69).startCondition(HerdNeeds::mayLeaveForNeeds)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"eat",true))
                                .whenStopping(dinosaur ->  DinoAnimationUtils.setAnimationState(dinosaur,"eat",false)),
                        // Plan review decision 17: on a roaming journey a follower lies down only when stamina is critical,
                        // or at normal tiredness while the leader rests (P13a); the leader's rest pauses the journey (HerdNeeds).
                        new Rest<>(69).startCondition(HerdNeeds::mayRest)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"sit",true))
                                // Both flags: a rest that stops before it switches sit -> rest left "sit" on (fix P10).
                                .whenStopping(dinosaur -> {
                                    DinoAnimationUtils.setAnimationState(dinosaur, "sit", false);
                                    DinoAnimationUtils.setAnimationState(dinosaur, "rest", false);
                                }),
                        new GettingUpFromRestBehaviour<>(69)
                                .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"getup",true))
                                .whenStopping(dinosaur ->DinoAnimationUtils.setAnimationState(dinosaur,"getup",false)),
                       new SoloHuntingBehaviour<>()
                                .attackablePredicate(entity -> canTarget(entity))
                                .startCondition(dinosaur -> dinosaur instanceof CarnivoreDinosaur),
                        new SoloHuntRoamBehaviour<>()
                                .dontAvoidWater()
                                .setRadius(40.0D)
                                .stopIf(dino -> BrainUtils.hasMemory(dino, MemoryModuleType.ATTACK_TARGET)),
                new ChallengeGroupLeader<>(),
                // The leader grazes in short moves and sets off on journeys every few minutes (LeaderJourney);
                // HerdLeading runs the journey and sets the herd state.
                new LeaderJourney<>(),
                // Animals without a group wander on their own, as before. Followers move with FollowFormation.
                new SetRandomWalkTarget<Dinosaur>().dontAvoidWater().setRadius(10, 4)
                        .walkTargetPredicate((dinosaur, pos) -> !BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.BRAIN_OVERRIDE.get()))
                        .startCondition(dinosaur -> dinosaur.isIdle() && !dinosaur.hasGroup())
                ));
    }

    @Override
    public BrainActivityGroup<? extends Dinosaur> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>()
                        .invalidateIf((entity, target) -> (target instanceof Player pl && (pl.isCreative() || pl.isSpectator())) || target.isDeadOrDying()),
                new SetWalkTargetToAttackTarget<>().speedMod((owner, target) -> 1.5f),
                new BreakFenceBehaviour<>(20),
                new AnimatableMeleeAttack<>(20)
                        .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState((Dinosaur) dinosaur,"attack",true))
                        .whenStopping(dinosaur -> DinoAnimationUtils.setAnimationState((Dinosaur) dinosaur,"attack",false))
        );
    }
    private int flinchAnimTicks;
    private int restTicks;
    @Override
    public void tick() {
        super.tick();

        if(this.entityData.get(DinoAnimationUtils.IS_FLINCHING_STATE)){
            flinchAnimTicks++;
            if(flinchAnimTicks > this.flinchTicks()){
                this.entityData.set(DinoAnimationUtils.IS_FLINCHING_STATE, false);
                flinchAnimTicks = 0;
            }
        }

        if (level().isClientSide()) {
            this.animator.clientTick();
        } else {
            this.oneShots.serverTick();
        }

        if(!level().isClientSide() && !this.isDeadOrDying()) {
         //   this.socialDrainTick++;
            if(this.isResting()) {
                this.restTicks++;
            }
            if(this.shouldTickStamina()) {
                this.staminaDrainTick++;
            }

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

          if(this.breedingCoolDown == 0){
                if(this.getDinoGender() == 1.0F && this.hasMate()) {
                    this.tryBreedWithMate();
                    this.breedingCoolDown++;
                }
            }
            if(this.breedingCoolDown >= 1){
                this.breedingCoolDown++;
            }
            if(this.breedingCoolDown > 2000){
                this.breedingCoolDown = 0;
            }

            if(this.staminaDrainTick >= 20 && this.shouldTickStamina()){
                DinoNeedsUtils.tickStamina(this);
                if(DinoNeedsUtils.isTired(this) && !BrainUtils.hasMemory(this, MemoryModuleTypeInit.IS_TIRED.get())){
                    BrainUtils.setMemory(this, MemoryModuleTypeInit.IS_TIRED.get(), true);
                }
                this.staminaDrainTick = 0;
            }

            if(this.isResting() && restTicks >= 20){
                if(DinoNeedsUtils.getMaxStamina(this) > DinoNeedsUtils.getCurrentStamina(this)) {
                    float stamina = DinoNeedsUtils.getCurrentStamina(this);
                    float newStamina = stamina + 10F;
                    DinoNeedsUtils.setCurrentStamina(this, newStamina);
                }
                restTicks = 0;
            }


            if(this.socialDrainTick >= 300){
                DinoNeedsUtils.tickSocial();
                this.socialDrainTick = 0;
            }

            if(cachedDayTime != (int) this.level().getDayTime() / 24000L){
                cachedDayTime = (int) (this.level().getDayTime() / 24000L);
                this.isNewDay = true;
            }
            if(this.isNewDay){
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
                    activeStart = 12000;
                    activeEnd = 23999;
                } else {
                    activeStart = 0;
                    activeEnd = 12000;
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
            if(!thirstSchedule.isEmpty()) {
                int toRemove = -1;
                int i = 0;

                for (long thirstTime : thirstSchedule) {
                    if (this.level().getDayTime() % 24000 >= thirstTime) {
                        toRemove = i;
                        DinoNeedsUtils.tickThirst(this, this.daysSincelastDrink, this.getDinoBehaviour().dehydrationLimit());
                    }
                    i++;
                }
                if(toRemove != -1) {
                    thirstSchedule.remove(toRemove);
                }
            }
            if(!hungerSchedule.isEmpty()) {
                int toRemove = -1;
                int i = 0;

                for (long hungerTime : hungerSchedule) {
                    if (this.level().getDayTime() % 24000 >= hungerTime) {
                        toRemove = i;
                        DinoNeedsUtils.tickHunger(this, this.daysSincelastAte, this.getDinoBehaviour().starvationLimit());
                    }
                    i++;
                }
                if(toRemove != -1) {
                    hungerSchedule.remove(toRemove);
                }
            }
            if(!DinoNeedsUtils.isHungry(this) && BrainUtils.hasMemory(this, MemoryModuleTypeInit.HUNTING.get())){
                BrainUtils.clearMemory(this, MemoryModuleTypeInit.HUNTING.get());
            }
        }
    }
    Random random = new Random();

    public void tryBreedWithMate(){
        int attempt = random.nextInt(0,100);
        if(attempt > 50) {
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
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
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

    public String getStringDinoGender() {
        //Gets Gender and if none has been set then returns as female.
        double geneGender = this.getDinoData().getGeneValue(GeneInit.GENDER.get());
        if(geneGender == 2.0D){
            return "male";
        } else return "female";
    }
    /** Same rule as {@link #getStringDinoGender()}: gene value 2.0 is male, anything else female. */
    public boolean isMale() {
        return this.getDinoGender() == 2.0D;
    }
    public double getDinoGender(){
      return this.getDinoData().getGeneValue(GeneInit.GENDER.get());
    }
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
        if(this.getDinoGender() == 1){
            mother = this;
        } else mother = (Dinosaur) mate;

        if(mate instanceof Dinosaur) {
            if (dinosaur != null) {
                dinosaur.setDinoData(mother.getDinoData());
                DinoNeedsUtils.setDinoBaseNeeds(dinosaur, mother.getDinoBehaviour());
                dinosaur.setDinoBehaviour(mother.getDinoBehaviour().toNBT(mother.getDinoBehaviour()));
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
        return age <= -18000;
    }

    public boolean isJuvanile(){
        int age = this.age;
        return age <= -12000 && age > -18000;
    }

    public boolean isSubAdult(){
        int age = this.age;
        return age <= -6000 && age > -12000;
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

    public boolean hasGroup() {
        return GroupManager.groupOf(this) != null;
    }

    public boolean isGroupLeader() {
        return this.level().isClientSide() ? this.entityData.get(GROUP_LEADER) : GroupManager.isLeader(this);
    }

    // A dead dinosaur is never removed (tickDeath is empty, the carcass stays), so leave the group on death.
    @Override
    public void die(DamageSource source) {
        GroupManager.leave(this);
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy() || reason == RemovalReason.CHANGED_DIMENSION) {
            GroupManager.leave(this);
        }
        super.remove(reason);
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
