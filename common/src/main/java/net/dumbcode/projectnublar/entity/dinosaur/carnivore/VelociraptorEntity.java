package net.dumbcode.projectnublar.entity.dinosaur.carnivore;

import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class VelociraptorEntity extends CarnivoreDinosaur {

    public VelociraptorEntity(EntityType<? extends VelociraptorEntity> $$0, Level $$1) {
        super($$0, $$1, 0);
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
