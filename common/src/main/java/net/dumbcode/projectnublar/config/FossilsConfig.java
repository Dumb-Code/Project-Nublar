package net.dumbcode.projectnublar.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilPieces;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The fossil generation settings, read from {@code config/projectnublar-fossils.toml}: which
 * species generate in which period, their piece sets and weights, the depth band and rarity of
 * each period, and the weight and DNA yield of each quality. It also holds two switches that no
 * other config file owns yet: {@code compat.team_reborn_energy_bridge} and
 * {@code size.use_scale_attribute}.
 *
 * <p>The values are the defaults of the {@code fossils-1.20.1} branch and are not tuned yet.
 */
public class FossilsConfig {
    // Filled by the constructor, so they are declared before the static block that runs it.
    private static final Map<Identifier, SpeciesFossils> FOSSILS = new LinkedHashMap<>();
    private static final Map<String, PieceSet> SETS = new HashMap<>();
    private static final Map<String, QualityWeights> QUALITIES = new HashMap<>();
    /** In the order {@link #getPeriod} tests the bands; the first band that holds the y wins. */
    private static final Map<String, PeriodDepth> PERIODS = new LinkedHashMap<>();

    public static final ModConfigSpec CONFIG_SPEC;

    /**
     * {@code size.use_scale_attribute}: whether the scale attribute sets dinosaur size. Public so
     * that a GameTest can set it.
     */
    public static ModConfigSpec.BooleanValue USE_SCALE_ATTRIBUTE;

    private static ModConfigSpec.BooleanValue teamRebornEnergyBridge;

    static {
        Pair<FossilsConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(FossilsConfig::new);
        CONFIG_SPEC = pair.getRight();
    }

    private FossilsConfig(ModConfigSpec.Builder builder) {
        builder.push("fossils");
        builder.comment("defines the configured information for each type of fossil");
        builder.push("tyrannosaurus_rex");
        registerFossil("tyrannosaurus_rex", new SpeciesFossils(builder, "biped",
                List.of(FossilPieces.TYRANNOSAURUS_SKULL.name(), FossilPieces.TYRANNOSAURUS_FOOT.name(),
                        FossilPieces.TYRANNOSAURUS_HAND.name(), FossilPieces.TYRANNOSAURUS_PELVIS.name()),
                List.of(1, 2, 2, 1),
                1, List.of("cretaceous"),
                List.of(Biomes.DESERT.identifier(), Biomes.FOREST.identifier())));
        builder.pop();
        builder.pop();

        builder.push("sets");
        builder.comment("Fossil Piece Sets");
        builder.push("biped");
        registerSet("biped",
                builder.defineListAllowEmpty("pieces", List.of("ribcage", "bipedal_arm", "bipedal_leg", "tail", "neck"),
                        s -> s instanceof String st && FossilPieces.byName(st) != null),
                builder.defineListAllowEmpty("weights", List.of(1, 2, 2, 1, 1), o -> o instanceof Integer));
        builder.pop();
        builder.pop();

        builder.push("periods");
        builder.comment("defines the depths of each time period for generation, this list is incomplete and the values are examples, DC team to populate a full list once the systems are in place to do so");
        registerPeriod(builder, "carboniferous", 2, 20, 0.5);
        registerPeriod(builder, "jurassic", -12, 10, 0.4);
        registerPeriod(builder, "cretaceous", 40, 56, 0.4);
        builder.pop();

        builder.comment("define the weights of each fossil quality, (weight / total weights) = percentage chance this quality will generate and how much dna they will yield as a percentage of a full genome.").push("qualities");
        registerQuality(builder, "fragmented", 40, 5.0);
        registerQuality(builder, "poor", 20, 7.5);
        registerQuality(builder, "common", 15, 10.0);
        registerQuality(builder, "pristine", 4, 30.0);
        builder.pop();

        builder.push("compat");
        teamRebornEnergyBridge = builder.comment("Fabric only. When false, Nublar blocks exchange no energy with Team Reborn Energy in either direction: they offer an empty storage to Team Reborn Energy consumers, and the generator does not push energy into Team Reborn Energy blocks. NeoForge ignores this key.")
                .define("team_reborn_energy_bridge", true);
        builder.pop();

        builder.push("size");
        USE_SCALE_ATTRIBUTE = builder.comment("When true, the scale attribute sets each dinosaur's model and hitbox size from its size gene and growth stage, with the corrected base hitboxes. On by default (lead, 2026-10-27). Read once when the animal is added to the level.")
                .define("use_scale_attribute", true);
        builder.pop();
    }

    private static void registerFossil(String entityPath, SpeciesFossils fossil) {
        FOSSILS.put(Constants.modLoc(entityPath), fossil);
    }

    private static void registerSet(String setName, ModConfigSpec.ConfigValue<List<? extends String>> pieces,
                                    ModConfigSpec.ConfigValue<List<? extends Integer>> weights) {
        SETS.put(setName, new PieceSet(pieces, weights));
    }

    private static void registerPeriod(ModConfigSpec.Builder builder, String period, int minY, int maxY, double rarity) {
        builder.push(period);
        PERIODS.put(period, new PeriodDepth(
                builder.comment("the min y level that this period generates in").defineInRange("min_y", minY, -64, 255),
                builder.comment("the max y level that this period generates in").defineInRange("max_y", maxY, -64, 255),
                builder.comment("the rarity modifier to apply to the \"will generate\" method of this vein").defineInRange("rarity_mod", rarity, 0.0, 1.0)));
        builder.pop();
    }

