package net.dumbcode.projectnublar.entity.dinosaur;

import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.api.Dinosaur;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CarnivoreDinosaur extends AbstractDinosaur {

    public CarnivoreDinosaur(EntityType<? extends CarnivoreDinosaur> $$0, Level $$1, Dinosaur pDinosaur, DinoBehaviourData pBehaviourData) {
        super($$0, $$1,pDinosaur,pBehaviourData);

    }

    public boolean canHuntWithPack() {
        return this.hasGroup() && !this.isBaby() && !this.isJuvanile();
    }
    @Override
    public boolean canTarget(LivingEntity target) {

        if(target instanceof Player player && (player.isSpectator() || player.isCreative())){
            return false;
        }

        //Check if target outside fence
       if(!DinoNeedsUtils.isTargetInsideEnclosure(target, this) && !DinoNeedsUtils.starving(this)){
           return false;
       }

       //Avoid Cannibalism unless starving
        if(target instanceof AbstractDinosaur targetDinosaur) {
            if (this.getDinoData().getBaseDino() == targetDinosaur.getDinoData().getBaseDino()) {
                return false;
            }
        }
       //check this is old enough to hunt
        if(this.isChild()){
            return false;
        }

        //check that target is not family
        if(this.isFamily(target)){
            return false;
        }

        //check that target is not trusted players
        if(target instanceof ServerPlayer player && this.getReputationForPlayer(player) > 50){
               return DinoNeedsUtils.starving(this);
       }

        //Check that target is not owner
        if(this.isTame() && this.getOwner() == target && !DinoNeedsUtils.starving(this)){
                    return false;
        }

        //Engage in wreckless target finding if starving
        float modifier = 1.0F;
        if (DinoNeedsUtils.starving(this)){
            modifier = 10.0F;
        }

        //Evaluate target threat compaired to own threat.
        if(DinoNeedsUtils.getThreatScore(this) * modifier < DinoNeedsUtils.getThreatScore(target)){
            return false;
        }

        //Ensure target is not self and If target is last attacker then go for revenge.
        return target.getVehicle() != this || this.getLastAttacker() == target;
    }


}
