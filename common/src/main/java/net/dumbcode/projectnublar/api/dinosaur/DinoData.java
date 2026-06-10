package net.dumbcode.projectnublar.api.dinosaur;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dumbcode.projectnublar.api.gene.GeneData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.api.util.NublarMath;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The full genetic make-up of a (future) dinosaur: base species, per-entity DNA percentages,
 * explicitly-set gene values, layer colors, and incubation progress.
 *
 * <p>Serialized into the {@code DinoData} compound on item stacks and entities; every tag-name
 * string below is a frozen save contract.
 */
public class DinoData {
    private static final String DINO_DATA_TAG = "DinoData";
    private static final String BASE_PERCENTAGE_KEY = "basePercentage";
    private static final String ENTITY_PERCENTAGES_KEY = "entityPercentages";
    private static final String ENTITY_ENTRY_PREFIX = "entity_";
    private static final String TYPE_KEY = "type";
    private static final String VARIANT_KEY = "variant";
    private static final String PERCENTAGE_KEY = "percentage";
    private static final String GENES_KEY = "genes";
    private static final String BASE_DINO_KEY = "baseDino";
    private static final String INCUBATION_PROGRESS_KEY = "incubationProgress";
    private static final String INCUBATION_TIME_LEFT_KEY = "incubationTimeLeft";

    /** Each gene contribution is weighted by the entity's DNA percentage times this factor. */
    private static final int GENE_INHERITANCE_MULTIPLIER = 2;
    private static final int LAYER_COUNT = 8;
    private static final int DEFAULT_LAYER_COLOR = 0xFFFFFF;

    /** Sentinel meaning "not incubating"/"unknown" for the incubation fields below. */
    private static final int NOT_SET = -1;

    private double basePercentage;
    private double incubationProgress = NOT_SET;
    private int incubationTimeLeft = NOT_SET;
    private EntityType<?> baseDino = null;
    private Map<EntityInfo, Double> entityPercentages = new HashMap<>();
    private Map<Genes.Gene, Double> advancedGenes = new HashMap<>();
    private Map<Genes.Gene, Double> finalGenes = new HashMap<>();
    private List<Integer> layerColors =
            Arrays.asList(Collections.nCopies(LAYER_COUNT, DEFAULT_LAYER_COLOR).toArray(new Integer[0]));

    public DinoData() {
    }

    public Integer getLayerColor(int layer) {
        return layerColors.get(layer);
    }

    public List<Integer> getLayerColors() {
        return layerColors;
    }

    public static DinoData fromStack(ItemStack stack) {
        return fromNBT(stack.getTag().getCompound(DINO_DATA_TAG));
    }

    public Map<EntityInfo, Double> getEntityPercentages() {
        return entityPercentages;
    }

    public void addEntity(EntityInfo type, double percentage) {
        entityPercentages.put(type, percentage);
    }

    // TODO(BUG): the map is keyed by EntityInfo, so remove(EntityType) never matches and this
    // method is always a no-op.
    public void removeEntity(EntityType<?> type) {
        entityPercentages.remove(type);
    }

    public void setBasePercentage(double basePercentage) {
        this.basePercentage = basePercentage;
    }

    public void setGeneValue(Genes.Gene gene, double value) {
        advancedGenes.put(gene, value);
    }

    public void addGeneValue(Genes.Gene gene, double value) {
        if (!advancedGenes.containsKey(gene)) {
            advancedGenes.put(gene, value);
        } else {
            advancedGenes.put(gene, advancedGenes.get(gene) + value);
        }
    }

    public double getBasePercentage() {
        return basePercentage;
    }

    public EntityType<?> getBaseDino() {
        return baseDino;
    }

    public double getEntityPercentage(EntityInfo type) {
        return entityPercentages.getOrDefault(type, 0D);
    }

    /** Appends incubation status and final gene tooltips to the item tooltip. */
    public void createToolTip(List<Component> components) {
        if (incubationProgress != NOT_SET) {
            int incubationPercentage = (int) NublarMath.round(incubationProgress * 100, 0);
            components.add(Component.literal("Incubation Progress: " + incubationPercentage + "%"));
        }
        if (incubationTimeLeft != NOT_SET) {
            components.add(Component.literal(StringUtil.formatTickDuration(incubationTimeLeft)));
        }
        if (finalGenes.isEmpty()) {
            finalizeGenes();
        }
        finalGenes.forEach((gene, value) -> components.add(gene.getTooltip(value)));
    }

    public void finalizeGenes() {
        finalGenes.clear();
        for (Genes.Gene gene : GeneInit.getList()) {
            double value = getFinalGeneValue(gene);
            if (value != 0) {
                finalGenes.put(gene, value);
            }
        }
    }

    public void setBaseDino(EntityType<?> baseDino) {
        this.baseDino = baseDino;
    }

    /** Explicit gene value if set, otherwise the DNA-weighted sum over the source entities. */
    public double getGeneValue(Genes.Gene gene) {
        if (advancedGenes.containsKey(gene)) {
            return advancedGenes.get(gene);
        }
        return computeInheritedGeneValue(gene);
    }

    /**
     * Like {@link #getGeneValue}, but reads through the {@code finalGenes} cache. Note the
     * computed (non-advanced) value is intentionally not cached here, matching the original code.
     */
    public double getFinalGeneValue(Genes.Gene gene) {
        if (finalGenes.containsKey(gene)) {
            return finalGenes.get(gene);
        }
        if (advancedGenes.containsKey(gene)) {
            finalGenes.put(gene, advancedGenes.get(gene));
            return finalGenes.get(gene);
        }
        return computeInheritedGeneValue(gene);
    }

