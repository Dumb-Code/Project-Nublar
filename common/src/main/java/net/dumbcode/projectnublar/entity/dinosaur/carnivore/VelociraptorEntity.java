package net.dumbcode.projectnublar.entity.dinosaur.carnivore;

import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class VelociraptorEntity extends CarnivoreDinosaur {
    /** Adult scale at {@code SIZE} gene 0 (moved from the 1.20.1 {@code VelociraptorRenderer}). */
    private static final float ADULT_SCALE_OFFSET = 0.6F;
    /**
     * Corrected adult box width, in blocks: the movement box of {@code docs/dinosaur-hitboxes-and-movement-design.md},
     * "Part counts and boxes", line 193. Used only with the scale attribute on.
     */
    private static final float BOX_WIDTH = 0.67F;
    /**
     * Corrected adult box height, in blocks: the posed model height 3.5 times render scale 0.6, with the quills as
     * the highest point, {@code docs/codebase-analysis.md}, "Model heights", line 2783. Used only with the scale
     * attribute on.
     */
    private static final float BOX_HEIGHT = 2.1F;
    /**
     * Drawn model reach, in blocks at render scale 1: the largest horizontal distance of any vertex from the model
     * origin in {@code velociraptor.geo.json}'s bind pose, bone and cube rotations applied (the tail tip). Used only
     * for view culling.
     */
    private static final float MODEL_REACH = 5.174F;
    /** Drawn model top, in blocks at render scale 1: the same measurement's highest vertex (the quills). */
    private static final float MODEL_TOP = 3.490F;
    /** {@link #modelBounds()}: built once, as the renderer reads it every frame. */
    private static final EntityDimensions MODEL_BOUNDS = EntityDimensions.scalable(2 * MODEL_REACH, MODEL_TOP);

    public VelociraptorEntity(EntityType<? extends VelociraptorEntity> $$0, Level $$1) {
        super($$0, $$1, 0);
    }

    @Override
    protected float adultScaleOffset() {
        return ADULT_SCALE_OFFSET;
    }

    @Override
    protected EntityDimensions correctedBaseBox() {
        return EntityDimensions.scalable(BOX_WIDTH / ADULT_SCALE_OFFSET, BOX_HEIGHT / ADULT_SCALE_OFFSET);
    }

    @Override
    public EntityDimensions modelBounds() {
        return MODEL_BOUNDS;
    }

    // TODO(BUG): calls super.canTarget(...) but ignores the result, reducing this species to
    // revenge-only targeting. (same in Dilophosaurus/Triceratops/Herbivore).
    @Override
    public boolean canTarget(LivingEntity target) {
        super.canTarget(target);
        if(this.getLastAttacker() != target ){
            return false;
        }
        return target.getVehicle() != this;
    }
}
