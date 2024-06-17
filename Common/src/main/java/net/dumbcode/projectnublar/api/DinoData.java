package net.dumbcode.projectnublar.api;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DinoData {
    public double basePercentage;
    private Map<EntityType<?>, Double> entityPercentages = new HashMap<>();
    private Map<Genes.Gene, Double> genes = new HashMap<>();
    private List<DyeColor> layerColors = List.of(
            DyeColor.GREEN,
            DyeColor.GREEN
    );
    private ResourceLocation textureLocation = null;

    public DinoData() {
    }

    public ResourceLocation getTextureLocation() {
        if (textureLocation == null) {
            String colorString = layerColors.stream().map(DyeColor::getName).reduce((s1, s2) -> s1 + "_" + s2).orElse("green");
            textureLocation = new ResourceLocation(Constants.MODID, "textures/entity/dinosaur/" + colorString + ".png");
        }
        return textureLocation;
    }

    public void addEntity(EntityType<?> type, double percentage) {
        entityPercentages.put(type, percentage);
    }

    public void removeEntity(EntityType<?> type) {
        entityPercentages.remove(type);
    }

    public void setBasePercentage(double basePercentage) {
        this.basePercentage = basePercentage;
    }

    public void setGeneValue(Genes.Gene gene, double value) {
        genes.put(gene, value);
    }

    public double getBasePercentage() {
        return basePercentage;
    }


    public double getGeneValue(Genes.Gene gene) {
        if (genes.containsKey(gene)) {
            return genes.get(gene);
        }
        double value = 0;
        for (Genes.Gene g : Genes.GENE_STORAGE) {
            for (Map.Entry<EntityType<?>, Double> entry : entityPercentages.entrySet()) {
                if (g.entities().containsKey(entry.getKey())) {
                    value += g.entities().get(entry.getKey()) * (entry.getValue() * 2);
                }
            }
        }
        return value;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("basePercentage", basePercentage);
        CompoundTag entityTag = new CompoundTag();
        for (Map.Entry<EntityType<?>, Double> entry : entityPercentages.entrySet()) {
            entityTag.putDouble(BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey()).toString(), entry.getValue());
        }
        tag.put("entityPercentages", entityTag);
        CompoundTag geneTag = new CompoundTag();
        for (Map.Entry<Genes.Gene, Double> entry : genes.entrySet()) {
            geneTag.putDouble(entry.getKey().name(), entry.getValue());
        }
        tag.put("genes", geneTag);
        return tag;
    }

    public static DinoData fromNBT(CompoundTag tag) {
        double basePercentage = tag.getDouble("basePercentage");
        Map<EntityType<?>, Double> entityPercentages = new HashMap<>();
        CompoundTag entityTag = tag.getCompound("entityPercentages");
        for (String key : entityTag.getAllKeys()) {
            entityPercentages.put(EntityType.byString(key).get(), entityTag.getDouble(key));
        }
        DinoData data = new DinoData();
        CompoundTag geneTag = tag.getCompound("genes");
        for (String key : geneTag.getAllKeys()) {
            data.genes.put(Genes.byName(key), geneTag.getDouble(key));
        }
        data.basePercentage = basePercentage;
        data.entityPercentages = entityPercentages;
        return data;
    }

    public DinoData copy() {
        DinoData data = new DinoData();
        data.genes = new HashMap<>(genes);
        data.entityPercentages = new HashMap<>(entityPercentages);
        data.basePercentage = basePercentage;
        return data;
    }
}
