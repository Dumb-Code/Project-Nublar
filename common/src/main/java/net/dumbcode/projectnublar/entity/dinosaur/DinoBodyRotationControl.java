package net.dumbcode.projectnublar.entity.dinosaur;

import net.dumbcode.projectnublar.entity.ai.behaviour.social.TurnEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;

/**
 * Body yaw with easing (playtest 4 fix P12b, motion cause 3). Vanilla's body control copies the yaw into the
 * body yaw while the animal moves, so the drawn body followed every step of the yaw, and after a stop it
 * jumped to the yaw in one tick. This control lets vanilla choose the body yaw it wants (the yaw while
 * moving, toward the head while standing), then moves the body only part of the way there each tick
 * ({@link TurnEasing#bodyStep}). Vanilla clamps the head yaw to {@link Mob#getMaxHeadYRot} of the body yaw it
 * chose; the eased body lags that, so the head is clamped again against the eased body (fix round 1), and the
 * drawn head never turns farther from the body than the limit. It runs on both sides, like vanilla's.
 */
public class DinoBodyRotationControl extends BodyRotationControl {

    private final Mob mob;

    public DinoBodyRotationControl(Mob mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    public void clientTick() {
        float before = this.mob.yBodyRot;
        super.clientTick();
        float wanted = this.mob.yBodyRot;
        this.mob.yBodyRot = before + TurnEasing.bodyStep(Mth.wrapDegrees(wanted - before));
        this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float) this.mob.getMaxHeadYRot());
    }
}
