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
 * {@link #moving}, else {@link DinoAnimator#stopController}.
 */
class RestartableAnimationControllerTest {

    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    /** The test model's clip for every name: one second, no bones. */
    private static final Animation CLIP = Animation.generateWaitAnimation(1);

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

    @Test
    void stopEndsTheLoopAtOnce() {
        walk(90);
        assertTrue(this.controller.isAnimatingBones());
        this.moving = false;
        assertNull(frame());
        assertFalse(this.controller.isAnimatingBones());
        for (int i = 0; i < 90; i++) {
            assertNull(frame(), "frame " + i + " after the stop");
        }
    }

    @Test
    void firstStartBlendsFromRest() {
        this.moving = true;
        ControllerState start = frame();
        assertNotNull(start);
        assertEquals(0D, start.transitionTime());
        assertNotNull(start.prevAnimationPoint());
    }

    @Test
    void startAfterAStopBlendsFromRest() {
        walk(90);
        this.moving = false;
        frame();
        this.moving = true;
        ControllerState start = frame();
        assertNotNull(start);
        assertEquals(0D, start.transitionTime());
        assertNotNull(start.prevAnimationPoint());
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
