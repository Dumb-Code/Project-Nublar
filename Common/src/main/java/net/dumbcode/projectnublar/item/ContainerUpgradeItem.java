package net.dumbcode.projectnublar.item;

import net.minecraft.world.item.Item;

public class ContainerUpgradeItem extends Item {
    final int containerSize;
    public ContainerUpgradeItem(Properties properties, int containerSize) {
        super(properties);
        this.containerSize = containerSize;
    }

    public int getContainerSize() {
        return containerSize;
    }
}
