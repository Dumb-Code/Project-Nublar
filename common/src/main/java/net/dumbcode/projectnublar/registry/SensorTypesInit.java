package net.dumbcode.projectnublar.registry;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.dinosaur.ai.sensor.NearbyDinosaurSensor;
import net.dumbcode.projectnublar.entity.dinosaur.ai.sensor.NearestWaterSourceSensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;

import java.util.function.Supplier;

/** Registers the SmartBrainLib sensor types. The id strings are frozen contracts. */
public class SensorTypesInit {
    /** No-op; forces class loading so the static registrations above run. */
    public static void init() {}

    public static final Supplier<SensorType<NearestWaterSourceSensor<?>>> NEAREST_WATER_SOURCE = register("nearest_drinkable_source_block", NearestWaterSourceSensor::new);
    public static final Supplier<SensorType<NearbyDinosaurSensor<?>>> NEARBY_DINOSAURS_SENSOR = register("nearest_dinosaurs", NearbyDinosaurSensor::new);

    private static <T extends ExtendedSensor<?>> Supplier<SensorType<T>> register(String id, Supplier<T> sensor) {
        return Constants.PN_SBL_LOADER.registerSensorType(id, sensor);
    }
}
