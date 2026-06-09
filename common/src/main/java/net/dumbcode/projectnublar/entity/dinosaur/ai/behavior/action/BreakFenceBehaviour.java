package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.action;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BreakFenceBehaviour<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleTypeInit.WANTS_TO_BREAK_FENCE.get(), MemoryStatus.VALUE_PRESENT));

    @Nullable BlockEntityElectricFence beElectricFence;
    @Nullable BlockEntity beToTest;

    public BreakFenceBehaviour(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if(BrainUtils.hasMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get())){
            List<Pair<BlockPos, BlockState>> nearby_blocks = BrainUtils.getMemory(entity, SBLMemoryTypes.NEARBY_BLOCKS.get());

            if(nearby_blocks == null) {return false;}

            for(Pair<BlockPos,BlockState> blockToTest: nearby_blocks){
                if (blockToTest.getSecond().is(BlockInit.ELECTRIC_FENCE.get())){
                  if(level.getBlockEntity(blockToTest.getFirst()) != null) {
                      BlockPos testPos = blockToTest.getFirst();
                      beToTest = level.getBlockEntity(blockToTest.getFirst());
                      if(beToTest != null) {
                          if(beToTest instanceof BlockEntityElectricFence entityElectricFence && entity.distanceToSqr(testPos.getCenter()) < 2) {
                              beElectricFence = entityElectricFence;
                          }
                      }
                  }
                }
            }

        }
        return beElectricFence != null;
    }

    @Override
    protected void start(E entity) {
        BrainUtils.clearMemory(entity, MemoryModuleTypeInit.WANTS_TO_BREAK_FENCE.get());
        BrainUtils.clearMemory(entity,SBLMemoryTypes.NEARBY_BLOCKS.get());
        DinoAnimationUtils.setAnimationState(entity,"attack", true);

    }

    @Override
    protected void doDelayedAction(E entity) {
        DinoAnimationUtils.setAnimationState(entity, "attack",false);
        this.beElectricFence.breakFence(10);
        entity.level().players().forEach(p ->
                p.sendSystemMessage(Component.literal("WARNING: Fence Destroyed at: " + entity.position() + ", by a: " + entity)));
    }

    @Override
    protected void stop(E entity) {
        this.beElectricFence = null;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }
}
