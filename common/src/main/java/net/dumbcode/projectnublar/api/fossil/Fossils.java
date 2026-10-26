package net.dumbcode.projectnublar.api.fossil;

import net.minecraft.resources.Identifier;

import java.util.List;

public record Fossils(
        String configId,
        Identifier speciesId,
        String pieces,
        List<SpecialFossilPieces> specialFossilPieces,
        int weight,
        List<String> timePeriods,
        List<String> biomes

) {
    public record SpecialFossilPieces(String piece, int weight) {}
}
