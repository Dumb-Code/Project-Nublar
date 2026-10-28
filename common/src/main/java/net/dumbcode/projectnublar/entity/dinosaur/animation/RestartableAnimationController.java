package net.dumbcode.projectnublar.entity.dinosaur.animation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.cache.animation.Animation;

/**
 * An {@link AnimationController} that can play its current animation again from the first frame, and that blends
 * a start from rest.
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
 * Start from rest: when nothing plays (the first start, or a start after {@link DinoAnimator}'s stop), GeckoLib 5
 * has no pose to blend from. It then fades from the clip's first frame toward rest and jumps back to the first
 * frame when the blend ends (75 degrees on a Velociraptor foot). {@link #setAnimation} gives such a start
 * {@link #REST} to blend from instead, so the bones blend from rest into the first frame, as in GeckoLib 4.
 *
 * @param <T> the animatable type
 */
public class RestartableAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
    /** The pose a start from rest blends from: an empty one-tick clip, so every bone blends from rest. */
    private static final AnimationPoint REST =
            AnimationPoint.createFor(Animation.generateWaitAnimation(0.05), null, LoopType.PLAY_ONCE, 0);

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
}
