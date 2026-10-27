package net.dumbcode.projectnublar.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import net.dumbcode.projectnublar.api.gene.GeneData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

/**
 * Loads per-entity gene data from the {@code gene_data} datapack folder. Each file name must be
 * an entity type id; note the Forge datagen provider writes these files into the {@code minecraft}
 * namespace (frozen contract).
 *
 * <p>Registered on both loaders through {@code NublarReloadListeners}.
 */
public class GeneDataReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final String FOLDER_PATH = "gene_data";

    public GeneDataReloadListener() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(FOLDER_PATH));
    }

    @Override
    protected void apply(
            Map<Identifier, JsonElement> elements,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {
        elements.forEach(GeneDataReloadListener::loadGeneData);
    }

    private static void loadGeneData(Identifier resourceLocation, JsonElement jsonElement) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(resourceLocation).orElse(null);
        if (type != null) {
            // TODO(BUG): .result().get() has no error handling; a malformed gene_data file
            // throws NoSuchElementException and aborts the whole reload.
            GeneData geneData =
                    GeneData.CODEC.decode(JsonOps.INSTANCE, jsonElement).result().get().getFirst();
            GeneData.register(type, geneData);
            geneData.genes().forEach((gene, value) -> Genes.addToGene(gene, type, value));
        }
    }
}
