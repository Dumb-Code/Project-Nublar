package net.dumbcode.projectnublar.config;

import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilPieces;
import net.dumbcode.projectnublar.api.FossilSet;
import net.dumbcode.projectnublar.api.FossilSets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FossilsConfig {
    private static final Map<String, Fossil> FOSSILS = new HashMap<>();
    private static final Map<String, Set> SETS = new HashMap<>();
    private static final Map<String, Quality> QUALITIES = new HashMap<>();

    static {
        Pair<FossilsConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(FossilsConfig::new);
        CONFIG_SPEC = pair.getRight();
        INSTANCE = pair.getLeft();
    }

    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final FossilsConfig INSTANCE;

    public Period carboniferous;
    public Period jurassic;
    public Period cretaceous;

    public Quality fragmented;
    public Quality poor;
    public Quality common;
    public Quality pristine;

    public Set biped;
    public Set quadruped;
    public Set fern;

    public Fossil tyrannosaurus_rex;

    public FossilsConfig(ForgeConfigSpec.Builder builder) {


        super();
        builder.push("fossils");
        builder.comment("defines the configured information for each type of fossil");
        builder.push("tyrannosaurus_rex");
        tyrannosaurus_rex = registerFossil("projectnublar:tyrannosaurus_rex", new Fossil(builder, FossilSets.BIPED, Map.of(FossilPieces.REX_SKULL, 1), 1, List.of("cretaceous"), List.of(Biomes.DESERT.location(), Biomes.FOREST.location())));
        builder.pop();
        builder.pop();

        builder.push("sets");
        builder.comment("define the pieces that belong to each set");
        builder.push("biped");
        biped = registerSet("biped", builder.defineList("pieces", List.of("ribcage", "foot", "arm", "leg", "tail", "spine"), s -> s instanceof String st && FossilPieces.getPieceByName(st) != null), builder.defineList("weights", List.of(1, 2, 2, 2, 1, 1), o -> o instanceof Integer));
        builder.pop();
        builder.push("quadruped");
        quadruped = registerSet("quadruped", builder.defineList("pieces", List.of("ribcage", "foot", "arm", "leg", "tail", "spine"), s -> s instanceof String st && FossilPieces.getPieceByName(st) != null), builder.defineList("weights", List.of(1, 4, 4, 1, 1), o -> o instanceof Integer));
        builder.pop();
        builder.push("fern");
        fern = registerSet("fern", builder.defineList("pieces", List.of("leaf"), s -> s instanceof String st && FossilPieces.getPieceByName(st) != null), builder.defineList("weights", List.of(1), o -> o instanceof Integer));
        builder.pop();
        builder.pop();

        builder.push("periods");
        builder.comment("defines the depths of each time period for generation, this list is incomplete and the values are examples, DC team to populate a full list once the systems are in place to do so");
        builder.push("carboniferous");
        carboniferous = new Period(
                builder.comment("the min y value that this period generates in").defineInRange("min_y", 2, -64, 255),
                builder.comment("the max y value that this period generates in").defineInRange("max_y", 20, -64, 255),
                builder.comment("the rarity modifier to apply to the \"will generate\" method of this vein").defineInRange("rarity_mod", 0.5, 0.0, 1.0));
        builder.pop();
        builder.push("jurassic");
        jurassic = new Period(
                builder.comment("the min y value that this period generates in").defineInRange("min_y", -12, -64, 255),
                builder.comment("the max y value that this period generates in").defineInRange("max_y", 10, -64, 255),
                builder.comment("the rarity modifier to apply to the \"will generate\" method of this vein").defineInRange("rarity_mod", 0.4, 0.0, 1.0));
        builder.pop();
        builder.push("cretaceous");
        cretaceous = new Period(
                builder.comment("the min y value that this period generates in").defineInRange("min_y", -40, -64, 255),
                builder.comment("the max y value that this period generates in").defineInRange("max_y", -10, -64, 255),
                builder.comment("the rarity modifier to apply to the \"will generate\" method of this vein").defineInRange("rarity_mod", 0.3, 0.0, 1.0));
        builder.pop();
        builder.pop();

        builder.comment("define the weights of each fossil quality, (weight / total weights) = percentage chance this quality will generate and how much dna they will yield as a percentage of a full genome.").push("qualities");
        builder.push("fragmented");
        fragmented = new Quality(builder.defineInRange("weight", 10, 0, Integer.MAX_VALUE), builder.defineInRange("dna_yield", 10, 0, 100));
        builder.pop();
        builder.push("poor");
        poor = new Quality(builder.defineInRange("weight", 20, 0, Integer.MAX_VALUE), builder.defineInRange("dna_yield", 20, 0, 100));
        builder.pop();
        builder.push("common");
        common = new Quality(builder.defineInRange("weight", 40, 0, Integer.MAX_VALUE), builder.defineInRange("dna_yield", 40, 0, 100));
        builder.pop();
        builder.push("pristine");
        pristine = new Quality(builder.defineInRange("weight", 30, 0, Integer.MAX_VALUE), builder.defineInRange("dna_yield", 30, 0, 100));
        builder.pop();
        builder.pop();
        QUALITIES.put("fragmented", fragmented);
        QUALITIES.put("poor", poor);
        QUALITIES.put("common", common);
        QUALITIES.put("pristine", pristine);
    }

    public static Fossil registerFossil(String fossilName, Fossil fossil) {
        FOSSILS.put(fossilName, fossil);
        return fossil;
    }

    public static Map<String, Fossil> getFossils() {
        return FOSSILS;
    }

    public static Set registerSet(String setName, ForgeConfigSpec.ConfigValue<List<? extends String>> pieces, ForgeConfigSpec.ConfigValue<List<? extends Integer>> weights) {
        Set set = new Set(pieces, weights);
        SETS.put(setName, set);
        return set;
    }

    public static Set getSet(String setName) {
        return SETS.get(setName);
    }

    public static String getPeriod(int yValue) {
        if (yValue >= INSTANCE.carboniferous.minY.get() && yValue <= INSTANCE.carboniferous.maxY.get()) {
            return "carboniferous";
        } else if (yValue >= INSTANCE.jurassic.minY.get() && yValue <= INSTANCE.jurassic.maxY.get()) {
            return "jurassic";
        } else if (yValue >= INSTANCE.cretaceous.minY.get() && yValue <= INSTANCE.cretaceous.maxY.get()) {
            return "cretaceous";
        }
        return "unknown";
    }

    public static boolean testPeriodChance(String period, RandomSource random) {
        return switch (period) {
            case "carboniferous" -> random.nextDouble() < INSTANCE.carboniferous.rarityModifier.get();
            case "jurassic" -> random.nextDouble() < INSTANCE.jurassic.rarityModifier.get();
            case "cretaceous" -> random.nextDouble() < INSTANCE.cretaceous.rarityModifier.get();
            default -> false;
        };
    }

    public static Quality getQuality(String quality) {
        return QUALITIES.get(quality);
    }
    public class Fossil {
        ForgeConfigSpec.ConfigValue<String> pieces;
        ForgeConfigSpec.ConfigValue<List<String>> special_pieces;
        ForgeConfigSpec.ConfigValue<List<Integer>> special_weights;
        ForgeConfigSpec.IntValue weight;
        ForgeConfigSpec.ConfigValue<List<String>> periods;
        ForgeConfigSpec.ConfigValue<List<String>> biomes;

        public Fossil(ForgeConfigSpec.Builder builder, FossilSet set, Map<FossilPiece, Integer> specialPieces, int weight, List<String> periods, List<ResourceLocation> biomes) {
            this.pieces = builder.comment("The pieces that make up this fossil").define("pieces", set.name());
            this.special_pieces = builder.comment("optional field to include if a species has a special identifiable fossil type").define("special_pieces", specialPieces.keySet().stream().map(FossilPiece::name).toList());
            this.special_weights = builder.comment("The weights of the special pieces").define("special_weights", specialPieces.values().stream().toList());
            this.weight = builder.comment("the lower the number the more rare the fossil; (weight / total weights) = percentage chance this fossil type generating").defineInRange("weight", weight, 0, Integer.MAX_VALUE);
            this.periods = builder.comment("The time period that this fossil type belongs to").define("periods", periods);
            this.biomes = builder.comment("all acceptable biomes for this fossil to generate in").define("biomes", biomes.stream().map(ResourceLocation::toString).toList());
        }

        public ForgeConfigSpec.ConfigValue<String> getPieces() {
            return pieces;
        }

        public ForgeConfigSpec.ConfigValue<List<String>> getSpecial_pieces() {
            return special_pieces;
        }

        public ForgeConfigSpec.ConfigValue<List<Integer>> getSpecial_weights() {
            return special_weights;
        }

        public ForgeConfigSpec.IntValue getWeight() {
            return weight;
        }

        public ForgeConfigSpec.ConfigValue<List<String>> getPeriods() {
            return periods;
        }

        public ForgeConfigSpec.ConfigValue<List<String>> getBiomes() {
            return biomes;
        }
    }

    public static class Set {
        public ForgeConfigSpec.ConfigValue<List<? extends String>> pieces;
        public ForgeConfigSpec.ConfigValue<List<? extends Integer>> weights;

        public Set(ForgeConfigSpec.ConfigValue<List<? extends String>> pieces,
                   ForgeConfigSpec.ConfigValue<List<? extends Integer>> weights) {
            this.pieces = pieces;
            this.weights = weights;
        }

        public ForgeConfigSpec.ConfigValue<List<? extends String>> pieces() {
            return pieces;
        }

        public ForgeConfigSpec.ConfigValue<List<? extends Integer>> weights() {
            return weights;
        }
    }

    public record Period(ForgeConfigSpec.IntValue minY, ForgeConfigSpec.IntValue maxY,
                         ForgeConfigSpec.DoubleValue rarityModifier) {
    }

    public record Quality(ForgeConfigSpec.IntValue weight, ForgeConfigSpec.IntValue dnaYield) {
    }
}
