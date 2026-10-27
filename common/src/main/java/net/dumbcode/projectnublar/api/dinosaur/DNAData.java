package net.dumbcode.projectnublar.api.dinosaur;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.ProjectNublar;
import net.dumbcode.projectnublar.api.fossil.FossilPiece;
import net.dumbcode.projectnublar.api.fossil.FossilPieces;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.dumbcode.projectnublar.util.SaveVersion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * A single DNA sample: source entity type (plus optional variant), DNA percentage, originating
 * fossil piece/quality, and embryo flag.
 *
 * <p>Items carry it as the {@link DataComponentInit#DNA_DATA} component, saved through
 * {@link #CODEC}; a storage drive holds one per storage key in {@link DiskStorage}. Every key name
 * below is a frozen save contract. The tropical-fish colours are not saved (as in 1.20.1), so they
 * take no part in {@link #equals}; the {@code version} read from a save takes no part either
 * (ruling P122). The syringe's embryo flag is a separate flag on the stack, not {@link #isEmbryo()}.
 */
public class DNAData {
    private static final String ENTITY_TYPE_KEY = "entityType";
    private static final String DNA_PERCENTAGE_KEY = "dnaPercentage";
    private static final String VARIANT_KEY = "variant";
    private static final String FOSSIL_PIECE_KEY = "fossilPiece";
    private static final String QUALITY_KEY = "quality";
    private static final String IS_EMBRYO_KEY = "isEmbryo";

    private static final String QUALITY_TRANSLATION_PREFIX = "quality.";
    private static final String TROPICAL_TOOLTIP_KEY = "tooltip." + Constants.MODID + ".tropical";
    private static final String COLOR_TRANSLATION_PREFIX = "color.minecraft.";

    /**
     * {@code entityType} as 1.20.1's saveToNBT and loadFromNBT had it: both use the defaulted entity
     * registry, so a null type writes the default key (pig), and a missing or unknown id reads as pig.
     */
    private static final MapCodec<EntityType<?>> ENTITY_TYPE_FIELD = Identifier.CODEC.optionalFieldOf(ENTITY_TYPE_KEY).xmap(
            saved -> BuiltInRegistries.ENTITY_TYPE.getValue(saved.orElse(null)),
            entityType -> Optional.of(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)));

    /**
     * The saved form, with the keys and missing-key defaults of 1.20.1's saveToNBT and loadFromNBT
     * (ruling P127): a fossil piece or quality name that no longer exists reads as null, and the
     * root {@code version} key always writes {@link SaveVersion#CURRENT} and reads 0 when missing.
     */
    public static final Codec<DNAData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf(SaveVersion.KEY, 0).forGetter(data -> SaveVersion.CURRENT),
            ENTITY_TYPE_FIELD.forGetter(data -> data.entityType),
            Codec.DOUBLE.optionalFieldOf(DNA_PERCENTAGE_KEY, 0D).forGetter(data -> data.dnaPercentage),
            Codec.STRING.optionalFieldOf(VARIANT_KEY).forGetter(data -> Optional.ofNullable(data.variant)),
            Codec.STRING.optionalFieldOf(FOSSIL_PIECE_KEY).forGetter(data -> Optional.ofNullable(data.fossilPiece).map(FossilPiece::name)),
            Codec.STRING.optionalFieldOf(QUALITY_KEY).forGetter(data -> Optional.ofNullable(data.quality).map(Quality::getName)),
            Codec.BOOL.optionalFieldOf(IS_EMBRYO_KEY, false).forGetter(data -> data.isEmbryo)
    ).apply(instance, DNAData::decoded));

    /** {@link #CODEC} on the network, as an NBT tag. */
    public static final StreamCodec<RegistryFriendlyByteBuf, DNAData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private int version = SaveVersion.CURRENT;
    private EntityType<?> entityType;
    private double dnaPercentage;
    private String variant;
    private FossilPiece fossilPiece;
    private Quality quality;
    private boolean isEmbryo;
    /** Tropical-fish pattern/base colors; BLACK doubles as the "not a tropical fish" sentinel. */
    private DyeColor tFish1 = DyeColor.BLACK;
    private DyeColor tFish2 = DyeColor.BLACK;

    public DNAData() {
    }

    /** Builds a sample from the fields {@link #CODEC} read, resolving the names as 1.20.1's loadFromNBT did. */
    private static DNAData decoded(int version, EntityType<?> entityType, double dnaPercentage, Optional<String> variant,
                                   Optional<String> fossilPiece, Optional<String> quality, boolean isEmbryo) {
        DNAData data = new DNAData();
        data.version = version;
        data.entityType = entityType;
        data.dnaPercentage = dnaPercentage;
        data.variant = variant.orElse(null);
        data.fossilPiece = fossilPiece.map(FossilPieces::byName).orElse(null);
        data.quality = quality.map(Quality::byName).orElse(null);
        data.isEmbryo = isEmbryo;
        return data;
    }

    /** The version the data was saved with: {@code 0} for a save without the key, {@link SaveVersion#CURRENT} when new. */
    public int getVersion() {
        return version;
    }

    public DyeColor gettFish1() {
        return tFish1;
    }

    public DyeColor gettFish2() {
        return tFish2;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    /** A fossil sample yields the configured DNA of its quality; any other sample its own percentage. */
    public double getDnaPercentage() {
        if (quality != null) {
            return FossilsConfig.getQuality(quality.getName()).dnaYield().get() / 100d;
        }
        return dnaPercentage;
    }

    public void setDnaPercentage(double dnaPercentage) {
        this.dnaPercentage = dnaPercentage;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public boolean isEmbryo() {
        return isEmbryo;
    }

    public void setEmbryo(boolean embryo) {
        isEmbryo = embryo;
    }

    public FossilPiece getFossilPiece() {
        return fossilPiece;
    }

    public void setFossilPiece(FossilPiece fossilPiece) {
        this.fossilPiece = fossilPiece;
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public String getNameSpace() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getNamespace();
    }

    public String getPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath();
    }

    public String getStorageName() {
        return createStorageKey(entityType, variant);
    }

    /** Adds the sample's type, DNA, quality, variant and tropical-fish lines to the item tooltip. */
    public static void createTooltip(ItemStack stack, Consumer<Component> tooltip) {
        // read only: component values are shared between stack copies
        DNAData dnaData = stack.get(DataComponentInit.DNA_DATA.get());
        if (dnaData != null) {
            tooltip.accept(dnaData.getFormattedType());
            if (dnaData.getDnaPercentage() != 0) {
                tooltip.accept(dnaData.getFormattedDNA());
            }
            if (dnaData.getQuality() != null) {
                tooltip.accept(
                        Component.translatable(
                                QUALITY_TRANSLATION_PREFIX
                                        + Constants.MODID
                                        + "."
                                        + dnaData.getQuality().getName()));
            }
            if (dnaData.variant != null) {
                tooltip.accept(Component.literal(ProjectNublar.checkReplace(dnaData.variant)));
            }
            dnaData.addTFishTT(tooltip);
        }
    }

    /** Adds the tropical-fish colour line when the sample came from a tropical fish. */
    public void addTFishTT(Consumer<Component> tooltip) {
        if (tFish1 != DyeColor.BLACK) {
            tooltip.accept(
                    Component.translatable(
                            TROPICAL_TOOLTIP_KEY,
                            Component.translatable(COLOR_TRANSLATION_PREFIX + tFish1.getName()),
                            Component.translatable(COLOR_TRANSLATION_PREFIX + tFish2.getName())));
        }
    }

    public static DNAData combineDNA(DNAData dna1, DNAData dna2) {
        DNAData dnaData = new DNAData();
        if (dna1.getStorageName().equals(dna2.getStorageName())) {
            dnaData.setEntityType(dna1.getEntityType());
            dnaData.setDnaPercentage(Math.min(1.0d, dna1.getDnaPercentage() + dna2.getDnaPercentage()));
            dnaData.setVariant(dna1.getVariant());
            dnaData.setFossilPiece(dna1.getFossilPiece());
            dnaData.setQuality(dna1.getQuality());
            dnaData.setEmbryo(false);
            return dnaData;
        }
        return null;
    }

    public MutableComponent getFormattedType() {
        String localVariant = "";
        if (getVariant() != null) {
            if (entityType.getDescription().getString().toLowerCase().contains("parrot")) {
                localVariant = ProjectNublar.checkReplace(variant);
            } else if (entityType.getDescription().getString().toLowerCase().contains("cat")) {
                localVariant = ProjectNublar.checkReplace(Identifier.parse(variant).getPath());
            }
        }
        return Component.literal(localVariant + getEntityType().getDescription().getString());
    }

    public MutableComponent getFormattedDNA() {
        return Component.literal(Mth.floor(getDnaPercentage() * 100) + "% DNA");
    }

    public MutableComponent getFormattedDNANoDescriptor() {
        return Component.literal(Mth.floor(getDnaPercentage() * 100) + "%");
    }

    public void addTFish(TropicalFish tropicalFish) {
        tFish1 = tropicalFish.getPatternColor();
        tFish2 = tropicalFish.getBaseColor();
    }

    /**
     * The stack's {@link DataComponentInit#DNA_DATA} as a new object the caller may change (a
     * {@link #copy}; ruling P120). A stack without the component gives {@link #missing()}. A caller
     * that only reads uses {@code stack.get(DNA_DATA)} and never changes the result.
     */
    public static DNAData fromStack(ItemStack stack) {
        DNAData data = stack.get(DataComponentInit.DNA_DATA.get());
        return data != null ? data.copy() : missing();
    }

    /**
     * What 1.20.1 read from a missing compound (ruling P131): the decode of an empty compound, a new
     * pig sample with no DNA.
     */
    static DNAData missing() {
        return CODEC.parse(NbtOps.INSTANCE, new CompoundTag()).getOrThrow();
    }

    /**
     * Stores {@link #storedCopy()} as the stack's {@link DataComponentInit#DNA_DATA}, so a later
     * change to this object does not reach the stack (ruling P120).
     */
    public void saveTo(ItemStack stack) {
        stack.set(DataComponentInit.DNA_DATA.get(), storedCopy());
    }

    /**
     * The value {@link #saveTo} stores: a {@link #copy} whose null entity type is the entity
     * registry's default (pig). 1.20.1 saved through NBT, which wrote a null type as the default key,
     * so the stored sample equals what a 1.20.1 save and load gave (ruling P133).
     */
    DNAData storedCopy() {
        DNAData data = copy();
        if (data.entityType == null) {
            data.entityType = BuiltInRegistries.ENTITY_TYPE.getValue(BuiltInRegistries.ENTITY_TYPE.getDefaultKey());
        }
        return data;
    }

    /**
     * A copy of every saved field and the version. The tropical-fish colours are not saved, so the
     * copy has their defaults, as a 1.20.1 save and load did.
     */
    public DNAData copy() {
        DNAData data = new DNAData();
        data.version = version;
        data.entityType = entityType;
        data.dnaPercentage = dnaPercentage;
        data.variant = variant;
        data.fossilPiece = fossilPiece;
        data.quality = quality;
        data.isEmbryo = isEmbryo;
        return data;
    }

    public static String createStorageKey(EntityType<?> entityType, String variant) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType) + (variant == null ? "" : "_" + variant);
    }

    public DinoData.EntityInfo getEntityInfo() {
        return new DinoData.EntityInfo(entityType, variant);
    }

    /** Equal when every saved field is equal; the version and the tropical-fish colours are ignored. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DNAData other)) {
            return false;
        }
        return entityType == other.entityType
                && Double.compare(dnaPercentage, other.dnaPercentage) == 0
                && Objects.equals(variant, other.variant)
                && Objects.equals(fossilPiece, other.fossilPiece)
                && quality == other.quality
                && isEmbryo == other.isEmbryo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, dnaPercentage, variant, fossilPiece, quality, isEmbryo);
    }
}
