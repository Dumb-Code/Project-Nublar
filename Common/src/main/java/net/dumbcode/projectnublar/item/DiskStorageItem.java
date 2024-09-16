package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.Genes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DiskStorageItem extends Item {
    final int processingTime;
    public DiskStorageItem(Properties properties, int processingTime) {
        super(properties);
        this.processingTime = processingTime;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public static double getGeneCompletion(Genes.Gene gene, ItemStack stack){
        double totalPercent = 0;
        for (String key : stack.getTag().getAllKeys()) {
            DNAData data = DNAData.loadFromNBT(stack.getTag().getCompound(key));
            if (gene.entities().containsKey(data.getEntityType())) {
                totalPercent += data.getDnaPercentage();
            }
        }
        return totalPercent / gene.entities().size();
    }
}
