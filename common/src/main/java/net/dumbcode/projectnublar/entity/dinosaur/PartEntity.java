package net.dumbcode.projectnublar.entity.dinosaur;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public abstract class PartEntity extends Entity {
    private static final EntityDataAccessor<Optional<UUID>> PARENT_UUID = SynchedEntityData.defineId(PartEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> SCALE_WIDTH = SynchedEntityData.defineId(PartEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SCALE_HEIGHT = SynchedEntityData.defineId(PartEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PART_YAW = SynchedEntityData.defineId(PartEntity.class, EntityDataSerializers.FLOAT);
    public EntityDimensions multiPartSize;
    public float radius;
    public float angleYaw;
    public float offsetY;
    protected float damageMultiplier;
    private String name;

    protected PartEntity(EntityType<?> type,Level world) {
        super(type, world);
        multiPartSize = type.getDimensions();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void doWaterSplashEffect() {

    }
    public PartEntity(EntityType<?> type, Entity parent,String name, float radius, float angleYaw, float offsetY, float sizeX,
                               float sizeY, float damageMultiplier) {
        super(type, parent.level());
        this.setParent(parent);
        this.setScaleX(sizeX);
        this.setScaleY(sizeY);
        this.radius = radius;
        this.angleYaw = (angleYaw + 90.0F) * ((float) Math.PI / 180.0F);
        this.offsetY = offsetY;
        this.name = name;
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return new EntityDimensions(getScaleX(), getScaleY(), false);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(PARENT_UUID, Optional.empty());
        this.entityData.define(SCALE_WIDTH, 0.5F);
        this.entityData.define(SCALE_HEIGHT, 0.5F);
        this.entityData.define(PART_YAW, 0F);
    }
    @Nullable
    public UUID getParentId() {
        return this.entityData.get(PARENT_UUID).orElse(null);
    }

    public void setParentId(@Nullable UUID uniqueId) {
        this.entityData.set(PARENT_UUID, Optional.ofNullable(uniqueId));
    }

    private float getScaleX() {
        return this.entityData.get(SCALE_WIDTH);
    }

    private void setScaleX(float scale) {
        this.entityData.set(SCALE_WIDTH, scale);
    }

    private float getScaleY() {
        return this.entityData.get(SCALE_HEIGHT);
    }

    private void setScaleY(float scale) {
        this.entityData.set(SCALE_HEIGHT, scale);
    }

    public float getPartYaw() {
        return this.entityData.get(PART_YAW);
    }

    private void setPartYaw(float yaw) {
        this.entityData.set(PART_YAW, yaw % 360);
    }
    public String getPartName(){
        if(name != null){
            return this.name;
        } else return "body";
    }

    @Override
    public void tick() {

        wasTouchingWater = false;
        if (this.tickCount > 10) {
            Entity parent = getParent();
            refreshDimensions();
            if (parent != null && !level().isClientSide) {

                this.markHurt();

                if (!this.level().isClientSide) {
                    this.collideWithNearbyEntities();
                }
                if (parent.isRemoved() && !level().isClientSide) {
                    this.remove(RemovalReason.DISCARDED);
                }
            } else if (tickCount > 20 && !level().isClientSide) {
                remove(RemovalReason.DISCARDED);
            }
        }


        super.tick();
    }
    protected boolean isSlowFollow(){
        return false;
    }
    /** Source: {@link net.minecraft.world.entity.ai.control.MoveControl(float, float, float)} */
    protected float limitAngle(float sourceAngle, float targetAngle, float maximumChange) {
        float f = Mth.wrapDegrees(targetAngle - sourceAngle);
        if (f > maximumChange) {
            f = maximumChange;
        }

        if (f < -maximumChange) {
            f = -maximumChange;
        }

        float f1 = sourceAngle + f;
        if (f1 < 0.0F) {
            f1 += 360.0F;
        } else if (f1 > 360.0F) {
            f1 -= 360.0F;
        }

        return f1;
    }
    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(RemovalReason.DISCARDED);
    }

    public Entity getParent() {
        UUID id = getParentId();

        if (id != null && level() instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(id);
        }

        return null;
    }

    public void setParent(Entity entity) {
        this.setParentId(entity.getUUID());
    }

    @Override
    public boolean is(@NotNull Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    public void collideWithNearbyEntities() {
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(0.20000000298023224D, 0.0D, 0.20000000298023224D));
        Entity parent = this.getParent();
        if (parent != null) {
            entities.stream().filter(entity -> entity != parent && !sharesRider(parent, entity) && !(entity instanceof DinosaurPart) && entity.isPushable()).forEach(entity -> entity.push(parent));

        }
    }

    public static boolean sharesRider(Entity parent, Entity entityIn) {
        for (Entity entity : parent.getPassengers()) {
            if (entity.equals(entityIn)) {
                return true;
            }

            if (sharesRider(entity, entityIn)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        Entity parent = getParent();
        if (level().isClientSide && parent != null) {
           //to do send packet here
        }
        return parent != null ? parent.interact(player, hand) : InteractionResult.PASS;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float damage) {
        Entity parent = getParent();
        if (level().isClientSide && source.getEntity() instanceof Player && parent != null) {
            //send packet
        }
        return parent != null && parent.hurt(source, damage * this.damageMultiplier);
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.LAVA) || source.is(DamageTypeTags.IS_FIRE) || super.isInvulnerableTo(source);
    }

    public boolean shouldContinuePersisting() {
        if(this.getParent() != null) {
            return getParent().isAlive() || this.isRemoved();
        } else return this.isRemoved();
    }

}
