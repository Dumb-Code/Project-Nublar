package net.dumbcode.projectnublar.entity.dinosaur.omnivore;

import net.dumbcode.projectnublar.entity.dinosaur.HerbivoreDinosaur;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class GallimimusEntity extends HerbivoreDinosaur {
    /** Adult scale at {@code SIZE} gene 0 (moved from the 1.20.1 {@code GallimimusRenderer}). */
    private static final float ADULT_SCALE_OFFSET = 1F;
    /**
     * Corrected adult box width, in blocks: the movement box of {@code docs/dinosaur-hitboxes-and-movement-design.md},
     * "Part counts and boxes", line 189 (layout reviewed by the lead, "Notes on the layouts", line 222). Used only with
     * the scale attribute on.
     */
    private static final float BOX_WIDTH = 0.80F;
    /**
     * Corrected adult box height, in blocks: the posed model height at render scale 1.0, {@code docs/codebase-analysis.md},
     * "Model heights", line 2785. Used only with the scale attribute on.
     */
    private static final float BOX_HEIGHT = 3.3F;
    /**
     * Drawn model reach, in blocks at render scale 1: the largest horizontal distance of any vertex from the model
     * origin in {@code gallimimus.geo.json}'s bind pose, bone and cube rotations applied (the tail tip). Used only
     * for view culling.
     */
    private static final float MODEL_REACH = 2.950F;
    /** Drawn model top, in blocks at render scale 1: the same measurement's highest vertex (the head). */
    private static final float MODEL_TOP = 3.301F;
    /** {@link #modelBounds()}: built once, as the renderer reads it every frame. */
    private static final EntityDimensions MODEL_BOUNDS = EntityDimensions.scalable(2 * MODEL_REACH, MODEL_TOP);

    public GallimimusEntity(EntityType<? extends GallimimusEntity> $$0, Level $$1) {
        super($$0, $$1, 0);
    }

    @Override
    public float adultScaleOffset() {
        return ADULT_SCALE_OFFSET;
    }

    @Override
    public EntityDimensions correctedBaseBox() {
        return EntityDimensions.scalable(BOX_WIDTH / ADULT_SCALE_OFFSET, BOX_HEIGHT / ADULT_SCALE_OFFSET);
    }

    @Override
    public EntityDimensions modelBounds() {
        return MODEL_BOUNDS;
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        super.canTarget(target);
        if(this.getLastAttacker() != target ){
            return false;
        }
        return target.getVehicle() != this;
    }
}
