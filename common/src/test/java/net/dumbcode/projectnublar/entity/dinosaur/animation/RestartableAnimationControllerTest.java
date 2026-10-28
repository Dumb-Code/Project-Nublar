package net.dumbcode.projectnublar.entity.dinosaur.animation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.state.ControllerState;
import com.geckolib.cache.animation.Animation;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives a real GeckoLib 5 controller the way {@link DinoAnimator#movement} does: a looping walk while
 * {@link #moving}, else {@link DinoAnimator#stopController}. Which {@link DinoAnimator} handlers end with
 * {@code stopController} (both stops of {@code movement}, every stop of the main controller) is checked in game,
 * not here: {@code DinoAnimator} needs a live {@code Dinosaur}.
 */
class RestartableAnimationControllerTest {

    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    /** The test model's clip for every name: one second, no bones. */
    private static final Animation CLIP = Animation.generateWaitAnimation(1);
    /** Frames of walk that pass the first loop wrap: the blend (15 frames) plus the one-second clip (60 frames). */
    private static final int PAST_A_LOOP_WRAP = 90;

    private boolean moving;
    /** Animatable age in ticks; each frame is a third of a tick (60 fps). */
    private double age;
    private final Animatable animatable = new Animatable();
    private final RestartableAnimationController<Animatable> controller = new RestartableAnimationController<>("movement",
            DinoAnimator.MOVEMENT_TRANSITION_TICKS, test -> this.moving ? test.setAndContinue(WALK) : DinoAnimator.stopController(test));
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
            return CLIP;
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

    /** One render frame: what the renderer asks the controller; {@code null} when it animates no bone. */
    private @Nullable ControllerState frame() {
        this.age += 1 / 3D;
        return this.controller.extractControllerState(this.animatable, this.renderState, null, null, this.model);
    }

    private void walk(int frames) {
        this.moving = true;
        for (int i = 0; i < frames; i++) {
            frame();
        }
    }

    /** After a bare STOP, GeckoLib 5 plays a loop on; with the reset, no bone moves from the stop frame on. */
    @Test
    void stopEndsTheLoopAtOnce() {
        walk(PAST_A_LOOP_WRAP);
        assertTrue(this.controller.isAnimatingBones());
        this.moving = false;
        assertNull(frame());
        assertFalse(this.controller.isAnimatingBones());
        for (int i = 0; i < PAST_A_LOOP_WRAP; i++) {
            assertNull(frame(), "frame " + i + " after the stop");
        }
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

    /** After the reset nothing plays, so a restart needs the rest point as much as a first start does. */
    @Test
    void startAfterAStopBlendsFromRest() {
        walk(PAST_A_LOOP_WRAP);
        this.moving = false;
        frame();
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
