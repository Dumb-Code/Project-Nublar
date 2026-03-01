package net.dumbcode.projectnublar.entity.ai.tasks;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.dumbcode.projectnublar.init.MemoryModuleTypeInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class SetMateFromNearbyDinosaurs<E extends AbstractDinosaur> extends ExtendedBehaviour<E> {

   @Nullable protected LivingEntity pMate = null;


    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleTypeInit.MATE_UUID.get(), MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

   protected Predicate<LivingEntity> canChoosePredicate = (dinosaur) -> dinosaur instanceof AbstractDinosaur dino && dino.isAlive();
protected BiPredicate<E, LivingEntity> canChooseAsMatePredicate = (dinosaur, pMate) -> (pMate instanceof AbstractDinosaur mate) &&  dinosaur.canMateWith(dinosaur, mate);


    public SetMateFromNearbyDinosaurs<E> canChooseAsMatePredicate(final BiPredicate<E, LivingEntity> predicate){
        this.canChooseAsMatePredicate = predicate;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if(this.pMate == null){
            NearestVisibleLivingEntities nearyByEntities = BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            this.pMate = nearyByEntities.findClosest(this.canChoosePredicate).orElse(null);
            if(this.pMate == null){
                return false;
            }
        }
        return this.canChooseAsMatePredicate.test(entity, this.pMate);
    }

    @Override
    protected void start(E entity) {
        if(this.pMate instanceof AbstractDinosaur dinosaur) {
            BrainUtils.setMemory(entity, MemoryModuleTypeInit.MATE_UUID.get(), dinosaur.getUUID());
            BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.MATE_UUID.get(), entity.getUUID());
            BrainUtils.setMemory(entity, MemoryModuleTypeInit.MATE.get(), dinosaur);
            BrainUtils.setMemory(dinosaur, MemoryModuleTypeInit.MATE.get(), entity);
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
