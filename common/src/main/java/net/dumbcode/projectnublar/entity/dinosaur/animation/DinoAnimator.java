package net.dumbcode.projectnublar.entity.dinosaur.animation;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.util.DinoAnimationAvailability;
import net.dumbcode.projectnublar.util.DinoAnimationLengths;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;

/**
 * Chooses a dinosaur's animations on the client (fix P10), for every species: each state plays the first
 * {@link AnimationChoice} candidate the species has ({@link DinoAnimationAvailability}), or a random one for
 * variants. Two controllers use it:
 * <ul>
 *   <li>{@link #movement}: stand, walk or run from the smoothed ground speed, with a minimum time in each gait, and
 *       the leg cycle rate scaled by the ground speed over the species' own stride ({@link MovementGait}, fix P12a,
 *       Task 45). It stops for a dead animal, so a corpse that knockback slides does not walk.</li>
 *   <li>{@link #main}, in priority order: dead, flinch, roar, attack, the server's one-shot action
 *       ({@link OneShotAction}: call, sentry, startled), sit down, get up, rest, drink, eat, swim, standing idle.
 *       Eat, drink and get up play the idle choice when the species has none of their names; dead plays
 *       nothing then (base pose). A get-up with no animation of its own, and every idle while the herd travels
 *       ({@link Dinosaur#herdTravels}), play the main idle only (P13b).</li>
 * </ul>
 * Variant picks (attack, eat, drink) are made when the state starts; the idle pick changes after its cycle
 * finishes or after 4-8 s, but not before one full cycle played, with the main idle weighted most. These picks use
 * client randomness: they are visual only. Nothing here allocates per frame.
 * <p>
 * Blends (P12a): the controllers blend into a new animation over {@link #MOVEMENT_TRANSITION_TICKS} and
 * {@link #MAIN_TRANSITION_TICKS} instead of snapping; flinch and attack use the shorter
 * {@link #REACTION_TRANSITION_TICKS}, so a hit or a bite shows at once.
 * <p>
 * A missing name is never requested (GeckoLib prints a line per frame for one); the controller stops instead.
 * <p>
 * Stop: in GeckoLib 5 a handler that returns {@code STOP} does not end a looping or holding clip; the controller
 * loops or holds it for ever (GeckoLib 4 ended it at once). A walk would then never end, and an idle that never ends
 * overwrites the walk, because the main controller runs after {@code movement}. So every stop here ends the
 * animation. The movement controller first blends the legs back to rest over its transition ticks
 * ({@link #blendToRest}, Task 53), as GeckoLib 4 did, then ends it. The main controller ends at once
 * ({@link #stopController}): it is written over the walk, so a blend to rest would hold the bones it shares with the
 * walk on a fading pose and hide the walk. It stops mostly when a walk starts, and the walk has then taken its pose
 * over (hand-over).
 * <p>
 * Hand-over (Task 58): a start with nothing playing blends from the other controller's pose instead of from rest
 * ({@link RestartableAnimationController}), so the shared bones do not snap to rest when one controller stops as the
 * other starts. A walk or run takes the main controller's whole pose ({@link #playGait}), so a bone only the main
 * clip moves fades to rest in the walk's ease-in. A main clip blends from the movement controller's pose (the stop
 * pose while it blends to rest) on the bones the main clip moves ({@link #playMain}); the movement controller keeps
 * the others. The receiving clock starts at the giving one, so no bone that {@code query.anim_time} moves jumps. A
 * controller still in its own ease-in would hand over its clip's first frame, not the pose shown; at the usual walk
 * start the idle began when the gait stopped, at least {@link MovementGait#HOLD_TICKS} earlier, and main clips play
 * at GeckoLib's default speed 1 (nothing here sets another), so their ease-in is over, as the walk's is (Task 56).
 */
