package net.dumbcode.projectnublar.entity.ik.components.debug_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.ik.components.IKAnimatable;
import net.dumbcode.projectnublar.entity.ik.components.IKLegComponent;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.AngleConstraintIKChain;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLeg;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLegWithFoot;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.IKChain;
import net.dumbcode.projectnublar.entity.ik.parts.sever_limbs.ServerLimb;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LegDebugRenderer<E extends IKAnimatable<E>, C extends IKChain> implements IKDebugRenderer<E, IKLegComponent<C, E>> {
    @Override
    public void renderDebug(IKLegComponent<C, E> component, E animatable, PoseStack poseStack, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        for (Object limb : component.getLimbs()) {
            if (!(animatable instanceof Dinosaur dinosaur) || !(limb instanceof EntityLeg entityLeg)) {
                return;
            }

            Vec3 entityPos = dinosaur.position();
            Vec3 offsetEntityPos = dinosaur.position().add(0.1, 0.1, 0.1);

            renderLeg(poseStack, bufferSource, entityLeg, dinosaur);

            for (ServerLimb endPoint : component.getEndpoints()) {

                Vec3 limbOffset = endPoint.baseOffset;

                if (component.getStillStandCounter() != component.getSettings().standStillCounter()) {
                    limbOffset = limbOffset.add(0, 0, component.getSettings().stepInFront());
                }

                limbOffset = limbOffset.yRot((float) Math.toRadians(-dinosaur.getYRot()));

                Vec3 rotatedLimbOffset = limbOffset.add(dinosaur.position());

                BlockHitResult rayCastResult = IKLegComponent.rayCastToGround(rotatedLimbOffset, dinosaur, ClipContext.Fluid.NONE);

                Vec3 rayCastHitPos = rayCastResult.getLocation();

                double distance = endPoint.target.distanceTo(rayCastHitPos);

                if (distance < 0.1) distance = 0;

                if (distance != 0) {
                    IKDebugRenderer.drawLine(poseStack, bufferSource, entityPos, endPoint.getPos(), endPoint.target, 255, 100, 255, 127);
                }

                DebugRenderer.renderFilledBox(poseStack, bufferSource, AABB.unitCubeFromLowerCorner(endPoint.getPos()).contract(0.8, 0.8, 0.8).move(-offsetEntityPos.x, -offsetEntityPos.y, -offsetEntityPos.z), endPoint.isGrounded() ? 0.0F : 1.0F, endPoint.isGrounded() ? 1.0F : 0, 0.0F, 0.5F);
                DebugRenderer.renderFilledBox(poseStack, bufferSource, AABB.unitCubeFromLowerCorner(rayCastHitPos).contract(0.8, 0.8, 0.8).move(-offsetEntityPos.x, -offsetEntityPos.y, -offsetEntityPos.z), 0.0F, 0.0F, 1.0F, 0.5F);
            }
        }
    }

    private static <C extends IKChain> void renderLeg(PoseStack poseStack, MultiBufferSource bufferSource, C chain, Dinosaur entity) {
        Vec3 entityPos = entity.position();
        Vec3 offsetEntityPos = entity.position().add(0.1, 0.1, 0.1);

        DebugRenderer.renderFilledBox(poseStack, bufferSource, AABB.unitCubeFromLowerCorner(chain.getFirst().getPosition()).contract(0.8, 0.8, 0.8).move(-offsetEntityPos.x, -offsetEntityPos.y, -offsetEntityPos.z), 1.0F, 1.0F, 0.0F, 0.5F);

        drawAngleConstraintsForBase(chain.getFirst(), chain, entity, poseStack, bufferSource);
        for (int i = 0; i < chain.getJoints().size() - 1; i++) {
            Vec3 currentJoint = chain.getJoints().get(i);
            Vec3 nextJoint = chain.getJoints().get(i + 1);

            if (chain instanceof AngleConstraintIKChain angleChain && i > 0) {
                drawAngleConstraints(i, angleChain, entity, poseStack, bufferSource);
            }

            IKDebugRenderer.drawLineToBox(poseStack, bufferSource, entityPos, currentJoint, nextJoint, entity, 255, 165, 0, 127);
        }

        if (chain instanceof EntityLegWithFoot entityLegWithFoot) {
            Vec3 footPos = entityLegWithFoot.foot.getPosition();
            IKDebugRenderer.drawLineToBox(poseStack, bufferSource, entityPos, chain.endJoint, footPos, entity, 255, 165, 0, 127);

            Vec3 angleConstraint = entityLegWithFoot.getFootPosition(entityLegWithFoot.foot.angleSize);

            Vec3 referencePoint = entityLegWithFoot.getFootPosition(0);

            IKDebugRenderer.drawLine(poseStack, bufferSource, entityPos, chain.endJoint, angleConstraint, 255, 165, 0, 127);
            IKDebugRenderer.drawLine(poseStack, bufferSource, entityPos, chain.endJoint, referencePoint, 255, 165, 0, 127);
        }
    }

    private static <C extends AngleConstraintIKChain> void drawAngleConstraints(int i, C chain, Dinosaur entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        Vec3 entityPos = entity.position();

        Segment currentSegment = chain.get(i);

        List<Vec3> positions = getConstrainedPositions(chain.get(i - 1).getPosition(), currentSegment, chain.getJoints().get(i + 1), entity);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), positions.get(0), 255, 0, 0, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), positions.get(1), 180, 180, 180, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), positions.get(2), 0, 255, 0, 127);
    }

    private static List<Vec3> getConstrainedPositions(Vec3 reference, Segment middle, Vec3 endpoint, Dinosaur entity) {
        Vec3 normal = MathUtil.getClosestNormalRelativeToEntity(endpoint, middle.getPosition(), reference, entity);

        Vec3 referencePoint = MathUtil.rotatePointOnAPlaneAround(reference, middle.getPosition(), middle.angleOffset, normal);

        double angle = Math.toDegrees(MathUtil.calculateAngle(middle.getPosition(), endpoint, referencePoint));
        double angleDelta = middle.angleSize - angle;

        Vec3 newPos = MathUtil.rotatePointOnAPlaneAround(endpoint, middle.getPosition(), angleDelta, normal);
        Vec3 otherNewPos = MathUtil.rotatePointOnAPlaneAround(endpoint, middle.getPosition(), (angleDelta - (middle.angleSize * 2)), normal);
        Vec3 middlePos = MathUtil.rotatePointOnAPlaneAround(endpoint, middle.getPosition(), (angleDelta - middle.angleSize), normal);

        return List.of(newPos, middlePos, otherNewPos);
    }

    private static <C extends IKChain> void drawAngleConstraintsForBase(Segment currentSegment, C chain, Dinosaur entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        Vec3 entityPos = entity.position();

        Vec3 C = chain.getFirst().getPosition().subtract(0, 1, 0);

        double angle = Math.toDegrees(MathUtil.calculateAngle(chain.getFirst().getPosition(), chain.segments.get(1).getPosition(), C));
        double angleDelta = chain.getFirst().angleSize - angle;

        Vec3 normal = MathUtil.getClosestNormalRelativeToEntity(chain.segments.get(1).getPosition(), chain.getFirst().getPosition(), chain.segments.get(2).getPosition(), entity);
        Vec3 newPos = MathUtil.rotatePointOnAPlaneAround(chain.segments.get(1).getPosition(), chain.getFirst().getPosition(), angleDelta, normal);
        Vec3 otherNewPos = MathUtil.rotatePointOnAPlaneAround(chain.segments.get(1).getPosition(), chain.getFirst().getPosition(), (angleDelta - (chain.getFirst().angleSize * 2)), normal);
        Vec3 middlePos = MathUtil.rotatePointOnAPlaneAround(chain.segments.get(1).getPosition(), chain.getFirst().getPosition(), (angleDelta - chain.getFirst().angleSize), normal);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), newPos, 255, 0, 0, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), middlePos, 180, 180, 180, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), otherNewPos, 0, 255, 0, 127);
    }
}
