package com.nyfaria.projectnublar.item;

import net.minecraft.world.item.Item;

public class ComputerChipItem extends Item {
    private final int maxProcessingTime;
    public ComputerChipItem(Properties properties, int maxProcessingTime) {
        super(properties);
        this.maxProcessingTime = maxProcessingTime;
    }
    public int getMaxProcessingTime() {
        return maxProcessingTime;
    }
}