public final class DinoAnimator {
    /**
     * Blend of the movement controller into walk or run, in ticks (Mowzie's Mobs uses 4-5 on such controllers).
     * GeckoLib 5 plays a blend to its end only at some lengths (5, 7, 9, 10 ticks; none under 5), because it turns
     * the ticks into seconds as a float and counts a length a hair over n ticks as n + 1: 4 ticks end at 80 % and step.
     */
    public static final int MOVEMENT_TRANSITION_TICKS = 5;
    /** Blend of the main controller into a new state or idle pick, in ticks. */
    public static final int MAIN_TRANSITION_TICKS = 5;
    /** Blend into flinch and attack, in ticks: shorter, so the reaction does not lag the hit. */
    public static final int REACTION_TRANSITION_TICKS = 2;
    /** {@link #walkCycleTicks} before the first look-up. */
    private static final int CYCLE_NOT_READ = Integer.MIN_VALUE;

    private final Dinosaur dinosaur;
    /** The two controllers, kept from their handlers, so each can hand its pose to the other (hand-over). */
    private @Nullable RestartableAnimationController<?> movementController;
    private @Nullable RestartableAnimationController<?> mainController;

    // State from the client tick.
    private final MovementGait gait = new MovementGait();
    /** The species' walk and run lengths in ticks, read once ({@link DinoAnimationLengths}). */
    private int walkCycleTicks = CYCLE_NOT_READ;
    private int runCycleTicks;
    /** The species' leg strides, read with the lengths ({@link MovementGait#strides}). */
    private MovementGait.Strides strides = MovementGait.Strides.NOT_MEASURED;
    /** The walk and run clips for the gait at {@link #clipScale}; rebuilt when the render scale changes. */
    private MovementGait.LegClip walkClip = MovementGait.LegClip.NOT_MEASURED;
    private MovementGait.LegClip runClip = MovementGait.LegClip.NOT_MEASURED;
    /** The render scale the clips were built for; NaN before the first build. */
    private float clipScale = Float.NaN;
    private boolean wasAttacking;
    private boolean wasEating;
    private boolean wasDrinking;
    private @Nullable RawAnimation attackPick;
    private @Nullable RawAnimation eatPick;
    private @Nullable RawAnimation drinkPick;
    private OneShotAction lastOneShot = OneShotAction.NONE;

    // State of the main controller.
    /** What {@link #main} last asked the controller to play; {@code null} after it stopped. */
    private @Nullable RawAnimation playing;
    /** Play this from its start even if it is already playing (a new attack, a new one-shot, a repeated idle). */
    private @Nullable RawAnimation restartTarget;
    private @Nullable RawAnimation idlePick;
    private long idlePickedAt;
    private long idleChangeAt;
    /** The pick's timer roll and cycle length, kept for a restart after an interruption. */
    private long idleTimerTicks;
    private int idleCycleTicks;

    public DinoAnimator(Dinosaur dinosaur) {
        this.dinosaur = dinosaur;
    }

    /** Once per client tick, after the entity moved: the gait and the starts of states. */
    public void clientTick() {
        if (this.walkCycleTicks == CYCLE_NOT_READ) {
            EntityType<?> type = this.dinosaur.getType();
            this.walkCycleTicks = DinoAnimationLengths.lengthTicks(type, AnimationChoice.WALK);
            this.runCycleTicks = DinoAnimationLengths.lengthTicks(type, AnimationChoice.RUN);
            this.strides = MovementGait.strides(BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath());
        }
        // The render scale grows with the animal (growth stage, SIZE gene), and the speeds its leg clips are made
        // for grow with it.
        float scale = this.dinosaur.scaleFactor();
        if (scale != this.clipScale) {
            this.clipScale = scale;
            this.walkClip = MovementGait.LegClip.of(this.strides.walk(), scale, this.walkCycleTicks);
            // A species with no run clip plays its walk for a run (movement()), so its run is the walk clip.
            this.runClip = this.runCycleTicks == AnimationChoice.LENGTH_NOT_DEFINED
                    ? this.walkClip
                    : MovementGait.LegClip.of(this.strides.run(), scale, this.runCycleTicks);
        }
        double dx = this.dinosaur.getX() - this.dinosaur.xo;
        double dz = this.dinosaur.getZ() - this.dinosaur.zo;
        this.gait.tick(Math.sqrt(dx * dx + dz * dz), this.dinosaur.getAttributeBaseValue(Attributes.MOVEMENT_SPEED),
                this.walkClip, this.runClip);

        boolean attacking = this.dinosaur.isAttacking();
        if (attacking && !this.wasAttacking) {
            this.attackPick = pick(AnimationChoice.ATTACK);
            this.restartTarget = this.attackPick;
        }
        this.wasAttacking = attacking;
        boolean eating = this.dinosaur.isEating();
        if (eating && !this.wasEating) {
            this.eatPick = pick(AnimationChoice.EAT);
        }
        this.wasEating = eating;
        boolean drinking = this.dinosaur.isDrinking();
        if (drinking && !this.wasDrinking) {
            this.drinkPick = pick(AnimationChoice.DRINK);
        }
        this.wasDrinking = drinking;

        OneShotAction oneShot = this.dinosaur.oneShot();
        if (oneShot != this.lastOneShot && oneShot != OneShotAction.NONE) {
            this.restartTarget = first(oneShot.choice());
        }
        this.lastOneShot = oneShot;
    }

