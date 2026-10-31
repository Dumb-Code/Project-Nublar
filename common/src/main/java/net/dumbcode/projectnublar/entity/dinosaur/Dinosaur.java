package net.dumbcode.projectnublar.entity.dinosaur;

import com.mojang.serialization.Codec;
import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.dinosaur.DinoDietData;
import net.dumbcode.projectnublar.block.DinosaurFeederBlock;
import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.entity.DeathMessageHandler;
import net.dumbcode.projectnublar.entity.dinosaur.ai.util.FenceAwareNavigation;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action.BreakFenceBehaviour;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action.DinosaurLookAtTarget;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action.GettingUpFromRestBehaviour;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.need.SoloHuntRoamBehaviour;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.need.SoloHuntingBehaviour;
import net.dumbcode.projectnublar.entity.dinosaur.ai.sensor.NearestFeederSensor;
import net.dumbcode.projectnublar.entity.dinosaur.animation.RestartableAnimationController;
import net.dumbcode.projectnublar.entity.dinosaur.ai.task.*;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.dinosaur.ai.sensor.NearestWaterSourceSensor;
import net.dumbcode.projectnublar.registry.*;
import net.dumbcode.projectnublar.util.DinoAnimationLengths;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
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
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.ChallengeGroupLeader;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.DinoGroup;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.FollowFormation;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.HerdLeading;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.HerdNeeds;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.GroupManager;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.HerdMoveControl;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.HerdState;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.JoinGroup;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.LeaderJourney;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.RunGait;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.TurnEasing;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action.ThreatDisplay;
import net.dumbcode.projectnublar.entity.dinosaur.animation.AnimationChoice;
import net.dumbcode.projectnublar.entity.dinosaur.animation.DinoAnimator;
import net.dumbcode.projectnublar.entity.dinosaur.animation.DinoOneShots;
import net.dumbcode.projectnublar.entity.dinosaur.animation.OneShotAction;
import net.tslat.smartbrainlib.api.core.behaviour.base.FirstApplicableBehaviour;
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
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;

import java.util.*;

