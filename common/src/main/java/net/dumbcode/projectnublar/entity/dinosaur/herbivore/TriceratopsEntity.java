package net.dumbcode.projectnublar.entity.dinosaur.herbivore;

import net.dumbcode.projectnublar.entity.dinosaur.HerbivoreDinosaur;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TriceratopsEntity extends HerbivoreDinosaur {
    /**
     * Adult scale at {@code SIZE} gene 0: 3.0 blocks tall, the lead's interim adult height (D11, {@code docs/plan/feature-specs.md}, "Size").
     * It was 2.0 on 1.20.1.
     */
    private static final float ADULT_SCALE_OFFSET = 1.32F;
    /**
     * Corrected adult box width, in blocks: the movement box of {@code docs/dinosaur-hitboxes-and-movement-design.md},
     * "Part counts and boxes", line 191 (the frill is a head part, not the movement box): 2.19 at render scale 2.0,
     * brought to scale 1.32. Used only with the scale attribute on.
     */
    private static final float BOX_WIDTH = 1.45F;
    /**
     * Corrected adult box height, in blocks: the posed model height 2.3 times render scale 1.32,
     * {@code docs/codebase-analysis.md}, "Model heights", line 2786. Used only with the scale attribute on.
     */
    private static final float BOX_HEIGHT = 3.0F;
    /**
     * Drawn model reach, in blocks at render scale 1: the largest horizontal distance of any vertex from the model
     * origin in {@code triceratops.geo.json}'s bind pose, bone and cube rotations applied (the tail tip). Used only
     * for view culling.
     */
    private static final float MODEL_REACH = 2.945F;
    /** Drawn model top, in blocks at render scale 1: the same measurement's highest vertex (the frill). */
    private static final float MODEL_TOP = 2.267F;
    /** {@link #modelBounds()}: built once, as the renderer reads it every frame. */
    private static final EntityDimensions MODEL_BOUNDS = EntityDimensions.scalable(2 * MODEL_REACH, MODEL_TOP);

    public TriceratopsEntity(EntityType<? extends TriceratopsEntity> $$0, Level $$1) {
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
