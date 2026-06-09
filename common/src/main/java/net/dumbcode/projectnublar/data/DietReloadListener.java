package net.dumbcode.projectnublar.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DinoDietData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DietReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String FOLDER_PATH = "diet";

    public static  Map<String, DinoDietData> dietDataMap = Collections.emptyMap();

    public DietReloadListener() {
        super(GSON, FOLDER_PATH);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Constants.LOG.info("Apply Diet Assignment Data(processing {} file)...", pObject.size());
        Map<String, DinoDietData> newMap = new HashMap<>();
        for(Map.Entry<ResourceLocation, JsonElement> entry: pObject.entrySet()){
            ResourceLocation fileId = entry.getKey();
            JsonElement element = entry.getValue();
            try {
                if(!element.isJsonObject()){
                    Constants.LOG.error("Skipping Diet Assignment File {}: Root Element is not a JSON object", fileId);
                    continue;
                }
                JsonObject jsonObject = element.getAsJsonObject();
                String dietId = GsonHelper.getAsString(jsonObject, "diet_id");
                Type mapType = new TypeToken<Map<String, Double>>(){}.getType();
                Map<String, Double> foodMap = GSON.fromJson(jsonObject.get("valid_food_items"),mapType);

                DinoDietData info = new DinoDietData(
                   dietId, foodMap
                );

                if(newMap.containsKey(dietId)){
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
}
