package net.dumbcode.projectnublar.entity.dinosaur.animation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.EasingType;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.animation.Keyframe;
import com.geckolib.cache.animation.KeyframeStack;
import com.geckolib.loading.math.value.Constant;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * An {@link AnimationController} that can play its current animation again from the first frame, that blends
 * a start from rest or from another controller's pose, and that can blend its bones back to rest when it stops.
 * <p>
 * Why it exists: GeckoLib 4 had {@code AnimationController.forceAnimationReset()}. After that call, the next
 * {@code setAndContinue} started its animation from the first frame even when it was the animation already
 * playing, and blended into it from the current pose over the controller's transition ticks. GeckoLib 5 removed
 * the method: {@code setAnimation} ignores an animation equal to the current one, so a repeated attack, a new
 * one-shot or a repeated idle would not start again. {@link DinoAnimator} needs that restart.
 * <p>
 * {@link #forceAnimationReset} forgets which animation is current, so the next {@code setAnimation} takes
 * GeckoLib's own path for a new animation: a new timeline from the first frame, blended from the current pose.
 * <p>
 * Start from rest: when nothing plays (the first start, or a start after a stop), GeckoLib 5 has no pose to blend
 * from. It then fades from the clip's first frame toward rest and jumps back to the first frame when the blend ends
 * (75 degrees on a Velociraptor foot). {@link #setAnimation} gives such a start {@link #REST} to blend from instead,
 * so the bones blend from rest into the first frame, as in GeckoLib 4.
 * <p>
 * Hand-over (Task 58): a dinosaur's two controllers animate one model, the later one written over every bone they
 * share ({@code AnimationProcessor.createBoneSnapshots}, javap of 5.5.6), so a start from rest as the other one stops
 * snaps the shared bones to rest. {@link #setAnimation(RawAnimation, RestartableAnimationController, HandOver)} lets
 * a start with nothing playing blend from a copy of the other controller's clip point instead. GeckoLib writes all
 * three transforms of each bone a clip animates and, in a blend, puts a transform the new clip has no keyframes for
 * at rest at once ({@code findTransitionPointValue}), so a hand-over's ease-in plays a copy of the clip with a rest
 * key there; then the clip plays unchanged. GeckoLib blends from one clip point: a controller still in its own
 * ease-in hands over its next clip's first frame, not the pose shown.
 * <p>
 * One clock: a controller has one Molang clock ({@link #getCurrentAnimationTime}, {@code query.anim_time}), and
 * GeckoLib works out the pose it blends from with it too (the Brachiosaurus idle and walk move the tail with it). So
 * a hand-over's clock starts at the other controller's clock, holds through the ease-in, then drifts into step with
 * the clip's own time (the same time within a whole number of cycles) over one cycle, by at most half a cycle. A
 * controller built with {@link SwitchClock#CARRY} also keeps its clock in this way when a new clip replaces the clip
 * that plays (the walk and run, Task 61); a {@link SwitchClock#RESTART} controller starts the new clip at time 0.
 * <p>
 * Stop with a blend (Task 53): GeckoLib 5 has no fade for a stopped loop (GeckoLib 4 faded the bones back over the
 * transition ticks), and {@code reset()} alone snaps the bones to rest. {@link #blendToRest} plays an empty clip
 * ({@link #REST_BLEND}) instead. GeckoLib keeps the pose of the clip that played as the point to blend from, and
 * during the blend it moves every bone of that point that the empty clip does not animate (all of them) from its
 * pose to rest ({@code AnimationProcessor.createBoneSnapshots}, javap of 5.5.6). Molang reads the time of that pose
 * during the blend ({@link #getCurrentAnimationTime}). When the blend ends, {@code reset()} ends the animation, so
 * no bone moves after it. The empty clip must not play to its own end: GeckoLib keeps the from-point through the
 * clip, so the fade at the end of the clip would move the bones from the old pose to rest a second time.
 *
 * @param <T> the animatable type
 */
public class RestartableAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
    /** The pose a start from rest blends from: an empty one-tick clip, so every bone blends from rest. */
    private static final AnimationPoint REST =
            AnimationPoint.createFor(Animation.generateWaitAnimation(0.05), null, LoopType.PLAY_ONCE, 0);
    /**
     * The clip {@link #blendToRest} plays: an empty one-tick wait. Its timeline starts with the blend (the
     * controller's transition ticks); the blend ends before the clip itself plays.
     */
    private static final RawAnimation REST_BLEND = RawAnimation.begin().thenWait(1);

    /** What the clock does when a new clip replaces a clip that plays on this controller (P194). */
    public enum SwitchClock {
        /**
         * The clock goes on from the time shown, so a bone that Molang moves with the time in the old clip does not
         * jump on the switch's first frame. For the walk and run, whose Molang waves (the Brachiosaurus tail) run free.
         */
        CARRY,
        /**
         * The new clip starts at its own time 0, as Molang timed to its keyframes expects (a T. rex bite, a
         * Velociraptor drink). A bone that Molang moves with the time in the old clip jumps to its pose at time 0 on
         * the switch's first frame (up to 21 degrees of Brachiosaurus tail out of its idle).
         */
        RESTART
    }

    /** Which bones of the other controller's pose a hand-over blends from. */
    enum HandOver {
        /** Every bone: the other controller stops, so a bone only its clip moves fades to rest here. */
        WHOLE_POSE,
        /** Only the bones the new clip moves: the other controller keeps playing or blending the others. */
        SHARED_BONES
    }

    /** What the clock does at a clip switch. */
    private final SwitchClock switchClock;
    /** The clock start of the current clip (class comment, one clock); 0 for GeckoLib's own clock. */
    private double clockStart;
    /** Whether the current clip has played one full cycle, so its clock drift is over. */
    private boolean clockInStep;
    /** The clip time on the last frame, to see the first loop wrap. */
    private double lastClipTime;
    /** A hand-over that started this frame, until GeckoLib loaded the new clip; {@code null} otherwise. */
    private @Nullable HandOver handOverBones;
    /** The new clip's first frame with rest keys, shown through a hand-over's ease-in; {@code null} otherwise. */
    private @Nullable AnimationPoint easeInPoint;
    /** GeckoLib's own point during that ease-in, given back to GeckoLib before each frame step. */
    private @Nullable AnimationPoint clipPoint;

    /**
     * Same as {@link AnimationController#AnimationController(String, int, AnimationStateHandler)}; a clip switch
     * starts the new clip at its own time 0 ({@link SwitchClock#RESTART}).
     */
    public RestartableAnimationController(String name, int transitionTicks, AnimationStateHandler<T> handler) {
        this(name, transitionTicks, handler, SwitchClock.RESTART);
    }

    /**
     * Same as {@link AnimationController#AnimationController(String, int, AnimationStateHandler)}, with what the
     * clock does at a clip switch.
     *
     * @param name            the controller's name
     * @param transitionTicks the blend length, in ticks
     * @param handler         the state handler
     * @param switchClock     what the clock does when a new clip replaces a clip that plays here
     * @throws NullPointerException when {@code switchClock} is {@code null}
     */
    public RestartableAnimationController(String name, int transitionTicks, AnimationStateHandler<T> handler,
            SwitchClock switchClock) {
        super(name, transitionTicks, handler);
        this.switchClock = Objects.requireNonNull(switchClock, "switchClock");
    }

    /**
     * Makes the next {@code setAnimation} (or {@code AnimationTest.setAndContinue}) start its animation from the
     * first frame, even when it is the animation already playing, blending from the current pose.
     */
    public void forceAnimationReset() {
        this.currentRawAnimation = null;
    }

    /** GeckoLib's {@code setAnimation}; a new animation with no pose to blend from blends from {@link #REST}. */
    @Override
    public void setAnimation(RawAnimation animation) {
        setAnimation(animation, null, HandOver.WHOLE_POSE);
    }

    /**
     * GeckoLib's {@code setAnimation} with a hand-over (class comment): a new animation with no pose to blend from
     * blends from {@code giver}'s pose and starts at its clock, or blends from {@link #REST} when {@code giver}
     * animates no bone. A new animation that replaces a clip playing here keeps this controller's clock when it was
     * built with {@link SwitchClock#CARRY}, because GeckoLib 5 works out the old clip's pose with the clock on every
     * frame of the blend, so a bone that Molang moves with the time would else jump to its pose at time 0 (52 degrees
     * of Brachiosaurus tail at a run to walk switch); with {@link SwitchClock#RESTART} it starts at its own time 0.
     * The handler calls it on every frame the clip should play; it acts on the frame the clip starts.
     *
     * @param giver the animatable's other controller, or {@code null} for none
     * @param bones which bones of {@code giver}'s pose to blend from
     * @throws NullPointerException when {@code bones} is {@code null}
     */
    void setAnimation(RawAnimation animation, @Nullable RestartableAnimationController<?> giver,
            HandOver bones) {
        // A null in handOverBones means no hand-over: the from-point would stay the giver's own point.
        Objects.requireNonNull(bones, "bones");
        RawAnimation previous = this.currentRawAnimation;
        // Read before super, which drops the clip point the clock reads.
        double clock = getCurrentAnimationTime();
        super.setAnimation(animation);
        // Only when super took a new animation: GeckoLib also clears the from-point while a clip plays (at each loop
        // wrap), and the handler calls this every frame, so without the check REST would be set on every frame of a
        // playing clip.
        if (this.currentRawAnimation == previous) {
            return;
        }
        this.clockStart = 0D;
        this.clockInStep = false;
        this.lastClipTime = 0D;
        this.handOverBones = null;
        this.easeInPoint = null;
        if (this.transitionFromPoint != null) {
            // GeckoLib blends from the clip that played here; CARRY: the clock goes on from the time shown.
            if (this.switchClock == SwitchClock.CARRY) {
                this.clockStart = clock;
            }
            return;
        }
        AnimationPoint pose = giver == null ? null : giver.handOverPose();
        if (pose == null) {
            this.transitionFromPoint = REST;
            return;
        }
        // Copied in checkControllerState, once GeckoLib loaded the new clip, before the giver moves on.
        this.transitionFromPoint = pose;
        this.clockStart = giver.getCurrentAnimationTime();
        this.handOverBones = bones;
    }

    /**
     * Stops the controller with a blend of its bones from their current pose to rest over the transition ticks
     * (class comment), then ends the animation as {@code reset()} does. The state handler calls it, and returns
     * its result, on every frame the controller should not play. When nothing plays, it ends at once.
     * <p>
     * The blend plays at speed 1, so it lasts the transition ticks whatever the speed of the clip that stopped.
     * GeckoLib blends from one clip's pose, never from a pose between two: a clip that starts during the blend blends
     * in from the empty clip's pose (rest), and a stop while the clip itself still blends in blends from the clip's
     * own point (its first frame), not from the pose shown. {@link DinoAnimator} never stops a walk there: its
     * ease-in plays at a speed of at least 1 ({@link DinoAnimator#playGait}, Task 56), so it is over before the
     * gait's shortest hold ends.
     *
     * @return {@code CONTINUE} while the blend plays, else {@code STOP}
     */
    public PlayState blendToRest() {
        if (this.currentRawAnimation != REST_BLEND) {
            if (!isAnimatingBones()) {
                reset();
                return PlayState.STOP;
            }
            double stopClock = getCurrentAnimationTime();
            // GeckoLib keeps the current point as the point to blend from (setAnimation).
            setAnimation(REST_BLEND);
            // setAnimation carries the clock only for CARRY, and only when GeckoLib keeps a from-point (timeline time
            // above 0); the blend always needs the stop time.
            this.clockStart = stopClock;
            setAnimationSpeed(1D);
            return PlayState.CONTINUE;
        }
        if (isBlendingToRest()) {
            return PlayState.CONTINUE;
        }
        reset();
        return PlayState.STOP;
    }

    /**
     * GeckoLib's clip time, which Molang reads as {@code query.anim_time} (and a keyframe event as its tick), from a
     * carried start (class comment, one clock). The clip time is 0 through an ease-in and a {@link #blendToRest}, so
     * the clock holds its start there: GeckoLib works out the pose it blends from on every frame, and a bone that a
     * Molang expression of the time moves would else jump (21 degrees of Brachiosaurus tail at a stop).
     */
    @Override
    public double getCurrentAnimationTime() {
        double clipTime = super.getCurrentAnimationTime();
        if (this.clockStart == 0D || this.animationPoint == null) {
            return clipTime;
        }
        double cycle = this.animationPoint.animation().length();
        if (cycle <= 0D) {
            return clipTime + this.clockStart;
        }
        // The part of the start that is not a whole number of cycles: at most half a cycle.
        double drift = this.clockStart - cycle * Math.rint(this.clockStart / cycle);
        double share = this.clockInStep ? 1D : Math.min(1D, clipTime / cycle);
        return clipTime + this.clockStart - drift * share;
    }

    /**
     * GeckoLib's frame step, wrapped for the hand-over and the one clock (class comment). Through a hand-over's
     * ease-in GeckoLib gets its own clip point back before each step, so it makes no new point per frame; after the
     * step come the hand-over points ({@link #copyHandOverPoints}) and the ease-in point ({@link #showEaseInPoint}).
     * The first loop wrap ends the clock drift.
     */
    @Override
    protected boolean checkControllerState(T animatable, GeoRenderState renderState, AnimatableManager<T> manager,
            GeoModel<T> model) {
        if (this.easeInPoint != null && this.animationPoint == this.easeInPoint) {
            this.animationPoint = this.clipPoint;
        }
        boolean animating = super.checkControllerState(animatable, renderState, manager, model);
        copyHandOverPoints();
        showEaseInPoint();
        double clipTime = super.getCurrentAnimationTime();
        if (clipTime < this.lastClipTime) {
            this.clockInStep = true;
        }
        this.lastClipTime = clipTime;
        return animating;
    }

    /**
     * On the frame a hand-over starts, once GeckoLib loaded the new clip: makes the from-point a copy of the giver's
     * point, with only the new clip's bones for {@link HandOver#SHARED_BONES}, and builds the ease-in point, the new
     * clip's first frame with rest keys. The copy is needed because GeckoLib moves a point's keyframe indices on in
     * place ({@code createNext}), so a giver that plays on would move the from-point with it.
     */
    private void copyHandOverPoints() {
        if (this.handOverBones == null || this.animationPoint == null || this.transitionFromPoint == null) {
            return;
        }
        Animation clip = this.animationPoint.animation();
        AnimationPoint pose = this.transitionFromPoint;
        Animation from = this.handOverBones == HandOver.SHARED_BONES
                ? sharedBones(pose.animation(), clip)
                : pose.animation();
        this.transitionFromPoint = AnimationPoint.createFor(from, pose.easingOverride(), pose.loopType(),
                pose.animTime());
        this.easeInPoint = AnimationPoint.createFor(withRestKeys(clip), this.animationPoint.easingOverride(),
                this.animationPoint.loopType(), 0D);
        this.handOverBones = null;
    }

    /**
     * Through a hand-over's ease-in, shows the ease-in point and keeps GeckoLib's own clip point to give back; when
     * the ease-in ends, drops both, so the clip itself plays.
     */
    private void showEaseInPoint() {
        if (this.easeInPoint == null) {
            return;
        }
        if (isTransitioning()) {
            this.clipPoint = this.animationPoint;
            this.animationPoint = this.easeInPoint;
        } else {
            this.easeInPoint = null;
            this.clipPoint = null;
        }
    }

    /**
     * The pose this controller hands over: during {@link #blendToRest} the pose the blend starts from, else its clip
     * point; {@code null} when it animates no bone.
     */
    private @Nullable AnimationPoint handOverPose() {
        if (!isAnimatingBones()) {
            return null;
        }
        return this.currentRawAnimation == REST_BLEND ? this.transitionFromPoint : this.animationPoint;
    }

    /** A copy of {@code from} with only the bones {@code clip} also animates. */
    private static Animation sharedBones(Animation from, Animation clip) {
        BoneAnimation[] kept = new BoneAnimation[from.boneAnimations().length];
        int count = 0;
        for (BoneAnimation bone : from.boneAnimations()) {
            for (BoneAnimation clipBone : clip.boneAnimations()) {
                if (clipBone.boneName().equals(bone.boneName())) {
                    kept[count++] = bone;
                    break;
                }
            }
        }
        return Animation.create(from.name(), from.length(), from.loopType(), Arrays.copyOf(kept, count),
                Animation.KeyframeMarkers.EMPTY);
    }

    /**
     * A copy of {@code clip} with a rest key on every axis of every transform it has no keyframes for. A bone with
     * keys on every axis is kept as it is, so a hand-over makes new objects only where a key is missing.
     */
    private static Animation withRestKeys(Animation clip) {
        BoneAnimation[] bones = new BoneAnimation[clip.boneAnimations().length];
        for (int i = 0; i < bones.length; i++) {
            BoneAnimation bone = clip.boneAnimations()[i];
            KeyframeStack rotation = withRestKeys(bone.rotationKeyFrames(), 0D);
            KeyframeStack position = withRestKeys(bone.positionKeyFrames(), 0D);
            KeyframeStack scale = withRestKeys(bone.scaleKeyFrames(), 1D);
            boolean unchanged = rotation == bone.rotationKeyFrames() && position == bone.positionKeyFrames()
                    && scale == bone.scaleKeyFrames();
            bones[i] = unchanged ? bone : new BoneAnimation(bone.boneName(), rotation, position, scale);
        }
        return new Animation(clip.name(), clip.length(), clip.loopType(), bones, clip.usedVariables(),
                clip.keyframeMarkers());
    }

    /**
     * {@code stack} with a key at {@code rest} (0, or 1 for scale) on each axis that has none; {@code stack} itself
     * when every axis has keys.
     */
    private static KeyframeStack withRestKeys(KeyframeStack stack, double rest) {
        if (stack.xKeyframes().length > 0 && stack.yKeyframes().length > 0 && stack.zKeyframes().length > 0) {
            return stack;
        }
        Keyframe[] restKey = {new Keyframe(0D, 0D, new Constant(rest), new Constant(rest), EasingType.LINEAR)};
        return new KeyframeStack(
                stack.xKeyframes().length == 0 ? restKey : stack.xKeyframes(),
                stack.yKeyframes().length == 0 ? restKey : stack.yKeyframes(),
                stack.zKeyframes().length == 0 ? restKey : stack.zKeyframes());
    }

    /**
     * Whether the {@link #REST_BLEND} timeline is still in its first stage, the blend. Called from the handler, after
     * GeckoLib moved the time on for this frame, so the frame that passes the end of the blend already stops.
     */
    private boolean isBlendingToRest() {
        return this.timeline != null && this.timelineTime >= 0D
                && this.timelineTime < this.timeline.stages()[0].endTime();
    }
}
