package net.dumbcode.projectnublar.entity.dinosaur.animation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.cache.animation.Animation;

/**
 * An {@link AnimationController} that can play its current animation again from the first frame, that blends
 * a start from rest, and that can blend its bones back to rest when it stops.
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

    /** Same as {@link AnimationController#AnimationController(String, int, AnimationStateHandler)}. */
    public RestartableAnimationController(String name, int transitionTicks, AnimationStateHandler<T> handler) {
        super(name, transitionTicks, handler);
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
        RawAnimation previous = this.currentRawAnimation;
        super.setAnimation(animation);
        // Only when super took a new animation: GeckoLib also clears the from-point while a clip plays (at each loop
        // wrap), and setAndContinue calls this every frame, so without the check REST would be set on every frame of
        // a playing clip.
        if (this.currentRawAnimation != previous && this.transitionFromPoint == null) {
            this.transitionFromPoint = REST;
        }
    }

    /**
     * Stops the controller with a blend of its bones from their current pose to rest over the transition ticks
     * (class comment), then ends the animation as {@code reset()} does. The state handler calls it, and returns
     * its result, on every frame the controller should not play. When nothing plays, it ends at once.
     * <p>
     * The blend plays at speed 1, so it lasts the transition ticks whatever the speed of the clip that stopped.
     * GeckoLib blends from one clip's pose, never from a pose between two: a clip that starts during the blend blends
     * in from the empty clip's pose (rest), and a stop while the clip itself still blends in (a slow walk that stops
     * less than 5 / speed ticks after it started) blends from the clip's own pose, not from the pose shown.
     *
     * @return {@code CONTINUE} while the blend plays, else {@code STOP}
     */
    public PlayState blendToRest() {
        if (this.currentRawAnimation != REST_BLEND) {
            if (!isAnimatingBones()) {
                reset();
                return PlayState.STOP;
            }
            // GeckoLib keeps the current point as the point to blend from (setAnimation).
            setAnimation(REST_BLEND);
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
     * GeckoLib's clip time, which Molang reads as {@code query.anim_time} (and a keyframe event as its tick). During
     * {@link #blendToRest} it is the time of the pose the blend starts from, not the empty clip's time: GeckoLib works
     * out that pose again on every frame of the blend, so a bone that a Molang expression of the time moves (the
     * Brachiosaurus tail, {@code math.sin(query.anim_time ...)}) would else jump to its pose at time 0 when the blend
     * starts (21 degrees in the replay).
     */
    @Override
    public double getCurrentAnimationTime() {
        if (this.currentRawAnimation == REST_BLEND && this.transitionFromPoint != null) {
            return this.transitionFromPoint.animTime();
        }
        return super.getCurrentAnimationTime();
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
