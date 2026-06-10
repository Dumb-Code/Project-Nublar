package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

/**
 * Turf-war behaviour between two large carnivores (approach, roar exchange, then fight/flee).
 *
 * <p>TODO(DEAD): not wired into any activity group - {@code StartTurfWar}/turf-war memories are
 * never registered into a brain, so this behaviour currently never runs. Left unwired on purpose.
 */
public class LargeCarnivoreFight<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleTypeInit.INITIATED_TURF_WAR.get(), MemoryStatus.VALUE_PRESENT));
    private int turfWarTicks;
    private Dinosaur host;
    private Dinosaur socialTarget;

    private boolean turfWarStarted;

    public LargeCarnivoreFight() {
        this.turfWarStarted = false;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if(BrainUtils.hasMemory(entity, MemoryModuleTypeInit.INITIATED_TURF_WAR.get())) {
            if(!this.turfWarStarted){
                this.turfWarStarted = true;
                return true;
            } else return false;
        } else return false;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of();
    }

    @Override
    protected void start(E dinosaur) {
        if(BrainUtils.hasMemory(dinosaur, MemoryModuleTypeInit.INITIATED_TURF_WAR.get())) {
            int dinoMember = BrainUtils.getMemory(dinosaur, MemoryModuleTypeInit.TURF_WAR_MEMBER.get());

            if (dinoMember == 1) {
                this.host = dinosaur;
                this.socialTarget = BrainUtils.getMemory(dinosaur, MemoryModuleTypeInit.SOCIAL_TARGET.get());
            } else {
                this.host = BrainUtils.getMemory(dinosaur, MemoryModuleTypeInit.SOCIAL_TARGET.get());
                this.socialTarget = dinosaur;
            }
        }
    }

    @Override
    protected void tick(E entity) {
        if (turfWarTicks != 0) {
            turfWarTicks++;
        }
        if (getHost() == entity) {

        if (getTarget() != null) {
            Dinosaur host = getHost();
            Dinosaur target = getTarget();

            if (host.distanceTo(target) > 10 && turfWarTicks == 0) {
                BrainUtils.setMemory(host, MemoryModuleType.WALK_TARGET, new WalkTarget(target.position(), 1.0F, 10));
            }

            if (host.distanceTo(target) < 12) {
                if (turfWarTicks == 0) {
                    turfWarTicks++;
                }
            }

            //Maybe Roar at beginning
            if (turfWarTicks == 40) {
                if (getTurfWarIdNo() == 1) {
                    BrainUtils.setMemory(host, MemoryModuleTypeInit.IS_ROARING.get(), true);
                }

            }
            if (turfWarTicks == 80) {
                if (getTurfWarIdNo() == 2) {
                    BrainUtils.setMemory(host, MemoryModuleTypeInit.IS_ROARING.get(), true);
                }
            }


            if (turfWarTicks == 100) {

                if (BrainUtils.hasMemory(host, MemoryModuleTypeInit.TURF_WAR_OUTCOME.get())) {
                    int outcome = BrainUtils.getMemory(host, MemoryModuleTypeInit.TURF_WAR_OUTCOME.get());

                    if (outcome == 1) {
                        doFight(entity);
                    }
                    if (outcome == 2) {
                        doThreatDisplayTargetFlees(entity);
                    }
                    if (outcome == 3) {
                        this.stop(entity);
                    }
                }
            }
            if (target == null || host.isDeadOrDying() || target.isDeadOrDying()) {
                this.stop(entity);
            }
        }
        }
        super.tick(entity);
    }
    public int getTurfWarIdNo(){
        if(BrainUtils.hasMemory(getHost(), MemoryModuleTypeInit.TURF_WAR_MEMBER.get())) {
            return BrainUtils.getMemory(getHost(), MemoryModuleTypeInit.TURF_WAR_MEMBER.get());
        } else return 1;
    }
    public Dinosaur getHost(){
        return this.host;
    }
    public Dinosaur getTarget(){
        return this.socialTarget;
    }
    public void doFight(E entity){
        BrainUtils.clearMemory(getHost(), MemoryModuleType.WALK_TARGET);
        BrainUtils.setTargetOfEntity(getHost(), getTarget());
        BrainUtils.clearMemory(getHost(), MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        this.stop(entity);
    }
    public void doThreatDisplayTargetFlees(E entity) {
        BrainUtils.setMemory(getHost(), MemoryModuleTypeInit.IS_ROARING.get(), true);
        BrainUtils.setMemory(getTarget(), MemoryModuleType.IS_PANICKING, true);
        this.stop(entity);
    }
    @Override
    protected void stop(E entity) {
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.INITIATED_TURF_WAR.get());
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.TURF_WAR_MEMBER.get());
    }
}
