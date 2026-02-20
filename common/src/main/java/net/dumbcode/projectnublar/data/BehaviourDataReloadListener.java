package net.dumbcode.projectnublar.data;

import com.google.gson.*;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BehaviourDataReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String LOCATION = "config/dinosaurs";

    private static Map<EntityType<?>, DinoBehaviourData> behaviourDataMap = Collections.emptyMap();

    public BehaviourDataReloadListener(){
        super(GSON, LOCATION);
        Constants.LOG.info("Dino behaviour manager initialized, scanning folder: data/'{}'", LOCATION);

    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<EntityType<?>, DinoBehaviourData> newMap = new HashMap<>();

        for(Map.Entry<ResourceLocation, JsonElement> entry: resourceLocationJsonElementMap.entrySet()){
            ResourceLocation fileID = entry.getKey();
            JsonElement element = entry.getValue();

            try {
                if(!element.isJsonObject()){
                    Constants.LOG.error("Skipping Species Behaviour File, root element not json: '{}' ", fileID);
                    continue;
                }
                JsonObject jsonObject = element.getAsJsonObject();

                String entityIdstring = GsonHelper.getAsString(jsonObject, "species_id");
                ResourceLocation entityRl = ResourceLocation.tryParse(entityIdstring);

                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityRl)
                        .orElseThrow(() -> new JsonSyntaxException("Unknown entity_id" + entityRl + "in DNA extraction file: " + fileID));

                String dietID = GsonHelper.getAsString(jsonObject, "diet_id");

                double maxHealth = GsonHelper.getAsDouble(jsonObject,"max_health");
                double energyCapacity = GsonHelper.getAsDouble(jsonObject, "max_stamina");
                double attack = GsonHelper.getAsDouble(jsonObject, "attack_damage");
                double speed= GsonHelper.getAsDouble(jsonObject, "speed_multiplier");
                double size= GsonHelper.getAsDouble(jsonObject, "size_multiplier");
                double intelligence= GsonHelper.getAsDouble(jsonObject, "intelligence");
                double immunity = GsonHelper.getAsDouble(jsonObject, "immunity");
                double resistance= GsonHelper.getAsDouble(jsonObject, "resistance");
                double healthRegen = GsonHelper.getAsDouble(jsonObject, "health_regen");
                double growthRate = GsonHelper.getAsDouble(jsonObject, "growth_rate");
                double fertility = GsonHelper.getAsDouble(jsonObject, "fertility_chance");
                double gestationTime = GsonHelper.getAsDouble(jsonObject, "gestation_time");
                double clutchSize = GsonHelper.getAsDouble(jsonObject, "egg_clutch_size");
                double visionQuality = GsonHelper.getAsDouble(jsonObject, "vision_quality");


                double domesticity = GsonHelper.getAsDouble(jsonObject, "domesticity");
                double aggressionScore = GsonHelper.getAsDouble(jsonObject, "aggression_level");
                double tamingScore = GsonHelper.getAsDouble(jsonObject, "taming_trust_threshold");
                double trustIncrease = GsonHelper.getAsDouble(jsonObject, "trust_increase");
                double social = GsonHelper.getAsDouble(jsonObject, "max_social_need");
                double socialDrain = GsonHelper.getAsDouble(jsonObject, "loneliness_drain");
                int groupSize = GsonHelper.getAsInt(jsonObject, "group_size");


                double eatRate = GsonHelper.getAsDouble(jsonObject, "eat_rate_per_day");
                int starvationLimit = GsonHelper.getAsInt(jsonObject, "max_days_without_food");
                int dehydrationLimit = GsonHelper.getAsInt(jsonObject, "max_days_without_water");
                double dehydrationRate = GsonHelper.getAsDouble(jsonObject, "drink_rate_per_day");
                double exhaustionRate = GsonHelper.getAsDouble(jsonObject, "stamina_drain_per_second");

                double lowRisk = GsonHelper.getAsDouble(jsonObject, "happy_threshold");
                double mediumRisk = GsonHelper.getAsDouble(jsonObject, "uncomfortable_threshold");
                double highRisk = GsonHelper.getAsDouble(jsonObject, "rage_threshold");

                boolean pack = GsonHelper.getAsBoolean(jsonObject,"can_form_group");
                boolean nocturnal = GsonHelper.getAsBoolean(jsonObject,"nocturnal");

                DinoBehaviourData data = new DinoBehaviourData(
                        entityIdstring,dietID,maxHealth,energyCapacity,attack,speed,size,intelligence,immunity,resistance,healthRegen,growthRate,fertility,
                        gestationTime,clutchSize,visionQuality,domesticity,aggressionScore,tamingScore,trustIncrease,social,socialDrain,groupSize,eatRate,dehydrationRate,
                        starvationLimit,dehydrationLimit,exhaustionRate,lowRisk,
                        mediumRisk,highRisk,pack,nocturnal
                );


                if(newMap.containsKey(entityType)){
                    Constants.LOG.info("Duplicate behaviour assigned, over-writing previous");
                }
                newMap.put(entityType, data);
                Constants.LOG.info("Loaded Behaviour data for: " + entityType);

            } catch (Exception e){
                System.err.println("Failed to parse Behaviour Assignment file: {} - Error: {}" +fileID + e.getMessage());
            }

        }
        behaviourDataMap = newMap;
        System.out.println("Finished Applying behaviour assignment data. Loaded {} valid entries" + behaviourDataMap.size());
    }
    @Nullable
    public static DinoBehaviourData getBehaviourInfoForDino(EntityType<?> speciesID){
        return behaviourDataMap.get(speciesID);
    }
}
