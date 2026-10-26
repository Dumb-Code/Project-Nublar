package net.dumbcode.projectnublar.item;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiskStorageItem extends Item {
    final int processingTime;

    public DiskStorageItem(Properties properties, int processingTime) {
        super(properties);
        this.processingTime = processingTime;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public static double getGeneCompletion(Genes.Gene gene, ItemStack stack) {
        double totalPercent = 0;
        // The drive keeps one DNAData compound per storage key at the root of its custom data
        // (temporary, until Task 16).
        CompoundTag storage = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        for (String key : storage.keySet()) {
            DNAData data = DNAData.loadFromNBT(storage.getCompoundOrEmpty(key));
            if (Genes.GENE_STORAGE.get(gene).stream().map(Pair::getFirst).toList().contains(data.getEntityType())) {
                totalPercent += data.getDnaPercentage();
            }
        }
        if(gene == GeneInit.COLOR.get()){
            Set<DyeColor> blah = new HashSet<>();
            storage.keySet().forEach(key -> {
                DNAData data = DNAData.loadFromNBT(storage.getCompoundOrEmpty(key));
                blah.add(data.gettFish1());
                blah.add(data.gettFish2());
            });
            return blah.size()/ 16.0;
        }
        return totalPercent / Genes.GENE_STORAGE.get(gene).size();
    }
}
