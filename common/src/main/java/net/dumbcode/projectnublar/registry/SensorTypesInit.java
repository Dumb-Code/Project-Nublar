package net.dumbcode.projectnublar.registry;

import net.dumbcode.projectnublar.entity.dinosaur.ai.sensor.NearbyDinosaurSensor;
import net.dumbcode.projectnublar.entity.dinosaur.ai.sensor.NearestFeederSensor;
import net.dumbcode.projectnublar.entity.dinosaur.ai.sensor.NearestWaterSourceSensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;

import java.util.function.Supplier;

/**
 * Registers the SmartBrainLib sensor types. The id strings are frozen contracts.
 *
 * <p>SmartBrainLib registers them, through its platform service, so the ids stay in its
 * namespace ({@code smartbrainlib:nearest_dinosaurs} and so on), exactly as on 1.20.1. Our own
 * deferred register would move them to {@code projectnublar:}, and it would also need access to
 * the {@code SensorType} constructor, which is private on 26.2.
 */
public class SensorTypesInit {
    /** No-op; forces class loading so the static registrations above run. */
    public static void init() {}

    public static final Supplier<SensorType<NearestWaterSourceSensor<?>>> NEAREST_WATER_SOURCE = register("nearest_drinkable_source_block", NearestWaterSourceSensor::new);
    public static final Supplier<SensorType<NearestFeederSensor<?>>> NEAREST_FEEDER_SENSOR = register("nearest_feeder_block", NearestFeederSensor::new);
    public static final Supplier<SensorType<NearbyDinosaurSensor<?>>> NEARBY_DINOSAURS_SENSOR = register("nearest_dinosaurs", NearbyDinosaurSensor::new);

    private static <T extends ExtendedSensor<?>> Supplier<SensorType<T>> register(String id, Supplier<T> sensor) {
        return SBLConstants.PLATFORM.registerSensorType(id, sensor);
    }
}
