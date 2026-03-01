package net.dumbcode.projectnublar.entity.dinosaur;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class HerbivoreDinosaur extends AbstractDinosaur {

    public HerbivoreDinosaur(EntityType<? extends HerbivoreDinosaur> $$0, Level $$1) {
        super($$0, $$1);
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
