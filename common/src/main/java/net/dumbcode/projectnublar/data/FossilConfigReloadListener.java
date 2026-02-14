package net.dumbcode.projectnublar.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.*;
import net.dumbcode.projectnublar.api.fossil.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FossilConfigReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String LOCATION = "config/fossils";
    private static final String FOSSIL_PERIOD = "time_periods";
    private static final String FOSSIL_SET = "fossil_sets";
    private static final String FOSSIL_QUALITY = "qualities";
    private static final String FOSSILS = "fossils";

    private static Map<String, FossilPeriod> periodMap = Collections.emptyMap();
    private static Map<String, FossilQuality> qualityMap = Collections.emptyMap();
    private static Map<EntityType<?>, Fossils> fossilsMap = Collections.emptyMap();
    private static Map<String, FossilSets> setsMap = Collections.emptyMap();
    private static Map<String, TraceFossils> traceFossilsMap = Collections.emptyMap();
    private static Map<String, AmberFossils> amberFossilsMap = Collections.emptyMap();


    public FossilConfigReloadListener() {
        super(GSON, LOCATION);

        Constants.LOG.info("Fossil Config initialized, scanning folder: data/'{}'", LOCATION);

    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, FossilPeriod> newPeriodMap = new HashMap<>();
        Map<String, FossilQuality> newQualityMap = new HashMap<>();
        Map<EntityType<?>, Fossils> newFossilsMap = new HashMap<>();
        Map<String, FossilSets> newSetsMap = new HashMap<>();
        Map<String, TraceFossils> newTraceFossilsMap = new HashMap<>();
        Map<String, AmberFossils> newAmberFossilsMap = new HashMap<>();

        for(Map.Entry<ResourceLocation,JsonElement> entry: elements.entrySet()) {
            ResourceLocation fileID = entry.getKey();
            JsonElement element = entry.getValue();

            try {
                if (!element.isJsonObject()) {
                    Constants.LOG.error("Skipping Fossil config file, root element not json: '{}' ", fileID);
                    continue;
                }

                JsonObject jsonObject = element.getAsJsonObject();
                String configId = GsonHelper.getAsString(jsonObject, "config_id");

                if (configId.equals(FOSSIL_PERIOD)) {

                    Type listType = new TypeToken<List<FossilPeriod.TimePeriods>>() {
                    }.getType();
                    List<FossilPeriod.TimePeriods> timePeriodsList = GSON.fromJson(jsonObject.get("periods"), listType);

                    FossilPeriod periodInfo = new FossilPeriod(
                            configId, timePeriodsList
                    );

                    if (newPeriodMap.containsKey(configId)) {
                        Constants.LOG.warn("Duplicate datapack file definition");
                    }
                    newPeriodMap.put(configId, periodInfo);

                }
                if (configId.equals(FOSSIL_QUALITY)) {
                    JsonObject fragmented = GsonHelper.getAsJsonObject(jsonObject, "fragmented");
                    int fragmentedWeight = GsonHelper.getAsInt(fragmented, "weight");
                    double fragmentedYield = GsonHelper.getAsDouble(fragmented, "dna_yield");

                    JsonObject poor = GsonHelper.getAsJsonObject(jsonObject, "poor");
                    int poorWeight = GsonHelper.getAsInt(poor, "weight");
                    double poorYield = GsonHelper.getAsDouble(poor, "dna_yield");

                    JsonObject common = GsonHelper.getAsJsonObject(jsonObject, "common");
                    int commonWeight = GsonHelper.getAsInt(common, "weight");
                    double commonYield = GsonHelper.getAsDouble(common, "dna_yield");

                    JsonObject pristine = GsonHelper.getAsJsonObject(jsonObject, "pristine");
                    int pristineWeight = GsonHelper.getAsInt(pristine, "weight");
                    double pristineYield = GsonHelper.getAsDouble(pristine, "dna_yield");

                    FossilQuality qualityInfo = new FossilQuality(
                            configId,
                            fragmentedWeight, fragmentedYield,
                            poorWeight, poorYield,
                            commonWeight, commonYield,
                            pristineWeight, pristineYield
                    );

                    if (newQualityMap.containsKey(configId)) {
                        Constants.LOG.warn("Duplicate datapack file definition");
                    }
                    newQualityMap.put(configId, qualityInfo);

                }
                if (configId.equals(FOSSIL_SET)) {


                    Type listType = new TypeToken<List<FossilSets.FossilPiece>>() {
                    }.getType();

                    List<FossilSets.FossilPiece> bipedList = GSON.fromJson(jsonObject.get("biped"), listType);
                    List<FossilSets.FossilPiece> quadrupedList = GSON.fromJson(jsonObject.get("quadruped"), listType);
                    List<FossilSets.FossilPiece> fernList = GSON.fromJson(jsonObject.get("fern"), listType);

                    FossilSets fossilSets = new FossilSets(
                            configId,
                            bipedList,
                            quadrupedList,
                            fernList
                    );
                    if (newSetsMap.containsKey(configId)) {
                        Constants.LOG.warn("Duplicate datapack file definition");
                    }
                    newSetsMap.put(configId, fossilSets);
                }
                if (configId.equals(FOSSILS)) {
                    Type listSpecialPieceType = new TypeToken<List<Fossils.SpecialFossilPieces>>() {
                    }.getType();
                    Type listStringType = new TypeToken<List<String>>() {
                    }.getType();

                    String rlString = GsonHelper.getAsString(jsonObject, "species");
                    ResourceLocation entityRl = ResourceLocation.tryParse(rlString);

                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityRl).orElseThrow(() -> new JsonSyntaxException("Unknown entity_id" + entityRl + "in Fossil config file: " + fileID));

                    String pieces = GsonHelper.getAsString(jsonObject, "pieces");

                    List<Fossils.SpecialFossilPieces> specialFossilPieces = GSON.fromJson(jsonObject.get("special_pieces"), listSpecialPieceType);

                    int weight = GsonHelper.getAsInt(jsonObject, "weight");

                    List<String> periods = GSON.fromJson(jsonObject.get("periods"), listStringType);
                    List<String> biomes = GSON.fromJson(jsonObject.get("biomes"), listStringType);

                    Fossils fossils = new Fossils(
                            configId, entityRl, pieces, specialFossilPieces, weight, periods, biomes
                    );

                    if (newFossilsMap.containsKey(entityType)) {
                        Constants.LOG.info("Duplicate Datapack file definition, overwriting previous");
                    }
                    newFossilsMap.put(entityType, fossils);
                }


            } catch (Exception e) {
                Constants.LOG.error("Failed to parse Fossil Config file: {} - Error: {}" + fileID + e.getMessage());
            }
        }

        periodMap = newPeriodMap;
        qualityMap = newQualityMap;
        setsMap = newSetsMap;
        fossilsMap = newFossilsMap;
        amberFossilsMap = newAmberFossilsMap;
        traceFossilsMap = newTraceFossilsMap;
        Constants.LOG.info("Finished Applying Fossil Config data, loaded fossil sets for: " + fossilsMap.size() + "dinosaurs");
    }
    @Nullable
    public static FossilPeriod getFossilPeriods(String configId){
        return periodMap.get(configId);
    }
    @Nullable
    public static FossilQuality getFossilQualities(String configId){
        return qualityMap.get(configId);
    }
    @Nullable
    public static FossilSets getFossilSets(String configId){
        return setsMap.get(configId);
    }
    @Nullable
    public static Fossils getFossils(EntityType<?> speciesId){
        return fossilsMap.get(speciesId);
    }


}

