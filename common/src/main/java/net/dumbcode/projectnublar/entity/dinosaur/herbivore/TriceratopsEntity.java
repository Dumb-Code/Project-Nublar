package net.dumbcode.projectnublar.entity.dinosaur.herbivore;

import net.dumbcode.projectnublar.entity.dinosaur.HerbivoreDinosaur;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TriceratopsEntity extends HerbivoreDinosaur {

    public TriceratopsEntity(EntityType<? extends TriceratopsEntity> $$0, Level $$1) {
        super($$0, $$1, 39);
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
