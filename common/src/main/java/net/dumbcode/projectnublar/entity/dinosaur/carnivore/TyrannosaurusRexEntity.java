package net.dumbcode.projectnublar.entity.dinosaur.carnivore;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.DinosaurPart;
import net.dumbcode.projectnublar.registry.SoundInit;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TyrannosaurusRexEntity extends CarnivoreDinosaur {
    /** Adult scale at {@code SIZE} gene 0 (moved from the 1.20.1 {@code TyrannosaurusRenderer}). */
    private static final float ADULT_SCALE_OFFSET = 1F;
    /**
     * Corrected adult box width, in blocks: the movement box of {@code docs/dinosaur-hitboxes-and-movement-design.md},
     * "Part counts and boxes", line 190. Used only with the scale attribute on.
     */
    private static final float BOX_WIDTH = 1.56F;
    /**
     * Corrected adult box height, in blocks: the posed model height at render scale 1.0, {@code docs/codebase-analysis.md},
     * "Model heights", line 2782 (inside the card's adult range of 3.78 to 6 blocks). Used only with the scale attribute on.
     */
    private static final float BOX_HEIGHT = 4.0F;

    public TyrannosaurusRexEntity(EntityType<? extends TyrannosaurusRexEntity> $$0, Level $$1) {
        super($$0, $$1, 34);
    }

    @Override
    protected float adultScaleOffset() {
        return ADULT_SCALE_OFFSET;
    }

    @Override
    protected EntityDimensions correctedBaseBox() {
        return EntityDimensions.scalable(BOX_WIDTH / ADULT_SCALE_OFFSET, BOX_HEIGHT / ADULT_SCALE_OFFSET);
    }

    /** The head part, in unscaled sizes: the part follows this animal's scale itself ({@code PartEntity}). */
    @Override
    public void resetParts(float scale) {
        removeParts();
        head = new DinosaurPart(this,"head", 1.0F, 0, 2.0F, 1F, 1F, 1.5F);
        head.setParent(this);

        this.subEntities = new DinosaurPart[]{this.head};
    }

    @Override
    public void removeParts() {
        if (head != null) {
            head.remove(RemovalReason.DISCARDED);
            head = null;
        }
        if (subEntities != null) {
            subEntities = null;
        }
    }

    @Override
    public void updateParts() {
        updatePart(head, this);
        this.subEntities = new DinosaurPart[]{this.head};
    }

    @Override
    public void updatePart(@Nullable DinosaurPart part, @NotNull Dinosaur parent) {
        if (part == null || !(parent.level() instanceof ServerLevel serverLevel) || parent.isRemoved()) {
            return;
        }

        if (!part.shouldContinuePersisting()) {
            UUID uuid = part.getUUID();
            Entity existing = serverLevel.getEntity(uuid);

            // Update UUID if a different entity with the same UUID exists already
            if (existing != null && existing != part) {
                while (serverLevel.getEntity(uuid) != null) {
                    uuid = Mth.createInsecureUUID(parent.getRandom());
                }
                Constants.LOG.debug("Updated the UUID of [{}] due to a clash with [{}]", part, existing);
            }

            part.setUUID(uuid);
            serverLevel.addFreshEntity(part);
        }
        part.setParent(parent);
        if (parent != null && !part.level().isClientSide()) {
            float renderYawOffset =  parent.yBodyRot;

            if(part.getPartName().equals("head")) {
                part.setPos(parent.getHeadBonePos());
                //   part.setPos(parent.getHeadBonePos().x + part.radius * Mth.cos((float) (renderYawOffset * (Math.PI / 180.0F) + part.angleYaw)), parent.getEntityData().get(Dinosaur.DINOSAUR_HEAD_Y), parent.getHeadBonePos().z + part.radius * Mth.sin((float) (renderYawOffset * (Math.PI / 180.0F) + part.angleYaw)));
            }


        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
    }

    @Override
    public void tick() {
        super.tick();
        refreshDimensions();
        updateParts();
    }


    @Override
    public void remove(RemovalReason reason) {
        removeParts();
        super.remove(reason);
    }

    @Override
    public @Nullable SoundEvent getRoarSound() {return SoundInit.TYRANNOSAUR_ROAR.get();}
    @Override
    protected @Nullable SoundEvent getAmbientSound() {return SoundInit.TYRANNOSAUR_BREATH.get();}
    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundInit.TYRANNOSAUR_HURT.get();
    }
    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundInit.TYRANNOSAUR_DEATH.get();
    }
    @Override
    public @Nullable SoundEvent getAttackGrowlSound() {return SoundInit.TYRANNOSAUR_GROWL.get();}
    @Override
    public @Nullable SoundEvent getAttackSound() {return SoundInit.TYRANNOSAUR_BITE.get();}
}