    /** Sums each source entity's gene contribution weighted by its DNA percentage doubled. */
    private double computeInheritedGeneValue(Genes.Gene gene) {
        double value = 0;
        for (Map.Entry<EntityInfo, Double> entry : entityPercentages.entrySet()) {
            if (GeneData.getData(entry.getKey().type) != null) {
                if (GeneData.getData(entry.getKey().type).genes().containsKey(gene)) {
                    value += GeneData.getData(entry.getKey().type).genes().get(gene)
                            * (entry.getValue() * GENE_INHERITANCE_MULTIPLIER);
                }
            }
        }
        return value;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(BASE_PERCENTAGE_KEY, basePercentage);
        tag.put(ENTITY_PERCENTAGES_KEY, saveEntityPercentages());
        tag.put(GENES_KEY, saveAdvancedGenes());
        // TODO(BUG): throws a NullPointerException when baseDino is null (getKey(null) NPEs);
        // fromNBT tolerates a missing key but toNBT cannot produce one.
        tag.putString(BASE_DINO_KEY, BuiltInRegistries.ENTITY_TYPE.getKey(baseDino).toString());
        tag.putDouble(INCUBATION_PROGRESS_KEY, incubationProgress);
        tag.putInt(INCUBATION_TIME_LEFT_KEY, incubationTimeLeft);
        return tag;
    }

    public static DinoData fromNBT(CompoundTag tag) {
        double basePercentage = tag.getDouble(BASE_PERCENTAGE_KEY);
        Map<EntityInfo, Double> entityPercentages =
                readEntityPercentages(tag.getCompound(ENTITY_PERCENTAGES_KEY));
        DinoData data = new DinoData();
        data.advancedGenes.putAll(readAdvancedGenes(tag.getCompound(GENES_KEY)));
        data.basePercentage = basePercentage;
        data.entityPercentages = entityPercentages;
        if (tag.contains(BASE_DINO_KEY)) {
            data.baseDino = EntityType.byString(tag.getString(BASE_DINO_KEY)).get();
        }
        if (tag.contains(INCUBATION_PROGRESS_KEY)) {
            data.incubationProgress = tag.getDouble(INCUBATION_PROGRESS_KEY);
        }
        if (tag.contains(INCUBATION_TIME_LEFT_KEY)) {
            data.incubationTimeLeft = tag.getInt(INCUBATION_TIME_LEFT_KEY);
        }
        return data;
    }

    public DinoData copy() {
        DinoData data = new DinoData();
        data.advancedGenes = new HashMap<>(advancedGenes);
        data.entityPercentages = new HashMap<>(entityPercentages);
        data.basePercentage = basePercentage;
        return data;
    }

    public String getNameSpace() {
        if (baseDino == null) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.getKey(baseDino).getNamespace();
    }

    public String getPath() {
        if (baseDino == null) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.getKey(baseDino).getPath();
    }

    public MutableComponent getFormattedType() {
        return MutableComponent.create(baseDino.getDescription().getContents());
    }

    public void setIncubationProgress(double progress) {
        incubationProgress = progress;
    }

    public void toStack(ItemStack stack) {
        stack.getOrCreateTag().put(DINO_DATA_TAG, toNBT());
    }

    public double getIncubationProgress() {
        return incubationProgress;
    }

    public void setIncubationTimeLeft(int ticks) {
        incubationTimeLeft = ticks;
    }

    private CompoundTag saveEntityPercentages() {
        CompoundTag entityTag = new CompoundTag();
        int i = 0;
        for (Map.Entry<EntityInfo, Double> entry : entityPercentages.entrySet()) {
            CompoundTag entityInfo = new CompoundTag();
            entityInfo.putString(
                    TYPE_KEY, BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey().type).toString());
            if (entry.getKey().variant != null) {
                entityInfo.putString(VARIANT_KEY, entry.getKey().variant);
            }
            entityInfo.putDouble(PERCENTAGE_KEY, entry.getValue());
            entityTag.put(ENTITY_ENTRY_PREFIX + i, entityInfo);
            i++;
        }
        return entityTag;
    }

    private CompoundTag saveAdvancedGenes() {
        CompoundTag geneTag = new CompoundTag();
        for (Map.Entry<Genes.Gene, Double> entry : advancedGenes.entrySet()) {
            geneTag.putDouble(entry.getKey().name(), entry.getValue());
        }
        return geneTag;
    }

    private static Map<EntityInfo, Double> readEntityPercentages(CompoundTag entityTag) {
        Map<EntityInfo, Double> entityPercentages = new HashMap<>();
        for (String key : entityTag.getAllKeys()) {
            CompoundTag entityInfo = entityTag.getCompound(key);
            EntityType<?> type = EntityType.byString(entityInfo.getString(TYPE_KEY)).get();
            String variant =
                    entityInfo.contains(VARIANT_KEY) ? entityInfo.getString(VARIANT_KEY) : null;
            double percentage = entityInfo.getDouble(PERCENTAGE_KEY);
            entityPercentages.put(new EntityInfo(type, variant), percentage);
        }
        return entityPercentages;
    }

    private static Map<Genes.Gene, Double> readAdvancedGenes(CompoundTag geneTag) {
        Map<Genes.Gene, Double> advancedGenes = new HashMap<>();
        for (String key : geneTag.getAllKeys()) {
            advancedGenes.put(Genes.byName(key), geneTag.getDouble(key));
        }
        return advancedGenes;
    }

    public record EntityInfo(EntityType<?> type, @Nullable String variant) {
        public static Codec<EntityInfo> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(EntityInfo::type),
                        Codec.STRING.optionalFieldOf("variant", null).forGetter(EntityInfo::variant)
                ).apply(instance, EntityInfo::new)
        );

    }
}
