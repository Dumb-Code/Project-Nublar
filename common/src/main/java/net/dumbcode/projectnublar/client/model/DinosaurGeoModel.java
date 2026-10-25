package net.dumbcode.projectnublar.client.model;

import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.animation.AnimationChoice;
import net.dumbcode.projectnublar.entity.dinosaur.animation.IdleSway;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationProcessor;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * The model of every dinosaur species. After GeckoLib applies the keyframed animations, it adds the always-on
 * breathing sway ({@link IdleSway}, fix P12a) to the bones the species' model has, so the sway shows under every
 * animation: standing, idle variants, resting and walking. A dead animal does not sway. Client only; no allocation
 * per frame.
 */
public class DinosaurGeoModel extends DefaultedEntityGeoModel<Dinosaur> {

    public DinosaurGeoModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    public DinosaurGeoModel(ResourceLocation assetSubpath, boolean turnsHead) {
        super(assetSubpath, turnsHead);
    }

    @Override
    public ResourceLocation getTextureResource(Dinosaur animatable) {
        return super.getTextureResource(animatable);
    }

    @Override
    public void setCustomAnimations(Dinosaur animatable, long instanceId, AnimationState<Dinosaur> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        if (animatable.isDeadOrDying()) {
            return;
        }
        addIdleSway(animationState.getAnimationTick() / AnimationChoice.TICKS_PER_SECOND);
    }

    /**
     * Adds the sway at {@code seconds} of animation time: the time base Molang's {@code query.life_time} uses, so
     * the sway runs in step with the file's {@code misc.idle}.
     * <p>
     * GeckoLib turns a Bedrock rotation into model space by negating x and y, so the angles are negated here to
     * match {@code misc.idle} exactly. GeckoLib writes every bone's rotation again on the next frame (from a
     * controller, or from its reset of bones no controller animates), so adding to the current value does not
     * build up. The changed-flags are cleared after the write, so the extra write does not keep GeckoLib from
     * resetting a bone, on this animal or on the next one that uses this shared model.
     */
    private void addIdleSway(double seconds) {
        AnimationProcessor<Dinosaur> processor = getAnimationProcessor();
        for (int i = 0; i < IdleSway.BONE_COUNT; i++) {
            CoreGeoBone bone = processor.getBone(IdleSway.bone(i));
            if (bone == null) {
                continue;
            }
            bone.setRotX(bone.getRotX() - (float) Math.toRadians(IdleSway.pitchDegrees(i, seconds)));
            bone.setRotY(bone.getRotY() - (float) Math.toRadians(IdleSway.yawDegrees(i, seconds)));
            bone.resetStateChanges();
        }
    }
}
