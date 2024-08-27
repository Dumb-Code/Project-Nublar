package net.dumbcode.projectnublar.item;

import net.minecraft.world.item.Item;

public class PlantTankItem extends Item {
    final int maxPlantMatter;
    public PlantTankItem(Properties properties, int maxPlantMatter) {
        super(properties);
        this.maxPlantMatter = maxPlantMatter;
    }

    public int getMaxPlantMatter() {
        return maxPlantMatter;
    }
}
