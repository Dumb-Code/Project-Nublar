package net.dumbcode.projectnublar.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import net.dumbcode.projectnublar.api.gene.GeneData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

/**
 * Loads per-entity gene data from the {@code gene_data} datapack folder. Each file name must be
 * an entity type id; note the Forge datagen provider writes these files into the {@code minecraft}
 * namespace (frozen contract).
 *
 * <p>Registered only on Forge; Fabric never receives this data?
 */
public class GeneDataReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String FOLDER_PATH = "gene_data";

    public GeneDataReloadListener() {
        super(GSON, FOLDER_PATH);
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> elements,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {
        elements.forEach(GeneDataReloadListener::loadGeneData);
    }

    private static void loadGeneData(ResourceLocation resourceLocation, JsonElement jsonElement) {
        EntityType<?> type = EntityType.byString(resourceLocation.toString()).orElse(null);
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
