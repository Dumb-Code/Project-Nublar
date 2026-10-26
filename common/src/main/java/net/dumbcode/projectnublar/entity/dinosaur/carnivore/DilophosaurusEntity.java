package net.dumbcode.projectnublar.entity.dinosaur.carnivore;

import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class DilophosaurusEntity extends CarnivoreDinosaur {
    /** Adult scale at {@code SIZE} gene 0 (moved from the 1.20.1 {@code DilophosaurusRenderer}). */
    private static final float ADULT_SCALE_OFFSET = 0.5F;
    /**
     * Corrected adult box width, in blocks: the movement box of {@code docs/dinosaur-hitboxes-and-movement-design.md},
     * "Part counts and boxes", line 194. Used only with the scale attribute on.
     */
    private static final float BOX_WIDTH = 0.56F;
    /**
     * Corrected adult box height, in blocks: the posed model height 4.3 times render scale 0.5, with the frill as the
     * highest point, {@code docs/codebase-analysis.md}, "Model heights", line 2784. Used only with the scale attribute on.
     */
    private static final float BOX_HEIGHT = 2.15F;

    public DilophosaurusEntity(EntityType<? extends DilophosaurusEntity> $$0, Level $$1) {
        super($$0, $$1, 39);
    }

    @Override
    protected float adultScaleOffset() {
        return ADULT_SCALE_OFFSET;
    }

    @Override
    protected EntityDimensions correctedBaseBox() {
        return EntityDimensions.scalable(BOX_WIDTH / ADULT_SCALE_OFFSET, BOX_HEIGHT / ADULT_SCALE_OFFSET);
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
