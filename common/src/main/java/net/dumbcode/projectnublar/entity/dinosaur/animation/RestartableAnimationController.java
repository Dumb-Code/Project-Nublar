package net.dumbcode.projectnublar.entity.dinosaur.animation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;

/**
 * An {@link AnimationController} that can play its current animation again from the first frame.
 * <p>
 * Why it exists: GeckoLib 4 had {@code AnimationController.forceAnimationReset()}. After that call, the next
 * {@code setAndContinue} started its animation from the first frame even when it was the animation already
 * playing, and blended into it from the current pose over the controller's transition ticks. GeckoLib 5 removed
 * the method: {@code setAnimation} ignores an animation equal to the current one, so a repeated attack, a new
 * one-shot or a repeated idle would not start again. {@link DinoAnimator} needs that restart.
 * <p>
 * {@link #forceAnimationReset} forgets which animation is current, so the next {@code setAnimation} takes
 * GeckoLib's own path for a new animation: a new timeline from the first frame, blended from the current pose.
 *
 * @param <T> the animatable type
 */
public class RestartableAnimationController<T extends GeoAnimatable> extends AnimationController<T> {

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
}
