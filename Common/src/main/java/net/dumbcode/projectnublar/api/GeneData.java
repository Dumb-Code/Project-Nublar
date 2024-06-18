package net.dumbcode.projectnublar.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record GeneData(Map<Genes.Gene, Double> genes, Map<String,List<Integer>> colors) {
    private static final Map<EntityType<?>,GeneData> GENE_DATA = new HashMap<>();
    public static Codec<GeneData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(Genes.CODEC, Codec.DOUBLE).fieldOf("genes").forGetter(GeneData::genes),
                    Codec.unboundedMap(Codec.STRING, Codec.INT.listOf()).fieldOf("colors").forGetter(GeneData::colors)
            ).apply(instance, GeneData::new)
    );
    public static void register(EntityType<?> entityType, GeneData geneData){
        GENE_DATA.put(entityType, geneData);
    }
    public static GeneData getData(EntityType<?> entityType){
        return GENE_DATA.get(entityType);
    }
    record VariantHolder(Object variant){}
}
