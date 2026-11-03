package net.dumbcode.projectnublar.entity.dinosaur.animation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.EasingType;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ControllerState;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.animation.Keyframe;
import com.geckolib.cache.animation.KeyframeStack;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoLocator;
import com.geckolib.loading.math.MathParser;
import com.geckolib.loading.math.MathValue;
import com.geckolib.loading.math.MolangQueries;
import com.geckolib.loading.math.value.Constant;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Task 58: the hand-over between a dinosaur's two controllers, with the real GeckoLib 5 classes. A movement and a
 * main controller animate one model, movement first and main written over it, as {@code Dinosaur} registers them.
 * Their handlers use the {@link DinoAnimator} rules: the walk by {@link DinoAnimator#playGait}, its stop by
 * {@link DinoAnimator#blendToRest}, a main clip by {@link DinoAnimator#playMain}, a main stop by
 * {@link DinoAnimator#stopController}. Which state picks which clip is checked in game: {@code DinoAnimator} needs
 * a live {@code Dinosaur}.
 * <p>
 * The clips share a key-framed bone ({@code body}) and a bone that Molang moves with the controller's clock
 * ({@code tail}, {@code query.anim_time}, as the Brachiosaurus idle and walk do), and each has a bone of its own
 * ({@code idleOnly}, {@code walkOnly}). On {@code body} each clip also has a transform the other does not key: the
 * walk moves its position, the idle its scale (as the Brachiosaurus walk moves leg positions its idle does not key).
 * A pose is the six channels of {@link #CHANNELS}. A run moves the walk's bones and transforms, with a tail wave of
 * its own, for a switch inside one controller.
 * <p>
 * No jump (the lead's proof, Task 58): the first hand-over frame repeats the frame before it, the blend moves every
 * channel by the same step on every frame (from a held pose to the new clip's held first frame), and the step out of
 * the blend is no larger than the blend's own step plus the new clip's own motion in one frame.
 */
class ControllerHandOverTest {

    private static final String BODY = "body";
    private static final String TAIL = "tail";
    private static final String IDLE_ONLY = "idleOnly";
    private static final String WALK_ONLY = "walkOnly";
    private static final String[] CHANNELS = {"body rotation", "tail rotation", "idleOnly rotation",
            "walkOnly rotation", "body position", "body scale"};
    private static final int BODY_ROTATION = 0;
    private static final int TAIL_ROTATION = 1;
    private static final int IDLE_ONLY_ROTATION = 2;
    private static final int WALK_ONLY_ROTATION = 3;
    private static final int BODY_POSITION = 4;
    private static final int BODY_SCALE = 5;
    /** Render frames per tick (60 fps). */
    private static final int FRAMES_PER_TICK = 3;
    /** Frames a blend takes: both controllers' transition ticks (5) at speed 1. */
    private static final int BLEND_FRAMES = DinoAnimator.MOVEMENT_TRANSITION_TICKS * FRAMES_PER_TICK;
    /** The idle's cycle, in seconds; its tail wave has the same period. */
    private static final double IDLE_CYCLE = 2D;
    /** The walk's cycle, in seconds; its tail wave has the same period. */
    private static final double WALK_CYCLE = 1D;
    /** The run's cycle, in seconds; its tail wave has the same period. */
    private static final double RUN_CYCLE = 0.5D;
    /** Frames of idle before a walk starts: past its ease-in; its clock (1.57 s) is far from a whole walk cycle. */
    private static final int IDLE_FRAMES = 110;
    /** Frames of walk before a stop: past its ease-in, with every walk channel far from rest. */
    private static final int WALK_FRAMES = 50;
    /** Frames of attack before a walk starts: its clip time is then 0.4 s, short of its keyframe at 0.5 s. */
    private static final int ATTACK_FRAMES = 40;
    /** The largest move of any channel in one frame of the clips themselves (the walk tail: 0.3 x 2 pi / 60). */
    private static final double CLIP_STEP = 0.032D;
    private static final double TOLERANCE = 1.0E-5D;

    private static final MathParser MOLANG = MathParser.create();
    private static final Animation IDLE_CLIP = Animation.create("misc.idle", IDLE_CYCLE, LoopType.LOOP,
            new BoneAnimation[] {
                    new BoneAnimation(BODY, onX(triangle(0.3D, 0.5D, IDLE_CYCLE), 0D), KeyframeStack.EMPTY,
                            onX(triangle(1.0D, 1.1D, IDLE_CYCLE), 1D)),
                    rotated(TAIL, onY(molang("math.sin(query.anim_time * 180) * 0.5", IDLE_CYCLE))),
                    rotated(IDLE_ONLY, onX(triangle(0.4D, 0.6D, IDLE_CYCLE), 0D))},
            Animation.KeyframeMarkers.EMPTY);
    private static final Animation WALK_CLIP = Animation.create("move.walk", WALK_CYCLE, LoopType.LOOP,
            new BoneAnimation[] {
                    new BoneAnimation(BODY, onX(triangle(0.1D, 0.9D, WALK_CYCLE), 0D),
                            onY(triangle(0.5D, 1.0D, WALK_CYCLE)), KeyframeStack.EMPTY),
                    rotated(TAIL, onY(molang("math.sin(query.anim_time * 360) * 0.3", WALK_CYCLE))),
                    rotated(WALK_ONLY, onX(triangle(0.2D, 0.8D, WALK_CYCLE), 0D))},
            Animation.KeyframeMarkers.EMPTY);
    /** Keys every transform the walk keys, so at a walk to run switch only the clock can make a channel jump. */
    private static final Animation RUN_CLIP = Animation.create("run", RUN_CYCLE, LoopType.LOOP,
            new BoneAnimation[] {
                    new BoneAnimation(BODY, onX(triangle(0.2D, 1.0D, RUN_CYCLE), 0D),
                            onY(triangle(0.8D, 1.2D, RUN_CYCLE)), KeyframeStack.EMPTY),
                    rotated(TAIL, onY(molang("math.sin(query.anim_time * 720) * 0.4", RUN_CYCLE))),
                    rotated(WALK_ONLY, onX(triangle(0.3D, 0.9D, RUN_CYCLE), 0D))},
            Animation.KeyframeMarkers.EMPTY);
    /** An action that turns only {@code body}, as an attack that starts during a walk. */
    private static final Animation ATTACK_CLIP = Animation.create("attack1", 1D, LoopType.PLAY_ONCE,
            new BoneAnimation[] {rotated(BODY, onX(triangle(0.2D, 1.0D, 1D), 0D))}, Animation.KeyframeMarkers.EMPTY);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(IDLE_CLIP.name());
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop(WALK_CLIP.name());
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop(RUN_CLIP.name());
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay(ATTACK_CLIP.name());

    private boolean moving;
    /** The gait clip the movement controller plays while moving. */
    private RawAnimation gait = WALK;
    /** What the main controller should play; {@code null} when it should stop. */
    private @Nullable RawAnimation mainClip = IDLE;
    private @Nullable RawAnimation mainPlaying;
    /** The pose of the last frame before the main controller wrote over it. */
    private double[] movementOnly = new double[CHANNELS.length];
    /** Animatable age in ticks; each frame is a third of a tick (60 fps). */
    private double age;
    private final Animatable animatable = new Animatable();
    private final RestartableAnimationController<Animatable> movement = new RestartableAnimationController<>(
            "movement", DinoAnimator.MOVEMENT_TRANSITION_TICKS, this::movementHandler);
    private final RestartableAnimationController<Animatable> main = new RestartableAnimationController<>(
            "dino_controller", DinoAnimator.MAIN_TRANSITION_TICKS, this::mainHandler);
    private final GeoModel<Animatable> model = new GeoModel<>() {
        @Override
        public Identifier getModelResource(GeoRenderState renderState) {
            return null;
        }

        @Override
        public Identifier getTextureResource(GeoRenderState renderState) {
            return null;
        }

        @Override
        public Identifier getAnimationResource(Animatable animatable) {
            return null;
        }

        @Override
        public Animation getBakedAnimation(Animatable animatable, String name) {
            for (Animation clip : new Animation[] {IDLE_CLIP, WALK_CLIP, RUN_CLIP, ATTACK_CLIP}) {
                if (clip.name().equals(name)) {
                    return clip;
                }
            }
            return null;
        }
    };
    /** Partial tick 0.5: at 1.0 GeckoLib asks the Minecraft client for its delta tracker. */
    private final GeoRenderState renderState = new GeoRenderState() {
        @Override
        public float getPartialTick() {
            return 0.5F;
        }

        @Override
        public double getAnimatableAge() {
            return ControllerHandOverTest.this.age;
        }
    };
    private final Map<String, GeoBone> bones = new HashMap<>();

    ControllerHandOverTest() {
        for (String name : new String[] {BODY, TAIL, IDLE_ONLY, WALK_ONLY}) {
            this.bones.put(name, new GeoBone(null, name, new GeoBone[0], new GeoLocator[0], 0, 0, 0, 0, 0, 0) {
                @Override
                public <R extends GeoRenderState> void render(RenderPassInfo<R> renderPassInfo, PoseStack poseStack,
                        VertexConsumer buffer, int packedLight, int packedOverlay, int renderColor) {
                }
            });
        }
    }

    /** The movement handler, as {@link DinoAnimator#movement}: the gait clip while moving, else the blend to rest. */
    private PlayState movementHandler(AnimationTest<Animatable> test) {
        if (!this.moving) {
            return DinoAnimator.blendToRest(test);
        }
        return DinoAnimator.playGait(test, this.gait, 1D, this.main);
    }

    /** The main handler, as {@link DinoAnimator#main} with its {@code set} and {@code stop}. */
    private PlayState mainHandler(AnimationTest<Animatable> test) {
        if (this.mainClip == null) {
            this.mainPlaying = null;
            return DinoAnimator.stopController(test);
        }
        if (this.mainClip != this.mainPlaying) {
            ((RestartableAnimationController<?>) test.controller()).forceAnimationReset();
            this.mainPlaying = this.mainClip;
        }
        return DinoAnimator.playMain(test, this.mainClip, this.movement);
    }

    /**
     * One render frame as the renderer works it out; returns the pose ({@link #CHANNELS}) and keeps the pose before
     * the main controller wrote over it in {@link #movementOnly}.
     */
    private double[] frame() {
        this.age += 1D / FRAMES_PER_TICK;
        Map<String, BoneSnapshot> snapshots = new HashMap<>();
        for (GeoBone bone : this.bones.values()) {
            snapshots.put(bone.name(), BoneSnapshot.create(bone));
        }
        apply(this.movement, snapshots);
        this.movementOnly = pose(snapshots);
        apply(this.main, snapshots);
        return pose(snapshots);
    }

    private void apply(RestartableAnimationController<Animatable> controller, Map<String, BoneSnapshot> snapshots) {
        MolangQueries.Actor<Animatable> actor = new MolangQueries.Actor<>(this.animatable, this.renderState, controller,
                this.age, this.renderState.getPartialTick(), null, null, null);
        ControllerState state = controller.extractControllerState(this.animatable, this.renderState, null, actor,
                this.model);
        if (state != null) {
            AnimationProcessor.createBoneSnapshots(state, name -> Optional.ofNullable(snapshots.get(name)));
        }
    }

    private static double[] pose(Map<String, BoneSnapshot> snapshots) {
        BoneSnapshot body = snapshots.get(BODY);
        return new double[] {body.getRotX(), snapshots.get(TAIL).getRotY(), snapshots.get(IDLE_ONLY).getRotX(),
                snapshots.get(WALK_ONLY).getRotX(), body.getTranslateY(), body.getScaleX()};
    }

    /** Plays {@code frames} frames; returns the pose of the last one. */
    private double[] play(int frames) {
        double[] pose = frame();
        for (int i = 1; i < frames; i++) {
            pose = frame();
        }
        return pose;
    }

    /**
     * Checks that the hand-over that starts on the next frame has no jump (class comment), given the pose of the
     * frame before it; returns the pose of the first frame after the blend.
     */
    private double[] assertNoJump(double[] before, String what) {
        double[] first = frame();
        assertPose(before, first, what + ": the first frame repeats the frame before it");
        double[] last = first;
        double[] blendStep = null;
        for (int i = 1; i < BLEND_FRAMES; i++) {
            double[] next = frame();
            double[] step = minus(next, last);
            if (blendStep == null) {
                blendStep = step;
            }
            assertPose(blendStep, step, what + ": an even blend step on frame " + i);
            last = next;
        }
        double[] after = frame();
        double[] step = minus(after, last);
        for (int c = 0; c < CHANNELS.length; c++) {
            assertTrue(Math.abs(step[c]) <= Math.abs(blendStep[c]) + CLIP_STEP,
                    what + ": the step out of the blend on " + CHANNELS[c] + ": " + step[c]);
        }
        return after;
    }

    /**
     * Walk start: the walk blends from the idle on screen, not from rest. The shared bones move from the idle pose
     * into the walk's first frame, the idle's own bone fades to rest, the walk's own bone moves in from rest, the
     * tail, which both clips move with the clock, does not jump, and neither does the body scale the walk does not
     * key.
     */
    @Test
    void aWalkStartBlendsFromTheIdlePose() {
        double[] idle = play(IDLE_FRAMES);
        assertTrue(idle[BODY_ROTATION] > 0.3D && Math.abs(idle[TAIL_ROTATION]) > 0.1D
                && idle[IDLE_ONLY_ROTATION] > 0.4D && idle[BODY_SCALE] > 1.02D, "the idle moves its channels");
        this.moving = true;
        this.mainClip = null;
        double[] walk = assertNoJump(idle, "walk start");
        assertEquals(0D, walk[IDLE_ONLY_ROTATION], TOLERANCE, "the idle's own bone is at rest after the blend");
        assertEquals(1D, walk[BODY_SCALE], TOLERANCE, "the scale the walk does not key is at rest after the blend");
    }

    /**
     * Stop: the idle blends from the walk on screen. The shared bones move from the walk's stop pose into the idle's
     * first frame (main), the body position the idle does not key eases to rest, and the walk's own bone, which the
     * idle does not move, fades to rest in the movement controller's blend, untouched by the main controller.
     */
    @Test
    void aStopBlendsTheIdleFromTheWalkPose() {
        this.mainClip = null;
        this.moving = true;
        double[] walk = play(WALK_FRAMES);
        assertTrue(walk[WALK_ONLY_ROTATION] > 0.2D && walk[BODY_POSITION] > 0.5D, "the walk moves its channels");
        this.moving = false;
        this.mainClip = IDLE;
        double[] idle = assertNoJump(walk, "stop");
        assertEquals(0D, idle[WALK_ONLY_ROTATION], TOLERANCE, "the walk's own bone is at rest after the blend");
        assertEquals(0D, idle[BODY_POSITION], TOLERANCE, "the position the idle does not key is at rest");
    }

    /**
     * GeckoLib 5.5.6 puts a transform the new clip does not key at rest on the first frame of a blend
     * ({@code findTransitionPointValue}). The ease-in of a hand-over plays a copy of the clip with a rest key there, so
     * the body position, which only the walk keys, moves to rest in even steps; after the ease-in the clip itself
     * plays, unchanged.
     */
    @Test
    void aTransformTheNewClipDoesNotKeyEasesToRestAndThenTheClipPlaysUnchanged() {
        this.mainClip = null;
        this.moving = true;
        double stopPosition = play(WALK_FRAMES)[BODY_POSITION];
        this.moving = false;
        this.mainClip = IDLE;
        for (int i = 0; i < BLEND_FRAMES; i++) {
            assertEquals(stopPosition * (1D - i / (double) BLEND_FRAMES), frame()[BODY_POSITION], TOLERANCE,
                    "frame " + i);
        }
        play(2);
        assertSame(IDLE_CLIP, this.main.getCurrentAnimationPoint().animation(), "the clip itself plays");
    }

    /**
     * One clock: through the walk's ease-in its clock holds the idle's clock, so the tail that both clips move with
     * {@code query.anim_time} holds its pose. Then the clock drifts into step with the walk's own time, by at most
     * half a cycle, and after one walk cycle it is the walk's time plus a whole number of cycles, so the tail wave is
     * back in phase with the walk's keyframes.
     */
    @Test
    void theClockCarriesOverAndDriftsIntoStepWithinOneCycle() {
        play(IDLE_FRAMES);
        double idleClock = this.main.getCurrentAnimationTime();
        double drift = idleClock - WALK_CYCLE * Math.rint(idleClock / WALK_CYCLE);
        assertTrue(Math.abs(drift) > 0.2D, "the idle clock is far from a whole number of walk cycles: " + idleClock);
        this.moving = true;
        this.mainClip = null;
        for (int i = 0; i < BLEND_FRAMES; i++) {
            frame();
            assertEquals(idleClock, this.movement.getCurrentAnimationTime(), 1.0E-9D, "held on blend frame " + i);
        }
        double lastClock = idleClock;
        double lastClipTime = 0D;
        for (int i = 0; ; i++) {
            assertTrue(i < 2 * FRAMES_PER_TICK * 20 * WALK_CYCLE, "the walk wraps within one cycle");
            frame();
            double clock = this.movement.getCurrentAnimationTime();
            double clipTime = this.movement.getCurrentAnimationPoint().animTime();
            if (clipTime < lastClipTime) {
                break;
            }
            assertTrue(clock >= lastClock, "the clock never runs back: " + lastClock + " to " + clock);
            assertTrue(Math.abs(clock - clipTime - idleClock) <= Math.abs(drift) + TOLERANCE,
                    "it drifts by no more than " + drift);
            lastClock = clock;
            lastClipTime = clipTime;
        }
        double carried = this.movement.getCurrentAnimationTime() - this.movement.getCurrentAnimationPoint().animTime();
        assertEquals(idleClock - drift, carried, 1.0E-9D, "in step after one cycle: a whole number of cycles");
    }

    /**
     * A switch inside one controller (Task 61), walk to run in mid-cycle: GeckoLib 5 works out the walk's pose it
     * blends from with the controller's clock, so the clock carries over and the tail, which the walk moves with
     * {@code query.anim_time}, does not jump on the first frame (before the fix it went to the walk's pose at time 0).
     */
    @Test
    void aSwitchInsideOneControllerKeepsTheTailPose() {
        this.mainClip = null;
        this.moving = true;
        double[] walk = play(WALK_FRAMES);
        assertTrue(Math.abs(walk[TAIL_ROTATION]) > 0.1D, "the walk's tail is far from its pose at clip time 0");
        this.gait = RUN;
        assertPose(walk, frame(), "the first frame of the switch repeats the frame before it");
    }

    /**
     * An attack that starts during a walk blends from the walk pose on the bone it moves, and leaves every other bone
     * to the walk ({@link RestartableAnimationController.HandOver#SHARED_BONES}): a fade there would freeze the walk's
     * own bone and the tail, then let them jump back into the walk.
     */
    @Test
    void aMainClipThatStartsDuringAWalkLeavesTheWalkItsOtherBones() {
        this.mainClip = null;
        this.moving = true;
        play(WALK_FRAMES);
        this.mainClip = ATTACK;
        for (int i = 0; i <= BLEND_FRAMES; i++) {
            double[] withMain = frame();
            if (i == 0) {
                // The main controller runs after the walk moved on this frame, so it starts from this frame's pose.
                assertEquals(this.movementOnly[BODY_ROTATION], withMain[BODY_ROTATION], TOLERANCE,
                        "the attack starts from the walk pose");
            }
            assertEquals(this.movementOnly[TAIL_ROTATION], withMain[TAIL_ROTATION], TOLERANCE,
                    "the tail stays the walk's, frame " + i);
            assertEquals(this.movementOnly[WALK_ONLY_ROTATION], withMain[WALK_ONLY_ROTATION], TOLERANCE,
                    "the walk's own bone stays the walk's, frame " + i);
        }
    }

    /**
     * A walk that starts while a main clip plays on (an attack here, as eat and drink do) blends from a copy of the
     * pose it took. GeckoLib moves a clip point's keyframe indices on in place ({@code createNext}), so without the
     * copy the attack, which passes its keyframe during the walk's ease-in, would move the walk's from-pose with it.
     * The main controller writes over the body, so this checks the walk's own pose, which shows if the attack ends
     * during the ease-in.
     */
    @Test
    void aWalkThatStartsDuringAnAttackBlendsFromThePoseItTook() {
        this.mainClip = ATTACK;
        double[] attack = play(ATTACK_FRAMES);
        double keyframe = ATTACK_CLIP.length() / 2D;
        assertTrue(this.main.getCurrentAnimationPoint().animTime() < keyframe, "the attack is short of its keyframe");
        this.moving = true;
        frame();
        assertEquals(attack[BODY_ROTATION], this.movementOnly[BODY_ROTATION], TOLERANCE,
                "the walk starts from the attack pose");
        double last = this.movementOnly[BODY_ROTATION];
        double blendStep = 0D;
        for (int i = 1; i < BLEND_FRAMES; i++) {
            frame();
            double step = this.movementOnly[BODY_ROTATION] - last;
            if (i == 1) {
                blendStep = step;
            }
            assertEquals(blendStep, step, TOLERANCE, "an even blend step on frame " + i);
            last = this.movementOnly[BODY_ROTATION];
        }
        assertTrue(this.main.getCurrentAnimationPoint().animTime() > keyframe, "the attack passed its keyframe");
    }

    /**
     * A main clip that stops still stops at once ({@link DinoAnimator#stopController}), as before Task 58: on the
     * frame the walk starts, the main controller writes no bone, and the walk shows the idle pose it took over.
     */
    @Test
    void aMainClipThatStopsStopsAtOnce() {
        double[] idle = play(IDLE_FRAMES);
        this.moving = true;
        this.mainClip = null;
        double[] first = frame();
        assertFalse(this.main.isAnimatingBones(), "the main controller ended");
        assertNull(this.main.getCurrentAnimationPoint());
        assertPose(idle, this.movementOnly, "the walk alone shows the idle pose");
        assertPose(idle, first, "on screen");
    }

    private static void assertPose(double[] expected, double[] actual, String message) {
        for (int c = 0; c < CHANNELS.length; c++) {
            assertEquals(expected[c], actual[c], TOLERANCE, message + " (" + CHANNELS[c] + ")");
        }
    }

    private static double[] minus(double[] a, double[] b) {
        double[] difference = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            difference[i] = a[i] - b[i];
        }
        return difference;
    }

    /** A bone animation with only a rotation. */
    private static BoneAnimation rotated(String name, KeyframeStack rotation) {
        return new BoneAnimation(name, rotation, KeyframeStack.EMPTY, KeyframeStack.EMPTY);
    }

    /** {@code keyframes} on the x axis, the other two held at {@code still}, as GeckoLib 5.5.6 bakes a transform. */
    private static KeyframeStack onX(Keyframe[] keyframes, double still) {
        return new KeyframeStack(keyframes, hold(still), hold(still));
    }

    /** {@code keyframes} on the y axis, the other two held at 0. */
    private static KeyframeStack onY(Keyframe[] keyframes) {
        return new KeyframeStack(hold(0D), keyframes, hold(0D));
    }

    private static Keyframe[] hold(double value) {
        return new Keyframe[] {new Keyframe(0D, 0D, new Constant(value), new Constant(value), EasingType.LINEAR)};
    }

    /** Keyframes from {@code low} up to {@code high} at half the cycle and back, linear, as GeckoLib 5.5.6 bakes. */
    private static Keyframe[] triangle(double low, double high, double cycle) {
        double half = cycle / 2D;
        return new Keyframe[] {
                new Keyframe(0D, 0D, new Constant(low), new Constant(low), EasingType.LINEAR),
                new Keyframe(half, half, new Constant(low), new Constant(high), EasingType.LINEAR),
                new Keyframe(cycle, half, new Constant(high), new Constant(low), EasingType.LINEAR)};
    }

    /** Keyframes that hold the Molang {@code expression} through the cycle, as GeckoLib bakes a single value. */
    private static Keyframe[] molang(String expression, double cycle) {
        MathValue value = MOLANG.compileMolang(expression);
        return new Keyframe[] {
                new Keyframe(0D, 0D, value, value, EasingType.LINEAR),
                new Keyframe(cycle, cycle, value, value, EasingType.LINEAR)};
    }

    private static final class Animatable implements GeoAnimatable {
        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return null;
        }
    }
}
