package net.dumbcode.projectnublar.item;

import net.minecraft.world.item.Item;

public class DiskStorageItem extends Item {
    final int processingTime;
    public DiskStorageItem(Properties properties, int processingTime) {
        super(properties);
        this.processingTime = processingTime;
    }

    public int getProcessingTime() {
        return processingTime;
    }
}
