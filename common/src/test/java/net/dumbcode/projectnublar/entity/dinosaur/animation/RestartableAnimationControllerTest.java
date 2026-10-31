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
import com.geckolib.loading.math.value.Constant;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives a real GeckoLib 5 controller the way {@link DinoAnimator#movement} does: a looping walk while
 * {@link #moving} ({@link DinoAnimator#playGait}), else {@link DinoAnimator#blendToRest}. The walk is a one-second
 * clip that turns one bone, {@code leg}, from 0 to {@link #RAMP} radians, so a frame's pose is the leg's angle
 * ({@link #legAngle}). Which {@link DinoAnimator} handlers end with {@code blendToRest} or {@code stopController} is
 * checked in game, not here: {@code DinoAnimator} needs a live {@code Dinosaur}.
 */
class RestartableAnimationControllerTest {

    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    /** The leg's angle at the end of the walk clip, in radians. */
    private static final double RAMP = 0.7D;
    /** The walk: one second, the leg turns linearly from 0 to {@link #RAMP} (keyframes as GeckoLib 5.5.6 bakes). */
    private static final Animation CLIP = Animation.create("move.walk", 1D, LoopType.LOOP, new BoneAnimation[] {
            new BoneAnimation("leg", new KeyframeStack(ramp(0D, RAMP), ramp(0D, 0D), ramp(0D, 0D)),
                    KeyframeStack.EMPTY, KeyframeStack.EMPTY)},
            Animation.KeyframeMarkers.EMPTY);
    private static final RawAnimation STRIDE = RawAnimation.begin().thenLoop("move.stride");
    /** The leg's angle on the first frame of {@link #STRIDE_CLIP}, in radians: away from rest, as in a real walk. */
    private static final double STRIDE_START = 0.7D;
    /** A walk that starts away from rest: one second, the leg turns linearly from {@link #STRIDE_START} to 0. */
    private static final Animation STRIDE_CLIP = Animation.create("move.stride", 1D, LoopType.LOOP,
            new BoneAnimation[] {
                    new BoneAnimation("leg", new KeyframeStack(ramp(STRIDE_START, 0D), ramp(0D, 0D), ramp(0D, 0D)),
                            KeyframeStack.EMPTY, KeyframeStack.EMPTY)},
            Animation.KeyframeMarkers.EMPTY);
    /** The leg rate of a grazing Velociraptor's walk ({@link MovementGait#MIN_ANIMATION_SPEED}): about 0.25. */
    private static final double GRAZE_RATE = 0.25D;
    /** Frames of walk that pass the first loop wrap: the blend (15 frames) plus the one-second clip (60 frames). */
    private static final int PAST_A_LOOP_WRAP = 90;
    /** Render frames per tick (60 fps). */
    private static final int FRAMES_PER_TICK = 3;
    /** Frames the blend to rest takes: the movement controller's transition ticks at speed 1. */
    private static final int BLEND_FRAMES = DinoAnimator.MOVEMENT_TRANSITION_TICKS * FRAMES_PER_TICK;

    private boolean moving;
    /** The clip {@link #moving} plays, and its gait rate (1 plays the clip as made). */
    private RawAnimation walkClip = WALK;
    private double gaitRate = 1D;
    /** Plays the ease-in at the gait rate too: the speed rule before Task 56, to show the jump it gave. */
    private boolean easeInAtGaitRate;
    /** Animatable age in ticks; each frame is a third of a tick (60 fps). */
    private double age;
    private final Animatable animatable = new Animatable();
    private final RestartableAnimationController<Animatable> controller = new RestartableAnimationController<>(
            "movement", DinoAnimator.MOVEMENT_TRANSITION_TICKS, this::handle);
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
            return name.equals(STRIDE_CLIP.name()) ? STRIDE_CLIP : CLIP;
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
            return RestartableAnimationControllerTest.this.age;
        }
    };
    private final GeoBone leg = new GeoBone(null, "leg", new GeoBone[0], new GeoLocator[0], 0, 0, 0, 0, 0, 0) {
        @Override
        public <R extends GeoRenderState> void render(RenderPassInfo<R> renderPassInfo, PoseStack poseStack,
                VertexConsumer buffer, int packedLight, int packedOverlay, int renderColor) {
        }
    };

    /** The clip's keyframes for one axis: {@code start} at the start, {@code end} after one second, linear. */
    private static Keyframe[] ramp(double start, double end) {
        return new Keyframe[] {
                new Keyframe(0D, 0D, new Constant(start), new Constant(start), EasingType.LINEAR),
                new Keyframe(1D, 1D, new Constant(start), new Constant(end), EasingType.LINEAR)};
    }

    /**
     * The controller's handler, as {@link DinoAnimator#movement}: while {@link #moving}, the walk by the Task 56
     * speed rule (or, with {@link #easeInAtGaitRate}, the rule before it), else the blend to rest.
     */
    private PlayState handle(AnimationTest<Animatable> test) {
        if (!this.moving) {
            return DinoAnimator.blendToRest(test);
        }
        if (this.easeInAtGaitRate) {
            test.controller().setAnimationSpeed(this.gaitRate);
            return test.setAndContinue(this.walkClip);
        }
        return DinoAnimator.playGait(test, this.walkClip, this.gaitRate);
    }

    /** One render frame: what the renderer asks the controller; {@code null} when it animates no bone. */
    private @Nullable ControllerState frame() {
        this.age += 1D / FRAMES_PER_TICK;
        return this.controller.extractControllerState(this.animatable, this.renderState, null, null, this.model);
    }

    /** The leg's angle that a frame's state gives, as the renderer works it out; 0 (rest) when nothing animates. */
    private double legAngle(@Nullable ControllerState state) {
        BoneSnapshot snapshot = BoneSnapshot.create(this.leg);
        if (state != null) {
            AnimationProcessor.createBoneSnapshots(state,
                    name -> name.equals("leg") ? Optional.of(snapshot) : Optional.empty());
        }
        return snapshot.getRotX();
    }

    /** Walks {@code frames} frames; returns the leg's angle on the last one. */
    private double walk(int frames) {
        this.moving = true;
        double angle = 0D;
        for (int i = 0; i < frames; i++) {
            angle = legAngle(frame());
        }
        return angle;
    }

    /**
     * Task 53: the leg moves from the pose it had when the walk stopped to rest over the transition ticks, in a
     * straight line, with no jump at the start (the stop frame repeats the last walk frame) or at the end; then the
     * controller animates nothing, also later, when GeckoLib would fade the empty clip's end a second time.
     */
    @Test
    void aStopBlendsTheBonesFromTheirStopPoseToRestOnce() {
        double stopPose = walk(PAST_A_LOOP_WRAP + 10);
        assertTrue(Math.abs(stopPose) > 0.1D, "the leg is away from rest when the walk stops: " + stopPose);
        this.moving = false;
        for (int i = 0; i < BLEND_FRAMES; i++) {
            ControllerState state = frame();
            assertNotNull(state, "still blending on frame " + i);
            assertEquals(stopPose * (1D - i / (double) BLEND_FRAMES), legAngle(state), 1.0E-5D, "frame " + i);
        }
        // This frame either ends the blend or is its last one, at 1 of the way (the frame times add up in floating
        // point); both show rest.
        assertEquals(0D, legAngle(frame()), 1.0E-5D, "at rest when the transition ticks are over");
        for (int i = 0; i < PAST_A_LOOP_WRAP; i++) {
            assertNull(frame(), "frame " + i + " after the blend");
        }
        assertFalse(this.controller.isAnimatingBones());
    }

    /** The blend's pose at its first, middle and last tick (5 ticks of 3 frames): 1, 0.6 and 0.2 of the stop pose. */
    @Test
    void theBlendPassesThroughTheStopPoseShareOfEachTick() {
        double stopPose = walk(PAST_A_LOOP_WRAP + 10);
        this.moving = false;
        double[] perTick = new double[DinoAnimator.MOVEMENT_TRANSITION_TICKS];
        for (int i = 0; i < BLEND_FRAMES; i++) {
            double angle = legAngle(frame());
            if (i % FRAMES_PER_TICK == 0) {
                perTick[i / FRAMES_PER_TICK] = angle;
            }
        }
        assertEquals(stopPose, perTick[0], 1.0E-5D, "first tick: the stop pose");
        assertEquals(stopPose * 0.6D, perTick[2], 1.0E-5D, "middle tick");
        assertEquals(stopPose * 0.2D, perTick[4], 1.0E-5D, "last tick");
    }

    /**
     * Molang reads {@code query.anim_time} from the controller's clip time. During the blend it stays the time of the
     * stop pose, so a bone moved by a Molang expression of the time holds its pose too; after it, nothing plays.
     */
    @Test
    void theBlendKeepsTheStopTimeForMolang() {
        walk(PAST_A_LOOP_WRAP + 10);
        double stopTime = this.controller.getCurrentAnimationTime();
        assertNotEquals(0D, stopTime);
        this.moving = false;
        for (int i = 0; i < BLEND_FRAMES; i++) {
            frame();
            assertEquals(stopTime, this.controller.getCurrentAnimationTime(), 1.0E-9D, "frame " + i);
        }
        // The blend ends on the next frame or the one after (the frame times add up in floating point).
        frame();
        frame();
        assertEquals(0D, this.controller.getCurrentAnimationTime(), "nothing plays after the blend");
    }

    /** A stop when nothing plays has nothing to blend: it ends at once. */
    @Test
    void aStopWithNothingPlayingEndsAtOnce() {
        this.moving = false;
        assertNull(frame());
        assertFalse(this.controller.isAnimatingBones());
    }

    /**
     * GeckoLib 5 blends a new clip in from one clip's pose. During the blend to rest that is the empty clip, so a walk
     * that starts then blends in from rest (on the movement controller the gait's 8-tick hold keeps a walk from
     * starting during the 5-tick blend).
     */
    @Test
    void aWalkThatStartsDuringTheBlendBlendsIn() {
        walk(PAST_A_LOOP_WRAP + 10);
        this.moving = false;
        for (int i = 0; i < BLEND_FRAMES / 2; i++) {
            frame();
        }
        this.moving = true;
        ControllerState start = frame();
        assertNotNull(start);
        assertEquals(0D, start.transitionTime(), "a new blend-in");
        assertNotNull(start.prevAnimationPoint());
        assertEquals(0D, legAngle(start), 1.0E-5D, "from the empty clip's pose: rest");
        double played = walk(PAST_A_LOOP_WRAP);
        assertTrue(this.controller.isAnimatingBones());
        assertNotEquals(0D, played, "the walk plays");
    }

    /**
     * Task 56: a slow walk (a grazing Velociraptor, rate 0.25) that stops at the end of the gait's shortest hold
     * ({@link MovementGait#HOLD_TICKS}) blends to rest from the pose on screen. Its ease-in played at a speed of at
     * least 1, so it was over after the transition ticks (5, and one frame: {@link DinoAnimator#playGait}), before
     * the stop.
     */
    @Test
    void aSlowWalkThatStopsAfterItsHoldBlendsToRestWithoutAJump() {
        double lastWalkFrame = walkASlowStrideForTheHold();
        assertFalse(this.controller.isTransitioning(), "the ease-in is over before the hold ends");
        this.moving = false;
        assertEquals(lastWalkFrame, legAngle(frame()), 1.0E-5D, "the first blend frame repeats the last walk frame");
    }

    /**
     * The rule before Task 56 played the ease-in at the gait rate too: 5 / 0.25 = 20 ticks, so the same stop lands
     * inside it. There GeckoLib's point is the clip's first frame, not the pose shown (rest eased about 40 % of the
     * way to that frame), and the blend to rest starts from that first frame: a jump of about 60 % of its pose.
     */
    @Test
    void anEaseInAtTheGaitRateJumpsWhenTheWalkStopsInsideIt() {
        this.easeInAtGaitRate = true;
        double lastWalkFrame = walkASlowStrideForTheHold();
        assertTrue(this.controller.isTransitioning(), "the stop lands inside the ease-in");
        this.moving = false;
        double firstBlendFrame = legAngle(frame());
        assertEquals(STRIDE_START, Math.abs(firstBlendFrame), 1.0E-5D, "the blend starts from the clip's first frame");
        assertTrue(Math.abs(firstBlendFrame - lastWalkFrame) > STRIDE_START / 2D,
                "a jump at the stop: " + lastWalkFrame + " to " + firstBlendFrame);
    }

    /**
     * Starts {@link #STRIDE} from rest at {@link #GRAZE_RATE} and walks it {@link MovementGait#HOLD_TICKS}, the
     * earliest tick the gait can stop; returns the leg's angle on the last walk frame.
     */
    private double walkASlowStrideForTheHold() {
        this.walkClip = STRIDE;
        this.gaitRate = GRAZE_RATE;
        return walk(MovementGait.HOLD_TICKS * FRAMES_PER_TICK);
    }

    /** With no from-point, GeckoLib 5 fades from the first frame toward rest and jumps back (the start pop). */
    @Test
    void firstStartBlendsFromRest() {
        this.moving = true;
        ControllerState start = frame();
        assertNotNull(start);
        assertEquals(0D, start.transitionTime());
        assertNotNull(start.prevAnimationPoint());
    }

    /** After the blend nothing plays, so a restart needs the rest point as much as a first start does. */
    @Test
    void startAfterAStopBlendsFromRest() {
        walk(PAST_A_LOOP_WRAP);
        this.moving = false;
        for (int i = 0; i <= BLEND_FRAMES + 1; i++) {
            frame();
        }
        assertFalse(this.controller.isAnimatingBones());
        this.moving = true;
        ControllerState start = frame();
        assertNotNull(start);
        assertEquals(0D, start.transitionTime());
        assertNotNull(start.prevAnimationPoint());
    }

    /**
     * GeckoLib clears the from-point at a loop wrap and {@code setAndContinue} sets the same clip every frame: the rest
     * point is only for a new start, so a playing clip keeps GeckoLib's own (empty) from-point.
     */
    @Test
    void aPlayingClipKeepsItsOwnFromPoint() {
        walk(PAST_A_LOOP_WRAP);
        ControllerState playing = frame();
        assertNotNull(playing);
        assertNull(playing.prevAnimationPoint());
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
