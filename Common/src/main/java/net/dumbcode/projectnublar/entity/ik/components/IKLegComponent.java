package net.dumbcode.projectnublar.entity.ik.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.AngleConstraintIKChain;
import net.dumbcode.projectnublar.entity.ik.parts.sever_limbs.ServerLimb;
import net.dumbcode.projectnublar.entity.ik.util.GeoModelUtil;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.ArrayList;
import java.util.List;

public class IKLegComponent<E extends GeoAnimatable, C extends AngleConstraintIKChain> implements IKModelComponent<E> {
    ///summon projectnublar:tyrannosaurus_rex ~ ~ ~ {NoAI:1b}

    public List<C> limbs = new ArrayList<>();
    public List<ServerLimb> endpoints;
    public LegSetting settings;
    private int stillStandCounter = 0;

    @SafeVarargs
    public IKLegComponent(LegSetting settings, List<ServerLimb> endpoints , C... limbs) {
        this.limbs.addAll(List.of(limbs));
        this.settings = settings;
        this.endpoints = endpoints;
    }

    @Override
    public <M extends DefaultedEntityGeoModel<? extends GeoAnimatable>> void tickClient(E animatable, long instanceId, AnimationState<E> animationState, M model) {
        if (!(animatable instanceof Dinosaur entity)) {
            return;
        }

        Vec3 pos = entity.position();

        int leg = 1;
        for (int i = 0; i < this.limbs.size(); i++) {
            GeoBone basePos = GeoModelUtil.getBoneOrNull(model, "base_" + "leg" + leg);

            Vec3 transformVec3dWorldSpace = GeoModelUtil.modelTransformToVec3d(basePos).yRot((float) -Math.toRadians(entity.getYRot()));
            transformVec3dWorldSpace = transformVec3dWorldSpace.add(pos);

            C limb = this.setLimb(i, transformVec3dWorldSpace, entity);

            for (int k = 0; k < limb.getJoints().size() - 1; k++) {
                Vec3 modelPosWorldSpace = limb.getJoints().get(k);
                Vec3 targetVecWorldSpace = limb.getJoints().get(k + 1);

                //Position
                GeoBone legSegment = GeoModelUtil.getBoneOrNull(model, "seg" + (k + 1) + "_leg" + leg);

                if (Constants.shouldRenderDebugLegs) {
                    GeoModelUtil.setBonePosFromWorldPos(legSegment, modelPosWorldSpace.subtract(0, 200, 0), entity);
                } else {
                GeoModelUtil.setBonePosFromWorldPos(legSegment, modelPosWorldSpace, entity);
                }

                //Rotation
                Vec3 rotation = MathUtil.getRotation(modelPosWorldSpace, targetVecWorldSpace);

                double xRot = -rotation.y - Math.toRadians(180);

                if (k == 0) {
                    xRot -= Math.toRadians(20);
                }

                if (k == 1) {
                    xRot -= Math.toRadians(20);
                }

                GeoModelUtil.setAngles(legSegment, xRot, -rotation.x + Math.toRadians(entity.getYRot() + 90), rotation.z);
            }

            leg++;
        }
    }

    @Override
    public void tickServer(E animatable) {
        this.updateEndPoints(animatable);
    }

    public void updateEndPoints(E animatable) {
        if (!(animatable instanceof PathfinderMob entity)) {
            return;
        }

        Vec3 pos = entity.position();

        for (int i = 0; i < endpoints.size(); i++) {
            ServerLimb limb = endpoints.get(i);

            limb.tick(this.endpoints, entity, i, this.settings.movementSpeed);

            Vec3 limbOffset = limb.baseOffset;

            if (!hasNotMovedOverLastTick(entity)) {
                limbOffset = limbOffset.add(0, 0, this.settings.stepInFront());
            }

            limbOffset = limbOffset.yRot((float) Math.toRadians(-entity.getYRot()));

            Vec3 rotatedLimbOffset = limbOffset.add(pos);

            BlockHitResult rayCastResult = rayCastToGround(rotatedLimbOffset, entity, this.settings.fluid());

            Vec3 rayCastHitPos = rayCastResult.getLocation();

            if (limb.hasToBeSet) {
                limb.set(rayCastHitPos);
                limb.hasToBeSet = false;
            }

            if (rayCastHitPos.distanceTo(limb.target) > this.getMaxLegFormTargetDistance(entity)) {
                limb.setTarget(rayCastHitPos);
            }
        }
    }

