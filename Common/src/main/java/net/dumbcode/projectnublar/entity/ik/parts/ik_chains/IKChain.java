package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.ik.components.IKLegComponent;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.sever_limbs.ServerLimb;
import net.dumbcode.projectnublar.entity.ik.util.ArrayUtil;
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
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.ArrayList;
import java.util.List;

public class IKChain {
    public List<Segment> segments = new ArrayList<>();
    public Vec3 endJoint = Vec3.ZERO;
    public static final int MAX_ITERATIONS = 10;
    public static final double TOLERANCE = 0.01;

    public IKChain(double... lengths) {
        for (double length : lengths) {
            this.segments.add(new Segment.Builder().length(length).build());
        }
    }

    public IKChain(Segment... segments) {
        this.segments.addAll(List.of(segments));
    }

    public void solve(Vec3 target, Vec3 base) {
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            if (this.isTargetToFar(target)) {
                this.iterate(target, base);
                break;
            }

            this.iterate(target, base);

            if (this.areStoppingConditionsMeet(target)) {
                break;
            }
        }
    }

    public void iterate(Vec3 target, Vec3 base) {
        this.getFirst().position = base;

        this.reachForwards(target);
        this.reachBackwards(base);
    }

    public void extendFully(Vec3 target, Vec3 base) {
        this.getFirst().position = base;

        Vec3 directionOfTarget = target.subtract(base).normalize();
        for (int i = 1; i < this.segments.size(); i++) {
            Segment prevSegment = this.segments.get(i - 1);
            Segment currentSegment = this.segments.get(i);

            currentSegment.position = prevSegment.position.add(directionOfTarget.scale(prevSegment.length));
        }
        this.endJoint = this.getLast().position.add(directionOfTarget.scale(this.getLast().length));
    }

    private boolean isTargetToFar(Vec3 target) {
        return target.distanceTo(this.getFirst().position) > this.maxLength();
    }

    private boolean areStoppingConditionsMeet(Vec3 target) {
        return this.endJoint.distanceTo(target) < TOLERANCE;
    }

    public void reachForwards(Vec3 target) {
        this.endJoint = target;

        this.getLast().position = moveSegment(this.getLast().position, this.endJoint, this.getLast().length);
        for (int i = this.segments.size() - 1; i > 0; i--) {
            Segment currentSegment = this.segments.get(i);
            Segment nextSegment = this.segments.get(i - 1);

            nextSegment.position = moveSegment(nextSegment.position, currentSegment.position, nextSegment.length);
        }
    }

    public void reachBackwards(Vec3 base) {
        this.getFirst().position = base;

        for (int i = 0; i < this.segments.size() - 1; i++) {
            Segment currentSegment = this.segments.get(i);
            Segment nextSegment = this.segments.get(i + 1);

            nextSegment.position = moveSegment(nextSegment.position, currentSegment.position, currentSegment.length);
        }
        this.endJoint = moveSegment(this.endJoint, this.getLast().position, this.getLast().length);
    }

    public void setSegmentsTo(List<Vec3> joints) {
        for (int i = 0; i < joints.size() - 2; i++) {
            this.segments.get(i).position = joints.get(i);
        }
        this.endJoint = ArrayUtil.getLast(joints);
    }

    public static Vec3 moveSegment(Vec3 point, Vec3 pullTowards, double length) {
        Vec3 direction = pullTowards.subtract(point).normalize();
        return pullTowards.subtract(direction.scale(length));
    }

    public double maxLength() {
        double totalLength = 0;
        for (Segment segment : this.segments) {
            totalLength += segment.length;
        }
        return totalLength;
    }

    public List<Vec3> getJoints() {
        List<Vec3> joints = new ArrayList<>();
        for (Segment segment : this.segments) {
            joints.add(segment.position);
        }
        joints.add(this.endJoint);
        return joints;
    }

    public Segment getFirst() {
        return ArrayUtil.getFirst(this.segments);
    }

    public Segment getLast() {
        return ArrayUtil.getLast(this.segments);
    }

    public <E extends GeoAnimatable, M extends DefaultedEntityGeoModel<E>> void renderDebug(PoseStack poseStack, E animatable, List<ServerLimb> endpoint, IKLegComponent.LegSetting legSetting, int standStillCounter, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!(animatable instanceof Dinosaur dinosaur)) {
            return;
        }

        Vec3 entityPos = dinosaur.position();
        Vec3 offsetEntityPos = dinosaur.position().add(0.1, 0.1, 0.1);


        renderLeg(poseStack, bufferSource, this, dinosaur);

        //DebugRenderer.renderFilledBox(poseStack, bufferSource, AABB.unitCubeFromLowerCorner(this.getFirst().position.add(0, -1, 0)).contract(0.8, 0.8, 0.8).move(-offsetEntityPos.x, -offsetEntityPos.y, -offsetEntityPos.z), 1, 1, 1, 0.5F);

        for (ServerLimb endPoint : endpoint) {

            Vec3 limbOffset = endPoint.baseOffset;

            if (standStillCounter != legSetting.standStillCounter()) {
                limbOffset = limbOffset.add(0, 0, legSetting.stepInFront());
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

    private static final int GREEN = getArgb(255, 0, 255, 0);
    private static final int RED = getArgb(255, 255, 0, 0);
    private static final int ORANGE = getArgb(255, 255, 165, 0);
    private static final int PINK = getArgb(255, 255, 100, 255);

    public static int getArgb(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static <C extends IKChain> void renderLeg(PoseStack poseStack, MultiBufferSource bufferSource, C chain, Dinosaur entity) {
        Vec3 entityPos = entity.position();
        Vec3 offsetEntityPos = entity.position().add(0.1, 0.1, 0.1);

        DebugRenderer.renderFilledBox(poseStack, bufferSource, AABB.unitCubeFromLowerCorner(chain.getFirst().position).contract(0.8, 0.8, 0.8).move(-offsetEntityPos.x, -offsetEntityPos.y, -offsetEntityPos.z), 1.0F, 1.0F, 0.0F, 0.5F);


        for (int i = 0; i < chain.getJoints().size() - 1; i++) {
            Vec3 currentJoint = chain.getJoints().get(i);
            Vec3 nextJoint = chain.getJoints().get(i + 1);

            drawAngleConstraints(chain.getFirst(), chain, entity, poseStack, bufferSource);

            /*
            if (i != 0) {
                Vec3 prevJoint = chain.getJoints().get(i - 1);
                Vec3 closestNormal = chain.getOrientationPoint();
                drawLineToBox(poseStack, bufferSource, entityPos, currentJoint, closestNormal.add(currentJoint), ORANGE, entity);
            }
             */

            drawLineToBox(poseStack, bufferSource, entityPos, currentJoint, nextJoint, ORANGE, entity);
        }
    }

    private static void drawLineToBox(PoseStack matrices, MultiBufferSource vertexConsumers, Vec3 camera, Vec3 startPos, Vec3 targetPos, int color, Dinosaur entity) {
        Vec3 offsetEntityPos = entity.position().add(0.1, 0.1, 0.1);

        drawLine(matrices, vertexConsumers, camera, startPos, targetPos, color);
        DebugRenderer.renderFilledBox(matrices, vertexConsumers, AABB.unitCubeFromLowerCorner(targetPos).contract(0.8, 0.8, 0.8).move(-offsetEntityPos.x, -offsetEntityPos.y, -offsetEntityPos.z), 1.0F, 1.0F, 0.0F, 0.5F);
    }

    private static void drawLine(PoseStack matrices, MultiBufferSource vertexConsumers, Vec3 camera, Vec3 startPos, Vec3 targetPos, int color) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.debugLineStrip(1.0));
        vertexConsumer.vertex(matrices.last().pose(), (float) (startPos.x - camera.x), (float) (startPos.y - camera.y), (float) (startPos.z - camera.z)).color(color).endVertex();
        vertexConsumer.vertex(matrices.last().pose(), (float) (targetPos.x - camera.x), (float) (targetPos.y - camera.y), (float) (targetPos.z - camera.z)).color(color).endVertex();
    }

    private static <C extends IKChain> void drawAngleConstraints(Segment currentSegment, C chain, Dinosaur entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        if (!currentSegment.hasAngleConstraints) {
            //return;
        }

        Vec3 entityPos = entity.position();

        Vec3 C = chain.getFirst().position.subtract(0, 1, 0);

        double angle = Math.toDegrees(MathUtil.calculateAngle(chain.getFirst().position, chain.segments.get(1).position, C));
        double angleDelta = chain.getFirst().angleSize - angle;

        Vec3 normal = MathUtil.getClosestNormalRelativeToEntity(chain.segments.get(1).position, chain.getFirst().position, chain.segments.get(2).position, entity);
        Vec3 newPos = MathUtil.rotatePointOnAPlainAround(chain.segments.get(1).position, chain.getFirst().position, angleDelta, normal);

        //Vec3 angleOffset = MathUtil.rotatePointOnAPlainAround(C, chain.getFirst().position, chain.getFirst().angleOffset, normal);
        //drawLineToBox(matrices, vertexConsumers, entityPos, currentSegment.position, angleOffset, ORANGE, entity);

        drawLineToBox(matrices, vertexConsumers, entityPos, currentSegment.position, newPos, ORANGE, entity);
    }

    protected Segment get(int i) {
        return this.segments.get(i);
    }
}