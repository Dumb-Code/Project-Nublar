package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.ai.sensors.NearbyDinosaurSensor;
import net.dumbcode.projectnublar.entity.ai.sensors.NearestWaterSourceSensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;

import java.util.function.Supplier;

public class SensorTypesInit {
    public static void init(){}

    public static final Supplier<SensorType<NearestWaterSourceSensor<?>>> NEAREST_WATER_SOURCE = register("nearest_drinkable_source_block", NearestWaterSourceSensor::new);
    public static final Supplier<SensorType<NearbyDinosaurSensor<?>>> NEARBY_DINOSAURS_SENSOR = register("nearest_dinosaurs", NearbyDinosaurSensor::new);

    private static <T extends ExtendedSensor<?>> Supplier<SensorType<T>> register(String id, Supplier<T> sensor) {
        return Constants.PN_SBL_LOADER.registerSensorType(id, sensor);
    }
}