    private static void registerQuality(ModConfigSpec.Builder builder, String quality, int weight, double dnaYield) {
        builder.push(quality);
        QUALITIES.put(quality, new QualityWeights(
                builder.defineInRange("weight", weight, 0, Integer.MAX_VALUE),
                builder.defineInRange("dna_yield", dnaYield, 0, 100)));
        builder.pop();
    }

    /**
     * Whether Nublar blocks exchange energy with Team Reborn Energy; read by Fabric only. When
     * {@code false}, the bridge is off in both directions: Nublar blocks offer an empty storage to
     * Team Reborn Energy consumers, and the generator does not push energy into Team Reborn Energy
     * blocks.
     */
    public static boolean teamRebornEnergyBridge() {
        return teamRebornEnergyBridge.get();
    }

    /** Whether the scale attribute sets dinosaur size; read once when an animal is added to the level. */
    public static boolean useScaleAttribute() {
        return USE_SCALE_ATTRIBUTE.get();
    }

    /** The configured species, keyed by entity type id. */
    public static Map<Identifier, SpeciesFossils> getFossils() {
        return FOSSILS;
    }

    public static PieceSet getSet(String setName) {
        return SETS.get(setName);
    }

    public static QualityWeights getQuality(String quality) {
        return QUALITIES.get(quality);
    }

    /** The first period whose depth band holds this y, or {@code "unknown"}. */
    public static String getPeriod(int y) {
        for (Map.Entry<String, PeriodDepth> entry : PERIODS.entrySet()) {
            PeriodDepth depth = entry.getValue();
            if (y >= depth.minY().get() && y <= depth.maxY().get()) {
                return entry.getKey();
            }
        }
        return "unknown";
    }

    /** The placement chance of a vein in this period; 0 for an unknown period. */
    public static double rarityModifier(String period) {
        PeriodDepth depth = PERIODS.get(period);
        return depth == null ? 0.0 : depth.rarityModifier().get();
    }

    /** The lowest y of this period; -64 for an unknown period. */
    public static int minY(String period) {
        PeriodDepth depth = PERIODS.get(period);
        return depth == null ? -64 : depth.minY().get();
    }

    /** The highest y of this period; 64 for an unknown period. */
    public static int maxY(String period) {
        PeriodDepth depth = PERIODS.get(period);
        return depth == null ? 64 : depth.maxY().get();
    }

    /** The fossil settings of one species. */
    public static class SpeciesFossils {
        private final ModConfigSpec.ConfigValue<String> pieces;
        private final ModConfigSpec.ConfigValue<List<? extends String>> specialPieces;
        private final ModConfigSpec.ConfigValue<List<? extends Integer>> specialWeights;
        private final ModConfigSpec.IntValue weight;
        private final ModConfigSpec.ConfigValue<List<? extends String>> periods;
        private final ModConfigSpec.ConfigValue<List<? extends String>> biomes;

        SpeciesFossils(ModConfigSpec.Builder builder, String setName, List<String> specialPieces,
                       List<Integer> specialWeights, int weight, List<String> periods, List<Identifier> biomes) {
            this.pieces = builder.comment("The pieces that make up this fossil").define("pieces", setName);
            this.specialPieces = builder.comment("optional field to include if a species has a special identifiable fossil type").defineListAllowEmpty("special_pieces", specialPieces,
                    o -> o instanceof String s && FossilPieces.byName(s) != null);
            this.specialWeights = builder.comment("The weights of the special pieces").defineListAllowEmpty("special_weights", specialWeights, o -> o instanceof Integer);
            this.weight = builder.comment("the lower the number the more rare the fossil; (weight / total weights) = percentage chance this fossil type generating").defineInRange("weight", weight, 0, Integer.MAX_VALUE);
            this.periods = builder.comment("The time period that this fossil type belongs to").defineListAllowEmpty("periods", periods, o -> o instanceof String);
            this.biomes = builder.comment("all acceptable biomes for this fossil to generate in").defineListAllowEmpty("biomes", biomes.stream().map(Identifier::toString).toList(), o -> o instanceof String);
        }

        /** The name of the piece set of this species. */
        public ModConfigSpec.ConfigValue<String> getPieces() {
            return pieces;
        }

        public ModConfigSpec.ConfigValue<List<? extends String>> getSpecialPieces() {
            return specialPieces;
        }

        public ModConfigSpec.ConfigValue<List<? extends Integer>> getSpecialWeights() {
            return specialWeights;
        }

        public ModConfigSpec.IntValue getWeight() {
            return weight;
        }

        public ModConfigSpec.ConfigValue<List<? extends String>> getPeriods() {
            return periods;
        }

        public ModConfigSpec.ConfigValue<List<? extends String>> getBiomes() {
            return biomes;
        }
    }

    /** A shared piece set and the weight of each piece, in the same order. */
    public record PieceSet(ModConfigSpec.ConfigValue<List<? extends String>> pieces,
                           ModConfigSpec.ConfigValue<List<? extends Integer>> weights) {
    }

    public record PeriodDepth(ModConfigSpec.IntValue minY, ModConfigSpec.IntValue maxY,
                              ModConfigSpec.DoubleValue rarityModifier) {
    }

    public record QualityWeights(ModConfigSpec.IntValue weight, ModConfigSpec.DoubleValue dnaYield) {
    }
}
