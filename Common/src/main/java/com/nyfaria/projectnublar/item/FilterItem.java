package com.nyfaria.projectnublar.item;

import net.minecraft.world.item.Item;

public class FilterItem extends Item {
    private final double efficiency;
    public FilterItem(Properties properties, double efficiency) {
        super(properties);
        this.efficiency = efficiency;
    }

    public double getEfficiency() {
        return efficiency;
    }
}
