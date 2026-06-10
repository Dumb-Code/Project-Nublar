package net.dumbcode.projectnublar.api.dinosaur;

import java.util.List;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.ProjectNublar;
import net.dumbcode.projectnublar.api.fossil.FossilPiece;
import net.dumbcode.projectnublar.api.fossil.FossilPieces;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * A single DNA sample: source entity type (plus optional variant), DNA percentage, originating
 * fossil piece/quality, and embryo flag.
 *
 * <p>Serialized into the {@code DNAData} compound on item stacks; every tag-name string below is
 * a frozen save contract. Note that {@code SyringeItem} additionally writes a root-level
 * {@code Embryo} boolean outside this compound - that inconsistency is preserved elsewhere.
 */
public class DNAData {
    private static final String DNA_DATA_TAG = "DNAData";
    private static final String ENTITY_TYPE_KEY = "entityType";
    private static final String DNA_PERCENTAGE_KEY = "dnaPercentage";
    private static final String VARIANT_KEY = "variant";
    private static final String FOSSIL_PIECE_KEY = "fossilPiece";
    private static final String QUALITY_KEY = "quality";
    private static final String IS_EMBRYO_KEY = "isEmbryo";

    private static final String QUALITY_TRANSLATION_PREFIX = "quality.";
    private static final String TROPICAL_TOOLTIP_KEY = "tooltip." + Constants.MODID + ".tropical";
    private static final String COLOR_TRANSLATION_PREFIX = "color.minecraft.";

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

    // TODO(DEAD): a quality-based DNA yield (from the old FossilsConfig) was planned here but
    // never finished; both branches returned the raw percentage, so the branch was removed.
    public double getDnaPercentage() {
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

    public static void createTooltip(ItemStack stack, List<Component> tooltip) {
        if (stack.hasTag()) {
            DNAData dnaData = loadFromNBT(stack.getTag().getCompound(DNA_DATA_TAG));
            tooltip.add(dnaData.getFormattedType());
            if (dnaData.getDnaPercentage() != 0) {
                tooltip.add(dnaData.getFormattedDNA());
            }
            if (dnaData.getQuality() != null) {
                tooltip.add(
                        Component.translatable(
                                QUALITY_TRANSLATION_PREFIX
                                        + Constants.MODID
                                        + "."
                                        + dnaData.getQuality().getName()));
            }
            if (dnaData.variant != null) {
                tooltip.add(Component.literal(ProjectNublar.checkReplace(dnaData.variant)));
            }
            dnaData.addTFishTT(tooltip);
        }
    }

    public void addTFishTT(List<Component> tooltip) {
        if (tFish1 != DyeColor.BLACK) {
            tooltip.add(
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
                localVariant = ProjectNublar.checkReplace(new ResourceLocation(variant).getPath());
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

    public CompoundTag saveToNBT(CompoundTag tag) {
        tag.putString(ENTITY_TYPE_KEY, BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
        if (dnaPercentage != 0) {
            tag.putDouble(DNA_PERCENTAGE_KEY, dnaPercentage);
        }
        if (variant != null) {
            tag.putString(VARIANT_KEY, variant);
        }
        if (fossilPiece != null) {
            tag.putString(FOSSIL_PIECE_KEY, fossilPiece.name());
        }
        if (quality != null) {
            tag.putString(QUALITY_KEY, quality.getName());
        }
        tag.putBoolean(IS_EMBRYO_KEY, isEmbryo);
        return tag;
    }

    public void addTFish(TropicalFish tropicalFish) {
        tFish1 = tropicalFish.getPatternColor();
        tFish2 = tropicalFish.getBaseColor();
    }

    public static DNAData loadFromNBT(CompoundTag tag) {
        DNAData dnaData = new DNAData();
        dnaData.setEntityType(
                BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(tag.getString(ENTITY_TYPE_KEY))));
        if (tag.contains(DNA_PERCENTAGE_KEY)) {
            dnaData.setDnaPercentage(tag.getDouble(DNA_PERCENTAGE_KEY));
        }
        if (tag.contains(VARIANT_KEY)) {
            dnaData.setVariant(tag.getString(VARIANT_KEY));
        }
        if (tag.contains(FOSSIL_PIECE_KEY)) {
            dnaData.setFossilPiece(FossilPieces.getPieceByName(tag.getString(FOSSIL_PIECE_KEY)));
        }
        if (tag.contains(QUALITY_KEY)) {
            dnaData.setQuality(Quality.byName(tag.getString(QUALITY_KEY)));
        }
        dnaData.setEmbryo(tag.getBoolean(IS_EMBRYO_KEY));
        return dnaData;
    }

    /** Reads the sample from a storage-drive stack; the entityType parameter is unused. */
    public static DNAData fromDrive(ItemStack stack, EntityType<?> entityType) {
        return loadFromNBT(stack.getTag().getCompound(DNA_DATA_TAG));
    }

    public static String createStorageKey(EntityType<?> entityType, String variant) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType) + (variant == null ? "" : "_" + variant);
    }

    public DinoData.EntityInfo getEntityInfo() {
        return new DinoData.EntityInfo(entityType, variant);
    }
}
