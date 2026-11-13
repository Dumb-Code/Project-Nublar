package net.dumbcode.projectnublar.entity.dinosaur.carnivore;

import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class DilophosaurusEntity extends CarnivoreDinosaur {
    /**
     * Adult scale at {@code SIZE} gene 0: about 3 blocks to the top of the crests with the frill folded, the lead's interim
     * adult height (D11, {@code docs/plan/feature-specs.md}, "Size"). The rest-pose model is 57.4 units tall.
     * It was 0.5 on 1.20.1.
     */
    private static final float ADULT_SCALE_OFFSET = 0.84F;
    /**
     * Corrected adult box width, in blocks: the movement box of {@code docs/dinosaur-hitboxes-and-movement-design.md},
     * "Part counts and boxes", line 194: 0.56 at render scale 0.5, brought to scale 0.84. Used only with the scale
     * attribute on.
     */
    private static final float BOX_WIDTH = 0.94F;
    /**
     * Corrected adult box height, in blocks: the rest-pose model height 57.4 units (3.59 blocks) times render scale 0.84,
     * frill folded. It replaces the open-frill height of
     * {@code docs/codebase-analysis.md}, "Model heights", line 2784. Used only with the scale attribute on.
     */
    private static final float BOX_HEIGHT = 3.01F;
    /**
     * Drawn model reach, in blocks at render scale 1: the largest horizontal distance of any vertex from the model
     * origin in {@code dilophosaurus.geo.json}'s bind pose, bone and cube rotations applied (the tail tip). Used only
     * for view culling.
     */
    private static final float MODEL_REACH = 4.915F;
    /** Drawn model top, in blocks at render scale 1: the same measurement's highest vertex (the frill). */
    private static final float MODEL_TOP = 4.248F;
    /** {@link #modelBounds()}: built once, as the renderer reads it every frame. */
    private static final EntityDimensions MODEL_BOUNDS = EntityDimensions.scalable(2 * MODEL_REACH, MODEL_TOP);

    public DilophosaurusEntity(EntityType<? extends DilophosaurusEntity> $$0, Level $$1) {
        super($$0, $$1, 39);
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

    // TODO(BUG): calls super.canTarget(...) but ignores the result, reducing this species to
    // revenge-only targeting.
    @Override
    public boolean canTarget(LivingEntity target) {
        super.canTarget(target);
        if(this.getLastAttacker() != target ){
            return false;
        }
        return target.getVehicle() != this;
    }
}
