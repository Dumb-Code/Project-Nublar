package net.dumbcode.projectnublar.entity.dinosaur.ai.sensor;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.registry.SensorTypesInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.PredicateSensor;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class NearbyDinosaurSensor <E extends Dinosaur> extends PredicateSensor<E, Dinosaur> {
    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleTypeInit.NEAREST_DINOSAURS.get());

    @Nullable
    protected SquareRadius radius = null;

    public NearbyDinosaurSensor() {
        super((owner, target) -> target != owner && target.isAlive());
    }

    /**
     * Set the radius for the sensor to scan.
     *
     * @param radius The coordinate radius, in blocks
     * @return this
     */
    public NearbyDinosaurSensor<E> setRadius(double radius) {
        return setRadius(radius, radius);
    }

    /**
     * Set the radius for the sensor to scan.
     *
     * @param xz The X/Z coordinate radius, in blocks
     * @param y  The Y coordinate radius, in blocks
     * @return this
     */
    public NearbyDinosaurSensor<E> setRadius(double xz, double y) {
        this.radius = new SquareRadius(xz, y);

        return this;
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SensorTypesInit.NEARBY_DINOSAURS_SENSOR.get();
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        SquareRadius radius = this.radius;

        if (radius == null) {
            double dist = entity.getAttributeValue(Attributes.FOLLOW_RANGE);

            radius = new SquareRadius(dist, dist);
        }

        List<Dinosaur> entities = EntityRetrievalUtil.getEntities(level, entity.getBoundingBox().inflate(radius.xzRadius(), radius.yRadius(), radius.xzRadius()), Dinosaur.class, dinosaur -> predicate().test(entity, dinosaur));

        entities.sort(Comparator.comparingDouble(entity::distanceToSqr));

        BrainUtil.setMemory(entity, MemoryModuleTypeInit.NEAREST_DINOSAURS.get(), entities);
    }
}