    private double getMaxLegFormTargetDistance(PathfinderMob entity) {
        if (this.stillStandCounter >= this.settings.standStillCounter() && !hasNotMovedOverLastTick(entity)) {
            this.stillStandCounter = 0;
        } else if (this.stillStandCounter < this.settings.standStillCounter()) {
            this.stillStandCounter += 1;
        }

        if (this.stillStandCounter == this.settings.standStillCounter()) {
            return this.settings.maxStandingStillDistance();
        } else {
            return this.settings.maxDistance();
        }
    }

    private static boolean hasNotMovedOverLastTick(PathfinderMob entity) {
        Vec3 oldPos = new Vec3(entity.xo, entity.yo, entity.zo);
        return entity.position().equals(oldPos);
    }

    public static BlockHitResult rayCastToGround(Vec3 rotatedLimbOffset, PathfinderMob entity, ClipContext.Fluid fluid) {
        Level world = entity.level();
        return world.clip(new ClipContext(rotatedLimbOffset.relative(Direction.UP, 3), rotatedLimbOffset.relative(Direction.DOWN, 10), ClipContext.Block.COLLIDER, fluid, entity));
    }

    @Override
    public void renderDebug(PoseStack poseStack, E animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        for (C limb : this.limbs) {
            limb.renderDebug(poseStack, animatable, this.endpoints, this.settings, this.stillStandCounter , bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
    }

    private C setLimb(int index, Vec3 base, PathfinderMob entity) {
        C limb = this.limbs.get(index);

        limb.entity = entity;

        limb.solve(this.endpoints.get(index).pos, base);

        return limb;
    }

    public static class LegSetting {
        private ClipContext.Fluid fluid;
        private double maxStandingStillDistance;
        private double maxDistance;
        private double stepInFront;
        private double movementSpeed;
        private int standStillCounter;

        private LegSetting(ClipContext.Fluid fluid, double maxStandingStillDistance, double maxDistance, double stepInFront, double movementSpeed, int standStillCounter) {
            this.fluid = fluid;
            if (fluid == null) {
                this.fluid = ClipContext.Fluid.NONE;
            }
            this.maxStandingStillDistance = maxStandingStillDistance;
            if (maxStandingStillDistance == 0) {
                this.maxStandingStillDistance = 0.1;
            }
            this.maxDistance = maxDistance;
            if (maxDistance == 0) {
                this.maxDistance = 1;
            }
            this.stepInFront = stepInFront;
            if (stepInFront == 0) {
                this.stepInFront = 1;
            }
            this.movementSpeed = movementSpeed;
            if (movementSpeed == 0) {
                this.movementSpeed = 0.2;
            }
            this.standStillCounter = standStillCounter;
            if (standStillCounter == 0) {
                this.standStillCounter = 20;
            }
        }

        public ClipContext.Fluid fluid() {
            return this.fluid;
        }

        public double maxStandingStillDistance() {
            return this.maxStandingStillDistance;
        }

        public double maxDistance() {
            return this.maxDistance;
        }

        public double stepInFront() {
            return this.stepInFront;
        }

        public double movementSpeed() {
            return this.movementSpeed;
        }

        public int standStillCounter() {
            return this.standStillCounter;
        }

        public static class Builder {
            private ClipContext.Fluid fluid;
            private double maxStandingStillDistance;
            private double maxDistance;
            private double stepInFront;
            private double movementSpeed;
            private int standStillCounter;

            public Builder() {
            }

            public LegSetting.Builder fluid(ClipContext.Fluid fluid) {
                this.fluid = fluid;
                return this;
            }
            public LegSetting.Builder maxStandingStillDistance(double maxStandingStillDistance) {
                this.maxStandingStillDistance = maxStandingStillDistance;
                return this;
            }

            public LegSetting.Builder maxDistance(double maxDistance) {
                this.maxDistance = maxDistance;
                return this;
            }

            public LegSetting.Builder standStillCounter(int standStillCounter) {
                this.standStillCounter = standStillCounter;
                return this;
            }

            public LegSetting.Builder stepInFront(double stepInFront) {
                this.stepInFront = stepInFront;
                return this;
            }

            public LegSetting.Builder movementSpeed(double movementSpeed) {
                this.movementSpeed = movementSpeed;
                return this;
            }

            public LegSetting build() {
                return new LegSetting(this.fluid, this.maxStandingStillDistance, this.maxDistance, this.stepInFront, this.movementSpeed, this.standStillCounter);
            }
        }
    }
}
