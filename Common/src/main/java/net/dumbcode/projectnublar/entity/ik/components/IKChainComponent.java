package net.dumbcode.projectnublar.entity.ik.components;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.ik.model.BoneAccessor;
import net.dumbcode.projectnublar.entity.ik.model.ModelAccessor;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLegWithFoot;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.IKChain;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public abstract class IKChainComponent<C extends IKChain, E extends IKAnimatable<E>> implements IKModelComponent<E> {

    protected List<C> limbs = new ArrayList<>();

    @Override
    public void tickClient(E animatable, ModelAccessor model) {
        if (!(animatable instanceof Dinosaur entity)) {
            return;
        }

        Vec3 pos = entity.position();

        for (int i = 0; i < this.limbs.size(); i++) {
            BoneAccessor baseAccessor = model.getBone("base_" + "leg" + (i + 1));

            Vec3 basePosWorldSpace = baseAccessor.getPivotPointOffset(entity);
            basePosWorldSpace = basePosWorldSpace.add(pos);

            /*
            if ((this.limbs.get(i).endJoint.distanceTo(this.endpoints.get(i).pos) < IKChain.TOLERANCE) && (this.limbs.get(i).getFirst().getPosition().distanceTo(basePosWorldSpace) < IKChain.TOLERANCE)) {
                continue;
            }
            */

            C limb = this.setLimb(i, basePosWorldSpace, entity);

            for (int k = 0; k < limb.getJoints().size() - 1; k++) {
                Vec3 modelPosWorldSpace = limb.getJoints().get(k);
                Vec3 targetVecWorldSpace = limb.getJoints().get(k + 1);

                BoneAccessor legSegmentAccessor = model.getBone("seg" + (k + 1) + "_leg" + (i + 1));

                if (Constants.shouldRenderDebugLegs) {
                    modelPosWorldSpace = modelPosWorldSpace.subtract(0, 200, 0);
                    targetVecWorldSpace = targetVecWorldSpace.subtract(0, 200, 0);
                }

                legSegmentAccessor.moveTo(modelPosWorldSpace, targetVecWorldSpace, entity);

                if (limb instanceof EntityLegWithFoot entityLegWithFoot) {
                    BoneAccessor footSegmentAccessor = model.getBone("foot_leg" + (i + 1));

                    footSegmentAccessor.moveTo(Constants.shouldRenderDebugLegs ? limb.endJoint.subtract(0, 200, 0) : limb.endJoint, entityLegWithFoot.getFootPosition(), entity);
                }
            }
        }
    }


    abstract C setLimb(int index, Vec3 base, PathfinderMob entity);
}
