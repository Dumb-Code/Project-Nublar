package net.dumbcode.projectnublar.item;

import net.minecraft.world.item.Item;

public class TankItem extends Item {
    private final int fluidAmount;
    public TankItem(Properties properties, int fluidAmount) {
        super(properties);
        this.fluidAmount = fluidAmount;
    }
    public int getFluidAmount() {
        return fluidAmount;
    }
}
