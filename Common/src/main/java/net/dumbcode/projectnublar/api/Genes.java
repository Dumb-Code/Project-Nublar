package net.dumbcode.projectnublar.api;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Genes {
    public static Codec<Gene> CODEC = Codec.STRING.xmap(Genes::byName, Gene::name);
    public static List<Gene> GENE_STORAGE = new ArrayList<>();
    public static Gene AGGRESSION = register("aggression", new HashMap<>());
    public static Gene DEFENSE = register("defense", new HashMap<>());
    public static Gene EAT_RATE = register("eat_rate", new HashMap<>());
    public static Gene HEALTH = register("health", new HashMap<>());
    public static Gene HEALTH_REGEN = register("health_regen", new HashMap<>());
    public static Gene HEAT_RESISTANCE = register("heat_resistance", new HashMap<>());
    public static Gene HERD_SIZE = register("herd_size", new HashMap<>());
    public static Gene PACK_SIZE = register("pack_size", new HashMap<>());
    public static Gene IMMUNITY = register("immunity", new HashMap<>());
    public static Gene INTELLIGENCE = register("intelligence", new HashMap<>());
    public static Gene JUMP = register("jump", new HashMap<>());
    public static Gene NOCTURNAL = register("nocturnal", new HashMap<>());
    public static Gene FERTILITY = register("fertility", new HashMap<>());
    public static Gene SIZE = register("size", new HashMap<>());
    public static Gene SPEED = register("speed", new HashMap<>());
    public static Gene STOMACH_CAPACITY = register("stomach_capacity", new HashMap<>());
    public static Gene STRENGTH = register("strength", new HashMap<>());
    public static Gene TAMABILITY = register("tamability", new HashMap<>());
    public static Gene UNDERWATER_CAPACITY = register("underwater_capacity", new HashMap<>());


    public static Gene register(String name, Map<EntityType<?>, Double> entities) {
        Gene gene = new Gene(name, entities);
        GENE_STORAGE.add(gene);
        return gene;
    }
    public static void addToGene(Gene gene, EntityType<?> type, double value) {
        for(Gene g : GENE_STORAGE) {
            if(g.equals(gene)) {
                g.addEntityType(type, value);
            }
        }
    }

    public static Gene byName(String name) {
        for (Gene gene : GENE_STORAGE) {
            if (gene.name().equals(name)) {
                return gene;
            }
        }
        return null;
    }

    public record Gene(String name, Map<EntityType<?>,Double> entities) {
        public void addEntityType(EntityType<?> type, double value) {
            entities.put(type, value);
        }
    }
}