    /**
     * The movement controller: walk or run while the gait moves, never for a dead animal (replaces GeckoLib's generic
     * walk controller). The leg cycle plays at {@link MovementGait#animationSpeed}, its ease-in at a speed of at least
     * 1 ({@link #playGait}). The controller must be a {@link RestartableAnimationController}, so a stop can blend the
     * legs to rest.
     */
    public PlayState movement(AnimationTest<Dinosaur> state) {
        this.movementController = (RestartableAnimationController<?>) state.controller();
        if (this.dinosaur.isDeadOrDying() || !this.gait.moving()) {
            return blendToRest(state);
        }
        AnimationChoice choice = this.gait.gait() == MovementGait.Gait.RUN ? AnimationChoice.RUN : AnimationChoice.WALK;
        RawAnimation animation = first(choice);
        if (animation == null) {
            AnimationChoice fallback = AnimationChoice.resolve(choice, false);
            animation = fallback == null ? null : first(fallback);
        }
        if (animation == null) {
            return blendToRest(state);
        }
        return playGait(state, animation, this.gait.animationSpeed(), this.mainController);
    }

    /**
     * Plays the walk or run clip {@code animation} at the gait's leg rate {@code gaitRate}, but its ease-in at a speed
     * of at least 1 (Task 56). A start with nothing playing blends from the whole pose of the main controller
     * {@code main} (class comment, hand-over), which stops on the same frame. Package-private for the test.
     * <p>
     * Why: GeckoLib plays the ease-in at the controller's speed, so at a slow walk's rate (0.25 when a Velociraptor
     * grazes) it lasted 5 / 0.25 = 20 ticks, longer than a gait's shortest hold ({@link MovementGait#HOLD_TICKS}).
     * During an ease-in GeckoLib's point is the clip's first frame, not the pose shown, and a stop
     * ({@link #blendToRest}) or a walk to run switch blends from that point: a jump of up to the whole first-frame
     * pose. At a speed of at least 1 the ease-in lasts at most {@link #MOVEMENT_TRANSITION_TICKS}, so every stop and
     * every switch starts from the pose on screen.
     * <p>
     * GeckoLib reads the speed before it calls the handler, and on the frame a clip starts the controller is not yet
     * in its ease-in: the first frame of an ease-in still plays at the gait rate, so it lasts up to one render frame
     * longer than the transition ticks.
     */
    static PlayState playGait(AnimationTest<?> state, RawAnimation animation, double gaitRate,
            @Nullable RestartableAnimationController<?> main) {
        RestartableAnimationController<?> controller = (RestartableAnimationController<?>) state.controller();
        controller.setAnimationSpeed(controller.isTransitioning() ? Math.max(1D, gaitRate) : gaitRate);
        controller.setAnimation(animation, main, RestartableAnimationController.HandOver.WHOLE_POSE);
        return PlayState.CONTINUE;
    }

