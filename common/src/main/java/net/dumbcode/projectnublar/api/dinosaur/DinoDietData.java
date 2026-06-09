package net.dumbcode.projectnublar.api.dinosaur;

import java.util.Map;

public record DinoDietData(
        String dietType,
        Map<String, Double> foodMap
) {

}
