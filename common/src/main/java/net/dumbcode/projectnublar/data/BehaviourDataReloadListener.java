package net.dumbcode.projectnublar.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class BehaviourDataReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String LOCATION = "config/dinosaurs";
    private static final String SPECIES_ID_KEY = "species_id";
    private static final String DIET_ID_KEY = "diet_id";
    private static final String MAX_HEALTH_KEY = "max_health";
    private static final String MAX_STAMINA_KEY = "max_stamina";
    private static final String ATTACK_DAMAGE_KEY = "attack_damage";
    private static final String SPEED_MULTIPLIER_KEY = "speed_multiplier";
    private static final String SIZE_MULTIPLIER_KEY = "size_multiplier";
    private static final String INTELLIGENCE_KEY = "intelligence";
    private static final String IMMUNITY_KEY = "immunity";
    private static final String RESISTANCE_KEY = "resistance";
    private static final String HEALTH_REGEN_KEY = "health_regen";
    private static final String GROWTH_RATE_KEY = "growth_rate";
    private static final String FERTILITY_CHANCE_KEY = "fertility_chance";
    private static final String GESTATION_TIME_KEY = "gestation_time";
    private static final String EGG_CLUTCH_SIZE_KEY = "egg_clutch_size";
    private static final String VISION_QUALITY_KEY = "vision_quality";
    private static final String DOMESTICITY_KEY = "domesticity";
    private static final String AGGRESSION_LEVEL_KEY = "aggression_level";
    private static final String TAMING_TRUST_THRESHOLD_KEY = "taming_trust_threshold";
    private static final String TRUST_INCREASE_KEY = "trust_increase";
    private static final String MAX_SOCIAL_NEED_KEY = "max_social_need";
    private static final String LONELINESS_DRAIN_KEY = "loneliness_drain";
    private static final String GROUP_SIZE_KEY = "group_size";
    private static final String EAT_RATE_PER_DAY_KEY = "eat_rate_per_day";
    private static final String MAX_DAYS_WITHOUT_FOOD_KEY = "max_days_without_food";
    private static final String MAX_DAYS_WITHOUT_WATER_KEY = "max_days_without_water";
    private static final String DRINK_RATE_PER_DAY_KEY = "drink_rate_per_day";
    private static final String STAMINA_DRAIN_PER_SECOND_KEY = "stamina_drain_per_second";
    private static final String HAPPY_THRESHOLD_KEY = "happy_threshold";
    private static final String UNCOMFORTABLE_THRESHOLD_KEY = "uncomfortable_threshold";
    private static final String RAGE_THRESHOLD_KEY = "rage_threshold";
    private static final String CAN_FORM_GROUP_KEY = "can_form_group";
    private static final String NOCTURNAL_KEY = "nocturnal";

    private static Map<EntityType<?>, DinoBehaviourData> behaviourDataMap = Collections.emptyMap();

    public BehaviourDataReloadListener() {
        super(GSON, LOCATION);
        Constants.LOG.info("Dino behaviour manager initialized, scanning folder: data/'{}'", LOCATION);
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> elements,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller) {
        Map<EntityType<?>, DinoBehaviourData> newMap = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            JsonElement element = entry.getValue();

            try {
                if (!element.isJsonObject()) {
                    Constants.LOG.error("Skipping Species Behaviour File, root element not json: '{}' ", fileId);
                    continue;
                }

                JsonObject jsonObject = element.getAsJsonObject();
                EntityType<?> entityType = parseEntityType(jsonObject, fileId);
                DinoBehaviourData data = parseBehaviourData(jsonObject);

                if (newMap.containsKey(entityType)) {
                    Constants.LOG.info("Duplicate behaviour assigned, over-writing previous");
                }
                newMap.put(entityType, data);
                Constants.LOG.info("Loaded Behaviour data for: {}", entityType);

            } catch (Exception e) {
                Constants.LOG.error(
                        "Failed to parse Behaviour Assignment file: {} - Error: {}",
                        fileId,
                        e.getMessage());
            }
        }
        behaviourDataMap = newMap;
        Constants.LOG.info(
                "Finished Applying behaviour assignment data. Loaded {} valid entries",
                behaviourDataMap.size());
    }

    @Nullable
    public static DinoBehaviourData getBehaviourInfoForDino(EntityType<?> speciesId) {
        return behaviourDataMap.get(speciesId);
    }

    private static EntityType<?> parseEntityType(JsonObject jsonObject, ResourceLocation fileId) {
        String entityIdString = GsonHelper.getAsString(jsonObject, SPECIES_ID_KEY);
        ResourceLocation entityLocation = ResourceLocation.tryParse(entityIdString);

        return BuiltInRegistries.ENTITY_TYPE.getOptional(entityLocation)
                .orElseThrow(
                        () -> new JsonSyntaxException(
                                "Unknown entity_id"
                                        + entityLocation
                                        + "in DNA extraction file: "
                                        + fileId));
    }

    private static DinoBehaviourData parseBehaviourData(JsonObject jsonObject) {
        String entityIdString = GsonHelper.getAsString(jsonObject, SPECIES_ID_KEY);
        String dietId = GsonHelper.getAsString(jsonObject, DIET_ID_KEY);

        double maxHealth = GsonHelper.getAsDouble(jsonObject, MAX_HEALTH_KEY);
        double energyCapacity = GsonHelper.getAsDouble(jsonObject, MAX_STAMINA_KEY);
        double attack = GsonHelper.getAsDouble(jsonObject, ATTACK_DAMAGE_KEY);
        double speed = GsonHelper.getAsDouble(jsonObject, SPEED_MULTIPLIER_KEY);
        double size = GsonHelper.getAsDouble(jsonObject, SIZE_MULTIPLIER_KEY);
        double intelligence = GsonHelper.getAsDouble(jsonObject, INTELLIGENCE_KEY);
        double immunity = GsonHelper.getAsDouble(jsonObject, IMMUNITY_KEY);
        double resistance = GsonHelper.getAsDouble(jsonObject, RESISTANCE_KEY);
        double healthRegen = GsonHelper.getAsDouble(jsonObject, HEALTH_REGEN_KEY);
        double growthRate = GsonHelper.getAsDouble(jsonObject, GROWTH_RATE_KEY);
        double fertility = GsonHelper.getAsDouble(jsonObject, FERTILITY_CHANCE_KEY);
        double gestationTime = GsonHelper.getAsDouble(jsonObject, GESTATION_TIME_KEY);
        double clutchSize = GsonHelper.getAsDouble(jsonObject, EGG_CLUTCH_SIZE_KEY);
        double visionQuality = GsonHelper.getAsDouble(jsonObject, VISION_QUALITY_KEY);

        double domesticity = GsonHelper.getAsDouble(jsonObject, DOMESTICITY_KEY);
        double aggressionScore = GsonHelper.getAsDouble(jsonObject, AGGRESSION_LEVEL_KEY);
        double tamingScore = GsonHelper.getAsDouble(jsonObject, TAMING_TRUST_THRESHOLD_KEY);
        double trustIncrease = GsonHelper.getAsDouble(jsonObject, TRUST_INCREASE_KEY);
        double social = GsonHelper.getAsDouble(jsonObject, MAX_SOCIAL_NEED_KEY);
        double socialDrain = GsonHelper.getAsDouble(jsonObject, LONELINESS_DRAIN_KEY);
        int groupSize = GsonHelper.getAsInt(jsonObject, GROUP_SIZE_KEY);

        double eatRate = GsonHelper.getAsDouble(jsonObject, EAT_RATE_PER_DAY_KEY);
        int starvationLimit = GsonHelper.getAsInt(jsonObject, MAX_DAYS_WITHOUT_FOOD_KEY);
        int dehydrationLimit = GsonHelper.getAsInt(jsonObject, MAX_DAYS_WITHOUT_WATER_KEY);
        double dehydrationRate = GsonHelper.getAsDouble(jsonObject, DRINK_RATE_PER_DAY_KEY);
        double exhaustionRate = GsonHelper.getAsDouble(jsonObject, STAMINA_DRAIN_PER_SECOND_KEY);

        double lowRisk = GsonHelper.getAsDouble(jsonObject, HAPPY_THRESHOLD_KEY);
        double mediumRisk = GsonHelper.getAsDouble(jsonObject, UNCOMFORTABLE_THRESHOLD_KEY);
        double highRisk = GsonHelper.getAsDouble(jsonObject, RAGE_THRESHOLD_KEY);

        boolean pack = GsonHelper.getAsBoolean(jsonObject, CAN_FORM_GROUP_KEY);
        boolean nocturnal = GsonHelper.getAsBoolean(jsonObject, NOCTURNAL_KEY);

        return new DinoBehaviourData(
                entityIdString,
                dietId,
                maxHealth,
                energyCapacity,
                attack,
                speed,
                size,
                intelligence,
                immunity,
                resistance,
                healthRegen,
                growthRate,
                fertility,
                gestationTime,
                clutchSize,
                visionQuality,
                domesticity,
                aggressionScore,
                tamingScore,
                trustIncrease,
                social,
                socialDrain,
                groupSize,
                eatRate,
                dehydrationRate,
                starvationLimit,
                dehydrationLimit,
                exhaustionRate,
                lowRisk,
                mediumRisk,
                highRisk,
                pack,
                nocturnal);
    }
}
