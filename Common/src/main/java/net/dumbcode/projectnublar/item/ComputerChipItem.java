package net.dumbcode.projectnublar.item;

import net.minecraft.world.item.Item;

public class ComputerChipItem extends Item {
    private final int maxProcessingTime;
    private final int maxSynthTime;
    private final int maxPrintTime;
    public ComputerChipItem(Properties properties, int maxProcessingTime, int maxSynthTime, int maxPrintTime) {
        super(properties);
        this.maxProcessingTime = maxProcessingTime;
        this.maxSynthTime = maxSynthTime;
        this.maxPrintTime = maxPrintTime;
    }
    public int getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public int getMaxSynthTime() {
        return maxSynthTime;
    }

    public int getMaxPrintTime() {
        return maxPrintTime;
    }
}
