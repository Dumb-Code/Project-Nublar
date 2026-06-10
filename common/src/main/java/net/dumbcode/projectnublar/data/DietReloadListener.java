package net.dumbcode.projectnublar.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DinoDietData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class DietReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String FOLDER_PATH = "diet";
    private static final String DIET_ID_KEY = "diet_id";
    private static final String VALID_FOOD_ITEMS_KEY = "valid_food_items";
    private static final Type FOOD_MAP_TYPE = new TypeToken<Map<String, Double>>() {}.getType();

    public static Map<String, DinoDietData> dietDataMap = Collections.emptyMap();

    public DietReloadListener() {
        super(GSON, FOLDER_PATH);
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> elements,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller) {
        Constants.LOG.info("Apply Diet Assignment Data(processing {} file)...", elements.size());
        Map<String, DinoDietData> newMap = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            JsonElement element = entry.getValue();
            try {
                if (!element.isJsonObject()) {
                    Constants.LOG.error("Skipping Diet Assignment File {}: Root Element is not a JSON object", fileId);
                    continue;
                }
                JsonObject jsonObject = element.getAsJsonObject();
                DinoDietData info = parseDietData(jsonObject);
                String dietId = info.dietType();

                if (newMap.containsKey(dietId)) {
                    Constants.LOG.warn("Duplicate Diet Assignment Definition(over-writing previous)");
                }
                newMap.put(dietId, info);
                Constants.LOG.debug("Loaded Diet type {} file {}", dietId, fileId);
            } catch (Exception e) {
                Constants.LOG.error("Failed to parse diet type file: {} - Error: {}", fileId, e.getMessage());
            }
        }
        dietDataMap = newMap;
        Constants.LOG.info("Finished applying Diet Assignment Data. Loaded {} valid entries", dietDataMap.size());
    }

    @Nullable
    public static DinoDietData getDietInfoForType(String dietType) {
        return dietDataMap.get(dietType);
    }

    private static DinoDietData parseDietData(JsonObject jsonObject) {
        String dietId = GsonHelper.getAsString(jsonObject, DIET_ID_KEY);
        Map<String, Double> foodMap = GSON.fromJson(jsonObject.get(VALID_FOOD_ITEMS_KEY), FOOD_MAP_TYPE);
        return new DinoDietData(dietId, foodMap);
    }
}