/**
 * Base class of every dinosaur entity: genetics ({@link DinoData}), datapack behaviour profile,
 * needs (hunger/thirst/stamina/social), breeding, growth stages, multipart support, and the
 * SmartBrainLib brain definition.
 *
 * <p>SmartBrainLib 2 builds and ticks the brain; nothing here ticks it. Its mixin runs the brain once per
 * tick, right after {@link #customServerAiStep}.
 *
 * <p><b>Frozen invariants:</b> every {@link EntityDataAccessor} of a dinosaur is declared in this
 * class, in the order given by the comment above {@link #DINO_DATA}; every NBT tag name is a save
 * contract; all numeric constants are behavioral contracts.
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
    /**
     * Blocks round the animal, each way, in which it sees food lying on the ground: the food-item sensor's radius, and
     * the range in which a hunter eats meat first instead of starting a hunt (SoloHuntingBehaviour).
     */
    public static final double FOOD_ITEM_RANGE = 20;

    // Synced values. Every EntityDataAccessor of a dinosaur is defined in this class: NeoForge 26.2 refuses a
    // definition made in another class (it stops the game in development). The game numbers these values in the
    // order they are defined here, from DINO_DATA down to TURNING_RIGHT_STATE, and the server and every client
    // must give each value the same number. Do not move or reorder them; add a new value only after the last one.
    // DinosaurSyncedDataOrderTest pins this order.
    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public static EntityDataAccessor<CompoundTag> DINO_BEHAVIOUR = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.COMPOUND_TAG);

    public static EntityDataAccessor<Optional<UUID>> DINO_FAMILY_UUID = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.OPTIONAL_UUID);
    public static EntityDataAccessor<Optional<UUID>> DINO_MATE = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.OPTIONAL_UUID);
    public static EntityDataAccessor<Boolean> BABY_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> JUVENILE_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> SUB_ADULT_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> ADULT_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> SHOULD_TICK_STAMINA = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    /** Mirror of {@link GroupManager#isLeader} for the client, which has no group data. Server writes it once a second. */
    public static final EntityDataAccessor<Boolean> GROUP_LEADER = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Vector3fc> DINOSAUR_HEAD_POS = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.VECTOR3);
    /** The one-shot action the server plays ({@link OneShotAction#id}); one channel for call, sentry and startled (fix P10). */
    public static final EntityDataAccessor<Byte> ONE_SHOT = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BYTE);
    /**
     * The animal's herd travels: its group is in TRAVEL, the journey's pauses included (P13b, ruling 3b). Set by
     * the server each tick ({@link #customServerAiStep}); the client then plays only the main idle
     * ({@code AnimationChoice.mainIdleOnly}).
     */
    public static final EntityDataAccessor<Boolean> HERD_TRAVELS = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    /**
     * The animal's size mode, fixed once per animal (D-g, P28): true when the {@code SCALE} attribute sets its model
     * and box size ({@link #updateScale}), false for today's boxes and the renderer's manual scale. The server
     * reads {@link FossilsConfig#useScaleAttribute()} once for a new animal and saves the mode with it; client and
     * server read this value, never the config, so both pick the same box and render path.
     */
    public static final EntityDataAccessor<Boolean> SCALE_MODE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    /**
     * Save key of {@link #SCALE_MODE}, written only once the mode is decided: an undecided animal is saved without it,
     * and an animal loaded without it is undecided until its first server tick (P66).
     */
    private static final String SCALE_MODE_KEY = "scale_mode";

    // Need bars and scores (in DinoNeedsUtils until 26.2, in the same order).
    public static final EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> THIRST = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> STAMINA = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> SOCIAL = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> AGGRESSION = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> FERTILITY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DOMESTICITY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> INTELLIGENCE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> VISION = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> IMMUNITY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> TAMING_SCORE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);

    // Animation states (in DinoAnimationUtils until 26.2, in the same order).
    public static final EntityDataAccessor<Boolean> IS_EATING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_DRINKING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_NESTING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_ROARING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_SPEAKING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_SITTING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_RESTING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_RISING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_ATTACKING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_FLINCHING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_DEAD_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_SWIMMING_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> LOOKING_LEFT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> LOOKING_RIGHT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> TURNING_LEFT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> TURNING_RIGHT_STATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    /**
     * Largest change of body yaw per tick, in degrees ({@link #setYRot}): the top rate of the eased turn
     * ({@code TurnEasing}, playtest 4 fix P12b). A turn from rest starts slower (2, 4, 6, 8 degrees in its first
     * ticks, then this rate; P13b) and slows again near its end. Herd steering uses the same value, so the direction
     * it plans is the direction the body can actually turn to.
     */
    public static final float MAX_TURN_PER_TICK = 10F;
    /**
     * The {@code MOVEMENT_SPEED} attribute every dinosaur spawns with. Herd steering derives its walk floor
     * from it ({@code GroundSpeed}): the ground speed is 0.138 x modifier^2 blocks per tick at this value.
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
    private int breedingCoolDown = INITIAL_BREEDING_COOLDOWN;
    private int flinchAnimLength;
    /** Server, eased turning ({@link #setYRot}): this tick's signed yaw change, and the tick it was made in. */
    private float turnRate;
    private int turnTick = Integer.MIN_VALUE;
    /** Server, eased turning: the rate and the yaw the current tick's turn started from. */
    private float turnRateIn;
    private float turnYawIn;
    /** Server: {@link #SCALE_MODE} is fixed, from the save or from the config on the first tick in the level. */
    private boolean scaleModeFixed;
    public DinosaurPart[] subEntities;
    private int cachedDayTime;
    public boolean isNewDay;
    List<Long> hungerSchedule = new ArrayList<>();
    List<Long> thirstSchedule = new ArrayList<>();

    public Dinosaur(EntityType<? extends Dinosaur> $$0, Level $$1, int flinchAnimLength) {
        super($$0, $$1);
        this.flinchAnimLength = flinchAnimLength;
        // Herd members steer round each other and never step inside a hard minimum (HerdMoveControl: context
        // steering masks and the guard). Vanilla pushing stays on as a backstop for overlaps from spawning,
        // shoves and teleports (turning it off made members stack in playtest 2).
        this.moveControl = new HerdMoveControl(this);
    }
    //ANIMATION
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Walk or run from the real ground speed (fix P10; GeckoLib's generic walk controller only walks).
        // Blends instead of snaps between animations (fix P12a), and from rest after a stop (RestartableAnimationController).
        // The main controller restarts an animation that plays again (DinoAnimator.set).
        // The empty sound handlers: some clips (Brachiosaurus move.walk, run, eatingtall2) have sound keyframes for
        // sounds the mod never registered. No footstep sound plays, the same as 1.20.1; without a handler GeckoLib 5
        // logs a warning at every start of such a clip.
        controllers.add(new RestartableAnimationController<>("movement", DinoAnimator.MOVEMENT_TRANSITION_TICKS, this.animator::movement)
                .setSoundKeyframeHandler(event -> {}));
        controllers.add(new RestartableAnimationController<>("dino_controller", DinoAnimator.MAIN_TRANSITION_TICKS, this.animator::main)
                .setSoundKeyframeHandler(event -> {}));
        controllers.add(new AnimationController<>("dino_secondary_Controller", 0, this::animationPredicateAmbient));
    }
    private PlayState animationPredicateAmbient(AnimationTest<Dinosaur> test) {

    return PlayState.STOP;

    }

    // TODO(BUG): overridden empty (original intent: "stop the game despawning after death"), so
    // dead dinosaurs never complete the vanilla death pipeline (death timer, removal, poof).
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
        return this.entityData.get(IS_FLINCHING_STATE);
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

    /**
     * Rebuilds the multipart hit parts. Nothing calls it yet (activation is B26/B27); a part takes the sizes given
     * here unscaled and follows the parent's {@link #getScale()} itself ({@link PartEntity}).
     */
    public void resetParts(float scale) {}
    public void removeParts() {}
    public void updateParts(){}
    public void updatePart(@Nullable final DinosaurPart part, @NotNull final Dinosaur parent) {}

    //SIZE
    /**
     * The size of an animal relative to its model: {@code SIZE} gene / 100 plus the species' adult offset, times
     * 0.25, 0.5 or 0.75 for growth stage 1, 2 or 3 (1.0 for an adult, stage 4). This is the 1.20.1 render scale.
     * The renderer applies it by hand when {@link #SCALE_MODE} is false; {@link #updateScale} puts it in the
     * {@code SCALE} attribute when it is true.
     */
    public static float scaleFactor(double sizeGene, int growthStage, float adultOffset) {
        float adult = (float) sizeGene / 100 + adultOffset;
        return switch (growthStage) {
            case 1 -> adult * 0.25F;
            case 2 -> adult * 0.5F;
            case 3 -> adult * 0.75F;
            default -> adult;
        };
    }

    /** The species' adult scale at {@code SIZE} gene 0 (moved here from the species renderers). */
    protected abstract float adultScaleOffset();

    /**
     * The species' corrected adult box divided by {@link #adultScaleOffset()}, used when {@link #SCALE_MODE} is
     * true: the {@code SCALE} attribute (the offset, at gene 0, for an adult) brings it back to the adult box.
     */
    protected abstract EntityDimensions correctedBaseBox();

    /**
     * Client: the drawn model's bind-pose size at render scale 1, in blocks, for the renderer's view culling (not a
     * hitbox): the width is twice the largest horizontal distance of any vertex from the model origin, the height is
     * the model's top. Measured from the species' {@code .geo.json}. Each species returns one constant, because the
     * renderer reads this for every dinosaur every frame.
     */
    public abstract EntityDimensions modelBounds();

    /** This animal's {@link #scaleFactor}, from its {@code SIZE} gene, growth stage and species offset. */
    public float scaleFactor() {
        return scaleFactor(this.getDinoData().getGeneValue(GeneInit.SIZE.get()), this.getGrowthStage(), this.adultScaleOffset());
    }

    /**
     * The box before vanilla's {@code LivingEntity.getDimensions(Pose)} (final on 26.2) scales it by
     * {@link #getScale()} (the {@code SCALE} attribute). With {@link #SCALE_MODE} false this is vanilla's own box,
     * the {@code sized()} value halved for a baby by {@code getAgeScale()}, exactly as on 1.20.1 (P9). With it true
     * the box is {@link #correctedBaseBox()} with no age scale, because the attribute already holds the growth
     * stage; vanilla applies {@code getAgeScale()} only inside this method, so the age is not counted twice.
     */
    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return this.entityData.get(SCALE_MODE) ? this.correctedBaseBox() : super.getDefaultDimensions(pose);
    }

    /**
     * Server: sets the size for the current mode, gene and growth stage. With {@link #SCALE_MODE} true the
     * {@code SCALE} attribute's base value becomes {@link #scaleFactor()}; with it false the attribute stays at
     * vanilla's 1.0. Vanilla does the rest: {@code Attributes.SCALE} is client-syncable, {@code LivingEntity.getScale()}
     * reads it, {@code getDimensions(Pose)} scales the box by it, {@code onAttributeUpdated} refreshes the
     * dimensions, and the base value is saved and restored with the entity's attributes
     * ({@code AttributeInstance.pack}/{@code apply}), so a loaded animal keeps its size until the next stage change.
     * Runs on the first server tick, when the growth stage changes and when the genetics are set. It does not call
     * {@link #resetParts}: parts follow the parent's scale themselves, and nothing creates parts before B26/B27.
     */
    private void updateScale() {
        if (this.entityData.get(SCALE_MODE)) {
            this.getAttribute(Attributes.SCALE).setBaseValue(this.scaleFactor());
        }
        this.refreshDimensions();
    }

    /**
     * Server, the animal's first tick in the level. This is the add-to-level hook: on 26.2 common code has no
     * add-to-level method an entity can override (vanilla has none; NeoForge's {@code onAddedToLevel} is not visible
     * in common). An animal loaded with a saved mode keeps it ({@link #readAdditionalSaveData}); an undecided animal
     * (a new spawn, or one saved before this port) takes {@link FossilsConfig#useScaleAttribute()} here once, and
     * saves it. The config is never read again for this animal. Then the size is set for the mode.
     */
    private void onFirstServerTick() {
        if (!this.scaleModeFixed) {
            this.entityData.set(SCALE_MODE, FossilsConfig.useScaleAttribute());
            this.scaleModeFixed = true;
        }
        this.updateScale();
    }

    /**
     * A new {@link #SCALE_MODE} changes the box: the client refreshes it here, as vanilla {@code AgeableMob} does for
     * its baby flag, because the {@code SCALE} attribute may keep its value (an adult at gene 0 with offset 1.0).
     */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (SCALE_MODE.equals(accessor)) {
            this.refreshDimensions();
        }
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DINO_DATA, new DinoData());
        builder.define(DINO_BEHAVIOUR, new CompoundTag());
        builder.define(DINO_FAMILY_UUID, Optional.empty());
        builder.define(DINO_MATE, Optional.empty());

        builder.define(HUNGER, 100.0F);
        builder.define(THIRST, 100.0F);
        builder.define(STAMINA, 100.0F);
        builder.define(SOCIAL, 100.0F);
        builder.define(AGGRESSION, 100.0F);
        builder.define(DOMESTICITY, 100.0F);
        builder.define(FERTILITY, 100.0F);
        builder.define(IMMUNITY, 100.0F);
        builder.define(INTELLIGENCE, 100.0F);
        builder.define(SIZE, 1F);
        builder.define(TAMING_SCORE, 100.0F);
        builder.define(VISION, 100.0F);

        builder.define(DINOSAUR_HEAD_POS, new Vector3f());

        builder.define(BABY_DATA_ID, false);
        builder.define(JUVENILE_DATA_ID, false);
        builder.define(SUB_ADULT_DATA_ID, false);
        builder.define(ADULT_DATA_ID, true);
        builder.define(SHOULD_TICK_STAMINA, true);
        builder.define(GROUP_LEADER, false);
        builder.define(ONE_SHOT, OneShotAction.NONE.id());
        builder.define(HERD_TRAVELS, false);

        builder.define(IS_EATING_STATE, false);
        builder.define(IS_DRINKING_STATE, false);
        builder.define(IS_NESTING_STATE, false);
        builder.define(IS_RESTING_STATE, false);
        builder.define(IS_RISING_STATE, false);
        builder.define(IS_SITTING_STATE, false);
        builder.define(IS_ATTACKING_STATE, false);
        builder.define(IS_FLINCHING_STATE, false);
        builder.define(IS_DEAD_STATE, false);
        builder.define(IS_SWIMMING_STATE, false);
        builder.define(LOOKING_LEFT_STATE, false);
        builder.define(LOOKING_RIGHT_STATE, false);
        builder.define(TURNING_RIGHT_STATE, false);
        builder.define(TURNING_LEFT_STATE, false);
        builder.define(IS_ROARING_STATE, false);
        builder.define(IS_SPEAKING_STATE, false);
        builder.define(SCALE_MODE, false);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        SaveVersion.write(output);
        super.addAdditionalSaveData(output);
        output.store("dino_data", DinoData.CODEC, this.getDinoData());
        // The behaviour profile stays a CompoundTag (Task 2); stored through the tag codec.
        output.store("behaviour_profile", CompoundTag.CODEC, this.entityData.get(DINO_BEHAVIOUR));
        output.putFloat("hunger_bar", this.entityData.get(HUNGER));
        output.putFloat("thirst_bar", this.entityData.get(THIRST));
        output.putFloat("stamina_bar", this.entityData.get(STAMINA));
        output.putFloat("social_bar", this.entityData.get(SOCIAL));
        output.putFloat("trust_threshold", this.entityData.get(TAMING_SCORE));
        output.putFloat("dino_vision", this.entityData.get(VISION));
        output.putFloat("dino_aggression", this.entityData.get(AGGRESSION));
        output.putFloat("dino_fertility", this.entityData.get(FERTILITY));
        output.putFloat("dino_domesticity", this.entityData.get(DOMESTICITY));
        output.putFloat("dino_size", this.entityData.get(SIZE));
        output.putFloat("dino_intelligence", this.entityData.get(INTELLIGENCE));
        output.putFloat("dino_immunity", this.entityData.get(IMMUNITY));
        output.putBoolean("baby_age_boolean", this.entityData.get(BABY_DATA_ID));
        output.putBoolean("juvenile_age_boolean", this.entityData.get(JUVENILE_DATA_ID));
        output.putBoolean("sub_adult_age_boolean", this.entityData.get(SUB_ADULT_DATA_ID));
        output.putBoolean("adult_age_boolean", this.entityData.get(ADULT_DATA_ID));
        if (this.scaleModeFixed) {
            output.putBoolean(SCALE_MODE_KEY, this.entityData.get(SCALE_MODE));
        }


        if(headBonePos != null){
            output.putDouble("headx",headBonePos.x);
            output.putDouble("heady",headBonePos.y);
            output.putDouble("headz",headBonePos.z);
        }

        // UUIDUtil.CODEC is the int-array form that 1.20.1's putUUID wrote.
        this.entityData.get(DINO_MATE).ifPresent(uuid -> output.store("mate_uuid", UUIDUtil.CODEC, uuid));
        this.entityData.get(DINO_FAMILY_UUID).ifPresent(uuid -> output.store("family_uuid", UUIDUtil.CODEC, uuid));
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(DINO_DATA, input.read("dino_data", DinoData.CODEC).orElseGet(DinoData::new));
        this.entityData.set(DINO_BEHAVIOUR, input.read("behaviour_profile", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        this.entityData.set(HUNGER, input.getFloatOr("hunger_bar", 0F));
        this.entityData.set(THIRST, input.getFloatOr("thirst_bar", 0F));
        this.entityData.set(STAMINA, input.getFloatOr("stamina_bar", 0F));
        this.entityData.set(SOCIAL, input.getFloatOr("social_bar", 0F));
        // TODO(BUG): all four age flags are read from "baby_age_boolean"; the dedicated
        // juvenile/sub_adult/adult keys are written in addAdditionalSaveData but never read.
        this.entityData.set(BABY_DATA_ID, input.getBooleanOr("baby_age_boolean", false));
        this.entityData.set(JUVENILE_DATA_ID, input.getBooleanOr("baby_age_boolean", false));
        this.entityData.set(SUB_ADULT_DATA_ID, input.getBooleanOr("baby_age_boolean", false));
        this.entityData.set(ADULT_DATA_ID, input.getBooleanOr("baby_age_boolean", false));
        // A saved mode wins (P28, P66); without the key (saved before this port) the animal is undecided and takes
        // the config on its first server tick.
        input.read(SCALE_MODE_KEY, Codec.BOOL).ifPresent(mode -> {
            this.entityData.set(SCALE_MODE, mode);
            this.scaleModeFixed = true;
        });
        this.entityData.set(TAMING_SCORE,input.getFloatOr("trust_threshold", 0F));
        this.entityData.set(VISION,input.getFloatOr("dino_vision", 0F));
        this.entityData.set(AGGRESSION,input.getFloatOr("dino_aggression", 0F));
        this.entityData.set(FERTILITY,input.getFloatOr("dino_fertility", 0F));
        this.entityData.set(DOMESTICITY,input.getFloatOr("dino_domesticity", 0F));
        this.entityData.set(SIZE,input.getFloatOr("dino_size", 0F));
        this.entityData.set(INTELLIGENCE,input.getFloatOr("dino_intelligence", 0F));
        this.entityData.set(IMMUNITY,input.getFloatOr("dino_immunity", 0F));

        Optional<Double> headX = input.read("headx", Codec.DOUBLE);
        Optional<Double> headY = input.read("heady", Codec.DOUBLE);
        Optional<Double> headZ = input.read("headz", Codec.DOUBLE);
        if(headX.isPresent() && headY.isPresent() && headZ.isPresent()){
           headBonePos = new Vec3(headX.get(),headY.get(),headZ.get());
        }

        this.entityData.set(DINO_MATE, input.read("mate_uuid", UUIDUtil.CODEC));
        this.entityData.set(DINO_FAMILY_UUID, input.read("family_uuid", UUIDUtil.CODEC));

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
    /** Sets the genetics; on the server the size follows the new {@code SIZE} gene ({@link #updateScale}). */
    public void setDinoData(DinoData dinoData) {
        this.entityData.set(DINO_DATA, dinoData);
        if (!this.level().isClientSide()) {
            this.updateScale();
        }
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
    public boolean wantsToPickUp(ServerLevel level, ItemStack stack) {
        if(this.isSleeping()){
            return false;
        }
        DinoDietData validfood = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        return validfood.foodMap().containsKey(stack.getItem().getDescriptionId());
    }

    // TODO(BUG): always applies a generic damage source, discarding the real source (attacker,
    // armor bypass flags, etc.) passed in by the part entity.
    public boolean hurtFromPart(DinosaurPart part, ServerLevel level, DamageSource source, float amount) {
        return this.hurtServer(level, this.damageSources().generic(), amount);
    }

    /**
     * The species rule for a hunt target. This base rule refuses only the animal's own rider; each species class
     * overrides it. The hunt never calls it alone: {@link #isHuntTarget} refuses a dead or dying target and a player in
     * creative or spectator mode first, so an override does not have to repeat those tests and cannot drop them.
     */
    public boolean canTarget(LivingEntity target) {
        return target.getVehicle() != this;
    }

    /**
     * The hunt's target test (SoloHuntingBehaviour): the one alive and game-mode check that every hunter passes, then
     * its species rule ({@link #canTarget}). Not a dead or dying animal: the hunt picks from a nearby-entity list that
     * is rescanned only every 20 ticks, and its score rises as the target's health falls, so without this test a hunter
     * re-picks its kill on every tick of the death animation. Not a player in creative or spectator mode, whom the
     * fight would drop at once ({@code InvalidateAttackTarget}).
     */
    private boolean isHuntTarget(LivingEntity target) {
        if (!target.isAlive() || target.isDeadOrDying()) {
            return false;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        return canTarget(target);
    }

    public boolean isChild(){
        return this.isDinoBaby() || this.isJuvanile();
    }

    public boolean isHuntingBlocked(){
        return BrainUtil.hasMemory(this, MemoryModuleTypeInit.IS_RESTING.get()) ||BrainUtil.hasMemory(this, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM) || BrainUtil.hasMemory(this, MemoryModuleTypeInit.IS_EATING.get()) ||
                BrainUtil.hasMemory(this, MemoryModuleTypeInit.IS_DRINKING.get());
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
        return this.getDinoDiet().foodMap().containsKey(target.getItem().getItem().getDescriptionId());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        DinoDietData validfood = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        return validfood.foodMap().containsKey(stack.getItem().getDescriptionId());
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

    /**
     * The 1.20.1 melee reach, which the brain's attack ({@code AnimatableMeleeAttack}) checks: true when the squared
     * distance to the target's position is at most (3 x this width)^2 + the target's width. 26.2 vanilla measures
     * reach from the hitbox instead and has no range method to override, so the 1.20.1 test is kept here. The
     * 1.20.1 rider-shielding offset (a rider on a camel) is gone, as in vanilla 26.2.
     */
    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        double range = this.getBbWidth() * 3.0F * this.getBbWidth() * 3.0F + target.getBbWidth();
        return this.distanceToSqr(target.position()) <= range;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        // Before the brain: no behaviour starts from the walk of a leader that lost or took the lead (L1, L2).
        HerdLeading.settleLeaderChange(this);
        // SmartBrainLib 2 ticks the brain after this method returns, so the steps below read last tick's brain result.
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
    public List<? extends ExtendedSensor<? extends Dinosaur>> getSensors(Dinosaur owner) {
        NearestWaterSourceSensor<Dinosaur> waterSourceSensor = new NearestWaterSourceSensor<>();
        waterSourceSensor.setPredicate((dinosaur, block) -> dinosaur.canTargetWaterSource(block));
        waterSourceSensor.setRadius(20);
        NearestFeederSensor<Dinosaur> feederSensor = new NearestFeederSensor<>();
        feederSensor.setPredicate((dinosaur, block) -> dinosaur.canTargetFeeder(block));
        feederSensor.setRadius(20);
        NearbyBlocksSensor<Dinosaur> fenceProximinitySensor = new NearbyBlocksSensor<>();
        fenceProximinitySensor.detectionRadius(10.0);
        // SmartBrainLib 2 scans blocks every 30 ticks by default; 20 is the 1.x rate this sensor always had.
        fenceProximinitySensor.scanRate(20);
        fenceProximinitySensor.setPredicate((dinosaur, block) -> block.getSecond().is(BlockInit.ELECTRIC_FENCE.get()) && DinoNeedsUtils.starving(dinosaur));
        NearestItemSensor<Dinosaur> foodItemSensor = new NearestItemSensor<>();
        foodItemSensor.setPredicate((dinosaur, item) -> dinosaur.canTargetFoodItem(item)) ;
        foodItemSensor.setRadius(FOOD_ITEM_RANGE);
        NearbyLivingEntitySensor<Dinosaur> nearbyLivingEntitySensor = new NearbyLivingEntitySensor<>();
        return List.of(
                waterSourceSensor,
                feederSensor,
                nearbyLivingEntitySensor,
                foodItemSensor,
                fenceProximinitySensor
        );
    }


    /** The core activity: always active, next to the idle or the fight activity. */
    @Override
    public List<? extends BehaviorControl<?>> getAlwaysRunningBehaviours(Dinosaur owner) {
        return List.of(
                new DinosaurLookAtTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dinosaur) && (dinosaur.isResting() || dinosaur.isDrinking() || dinosaur.isDeadOrDying())),
                new MoveToWalkTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dinosaur) && (dinosaur.isResting() || dinosaur.isDrinking() || dinosaur.isDeadOrDying())) ,
                new SetHunting<>(),
                // Lead decision 8: a travelling follower leaves the line only for a critical need (HerdNeeds).
                new SetWalkTargetToWaterSource<>().closeEnoughWhen((entity, pos)-> 3).startCondition(HerdNeeds::mayLeaveForNeeds),
                // Its own close-enough of 0: the walk ends in eating reach, not 3 blocks out (Task 41).
                new WalkToNearestFeeder<>().startCondition(HerdNeeds::mayLeaveForNeeds),
                new SetWalkTargetToFoodItem<>().predicate(Dinosaur::canTargetFoodItem).startCondition(HerdNeeds::mayLeaveForNeeds),
                // isFamily is true only for a Dinosaur parent.
                new FollowParent<>().parentPredicate((baby, parent)-> baby instanceof Dinosaur dino && dino.isFamily(parent)
                        && !((Dinosaur) parent).isDinoBaby()),
                new JoinGroup<>(),
                new ThreatDisplay<>(34),
                new FollowFormation<>()
        );
    }


    @Override
    public List<? extends BehaviorControl<?>> getIdleBehaviours(Dinosaur owner) {
        return List.of(
                new FirstApplicableBehaviour<Dinosaur>(
                        // TODO(DEAD): a Panic behaviour was commented out here; left unwired.
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
                                .attackablePredicate(this::isHuntTarget)
                                .startCondition(dinosaur -> dinosaur instanceof CarnivoreDinosaur),
                        // 40 blocks out, but only 7 up or down (vanilla's random stroll): a target 40 blocks up
                        // or down is in the air or underground, has no path, and the walk ends after a few steps.
                        new SoloHuntRoamBehaviour<>()
                                .dontAvoidWater()
                                .setRadius(40.0D, 7.0D)
                                .stopIf(dino -> BrainUtil.hasMemory(dino, MemoryModuleType.ATTACK_TARGET)),
                new ChallengeGroupLeader<>(),
                // The leader grazes in short moves and sets off on journeys every few minutes (LeaderJourney);
                // HerdLeading runs the journey and sets the herd state.
                new LeaderJourney<>(),
                // Animals without a group wander on their own, as before. Followers move with FollowFormation.
                new SetRandomWalkTarget<Dinosaur>().dontAvoidWater().setRadius(10, 4)
                        .isValidPositionIf((dinosaur, pos) -> !BrainUtil.hasMemory(dinosaur, MemoryModuleTypeInit.BRAIN_OVERRIDE.get()))
                        .startCondition(dinosaur -> dinosaur.isIdle() && !dinosaur.hasGroup())
                ));
    }

    /** The fight activity: SmartBrainLib runs it while the attack target is set and clears the target when it ends. */
    @Override
    public List<? extends BehaviorControl<?>> getFightingBehaviours(Dinosaur owner) {
        return List.of(
                new InvalidateAttackTarget<>()
                        .invalidateIf((entity, target) -> (target instanceof Player pl && (pl.isCreative() || pl.isSpectator())) || target.isDeadOrDying()),
                new SetWalkTargetToAttackTarget<>().speedModifier((attacker, target) -> 1.5f),
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
        if (this.firstTick && !this.level().isClientSide()) {
            this.onFirstServerTick();
        }
        super.tick();

        tickFlinchAnimation();

        if (level().isClientSide()) {
            this.animator.clientTick();
        } else {
            this.oneShots.serverTick();
        }

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
            // The hungry flag follows the bar (set below 75 %, cleared above 90 %, lead 2026-10-27), also for a bar
            // set by a command; a hunter that no longer wants food stops hunting.
            DinoNeedsUtils.updateHungryFlag(this);
            if(!DinoNeedsUtils.isHungry(this) && BrainUtil.hasMemory(this, MemoryModuleTypeInit.HUNTING.get())){
                BrainUtil.clearMemory(this, MemoryModuleTypeInit.HUNTING.get());
            }
        }
    }

    /** Counts down the flinch animation and clears the flag when it has played out. */
    private void tickFlinchAnimation() {
        if(this.entityData.get(IS_FLINCHING_STATE)){
            flinchAnimTicks++;
            if(flinchAnimTicks > this.flinchTicks()){
                this.entityData.set(IS_FLINCHING_STATE, false);
                flinchAnimTicks = 0;
            }
        }
    }

    /** Mirrors the age-derived growth stage into the synched boolean flags; a new stage updates the size. */
    private void syncGrowthStageFlags() {
        int stage = this.getGrowthStage();
        if(this.isDinoBaby() && !this.entityData.get(BABY_DATA_ID)){
            this.entityData.set(BABY_DATA_ID, true);
        } else if (!this.isDinoBaby() && this.entityData.get(BABY_DATA_ID)){
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
        if (this.getGrowthStage() != stage) {
            this.updateScale();
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
            if(DinoNeedsUtils.isTired(this) && !BrainUtil.hasMemory(this, MemoryModuleTypeInit.IS_TIRED.get())){
                BrainUtil.setMemory(this, MemoryModuleTypeInit.IS_TIRED.get(), true);
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
        if(cachedDayTime != (int) this.level().getOverworldClockTime() / DAY_LENGTH){
            cachedDayTime = (int) (this.level().getOverworldClockTime() / DAY_LENGTH);
            this.isNewDay = true;
        }
    }

    /**
     * Day rollover: bumps the days-without-food/water counters, resets the daily memories,
     * applies one hunger/thirst tick, and rolls a fresh random hunger/thirst schedule inside the
     * species' active hours.
     */
    private void processNewDay() {
        boolean eatenToday = Boolean.TRUE.equals(BrainUtil.getMemory(this, MemoryModuleTypeInit.EATEN_TODAY.get()));
        boolean drankToday = Boolean.TRUE.equals(BrainUtil.getMemory(this, MemoryModuleTypeInit.DRANK_TODAY.get()));
        if(!eatenToday){
            if(BrainUtil.hasMemory(this,MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get())) {
                daysSincelastAte = BrainUtil.getMemory(this,MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get());
            } else { daysSincelastAte = 0; }
            daysSincelastAte++;

            BrainUtil.setMemory(this, MemoryModuleTypeInit.DAYS_SINCE_LAST_FED.get(), daysSincelastAte);
        }
        if(!drankToday){
            // A drink since the last rollover stored 0 (DinoNeedsUtils.drink), so this makes it 1.
            daysSincelastDrink = DinoNeedsUtils.daysWithoutWater(
                    BrainUtil.getMemory(this, MemoryModuleTypeInit.DAYS_SINCE_LAST_DRANK.get()), false);
            BrainUtil.setMemory(this, MemoryModuleTypeInit.DAYS_SINCE_LAST_DRANK.get(), daysSincelastDrink);
        }

        this.isNewDay = false;
        BrainUtil.setMemory(this,MemoryModuleTypeInit.MEAL_COUNTER.get(), 0);
        BrainUtil.setMemory(this, MemoryModuleTypeInit.DRANK_TODAY.get(), false);
        BrainUtil.setMemory(this, MemoryModuleTypeInit.EATEN_TODAY.get(), false);
        DinoNeedsUtils.tickThirst(this,this.daysSincelastDrink,this.getDinoBehaviour().dehydrationLimit());
        DinoNeedsUtils.tickHunger(this,this.daysSincelastAte,this.getDinoBehaviour().starvationLimit());

        long activeStart;
        long activeEnd;

        // A cathemeral species keeps the day schedule; only a nocturnal one eats and drinks by night.
        if(this.getDinoBehaviour().activityWindow() == DinoBehaviourData.ActivityWindow.NOCTURNAL){
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
                if (this.level().getOverworldClockTime() % DAY_LENGTH >= thirstTime) {
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
                if (this.level().getOverworldClockTime() % DAY_LENGTH >= hungerTime) {
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

    /**
     * Clock ticks to this animal's next hunger tick, for the Debug Stick: the earliest entry left in today's schedule,
     * or the next day rollover, which always ticks hunger. 0 means an entry is due now.
     */
    public long ticksToNextHungerTick() {
        long timeOfDay = this.level().getOverworldClockTime() % DAY_LENGTH;
        long next = DAY_LENGTH - timeOfDay;
        for (long hungerTime : hungerSchedule) {
            next = Math.min(next, Math.max(0, hungerTime - timeOfDay));
        }
        return next;
    }

    Random random = new Random();

    /** Rolls 0–99; above {@value #BREEDING_ATTEMPT_THRESHOLD} both partners enter love mode. */
    public void tryBreedWithMate(){
        int attempt = random.nextInt(0,100);
        if(attempt > BREEDING_ATTEMPT_THRESHOLD) {
            this.setInLove(null);
            @Nullable Dinosaur mate = BrainUtil.getMemory(this, MemoryModuleTypeInit.MATE.get());

            if(mate != null) {
                mate.setInLove(null);
            } else {
                this.clearDinoMate();
            }
        }
    }

    public boolean isNight(){
        int day = (int) this.level().getOverworldClockTime() % 24000;
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
                // Walk up one-block rises like a horse. With the default 0.6, a body 2 or more blocks wide never
                // gets close enough to its next path point to trigger MoveControl's jump, so it stalls at the step.
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesInit.DINO_ENERGY_NEED.get()),100)
                .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesInit.DINO_THIRST_NEED.get()), 100)
                .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesInit.DINO_HUNGER_NEED.get()),100)
                .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesInit.DINO_SOCIAL_NEED.get()),100)
                .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesInit.TRUST_SCORE.get()),1000)
                .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesInit.DINO_VISION.get()),100)
                .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesInit.DINO_AGGRESSION.get()), 0)
                .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesInit.DINO_INTELLIGENCE.get()), 50)
                .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesInit.DINO_FERTILITY.get()),50)
                .add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesInit.DINO_IMMUNITY.get()),0);
    }

    public boolean isDrinking() {
        return this.entityData.get(IS_DRINKING_STATE);
    }
    public boolean isEating() {return this.entityData.get(IS_EATING_STATE);}
    public boolean isSitting() {return this.entityData.get(IS_SITTING_STATE);}
    public boolean isRising() {
        return this.entityData.get(IS_RISING_STATE);
    }
    public boolean isResting() {
        return this.entityData.get(IS_RESTING_STATE);
    }
    public boolean isRoaring() {
        return this.entityData.get(IS_ROARING_STATE);
    }
    public boolean isAttacking() {return this.entityData.get(IS_ATTACKING_STATE);}
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
    /** Same rule as {@link #getStringDinoGender()}: gene value 2.0 is male, anything else female. */
    public boolean isMale() {
        return this.getDinoGender() == 2.0D;
    }
    public double getDinoGender(){
      return this.getDinoData().getGeneValue(GeneInit.GENDER.get());
    }
    // TODO(BUG): nothing ever populates the PLAYER_REPUTATION memory, and the mutators below
    // return early when the player is *not* in the map - so reputation is permanently inert.
    @Nullable
    public Map<Player,Integer> getPlayerReputationMap(){
        if(BrainUtil.hasMemory(this, MemoryModuleTypeInit.PLAYER_REPUTATION.get())){
            return BrainUtil.getMemory(this, MemoryModuleTypeInit.PLAYER_REPUTATION.get());
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
        return (Dinosaur) babyType.create(serverLevel, EntitySpawnReason.BREEDING);
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
                // The baby gets its own copy: synced data must not be changed in place (P121).
                DinoData babyData = mother.getDinoData().copy();
                // TODO(BUG): nextInt(1, 2) can only ever return 1, so every offspring gets the
                // same gender value.
                babyData.setGeneValue(GeneInit.GENDER.get(), random.nextInt(1,2));
                dinosaur.setDinoData(babyData);
                DinoNeedsUtils.setDinoBaseNeeds(dinosaur, mother.getDinoBehaviour());
                dinosaur.setDinoBehaviour(mother.getDinoBehaviour().toNBT(mother.getDinoBehaviour()));
                dinosaur.setBaby(true);
                dinosaur.setDinoFamilyUuid(this.getFamilyId());
                dinosaur.snapTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                this.finalizeSpawnChildFromBreeding(level, mate, dinosaur);
                level.addFreshEntityWithPassengers(dinosaur);
            }
        }
    }

    /**
     * Growth stage 1: the 1.20.1 {@code isBaby()} rule of this mod. On 26.2 vanilla's {@code AgeableMob.isBaby()} is
     * final and true at any negative age (stages 1 to 3), so this mod's readers call this method instead. It reads
     * the server's age; on the client the age is not synced, so this is false there, as {@code isBaby()} was on 1.20.1.
     */
    public boolean isDinoBaby() {
        int age = this.age;
        return age <= BABY_MAX_AGE;
    }

    /**
     * The 1.20.1 box scale: half for a {@link #isDinoBaby() stage 1} animal, else 1. Vanilla scales the default box by
     * this ({@code LivingEntity.getDefaultDimensions}); 1.20.1 did the same through {@code getScale()} with the
     * {@code isBaby()} override, so with {@link #SCALE_MODE} false the box is exactly the 1.20.1 box (ruling P9).
     */
    @Override
    public float getAgeScale() {
        return this.isDinoBaby() ? 0.5F : 1.0F;
    }

    /**
     * Vanilla's rule ({@code !isBaby()}) with the 1.20.1 baby test: only a {@link #isDinoBaby() stage 1} animal drops
     * no XP. On 26.2 {@code isBaby()} is final and true at any negative age, so stages 2 and 3 would drop none (P89).
     */
    @Override
    public boolean shouldDropExperience() {
        return !this.isDinoBaby();
    }

    /**
     * Vanilla's rule ({@code !isBaby()} and the {@code MOB_DROPS} game rule) with the 1.20.1 baby test,
     * {@link #isDinoBaby()}. On 26.2 {@code isBaby()} is final and true at any negative age (P89).
     */
    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return !this.isDinoBaby() && level.getGameRules().get(GameRules.MOB_DROPS);
    }

    /**
     * Vanilla's voice pitch (1.5 for a baby, else 1.0, each moved up to 0.2 at random) with the 1.20.1 baby test:
     * only a {@link #isDinoBaby() stage 1} animal sounds young. On 26.2 {@code isBaby()} is final and true at any
     * negative age (P89).
     */
    @Override
    public float getVoicePitch() {
        RandomSource source = this.getRandom();
        return this.isDinoBaby()
                ? (source.nextFloat() - source.nextFloat()) * 0.2F + 1.5F
                : (source.nextFloat() - source.nextFloat()) * 0.2F + 1.0F;
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

    public boolean hasGroup() {
        return GroupManager.groupOf(this) != null;
    }

    public boolean isGroupLeader() {
        return this.level().isClientSide() ? this.entityData.get(GROUP_LEADER) : GroupManager.isLeader(this);
    }

    // A dead dinosaur is never removed (tickDeath is empty, the carcass stays), so leave the group on death.
    // The death message is sent here on both loaders (1.20.1 sent it from a Forge LivingDeathEvent listener).
    @Override
    public void die(DamageSource source) {
        DeathMessageHandler.onLivingDeath(this, source);
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
        return !this.isDinoBaby() && !this.isJuvanile() && !this.isSubAdult();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();

        if (this.level().isClientSide()) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemStack.is(Items.STICK) && !(this.getLastHurtByMob() != null && this.getLastAttacker().is(player));
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else if (this.isTame()) {
            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                // TODO(BUG): a diet item without food properties (wheat) throws here, as on 1.20.1.
                this.heal((float) item.components().get(DataComponents.FOOD).nutrition());
                return InteractionResult.SUCCESS;
            } else {
                InteractionResult interactionresult = this.superMobInteract(player, hand);
                if ((!interactionresult.consumesAction() || this.isDinoBaby()) && this.isOwnedBy(player)) {
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
            return this.superMobInteract(player, hand);
        }
    }

    /**
     * {@code super.mobInteract} with the 1.20.1 result for a stage 2 or 3 animal fed its food. 26.2's
     * {@code Animal.mobInteract} ages up any fed animal with a negative age ({@code canAgeUp()} reads the final
     * {@code isBaby()}, age &lt; 0). On 1.20.1 only a {@link #isDinoBaby() stage 1} animal aged up: a stage 2 or 3
     * animal fell through {@code Animal.mobInteract} to {@code Mob.mobInteract}, which returns {@code PASS}, and
     * used no item (P89). Server only: on the client {@link #mobInteract} returns before it calls this.
     */
    private InteractionResult superMobInteract(Player player, InteractionHand hand) {
        if (this.isFood(player.getItemInHand(hand)) && this.getAge() < 0 && !this.isDinoBaby()) {
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    public boolean canMateWith(Dinosaur pDinosaur, Dinosaur pMate){

        if(pDinosaur.isDinoBaby() || pMate.isDinoBaby()){
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
    /**
     * The colour of a skin layer as an ARGB int (GeckoLib 5 has no colour class). White for a layer with no basic
     * colour; a layer past the stored colours uses its basic layer's colour.
     */
    public int layerColor(int layer, DinoLayer dinoLayer) {
        if (dinoLayer != null && dinoLayer.getBasicLayer() == -1) {
            return 0xFFFFFFFF;
        }
        if (layer >= this.getDinoData().getLayerColors().stream().count()) {
            return Mth.floor(this.getDinoData().getLayerColor(dinoLayer.getBasicLayer()));
        }
        return Mth.floor(this.getDinoData().getLayerColor(layer));
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
    protected void actuallyHurt(ServerLevel level, DamageSource damageSource, float damageAmount) {
        if(damageSource.getEntity() instanceof Player player){
            this.decreaseReputationForPlayer(player, 20);
            if(this.isTame() && this.getOwner().is(player) && this.getReputationForPlayer(player) < 40){
                this.setTame(false, true);
                this.setOwnerReference(null);
            }
        }
        DinoAnimationUtils.setAnimationState(this,"flinch", true);
        super.actuallyHurt(level, damageSource, damageAmount);

    }
}
