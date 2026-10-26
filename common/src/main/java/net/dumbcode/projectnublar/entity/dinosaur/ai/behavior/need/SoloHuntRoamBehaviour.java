package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.need;

import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class SoloHuntRoamBehaviour <E extends PathfinderMob> extends ExtendedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).noMemory(MemoryModuleType.WALK_TARGET)
            .hasMemory(MemoryModuleTypeInit.HUNTING.get());

    protected BiFunction<E, Vec3, Float> speedModifier = (entity, targetPos) -> 1f;
    protected Predicate<E> avoidWaterPredicate = entity -> true;
    protected SquareRadius radius = new SquareRadius(10, 7);
    protected BiPredicate<E, Vec3> positionPredicate = (entity, pos) -> true;

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }


    public SoloHuntRoamBehaviour<E> setRadius(double radius) {
        return setRadius(radius, radius);
    }


    public SoloHuntRoamBehaviour<E> setRadius(double xz, double y) {
        this.radius = new SquareRadius(xz, y);

        return this;
    }


    public SoloHuntRoamBehaviour<E> speedModifier(float modifier) {
        return speedModifier((entity, targetPos) -> modifier);
    }


    public SoloHuntRoamBehaviour<E> speedModifier(BiFunction<E, Vec3, Float> function) {
        this.speedModifier = function;

        return this;
    }


    public SoloHuntRoamBehaviour<E> walkTargetPredicate(BiPredicate<E, Vec3> predicate) {
        this.positionPredicate = predicate;

        return this;
    }


    public SoloHuntRoamBehaviour<E> dontAvoidWater() {
        return avoidWaterWhen(entity -> false);
    }


    public SoloHuntRoamBehaviour<E> avoidWaterWhen(Predicate<E> predicate) {
        this.avoidWaterPredicate = predicate;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return !BrainUtil.hasMemory(entity, MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void start(E entity) {
        Vec3 targetPos = getTargetPos(entity);

        if (!this.positionPredicate.test(entity, targetPos))
            targetPos = null;

        if (targetPos == null) {
            BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
        }
        else {
            BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, this.speedModifier.apply(entity, targetPos), 0));
        }
    }

    @Nullable
    protected Vec3 getTargetPos(E entity) {
        if (this.avoidWaterPredicate.test(entity)) {
            return LandRandomPos.getPos(entity, (int)this.radius.xzRadius(), (int)this.radius.yRadius());
        }
        else {
            return DefaultRandomPos.getPos(entity, (int)this.radius.xzRadius(), (int)this.radius.yRadius());
        }
    }
}