    /**
     * The main controller: action states in priority order (class comment). The controller must be a
     * {@link RestartableAnimationController}, so a state can start its animation again.
     */
    public PlayState main(AnimationTest<Dinosaur> state) {
        this.mainController = (RestartableAnimationController<?>) state.controller();
        Dinosaur dino = this.dinosaur;
        boolean reaction = !dino.isDeadOrDying() && (dino.isFlinching() || dino.isAttacking());
        state.controller().setTransitionTicks(reaction ? REACTION_TRANSITION_TICKS : MAIN_TRANSITION_TICKS);
        if (dino.isDeadOrDying()) {
            return play(state, AnimationChoice.DEAD, first(AnimationChoice.DEAD));
        }
        if (dino.isFlinching()) {
            return play(state, AnimationChoice.FLINCH, first(AnimationChoice.FLINCH));
        }
        if (dino.isRoaring()) {
            return play(state, AnimationChoice.ROAR, first(AnimationChoice.ROAR));
        }
        if (dino.isAttacking()) {
            if (this.attackPick == null) {
                this.attackPick = pick(AnimationChoice.ATTACK);
            }
            return play(state, AnimationChoice.ATTACK, this.attackPick);
        }
        OneShotAction oneShot = dino.oneShot();
        if (oneShot != OneShotAction.NONE) {
            RawAnimation animation = first(oneShot.choice());
            if (animation != null) {
                return set(state, animation);
            }
        }
        if (dino.isSitting()) {
            return play(state, AnimationChoice.SIT, first(AnimationChoice.SIT));
        }
        if (dino.isRising()) {
            return play(state, AnimationChoice.GET_UP, first(AnimationChoice.GET_UP));
        }
        if (dino.isResting()) {
            return play(state, AnimationChoice.REST, first(AnimationChoice.REST));
        }
        if (dino.isDrinking()) {
            return play(state, AnimationChoice.DRINK, this.drinkPick != null ? this.drinkPick : first(AnimationChoice.DRINK));
        }
        if (dino.isEating()) {
            return play(state, AnimationChoice.EAT, this.eatPick != null ? this.eatPick : first(AnimationChoice.EAT));
        }
        if (dino.isInWater() && this.gait.moving()) {
            return play(state, AnimationChoice.SWIM, first(AnimationChoice.SWIM));
        }
        if (!this.gait.moving() && dino.isIdle()) {
            return idle(state, AnimationChoice.mainIdleOnly(AnimationChoice.IDLE, dino.herdTravels()));
        }
        return stop(state);
    }

    /** Plays {@code animation} for {@code choice}, or the choice's fallback when the species has none. */
    private PlayState play(AnimationTest<Dinosaur> state, AnimationChoice choice, @Nullable RawAnimation animation) {
        if (animation != null) {
            return set(state, animation);
        }
        if (AnimationChoice.resolve(choice, false) == AnimationChoice.IDLE) {
            return idle(state, AnimationChoice.mainIdleOnly(choice, this.dinosaur.herdTravels()));
        }
        return stop(state);
    }

    /**
     * Standing idle: a weighted pick of the species' idles, changed after a finished cycle or after 4-8 s, but not
     * before one full cycle of the pick played ({@link AnimationChoice#idleChangeAt}). A pick that plays again after
     * an interruption starts from its first frame, so its timer starts again ({@link AnimationChoice#idleTimerStarts}).
     * <p>
     * {@code mainOnly} (P13b, {@link AnimationChoice#mainIdleOnly}: the herd travels, or a get-up with no get-up
     * animation): the main idle plays, a variant that plays blends out, and the main idle becomes the pick with its
     * timer held at its start. So the first variant comes one full timer (4-8 s) after the herd stops travelling or
     * the animal is up, not at once.
     */
    private PlayState idle(AnimationTest<Dinosaur> state, boolean mainOnly) {
        long now = this.dinosaur.tickCount;
        if (mainOnly) {
            return mainIdle(state, now);
        }
        boolean finished = this.playing != null && this.playing == this.idlePick && state.controller().hasAnimationFinished();
        boolean picked = this.idlePick == null || AnimationChoice.shouldRepickIdle(finished, now, this.idlePickedAt, this.idleChangeAt);
        if (picked) {
            int index = pickIndex(AnimationChoice.IDLE);
            if (index < 0) {
                return stop(state);
            }
            RawAnimation next = DinoAnimationAvailability.raw(AnimationChoice.IDLE, index);
            if (next == this.idlePick && finished) {
                this.restartTarget = next;
            }
            this.idlePick = next;
            this.idleTimerTicks = AnimationChoice.idleTicks(this.dinosaur.getRandom().nextDouble());
            this.idleCycleTicks = DinoAnimationLengths.lengthTicks(this.dinosaur.getType(), AnimationChoice.IDLE, index);
        }
        if (AnimationChoice.idleTimerStarts(picked, this.playing == this.idlePick)) {
            this.idlePickedAt = now;
            this.idleChangeAt = AnimationChoice.idleChangeAt(now, this.idleTimerTicks, this.idleCycleTicks, MAIN_TRANSITION_TICKS);
        }
        return set(state, this.idlePick);
    }

