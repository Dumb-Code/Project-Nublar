package net.dumbcode.projectnublar.api;

import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Genes {
    public static List<Gene> GENE_STORAGE = new ArrayList<>();
    public static Gene AGGRESSION = register("aggression", Map.of(EntityType.HOGLIN, 0.85));
    public static Gene DEFENSE = register("defense", Map.of(EntityType.HOGLIN, 0.3, EntityType.LLAMA, 0.25, EntityType.PANDA, -0.5, EntityType.PIG, 0.2, EntityType.POLAR_BEAR, 0.75, EntityType.TURTLE, 0.5));
    public static Gene EAT_RATE = register("eat_rate", Map.of(EntityType.PANDA, -0.1));
    public static Gene HEALTH = register("health", Map.of(EntityType.BEE, -0.3, EntityType.COD, -.05, EntityType.POLAR_BEAR, 0.35, EntityType.PUFFERFISH, -0.5, EntityType.SALMON, -0.5, EntityType.SQUID, -0.3, EntityType.TROPICAL_FISH, -0.5, EntityType.TURTLE, 0.75));
    public static Gene HEALTH_REGEN = register("health_regen", Map.of(EntityType.SQUID, 0.25));
    public static Gene HEAT_RESISTANCE = register("heat_resistance", Map.of(EntityType.HOGLIN, 0.5));
    public static Gene HERD_SIZE = register("herd_size", Map.of(EntityType.COD, 0.5, EntityType.SHEEP, 0.5));
    public static Gene PACK_SIZE = register("pack_size", Map.of(EntityType.WOLF, 0.75));
    public static Gene IMMUNITY = register("immunity", Map.of(EntityType.PIG, 0.25, EntityType.TURTLE, 1.25));
    public static Gene INTELLIGENCE = register("intelligence", Map.of(EntityType.CAT, 0.5, EntityType.CHICKEN, -0.5, EntityType.DONKEY, 0.25, EntityType.FOX, 0.3, EntityType.MULE, 0.2, EntityType.PARROT, 0.25, EntityType.SHEEP, -0.25, EntityType.WOLF, 0.75));
    public static Gene JUMP = register("jump", Map.of(EntityType.HORSE, 0.3));
    public static Gene NOCTURNAL = register("nocturnal", Map.of(EntityType.BAT, 0.25, EntityType.SPIDER, 0.75));
    public static Gene FERTILITY = register("fertility", Map.of(EntityType.MULE, -0.5, EntityType.PANDA, -0.3));
    public static Gene SIZE = register("size", Map.ofEntries(new HashMap.SimpleEntry<>(EntityType.BAT, -0.25), new HashMap.SimpleEntry<>(EntityType.BEE, -0.5), new HashMap.SimpleEntry<>(EntityType.CAT, -0.6), new HashMap.SimpleEntry<>(EntityType.CAVE_SPIDER, -0.5), new HashMap.SimpleEntry<>(EntityType.COD, -0.5), new HashMap.SimpleEntry<>(EntityType.HOGLIN, 0.75), new HashMap.SimpleEntry<>(EntityType.OCELOT, -0.6), new HashMap.SimpleEntry<>(EntityType.POLAR_BEAR, 0.5), new HashMap.SimpleEntry<>(EntityType.PUFFERFISH, -0.5), new HashMap.SimpleEntry<>(EntityType.RABBIT, -0.75), new HashMap.SimpleEntry<>(EntityType.SALMON, -0.5), new HashMap.SimpleEntry<>(EntityType.TROPICAL_FISH, -0.5)));
    public static Gene SPEED = register("speed", Map.ofEntries(new HashMap.SimpleEntry<>(EntityType.BEE, 0.3), new HashMap.SimpleEntry<>(EntityType.DOLPHIN, 0.35), new HashMap.SimpleEntry<>(EntityType.DONKEY, -0.20), new HashMap.SimpleEntry<>(EntityType.FOX, 0.7), new HashMap.SimpleEntry<>(EntityType.MULE, -0.20), new HashMap.SimpleEntry<>(EntityType.OCELOT, 0.5), new HashMap.SimpleEntry<>(EntityType.PANDA, 0.35), new HashMap.SimpleEntry<>(EntityType.POLAR_BEAR, 0.5), new HashMap.SimpleEntry<>(EntityType.RABBIT, 0.5), new HashMap.SimpleEntry<>(EntityType.TURTLE, -0.5), new HashMap.SimpleEntry<>(EntityType.WOLF, 0.25)));
    public static Gene STOMACH_CAPACITY = register("stomach_capacity", Map.of(EntityType.LLAMA, 0.3, EntityType.PANDA, 0.75));
    public static Gene STRENGTH = register("strength", Map.of(EntityType.DONKEY, 0.25, EntityType.HOGLIN, 0.5, EntityType.PANDA, 0.25, EntityType.POLAR_BEAR, 0.5));
    public static Gene TAMABILITY = register("tamability", Map.of(EntityType.CAT, 0.45, EntityType.HOGLIN, -0.5, EntityType.HORSE, 0.4, EntityType.LLAMA, 0.25, EntityType.MULE, 0.3, EntityType.OCELOT, -0.3, EntityType.WOLF, 1.75));
    public static Gene UNDERWATER_CAPACITY = register("underwater_capacity", Map.of(EntityType.COD, 0.3, EntityType.DOLPHIN, 0.75, EntityType.PUFFERFISH, 0.3, EntityType.SALMON, 0.3, EntityType.SQUID, 0.5, EntityType.TROPICAL_FISH, 0.3, EntityType.TURTLE, 1.25));
    public static Gene COLOR = register("color", Map.of(EntityType.TROPICAL_FISH, 0.1));

    public static Gene register(String name, Map<EntityType<?>, Double> entities) {
        Gene gene = new Gene(name, entities);
        GENE_STORAGE.add(gene);
        return gene;
    }

    public static Gene byName(String name) {
        for (Gene gene : GENE_STORAGE) {
            if (gene.name().equals(name)) {
                return gene;
            }
        }
        return null;
    }

    public record Gene(String name, Map<EntityType<?>, Double> entities) {
    }
}
