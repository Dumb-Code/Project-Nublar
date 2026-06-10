package net.dumbcode.projectnublar.api.fossil;

import java.util.List;

/** Time-period definitions (Y ranges and rarity modifiers) for fossil generation (datapack model). */
public record FossilPeriod(
        String configId,
        List<TimePeriods> periods
) {
    public record TimePeriods(String timePeriod, int minY, int maxY, double rarityModifier) {}
}
