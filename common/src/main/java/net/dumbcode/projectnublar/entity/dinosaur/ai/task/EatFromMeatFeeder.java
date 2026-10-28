package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.DelayedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public class EatFromMeatFeeder<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS =
            MemoryTest.builder(2).hasMemory(MemoryModuleType.WALK_TARGET)
                    .hasMemory(MemoryModuleTypeInit.IS_HUNGRY.get());

    protected BiPredicate<E,? extends DinosaurFeederBlockEntity> targetPredicate = (dinosaur, feeder) -> true ;

    private DinosaurFeederBlockEntity feeder;

    public EatFromMeatFeeder(int delayTicks) {
        super(delayTicks);
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {

        if(level.getBlockEntity(BrainUtil.getMemory(dinosaur,MemoryModuleType.WALK_TARGET).getTarget().currentBlockPosition()) instanceof DinosaurFeederBlockEntity fbe) {
            feeder = fbe;
            return dinosaur.distanceToSqr(Vec3.atCenterOf(feeder.getBlockPos())) <=2;
        } else return false;
    }

    /**
     * Eats from the first feeder slot that holds food in the animal's diet, one item at a time through
     * {@link DinoNeedsUtils#feed} (as {@code Eat} does with food on the ground), until the animal is full or the slot
     * is empty. The diet is keyed by item description id; the old check looked items up by stack, which never
     * matched, so a feeder visit fed nothing (the same on 1.20.1).
     */
    @Override
    protected void doDelayedAction(E dinosaur) {
        if (this.feeder == null || dinosaur.getDinoDiet() == null) {
            return;
        }
        Map<String, Double> diet = dinosaur.getDinoDiet().foodMap();
        for (int slot = 0; slot < this.feeder.getContainerSize(); slot++) {
            ItemStack stack = this.feeder.getItem(slot);
            if (stack.isEmpty() || !diet.containsKey(stack.getItem().getDescriptionId())) {
                continue;
            }
            String food = stack.getItem().getDescriptionId();
            while (!this.feeder.getItem(slot).isEmpty()
                    && DinoNeedsUtils.getCurrentHunger(dinosaur) < DinoNeedsUtils.getMaxHunger(dinosaur)) {
                float before = DinoNeedsUtils.getCurrentHunger(dinosaur);
                DinoNeedsUtils.feed(dinosaur, food);
                this.feeder.removeItem(slot, 1);
                // feed caps hunger at 100; an animal whose need maximum is higher stops here instead of
                // emptying the slot for nothing.
                if (DinoNeedsUtils.getCurrentHunger(dinosaur) <= before) {
                    break;
                }
            }
            // An emptied feeder stops showing food; otherwise SetHunting keeps waiting for it and the carnivore
            // keeps walking back to an empty feeder.
            if (this.feeder.isEmpty()) {
                this.feeder.setTexture(false);
            }
            return;
        }
    }

    @Override
    protected void start(E dinosaur) {
        BrainUtil.clearMemory(dinosaur,MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());
      BrainUtil.setMemory(dinosaur, MemoryModuleTypeInit.IS_EATING.get(), true);
    }

    @Override
    protected void stop(E dinosaur) {

        BrainUtil.clearMemory(dinosaur,MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());
        BrainUtil.clearMemory(dinosaur,MemoryModuleTypeInit.IS_EATING.get());
        BrainUtil.clearMemory(dinosaur, MemoryModuleType.WALK_TARGET);
        BrainUtil.clearMemory(dinosaur, MemoryModuleType.LOOK_TARGET);
    }

}
