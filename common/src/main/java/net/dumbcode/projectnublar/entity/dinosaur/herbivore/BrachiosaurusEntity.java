package net.dumbcode.projectnublar.entity.dinosaur.herbivore;

import net.dumbcode.projectnublar.entity.dinosaur.HerbivoreDinosaur;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BrachiosaurusEntity extends HerbivoreDinosaur {
    /** Adult scale at {@code SIZE} gene 0 (moved from the 1.20.1 {@code BrachiosaurusRenderer}). */
    private static final float ADULT_SCALE_OFFSET = 2.5F;
    /**
     * Corrected adult box width, in blocks: the movement box of {@code docs/dinosaur-hitboxes-and-movement-design.md},
     * "Part counts and boxes", line 192 (the legs spread 3.68 blocks, "Notes on the layouts", line 223). Used only
     * with the scale attribute on.
     */
    private static final float BOX_WIDTH = 3.68F;
    /**
     * Corrected adult box height, in blocks: the posed model height 7.723 (measured from
     * {@code brachiosaurus.geo.json} with the bone and cube rotations applied, highest point the male crest) times
     * render scale 2.5. {@code docs/codebase-analysis.md}, "Model heights", line 2787, lists 7.8; Task 15 confirms
     * the value with F3+B. Used only with the scale attribute on.
     */
    private static final float BOX_HEIGHT = 19.31F;
    /**
     * Drawn model reach, in blocks at render scale 1: the largest horizontal distance of any vertex from the model
     * origin in {@code brachiosaurus.geo.json}'s bind pose, bone and cube rotations applied (the tail tip). Used only
     * for view culling.
     */
    private static final float MODEL_REACH = 4.666F;
    /** Drawn model top, in blocks at render scale 1: the same measurement's highest vertex (the male crest). */
    private static final float MODEL_TOP = 7.723F;
    /** {@link #modelBounds()}: built once, as the renderer reads it every frame. */
    private static final EntityDimensions MODEL_BOUNDS = EntityDimensions.scalable(2 * MODEL_REACH, MODEL_TOP);

    public BrachiosaurusEntity(EntityType<? extends BrachiosaurusEntity> $$0, Level $$1) {
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
