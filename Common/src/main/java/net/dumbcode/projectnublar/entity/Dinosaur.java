package net.dumbcode.projectnublar.entity;

import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.ik.components.IKAnimatable;
import net.dumbcode.projectnublar.entity.ik.components.IKLegComponent;
import net.dumbcode.projectnublar.entity.ik.components.IKModelComponent;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.WorldCollidingSegment;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLegWithFoot;
import net.dumbcode.projectnublar.entity.ik.parts.sever_limbs.ServerLimb;
import net.dumbcode.projectnublar.init.DataSerializerInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dinosaur extends PathfinderMob implements FossilRevived, GeoEntity, IKAnimatable {
    public List<IKModelComponent> components = new ArrayList<>();

    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<String> idleAnimations = List.of("sniffingair", "sniffground", "speak1", "lookleft", "lookright", "scratching","shakehead","shakebody");
    public Dinosaur(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        this.setUpLimbs();
    }

    protected void setUpLimbs() {
        this.addComponent(new IKLegComponent<>(
                new IKLegComponent.LegSetting.Builder()
                        .maxDistance(2)
                        .standStillCounter(40)
                        .stepInFront(1.5)
                        .movementSpeed(0.4).build(),
                List.of(new ServerLimb(-0.7, 0, 0.4),
                        new ServerLimb(0.7, 0, 0.4)),
                new EntityLegWithFoot(new WorldCollidingSegment(new Segment.Builder().length(0.5625).angleOffset(70).angleSize(40)), new Segment.Builder().length(1).angleSize(40).angleOffset(110).build(), new Segment.Builder().length(1.3).angleOffset(80).build(), new Segment.Builder().length(0.94).angleOffset(-130).angleSize(40).build()),
                new EntityLegWithFoot(new WorldCollidingSegment(new Segment.Builder().length(0.5625).angleOffset(70).angleSize(40)), new Segment.Builder().length(1).angleSize(40).angleOffset(110).build(), new Segment.Builder().length(1.3).angleOffset(80).build(), new Segment.Builder().length(0.94).angleOffset(-130).angleSize(40).build())));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::predicate));
    }
    public DinoData getDinoData() {
        return this.entityData.get(DINO_DATA);
    }

    @Override
    public void push(Entity pEntity) {
        super.push(pEntity);
    }

    public ResourceLocation getTextureLocation(){
        if(true){
            return new ResourceLocation("projectnublar:textures/entity/tyrannosaurus_rex.png");
        }
        return this.entityData.get(DINO_DATA).getTextureLocation();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DINO_DATA, new DinoData());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("dino_data", entityData.get(DINO_DATA).toNBT());

    }

    @Override
    public void readAdditionalSaveData(CompoundTag $$0) {
        super.readAdditionalSaveData($$0);
        entityData.set(DINO_DATA, DinoData.fromNBT($$0.getCompound("dino_data")));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0D));
    }

    protected PlayState predicate(AnimationState<GeoAnimatable> state) {
        AnimationController<?> controller = state.getController();
        if(isMoving()){
            controller.setAnimation(RawAnimation.begin().thenLoop("walk"));
        } else {
            if(controller.hasAnimationFinished() || (controller.getCurrentAnimation()!=null && Objects.equals(controller.getCurrentAnimation().animation().name(), "walk"))){
                controller.setAnimation(RawAnimation.begin().thenLoop(idleAnimations.get(this.random.nextInt(idleAnimations.size()))));
            }
        }
        return PlayState.CONTINUE;
    }

    public boolean isMoving(){
        return this.xo != this.getX() || this.zo != this.getZ();
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void setDinoData(DinoData dinoData) {
        this.entityData.set(DINO_DATA, dinoData);
    }

    @Override
    public void tick() {
        super.tick();
        this.tickComponentsServer(this);
    }

    @Override
    public List<IKModelComponent> getComponents() {
        return this.components;
    }
}