    /**
     * The main idle for {@link #idle} with {@code mainOnly}: the first idle candidate the species has. Every
     * species' {@code misc.idle} loops (its file), so no finished-cycle restart is needed here.
     */
    private PlayState mainIdle(AnimationTest<Dinosaur> state, long now) {
        int[] available = DinoAnimationAvailability.available(this.dinosaur.getType(), AnimationChoice.IDLE);
        if (available.length == 0) {
            return stop(state);
        }
        RawAnimation main = DinoAnimationAvailability.raw(AnimationChoice.IDLE, available[0]);
        if (this.idlePick != main) {
            this.idlePick = main;
            this.idleTimerTicks = AnimationChoice.idleTicks(this.dinosaur.getRandom().nextDouble());
            this.idleCycleTicks = DinoAnimationLengths.lengthTicks(this.dinosaur.getType(), AnimationChoice.IDLE, available[0]);
        }
        this.idlePickedAt = now;
        this.idleChangeAt = AnimationChoice.idleChangeAt(now, this.idleTimerTicks, this.idleCycleTicks, MAIN_TRANSITION_TICKS);
        return set(state, main);
    }

    /**
     * Asks the controller to play {@code animation}. GeckoLib does not restart an animation it already plays,
     * so a new start of the same one (or the first play after a stop) forces a reset.
     */
    private PlayState set(AnimationTest<Dinosaur> state, RawAnimation animation) {
        if (animation != this.playing || animation == this.restartTarget) {
            ((RestartableAnimationController<?>) state.controller()).forceAnimationReset();
        }
        this.playing = animation;
        this.restartTarget = null;
        return playMain(state, animation, this.movementController);
    }

    /**
     * Plays the main clip {@code animation}. A start with nothing playing blends from the pose of the movement
     * controller {@code movement} on the bones the clip moves (class comment, hand-over): the movement controller
     * keeps playing or blending its other bones. Package-private for the test.
     */
    static PlayState playMain(AnimationTest<?> state, RawAnimation animation,
            @Nullable RestartableAnimationController<?> movement) {
        ((RestartableAnimationController<?>) state.controller())
                .setAnimation(animation, movement, RestartableAnimationController.HandOver.SHARED_BONES);
        return PlayState.CONTINUE;
    }

    /** Stops the main controller. */
    private PlayState stop(AnimationTest<Dinosaur> state) {
        this.playing = null;
        return stopController(state);
    }

    /**
     * Ends the controller's animation at once and returns {@code STOP}: in GeckoLib 5, {@code STOP} alone does not
     * end a looping or holding clip (class comment). Package-private for the test.
     */
    static PlayState stopController(AnimationTest<?> state) {
        state.controller().reset();
        return PlayState.STOP;
    }

    /**
     * Stops the movement controller with a blend of the legs to rest, then ends its animation
     * ({@link RestartableAnimationController#blendToRest}). Package-private for the test.
     */
    static PlayState blendToRest(AnimationTest<?> state) {
        return ((RestartableAnimationController<?>) state.controller()).blendToRest();
    }

    private @Nullable RawAnimation first(AnimationChoice choice) {
        return DinoAnimationAvailability.first(this.dinosaur.getType(), choice);
    }

    /** A random variant of {@code choice} among those the species has, or {@code null}. */
    private @Nullable RawAnimation pick(AnimationChoice choice) {
        int index = pickIndex(choice);
        return index < 0 ? null : DinoAnimationAvailability.raw(choice, index);
    }

    /** The candidate index of a random variant of {@code choice} among those the species has, or -1. */
    private int pickIndex(AnimationChoice choice) {
        int[] available = DinoAnimationAvailability.available(this.dinosaur.getType(), choice);
        return choice.pick(available, this.dinosaur.getRandom().nextDouble());
    }
}
