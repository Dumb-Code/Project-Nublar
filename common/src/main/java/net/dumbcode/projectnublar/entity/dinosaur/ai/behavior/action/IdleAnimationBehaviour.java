package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action;

import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import software.bernie.geckolib.animatable.GeoEntity;

// TODO(DEAD): not wired into any activity group. Left unwired on purpose.
public class IdleAnimationBehaviour<E extends LivingEntity & GeoEntity> extends Idle<E> {


    @Override
    protected void start(E entity) {
        super.start(entity);
        if (entity.getRandom().nextDouble() < .5) {

        }
    }
}
