package net.dumbcode.projectnublar.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DinoData {
    public double basePercentage;
    public double incubationProgress = -1;
    public int incubationTimeLeft = -1;
    private EntityType<?> baseDino = null;
    private Map<EntityInfo,Double> entityPercentages = new HashMap<>();
    private Map<Genes.Gene, Double> advancedGenes = new HashMap<>();
    private Map<Genes.Gene, Double> finalGenes = new HashMap<>();
    private List<DyeColor> layerColors = List.of(
            DyeColor.GREEN,
            DyeColor.GREEN
    );
    private ResourceLocation textureLocation = null;

    public DinoData() {
    }

    public static DinoData fromStack(ItemStack stack) {
        return fromNBT(stack.getTag().getCompound("DinoData"));
    }

    public Map<EntityInfo, Double> getEntityPercentages() {
        return entityPercentages;
    }

    public ResourceLocation getTextureLocation() {
        if (textureLocation == null) {
            String colorString = layerColors.stream().map(DyeColor::getName).reduce((s1, s2) -> s1 + "_" + s2).orElse("green");
            textureLocation = Constants.modLoc("textures/entity/dinosaur/" + colorString + ".png");
        }
        return textureLocation;
    }

    public void addEntity(EntityInfo type, double percentage) {
        entityPercentages.put(type, percentage);
    }

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
        if(!advancedGenes.containsKey(gene)){
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

    public void createToolTip(List<Component> components) {
//        components.add(baseDino.getDescription());
        if(incubationProgress != -1){
            components.add(Component.literal(("Incubation Progress: " + (int)NublarMath.round(incubationProgress * 100,0)) + "%"));
        }
        if(incubationTimeLeft != -1){
            components.add(Component.literal(StringUtil.formatTickDuration(incubationTimeLeft)));
        }
        if (finalGenes.isEmpty()) {
            finalizeGenes();
        }
        finalGenes.forEach((gene, value) -> components.add(gene.getTooltip(value)));
    }

    public void finalizeGenes() {
        finalGenes.clear();
        for (Genes.Gene gene : Genes.GENE_STORAGE) {
            double value = getFinalGeneValue(gene);
            if (value != 0) {
                finalGenes.put(gene, value);
            }
        }
    }

    public void setBaseDino(EntityType<?> baseDino) {
        this.baseDino = baseDino;
    }

    public double getGeneValue(Genes.Gene gene) {
        if (advancedGenes.containsKey(gene)) {
            return advancedGenes.get(gene);
        }
        double value = 0;
        for (Map.Entry<EntityInfo, Double> entry : entityPercentages.entrySet()) {
            if (GeneData.getData(entry.getKey().type) != null) {
                if (GeneData.getData(entry.getKey().type).genes().containsKey(gene)) {
                    value += GeneData.getData(entry.getKey().type).genes().get(gene) * (entry.getValue() * 2);
                }
            }
        }

        return value;
    }

    public double getFinalGeneValue(Genes.Gene gene) {
        if (finalGenes.containsKey(gene)) {
            return finalGenes.get(gene);
        }
        if (advancedGenes.containsKey(gene)) {
            finalGenes.put(gene, advancedGenes.get(gene));
            return finalGenes.get(gene);
        }
        double value = 0;
        for (Map.Entry<EntityInfo, Double> entry : entityPercentages.entrySet()) {
            if (GeneData.getData(entry.getKey().type) != null) {
                if (GeneData.getData(entry.getKey().type).genes().containsKey(gene)) {
                    value += GeneData.getData(entry.getKey().type).genes().get(gene) * (entry.getValue() * 2);
                }
            }
        }
        return value;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("basePercentage", basePercentage);
        CompoundTag entityTag = new CompoundTag();
        int i = 0;
        for (Map.Entry<EntityInfo, Double> entry : entityPercentages.entrySet()) {
            CompoundTag entityInfo = new CompoundTag();
            entityInfo.putString("type", BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey().type).toString());
            if (entry.getKey().variant != null) {
                entityInfo.putString("variant", entry.getKey().variant);
            }
            entityInfo.putDouble("percentage", entry.getValue());
            entityTag.put("entity_"+i, entityInfo);
            i++;
        }
        tag.put("entityPercentages", entityTag);
        CompoundTag geneTag = new CompoundTag();
        for (Map.Entry<Genes.Gene, Double> entry : advancedGenes.entrySet()) {
            geneTag.putDouble(entry.getKey().name(), entry.getValue());
        }
        tag.put("genes", geneTag);
        tag.putString("baseDino", BuiltInRegistries.ENTITY_TYPE.getKey(baseDino).toString());
        tag.putDouble("incubationProgress", incubationProgress);
        tag.putInt("incubationTimeLeft", incubationTimeLeft);
        return tag;
    }

    public static DinoData fromNBT(CompoundTag tag) {
        double basePercentage = tag.getDouble("basePercentage");
        Map<EntityInfo, Double> entityPercentages = new HashMap<>();
        CompoundTag entityTag = tag.getCompound("entityPercentages");
        for (String key : entityTag.getAllKeys()) {
            CompoundTag entityInfo = entityTag.getCompound(key);
            EntityType<?> type = EntityType.byString(entityInfo.getString("type")).get();
            String variant = entityInfo.contains("variant") ? entityInfo.getString("variant") : null;
            double percentage = entityInfo.getDouble("percentage");
            entityPercentages.put(new EntityInfo(type, variant), percentage);
        }
        DinoData data = new DinoData();
        CompoundTag geneTag = tag.getCompound("genes");
        for (String key : geneTag.getAllKeys()) {
            data.advancedGenes.put(Genes.byName(key), geneTag.getDouble(key));
        }
        data.basePercentage = basePercentage;
        data.entityPercentages = entityPercentages;
        if (tag.contains("baseDino"))
            data.baseDino = EntityType.byString(tag.getString("baseDino")).get();
        if (tag.contains("incubationProgress"))
            data.incubationProgress = tag.getDouble("incubationProgress");
        if(tag.contains("incubationTimeLeft"))
            data.incubationTimeLeft = tag.getInt("incubationTimeLeft");
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
        if (baseDino == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.getKey(baseDino).getNamespace();
    }

    public String getPath() {
        if (baseDino == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.getKey(baseDino).getPath();
    }

    public MutableComponent getFormattedType() {
        return MutableComponent.create(baseDino.getDescription().getContents());
    }

    public void setIncubationProgress(double i) {
        incubationProgress = i;
    }

    public void toStack(ItemStack stack) {
        stack.getOrCreateTag().put("DinoData", toNBT());

    }

    public double getIncubationProgress() {
        return incubationProgress;
    }

    public void setIncubationTimeLeft(int v) {
        incubationTimeLeft = v;
    }

    public record EntityInfo(EntityType<?> type, @Nullable String variant){
        public static Codec<EntityInfo> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(EntityInfo::type),
                        Codec.STRING.optionalFieldOf("variant", null).forGetter(EntityInfo::variant)
                ).apply(instance, EntityInfo::new)
        );

    }
}
