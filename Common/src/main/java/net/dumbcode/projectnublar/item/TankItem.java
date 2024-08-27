package net.dumbcode.projectnublar.item;

import net.minecraft.world.item.Item;

public class TankItem extends Item {
    private final int fluidAmount;
    private final int synthFluid;
    private final int synthPlant;
    private final int incPlant;
    public TankItem(Properties properties, int fluidAmount, int synthFluid, int synthPlant, int incPlant) {
        super(properties);
        this.fluidAmount = fluidAmount;
        this.synthFluid = synthFluid;
        this.synthPlant = synthPlant;
        this.incPlant = incPlant;
    }
    public int getFluidAmount() {
        return fluidAmount;
    }

    public int getSynthFluid() {
        return synthFluid;
    }

    public int getSynthPlant() {
        return synthPlant;
    }

    public int getIncPlant() {
        return incPlant;
    }
}
