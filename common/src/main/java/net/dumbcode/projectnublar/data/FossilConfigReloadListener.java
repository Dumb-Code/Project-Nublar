package net.dumbcode.projectnublar.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.AmberFossils;
import net.dumbcode.projectnublar.api.fossil.FossilPeriod;
import net.dumbcode.projectnublar.api.fossil.FossilQuality;
import net.dumbcode.projectnublar.api.fossil.FossilSets;
import net.dumbcode.projectnublar.api.fossil.Fossils;
import net.dumbcode.projectnublar.api.fossil.TraceFossils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Loads fossil generation config from the {@code config/fossils} datapack folder (periods,
 * qualities, sets, and per-species fossils).
 *
 * <p>Note: like all of this mod's reload listeners, this is registered <b>only on Forge</b>
 * ({@code CommonForgeEvents}); Fabric never receives this data.
 */
public class FossilConfigReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String LOCATION = "config/fossils";
    private static final String FOSSIL_PERIOD = "time_periods";
    private static final String FOSSIL_SET = "fossil_sets";
    private static final String FOSSIL_QUALITY = "qualities";
    private static final String FOSSILS = "fossils";

    private static final String CONFIG_ID_KEY = "config_id";
    private static final String PERIODS_KEY = "periods";
    private static final String FRAGMENTED_KEY = "fragmented";
    private static final String POOR_KEY = "poor";
    private static final String COMMON_KEY = "common";
    private static final String PRISTINE_KEY = "pristine";
    private static final String WEIGHT_KEY = "weight";
    private static final String DNA_YIELD_KEY = "dna_yield";
    private static final String BIPED_KEY = "biped";
    private static final String QUADRUPED_KEY = "quadruped";
    private static final String FERN_KEY = "fern";
    private static final String SPECIES_KEY = "species";
    private static final String PIECES_KEY = "pieces";
    private static final String SPECIAL_PIECES_KEY = "special_pieces";
    private static final String BIOMES_KEY = "biomes";

    private static final Type TIME_PERIODS_LIST_TYPE =
            new TypeToken<List<FossilPeriod.TimePeriods>>() {}.getType();
    private static final Type FOSSIL_PIECES_LIST_TYPE =
            new TypeToken<List<FossilSets.FossilPiece>>() {}.getType();
    private static final Type SPECIAL_FOSSIL_PIECES_LIST_TYPE =
            new TypeToken<List<Fossils.SpecialFossilPieces>>() {}.getType();
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();

    private static Map<String, FossilPeriod> periodMap = Collections.emptyMap();
    private static Map<String, FossilQuality> qualityMap = Collections.emptyMap();
    private static Map<EntityType<?>, Fossils> fossilsMap = Collections.emptyMap();
    private static Map<String, FossilSets> setsMap = Collections.emptyMap();
    // TODO(BUG): traceFossilsMap and amberFossilsMap are reassigned on reload but no config_id
    // branch in apply() ever populates them - trace/amber configs are silently ignored.
    private static Map<String, TraceFossils> traceFossilsMap = Collections.emptyMap();
    private static Map<String, AmberFossils> amberFossilsMap = Collections.emptyMap();

    public FossilConfigReloadListener() {
        super(GSON, LOCATION);
        Constants.LOG.info("Fossil Config initialized, scanning folder: data/'{}'", LOCATION);
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> elements,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {
        Map<String, FossilPeriod> newPeriodMap = new HashMap<>();
        Map<String, FossilQuality> newQualityMap = new HashMap<>();
        Map<EntityType<?>, Fossils> newFossilsMap = new HashMap<>();
        Map<String, FossilSets> newSetsMap = new HashMap<>();
        Map<String, TraceFossils> newTraceFossilsMap = new HashMap<>();
        Map<String, AmberFossils> newAmberFossilsMap = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            JsonElement element = entry.getValue();

            try {
                if (!element.isJsonObject()) {
                    Constants.LOG.error("Skipping Fossil config file, root element not json: '{}' ", fileId);
                    continue;
                }

                JsonObject jsonObject = element.getAsJsonObject();
                String configId = GsonHelper.getAsString(jsonObject, CONFIG_ID_KEY);

                if (configId.equals(FOSSIL_PERIOD)) {
                    FossilPeriod periodInfo = parseFossilPeriod(configId, jsonObject);

                    if (newPeriodMap.containsKey(configId)) {
                        Constants.LOG.warn("Duplicate datapack file definition");
                    }
                    newPeriodMap.put(configId, periodInfo);
                }
                if (configId.equals(FOSSIL_QUALITY)) {
                    FossilQuality qualityInfo = parseFossilQuality(configId, jsonObject);

                    if (newQualityMap.containsKey(configId)) {
                        Constants.LOG.warn("Duplicate datapack file definition");
                    }
                    newQualityMap.put(configId, qualityInfo);
                }
                if (configId.equals(FOSSIL_SET)) {
                    FossilSets fossilSets = parseFossilSets(configId, jsonObject);

                    if (newSetsMap.containsKey(configId)) {
                        Constants.LOG.warn("Duplicate datapack file definition");
                    }
                    newSetsMap.put(configId, fossilSets);
                }
                if (configId.equals(FOSSILS)) {
                    ParsedFossils parsedFossils = parseFossils(configId, jsonObject, fileId);

                    if (newFossilsMap.containsKey(parsedFossils.entityType())) {
                        Constants.LOG.info("Duplicate Datapack file definition, overwriting previous");
                    }
                    newFossilsMap.put(parsedFossils.entityType(), parsedFossils.fossils());
                }
            } catch (Exception e) {
                // TODO(BUG): the broad catch swallows any parse failure, logging only the
                // message; the file is skipped and the reload continues with partial data.
                Constants.LOG.error(
                        "Failed to parse Fossil Config file: {} - Error: {}",
                        fileId,
                        e.getMessage());
            }
        }

        periodMap = newPeriodMap;
        qualityMap = newQualityMap;
        setsMap = newSetsMap;
        fossilsMap = newFossilsMap;
        amberFossilsMap = newAmberFossilsMap;
        traceFossilsMap = newTraceFossilsMap;
        Constants.LOG.info(
                "Finished Applying Fossil Config data, loaded fossil sets for: {}dinosaurs",
                fossilsMap.size());
    }

    @Nullable
    public static FossilPeriod getFossilPeriods(String configId) {
        return periodMap.get(configId);
    }

    @Nullable
    public static FossilQuality getFossilQualities(String configId) {
        return qualityMap.get(configId);
    }

    @Nullable
    public static FossilSets getFossilSets(String configId) {
        return setsMap.get(configId);
    }

    @Nullable
    public static Fossils getFossils(EntityType<?> speciesId) {
        return fossilsMap.get(speciesId);
    }

    private static FossilPeriod parseFossilPeriod(String configId, JsonObject jsonObject) {
        List<FossilPeriod.TimePeriods> timePeriodsList =
                GSON.fromJson(jsonObject.get(PERIODS_KEY), TIME_PERIODS_LIST_TYPE);

        return new FossilPeriod(configId, timePeriodsList);
    }

    private static FossilQuality parseFossilQuality(String configId, JsonObject jsonObject) {
        FossilQualityEntry fragmented = parseFossilQualityEntry(jsonObject, FRAGMENTED_KEY);
        FossilQualityEntry poor = parseFossilQualityEntry(jsonObject, POOR_KEY);
        FossilQualityEntry common = parseFossilQualityEntry(jsonObject, COMMON_KEY);
        FossilQualityEntry pristine = parseFossilQualityEntry(jsonObject, PRISTINE_KEY);

        return new FossilQuality(
                configId,
                fragmented.weight(),
                fragmented.dnaYield(),
                poor.weight(),
                poor.dnaYield(),
                common.weight(),
                common.dnaYield(),
                pristine.weight(),
                pristine.dnaYield());
    }

    private static FossilQualityEntry parseFossilQualityEntry(JsonObject jsonObject, String key) {
        JsonObject qualityObject = GsonHelper.getAsJsonObject(jsonObject, key);
        int weight = GsonHelper.getAsInt(qualityObject, WEIGHT_KEY);
        double dnaYield = GsonHelper.getAsDouble(qualityObject, DNA_YIELD_KEY);

        return new FossilQualityEntry(weight, dnaYield);
    }

    private static FossilSets parseFossilSets(String configId, JsonObject jsonObject) {
        List<FossilSets.FossilPiece> bipedList =
                GSON.fromJson(jsonObject.get(BIPED_KEY), FOSSIL_PIECES_LIST_TYPE);
        List<FossilSets.FossilPiece> quadrupedList =
                GSON.fromJson(jsonObject.get(QUADRUPED_KEY), FOSSIL_PIECES_LIST_TYPE);
        List<FossilSets.FossilPiece> fernList =
                GSON.fromJson(jsonObject.get(FERN_KEY), FOSSIL_PIECES_LIST_TYPE);

        return new FossilSets(configId, bipedList, quadrupedList, fernList);
    }

    private static ParsedFossils parseFossils(
            String configId, JsonObject jsonObject, ResourceLocation fileId) {
        String resourceLocationString = GsonHelper.getAsString(jsonObject, SPECIES_KEY);
        ResourceLocation entityLocation = ResourceLocation.tryParse(resourceLocationString);

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityLocation)
                .orElseThrow(
                        () -> new JsonSyntaxException(
                                "Unknown entity_id"
                                        + entityLocation
                                        + "in Fossil config file: "
                                        + fileId));

        String pieces = GsonHelper.getAsString(jsonObject, PIECES_KEY);
        List<Fossils.SpecialFossilPieces> specialFossilPieces =
                GSON.fromJson(jsonObject.get(SPECIAL_PIECES_KEY), SPECIAL_FOSSIL_PIECES_LIST_TYPE);
        int weight = GsonHelper.getAsInt(jsonObject, WEIGHT_KEY);
        List<String> periods = GSON.fromJson(jsonObject.get(PERIODS_KEY), STRING_LIST_TYPE);
        List<String> biomes = GSON.fromJson(jsonObject.get(BIOMES_KEY), STRING_LIST_TYPE);

        return new ParsedFossils(
                entityType,
                new Fossils(
                        configId,
                        entityLocation,
                        pieces,
                        specialFossilPieces,
                        weight,
                        periods,
                        biomes));
    }

    private record FossilQualityEntry(int weight, double dnaYield) {}

    private record ParsedFossils(EntityType<?> entityType, Fossils fossils) {}
}
