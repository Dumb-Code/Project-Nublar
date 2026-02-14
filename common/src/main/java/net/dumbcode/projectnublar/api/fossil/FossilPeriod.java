package net.dumbcode.projectnublar.api.fossil;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record FossilPeriod(
        String configId,
        List<TimePeriods> periods
) {
    public record TimePeriods(String timePeriod,int minY, int maxY, double rarityModifier){}
}
