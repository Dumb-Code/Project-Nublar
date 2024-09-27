package net.dumbcode.projectnublar.entity.ik.components;

import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLeg;
import net.dumbcode.projectnublar.entity.ik.parts.sever_limbs.ServerLimb;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.List;

public class IKLegWithFoot<E extends GeoAnimatable & IKAnimatable<E>, C extends EntityLeg> extends IKLegComponent<E, C> {

    public IKLegWithFoot(LegSetting settings, List<ServerLimb> endpoints, C... limbs) {
        super(settings, endpoints, limbs);
    }

    @Override
    public <M extends DefaultedEntityGeoModel<E>> void tickClient(E animatable, long instanceId, AnimationState<E> animationState, M model) {
        super.tickClient(animatable, instanceId, animationState, model);
    }
}
