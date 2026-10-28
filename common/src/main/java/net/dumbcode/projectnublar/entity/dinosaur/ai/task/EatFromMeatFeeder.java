package net.dumbcode.projectnublar.entity.dinosaur.ai.task;

import net.dumbcode.projectnublar.block.entity.DinosaurFeederBlockEntity;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
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

/**
 * A hungry animal in reach of the feeder its sensor found eats the food of its diet there. Any hungry animal in reach
 * eats, not only one that walked there ({@link WalkToNearestFeeder} ends its walk in this reach).
 */
public class EatFromMeatFeeder<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS =
            MemoryTest.builder(2).hasMemory(MemoryModuleTypeInit.HAS_FOUND_FEEDER.get())
                    .hasMemory(MemoryModuleTypeInit.IS_HUNGRY.get());

    /**
     * Eating reach: 1.41 blocks (squared 2) from the feeder block's centre, the reach of the 1.20.1 test, but measured to
     * the side of the animal's body instead of to its feet, so a wide species that stands beside the feeder reaches it
     * too (before Task 41 a body wider than about 1.6 blocks never did).
     */
    private static final double REACH_SQR = 2.0D;

    protected BiPredicate<E,? extends DinosaurFeederBlockEntity> targetPredicate = (dinosaur, feeder) -> true ;

    private DinosaurFeederBlockEntity feeder;

    public EatFromMeatFeeder(int delayTicks) {
        super(delayTicks);
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    /** Whether the animal's body is in eating reach of the feeder at {@code feeder} ({@link #REACH_SQR}). */
    public static boolean inReach(Dinosaur dinosaur, BlockPos feeder) {
        return dinosaur.getBoundingBox().distanceToSqr(Vec3.atCenterOf(feeder)) <= REACH_SQR;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E dinosaur) {
        BlockPos pos = BrainUtil.getMemory(dinosaur, MemoryModuleTypeInit.HAS_FOUND_FEEDER.get());
        if (level.getBlockEntity(pos) instanceof DinosaurFeederBlockEntity fbe && fbe.holdsFoodFor(dinosaur)) {
            feeder = fbe;
            return inReach(dinosaur, pos);
        }
        return false;
    }

    /**
     * Eats from the first feeder slot that holds food in the animal's diet, one item at a time through
     * {@link DinoNeedsUtils#feed} (as {@code Eat} does with food on the ground), until the animal no longer wants food
     * (above 90 % hunger, lead 2026-10-27; {@link DinoNeedsUtils#FULL_ABOVE}) or the slot is empty. The diet is keyed by
     * item description id; the old check looked items up by stack, which never matched, so a feeder visit fed nothing
     * (the same on 1.20.1).
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
            while (!this.feeder.getItem(slot).isEmpty() && DinoNeedsUtils.isHungry(dinosaur)) {
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
