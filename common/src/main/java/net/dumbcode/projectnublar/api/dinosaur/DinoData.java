package net.dumbcode.projectnublar.api.dinosaur;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.dumbcode.projectnublar.api.gene.GeneData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.api.util.NublarMath;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The full genetic make-up of a (future) dinosaur: base species, per-entity DNA percentages,
 * explicitly-set gene values, layer colors, and incubation progress.
 *
 * <p>Items carry it as the {@link DataComponentInit#DINO_DATA} component, saved through
 * {@link #CODEC}; every key name below is a frozen save contract. The layer colours and the
 * final-gene cache are not saved (as in 1.20.1), so they take no part in {@link #equals}; the
 * {@code version} read from a save takes no part either, so a stack saved before a version change
 * still stacks with the same data saved now (ruling P122).
 */
public class DinoData {
    private static final String BASE_PERCENTAGE_KEY = "basePercentage";
    private static final String ENTITY_PERCENTAGES_KEY = "entityPercentages";
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

    /**
     * An entity type id as 1.20.1's toNBT wrote it and fromNBT read it: the write is {@code getKey},
     * so a null type writes the registry's default key (pig); an unknown id fails the read.
     */
    private static final Codec<EntityType<?>> ENTITY_TYPE_ID = Identifier.CODEC.comapFlatMap(
            id -> BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                    .<DataResult<EntityType<?>>>map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown entity type: " + id)),
            type -> BuiltInRegistries.ENTITY_TYPE.getKey(type));

    /** One {@code entityPercentages} entry: {@code type}, {@code variant} when not null, {@code percentage}. */
    private static final Codec<Map.Entry<EntityInfo, Double>> ENTITY_PERCENTAGE = RecordCodecBuilder.create(
            instance -> instance.group(
                    ENTITY_TYPE_ID.fieldOf(TYPE_KEY).forGetter(entry -> entry.getKey().type()),
                    Codec.STRING.optionalFieldOf(VARIANT_KEY).forGetter(entry -> Optional.ofNullable(entry.getKey().variant())),
                    Codec.DOUBLE.optionalFieldOf(PERCENTAGE_KEY, 0D).forGetter(Map.Entry::getValue)
            ).apply(instance, (type, variant, percentage) -> Map.entry(new EntityInfo(type, variant.orElse(null)), percentage)));

    /** {@code entityPercentages} as a list of entries (ruling P131); a missing key reads as no entity. */
    private static final MapCodec<Map<EntityInfo, Double>> ENTITY_PERCENTAGES_FIELD =
            ENTITY_PERCENTAGE.listOf().optionalFieldOf(ENTITY_PERCENTAGES_KEY, List.of()).xmap(
                    entries -> {
                        Map<EntityInfo, Double> percentages = new HashMap<>();
                        entries.forEach(entry -> percentages.put(entry.getKey(), entry.getValue()));
                        return percentages;
                    },
                    percentages -> List.copyOf(percentages.entrySet()));

    /**
     * {@code genes}: gene name to value. A name resolves through {@link Genes#byName} as in 1.20.1's fromNBT;
     * an unknown name is dropped (ruling P129).
     */
    private static final MapCodec<Map<Genes.Gene, Double>> GENES_FIELD =
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf(GENES_KEY, Map.of()).xmap(
                    names -> {
                        Map<Genes.Gene, Double> genes = new HashMap<>();
                        names.forEach((name, value) -> {
                            Genes.Gene gene = Genes.byName(name);
                            if (gene != null) {
                                genes.put(gene, value);
                            }
                        });
                        return genes;
                    },
                    genes -> {
                        Map<String, Double> names = new HashMap<>();
                        genes.forEach((gene, value) -> names.put(gene.name(), value));
                        return names;
                    });

    /**
     * The saved form, with the keys and missing-key defaults of 1.20.1's toNBT and fromNBT (ruling
     * P127): {@code entityPercentages} is now a list, and the root {@code version} key always writes
     * {@link SaveVersion#CURRENT} and reads 0 when missing. The layer colours are not saved.
     */
    public static final Codec<DinoData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf(SaveVersion.KEY, 0).forGetter(data -> SaveVersion.CURRENT),
            Codec.DOUBLE.optionalFieldOf(BASE_PERCENTAGE_KEY, 0D).forGetter(data -> data.basePercentage),
            ENTITY_PERCENTAGES_FIELD.forGetter(data -> data.entityPercentages),
            GENES_FIELD.forGetter(data -> data.advancedGenes),
            ENTITY_TYPE_ID.optionalFieldOf(BASE_DINO_KEY).forGetter(data -> Optional.of(savedEntityType(data.baseDino))),
            Codec.DOUBLE.optionalFieldOf(INCUBATION_PROGRESS_KEY, (double) NOT_SET).forGetter(data -> data.incubationProgress),
            Codec.INT.optionalFieldOf(INCUBATION_TIME_LEFT_KEY, NOT_SET).forGetter(data -> data.incubationTimeLeft)
    ).apply(instance, (version, basePercentage, entityPercentages, genes, baseDino, incubationProgress, incubationTimeLeft) ->
            new DinoData(version, basePercentage, entityPercentages, genes, baseDino.orElse(null), incubationProgress, incubationTimeLeft)));

    /** {@link #CODEC} on the network, as an NBT tag. */
    public static final StreamCodec<RegistryFriendlyByteBuf, DinoData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private int version = SaveVersion.CURRENT;
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

    /** The decoded form; {@link #CODEC} and {@link #copy} build through it. The maps are copied. */
    private DinoData(int version, double basePercentage, Map<EntityInfo, Double> entityPercentages,
                     Map<Genes.Gene, Double> advancedGenes, @Nullable EntityType<?> baseDino,
                     double incubationProgress, int incubationTimeLeft) {
        this.version = version;
        this.basePercentage = basePercentage;
        this.entityPercentages = new HashMap<>(entityPercentages);
        this.advancedGenes = new HashMap<>(advancedGenes);
        this.baseDino = baseDino;
        this.incubationProgress = incubationProgress;
        this.incubationTimeLeft = incubationTimeLeft;
    }

    /**
     * The entity type that 1.20.1's toNBT wrote (ruling P131): {@code getKey(type)}, which is the
     * entity registry's default key (pig) for a null type. fromNBT and {@link #CODEC} read a missing
     * {@code baseDino} key as null.
     */
    private static EntityType<?> savedEntityType(@Nullable EntityType<?> type) {
        return type != null
                ? type
                : BuiltInRegistries.ENTITY_TYPE.getValue(BuiltInRegistries.ENTITY_TYPE.getDefaultKey());
    }

    /** The version the data was saved with: {@code 0} for a save without the key, {@link SaveVersion#CURRENT} when new. */
    public int getVersion() {
        return version;
    }

    public Integer getLayerColor(int layer) {
        return layerColors.get(layer);
    }

    public List<Integer> getLayerColors() {
        return layerColors;
    }

    /**
     * The stack's {@link DataComponentInit#DINO_DATA} as a new object the caller may change (a
     * {@link #copy}; ruling P120). A stack without the component gives what the 1.20.1 read of a
     * missing compound gave: the decode of an empty compound (no base, no entity, not incubating).
     * A caller that only reads uses {@code stack.get(DINO_DATA)} and never changes the result.
     */
    public static DinoData fromStack(ItemStack stack) {
        DinoData data = stack.get(DataComponentInit.DINO_DATA.get());
        return data != null ? data.copy() : CODEC.parse(NbtOps.INSTANCE, new CompoundTag()).getOrThrow();
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

    /** The gene values set explicitly in the sequencer's edit tab, read-only; the server validates them. */
    public Map<Genes.Gene, Double> getAdvancedGenes() {
        return Collections.unmodifiableMap(advancedGenes);
    }

    public EntityType<?> getBaseDino() {
        return baseDino;
    }

    public double getEntityPercentage(EntityInfo type) {
        return entityPercentages.getOrDefault(type, 0D);
    }

    /** Appends incubation status and final gene tooltips to the item tooltip. */
    public void createToolTip(Consumer<Component> tooltip) {
        if (incubationProgress != NOT_SET) {
            int incubationPercentage = (int) NublarMath.round(incubationProgress * 100, 0);
            tooltip.accept(Component.literal("Incubation Progress: " + incubationPercentage + "%"));
        }
        if (incubationTimeLeft != NOT_SET) {
            tooltip.accept(Component.literal(
                    StringUtil.formatTickDuration(incubationTimeLeft, SharedConstants.TICKS_PER_SECOND)));
        }
        if (finalGenes.isEmpty()) {
            finalizeGenes();
        }
        finalGenes.forEach((gene, value) -> tooltip.accept(gene.getTooltip(value)));
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

    /**
     * A copy of every saved field and the version (ruling P130). The layer colours and the
     * final-gene cache are not saved, so the copy has their defaults, as a 1.20.1 save and load did.
     */
    public DinoData copy() {
        return new DinoData(version, basePercentage, entityPercentages, advancedGenes, baseDino,
                incubationProgress, incubationTimeLeft);
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

    /**
     * Stores {@link #storedCopy()} as the stack's {@link DataComponentInit#DINO_DATA}, so a later
     * change to this object does not reach the stack (ruling P120).
     */
    public void saveTo(ItemStack stack) {
        stack.set(DataComponentInit.DINO_DATA.get(), storedCopy());
    }

    /**
     * The value {@link #saveTo} stores: a {@link #copy} whose null entity types (the base and each
     * entity percentage's type) are the entity registry's default (pig). 1.20.1 saved through NBT,
     * which wrote a null type as the default key, so the stored data equals what a 1.20.1 save and
     * load gave (ruling P133).
     */
    DinoData storedCopy() {
        DinoData data = copy();
        data.baseDino = savedEntityType(baseDino);
        data.entityPercentages = new HashMap<>();
        entityPercentages.forEach((info, percentage) ->
                data.entityPercentages.put(new EntityInfo(savedEntityType(info.type()), info.variant()), percentage));
        return data;
    }

    public double getIncubationProgress() {
        return incubationProgress;
    }

    public void setIncubationTimeLeft(int ticks) {
        incubationTimeLeft = ticks;
    }

    /** Equal when every saved field is equal; the version, the layer colours and the gene cache are ignored. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DinoData other)) {
            return false;
        }
        return Double.compare(basePercentage, other.basePercentage) == 0
                && Double.compare(incubationProgress, other.incubationProgress) == 0
                && incubationTimeLeft == other.incubationTimeLeft
                && baseDino == other.baseDino
                && entityPercentages.equals(other.entityPercentages)
                && advancedGenes.equals(other.advancedGenes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePercentage, incubationProgress, incubationTimeLeft, baseDino, entityPercentages, advancedGenes);
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
