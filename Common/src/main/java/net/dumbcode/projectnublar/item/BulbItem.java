package net.dumbcode.projectnublar.item;

import net.minecraft.world.item.Item;

public class BulbItem extends Item {
    final int ticksPerPercent;
    public BulbItem(Properties properties, int ticksPerPercent) {
        super(properties);
        this.ticksPerPercent = ticksPerPercent;
    }

    public int getTicksPerPercent() {
        return ticksPerPercent;
    }
}
