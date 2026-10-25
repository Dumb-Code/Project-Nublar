package net.dumbcode.projectnublar.entity.dinosaur.ai.sensor;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.registry.SensorTypesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.util.BrainUtils;

import javax.annotation.Nullable;
import java.util.List;

public class NearestWaterSourceSensor<E extends Dinosaur> extends PredicateSensor<BlockState, E> {

    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleTypeInit.HAS_FOUND_WATER.get());

protected SquareRadius radius = new SquareRadius(10, 3);
    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SensorTypesInit.NEAREST_WATER_SOURCE.get();
    }

    public NearestWaterSourceSensor<E> setRadius(double radius){return setRadius(radius, radius);}
    public NearestWaterSourceSensor<E> setRadius(double xz, double y){
        this.radius = new SquareRadius(xz, y);

        return this;
    }
    @Nullable BlockPos nearestWater;

    @Override
    protected void doTick(ServerLevel level, E entity) {

            List<Pair<BlockPos, BlockState>> blocks = new ObjectArrayList<>();

            for (BlockPos pos : BlockPos.betweenClosed(entity.blockPosition().subtract(this.radius.toVec3i()), entity.blockPosition().offset(this.radius.toVec3i()))) {
                BlockState state = level.getBlockState(pos);

                if (this.predicate().test(state, entity))
                    blocks.add(Pair.of(pos.immutable(), state));
            }


            for (int i = 0; i < blocks.size(); i++) {
                BlockPos currentTarget = blocks.get(i).getFirst();
                if (nearestWater == null) {
                    nearestWater = currentTarget;
                } else if (entity.distanceToSqr(nearestWater.getCenter()) > entity.distanceToSqr(currentTarget.getCenter())) {
                    nearestWater = currentTarget;
                }
            }

            if (nearestWater != null) {
                BrainUtils.setMemory(entity, MemoryModuleTypeInit.HAS_FOUND_WATER.get(), nearestWater);
            }
        }

}
