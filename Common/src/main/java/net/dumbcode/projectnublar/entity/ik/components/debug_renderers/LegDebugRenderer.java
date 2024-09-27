package net.dumbcode.projectnublar.entity.ik.components.debug_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.ik.components.IKAnimatable;
import net.dumbcode.projectnublar.entity.ik.components.IKLegComponent;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.AngleConstraintIKChain;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLeg;
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
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;

import java.util.List;

public class LegDebugRenderer<E extends GeoAnimatable & IKAnimatable<E>> implements IKDebugRenderer<E, IKLegComponent<E, ? extends IKChain>> {



    @Override
    public void renderDebug(IKLegComponent<E, ? extends IKChain> component, E animatable, PoseStack poseStack, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        for (Object limb : component.getLimbs()) {
            if (!(animatable instanceof Dinosaur dinosaur) || !(limb instanceof EntityLeg entityLeg)) {
                return;
            }

            Vec3 entityPos = dinosaur.position();
            Vec3 offsetEntityPos = dinosaur.position().add(0.1, 0.1, 0.1);


            renderLeg(poseStack, bufferSource, entityLeg, dinosaur);

            //DebugRenderer.renderFilledBox(poseStack, bufferSource, AABB.unitCubeFromLowerCorner(this.getFirst().position.add(0, -1, 0)).contract(0.8, 0.8, 0.8).move(-offsetEntityPos.x, -offsetEntityPos.y, -offsetEntityPos.z), 1, 1, 1, 0.5F);

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
                    //Vec3d middlePos = getAveragePos(endPoint.target, endPoint.pos);

                    //Vec3d localizedMiddlePos = middlePos.subtract(cameraPos);

                    //DebugRenderer.drawString(matrices, vertexConsumers, "Distance: " + distance, cameraPos.add(localizedMiddlePos).getX(), cameraPos.add(localizedMiddlePos).getY(), cameraPos.add(localizedMiddlePos).getZ(), RED);
                    drawLine(poseStack, bufferSource, entityPos, endPoint.getPos(), endPoint.target, PINK);
                }
                DebugRenderer.renderFilledBox(poseStack, bufferSource, AABB.unitCubeFromLowerCorner(endPoint.getPos()).contract(0.8, 0.8, 0.8).move(-offsetEntityPos.x, -offsetEntityPos.y, -offsetEntityPos.z), endPoint.isGrounded() ? 0.0F : 1.0F, endPoint.isGrounded() ? 1.0F : 0, 0.0F, 0.5F);
                DebugRenderer.renderFilledBox(poseStack, bufferSource, AABB.unitCubeFromLowerCorner(rayCastHitPos).contract(0.8, 0.8, 0.8).move(-offsetEntityPos.x, -offsetEntityPos.y, -offsetEntityPos.z), 0.0F, 0.0F, 1.0F, 0.5F);
            }
        }
    }

    private static final int GREEN = getArgb(255, 0, 255, 0);
    private static final int GREY = getArgb(255, 180, 180, 180);
    private static final int RED = getArgb(255, 255, 0, 0);
    private static final int ORANGE = getArgb(255, 255, 165, 0);
    private static final int PINK = getArgb(255, 255, 100, 255);

    public static int getArgb(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
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

            drawLineToBox(poseStack, bufferSource, entityPos, currentJoint, nextJoint, ORANGE, entity, 1.0F, 1.0F, 0.0F);
        }
    }

    private static void drawLineToBox(PoseStack matrices, MultiBufferSource vertexConsumers, Vec3 camera, Vec3 startPos, Vec3 targetPos, int color, Dinosaur entity, float pRed, float pGreen, float pBlue) {
        Vec3 offsetEntityPos = entity.position().add(0.1, 0.1, 0.1);

        DebugRenderer.renderFilledBox(matrices, vertexConsumers, AABB.unitCubeFromLowerCorner(targetPos).contract(0.8, 0.8, 0.8).move(-offsetEntityPos.x, -offsetEntityPos.y, -offsetEntityPos.z), pRed, pGreen, pBlue, 0.5F);
        drawLine(matrices, vertexConsumers, camera, startPos, targetPos, color);
    }

    private static void drawLine(PoseStack matrices, MultiBufferSource vertexConsumers, Vec3 camera, Vec3 startPos, Vec3 targetPos, int color) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.debugLineStrip(1.0));
        vertexConsumer.vertex(matrices.last().pose(), (float) (startPos.x - camera.x), (float) (startPos.y - camera.y), (float) (startPos.z - camera.z)).color(color).endVertex();
        vertexConsumer.vertex(matrices.last().pose(), (float) (targetPos.x - camera.x), (float) (targetPos.y - camera.y), (float) (targetPos.z - camera.z)).color(color).endVertex();
    }

    private static <C extends AngleConstraintIKChain> void drawAngleConstraints(int i, C chain, Dinosaur entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        Vec3 entityPos = entity.position();

        Segment currentSegment = chain.get(i);

        List<Vec3> positions = getConstrainedPositions(chain.get(i - 1).getPosition(), currentSegment, chain.getJoints().get(i + 1), entity);

        drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), positions.get(0), RED);
        drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), positions.get(1), GREY);
        drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), positions.get(2), GREEN);
    }


    private static List<Vec3> getConstrainedPositions(Vec3 reference, Segment middle, Vec3 endpoint, Dinosaur entity) {
        Vec3 normal = MathUtil.getClosestNormalRelativeToEntity(endpoint, middle.getPosition(), reference, entity);

        Vec3 referencePoint = MathUtil.rotatePointOnAPlainAround(reference, middle.getPosition(), middle.angleOffset, normal);

        double angle = Math.toDegrees(MathUtil.calculateAngle(middle.getPosition(), endpoint, referencePoint));
        double angleDelta = middle.angleSize - angle;

        Vec3 newPos = MathUtil.rotatePointOnAPlainAround(endpoint, middle.getPosition(), angleDelta, normal);
        Vec3 otherNewPos = MathUtil.rotatePointOnAPlainAround(endpoint, middle.getPosition(), (angleDelta - (middle.angleSize * 2)), normal);
        Vec3 middlePos = MathUtil.rotatePointOnAPlainAround(endpoint, middle.getPosition(), (angleDelta - middle.angleSize), normal);

        return List.of(newPos, middlePos, otherNewPos);
    }

    private static <C extends IKChain> void drawAngleConstraintsForBase(Segment currentSegment, C chain, Dinosaur entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        Vec3 entityPos = entity.position();

        Vec3 C = chain.getFirst().getPosition().subtract(0, 1, 0);

        double angle = Math.toDegrees(MathUtil.calculateAngle(chain.getFirst().getPosition(), chain.segments.get(1).getPosition(), C));
        double angleDelta = chain.getFirst().angleSize - angle;

        Vec3 normal = MathUtil.getClosestNormalRelativeToEntity(chain.segments.get(1).getPosition(), chain.getFirst().getPosition(), chain.segments.get(2).getPosition(), entity);
        Vec3 newPos = MathUtil.rotatePointOnAPlainAround(chain.segments.get(1).getPosition(), chain.getFirst().getPosition(), angleDelta, normal);
        Vec3 otherNewPos = MathUtil.rotatePointOnAPlainAround(chain.segments.get(1).getPosition(), chain.getFirst().getPosition(), (angleDelta - (chain.getFirst().angleSize * 2)), normal);
        Vec3 middlePos = MathUtil.rotatePointOnAPlainAround(chain.segments.get(1).getPosition(), chain.getFirst().getPosition(), (angleDelta - chain.getFirst().angleSize), normal);

        drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), newPos, RED);
        drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), middlePos, GREY);
        drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), otherNewPos, GREEN);
    }
}
