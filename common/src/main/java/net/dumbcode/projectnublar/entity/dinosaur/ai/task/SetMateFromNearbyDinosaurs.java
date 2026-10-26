package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

// TODO(DEAD): not wired into any activity group; mates are never assigned this way. Left
// unwired on purpose.
public class SetMateFromNearbyDinosaurs<E extends Dinosaur> extends ExtendedBehaviour<E> {

   @Nullable protected LivingEntity pMate = null;


    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2)
            .noMemory(MemoryModuleTypeInit.MATE_UUID.get())
            .hasMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

   protected Predicate<LivingEntity> canChoosePredicate = (dinosaur) -> dinosaur instanceof Dinosaur dino && dino.isAlive();
protected BiPredicate<E, LivingEntity> canChooseAsMatePredicate = (dinosaur, pMate) -> (pMate instanceof Dinosaur mate) &&  dinosaur.canMateWith(dinosaur, mate);


    public SetMateFromNearbyDinosaurs<E> canChooseAsMatePredicate(final BiPredicate<E, LivingEntity> predicate){
        this.canChooseAsMatePredicate = predicate;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if(this.pMate == null){
            NearestVisibleLivingEntities nearyByEntities = BrainUtil.getMemory(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            this.pMate = nearyByEntities.findClosest(this.canChoosePredicate).orElse(null);
            if(this.pMate == null){
                return false;
            }
        }
        return this.canChooseAsMatePredicate.test(entity, this.pMate);
    }

    @Override
    protected void start(E entity) {
        if(this.pMate instanceof Dinosaur dinosaur) {
            BrainUtil.setMemory(entity, MemoryModuleTypeInit.MATE_UUID.get(), dinosaur.getUUID());
            BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.MATE_UUID.get(), entity.getUUID());
            BrainUtil.setMemory(entity, MemoryModuleTypeInit.MATE.get(), dinosaur);
            BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.MATE.get(), entity);
            dinosaur.createDinosaurFamily(entity);
            dinosaur.registerDinoMate(entity.getUUID());
            entity.registerDinoMate(dinosaur.getUUID());
            if(entity instanceof CarnivoreDinosaur carnivore){
             //   CarnivoreDinosaur mate = (CarnivoreDinosaur) this.pMate;
              //  if(carnivore.hasPack() && !mate.hasPack()) {
                //    carnivore.getPackEntity().registerWithPack(mate);
            //    }
              //  if(!carnivore.hasPack() && mate.hasPack()){
               //     mate.getPackEntity().registerWithPack(carnivore);
         //       }
              //  if(!carnivore.hasPack() && !mate.hasPack()){
                 //   EntityType<PackEntity> pack = EntityInit.CARNIVORE_PACK.get();
                  //  BlockPos pos = carnivore.getOnPos();
                  //  ServerLevel serverLevel = carnivore.getServer().overworld();
                 //   PackEntity packEntity = pack.spawn(serverLevel,pos, MobSpawnType.EVENT);
                  //  packEntity.registerWithPack(carnivore);
                  //  packEntity.registerWithPack(mate);
                  //  packEntity.setPackLeader(Optional.of(carnivore.getUUID()));

              //  }
            }
        }
    }
}
