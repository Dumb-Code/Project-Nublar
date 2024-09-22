package net.dumbcode.projectnublar.data;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.dumbcode.projectnublar.api.GeneData;
import net.dumbcode.projectnublar.api.Genes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public class GeneDataReloadListener extends SimpleJsonResourceReloadListener {
    public GeneDataReloadListener() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "gene_data");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        pObject.forEach((resourceLocation, jsonElement) -> {
            EntityType<?> type = EntityType.byString(resourceLocation.toString()).orElse(null);
            if (type != null) {
                GeneData geneData = GeneData.CODEC.decode(JsonOps.INSTANCE,jsonElement).result().get().getFirst();
                GeneData.register(type,geneData);
                geneData.genes().forEach((gene, value) -> {
                    Genes.addToGene(gene, type, value);
                });
            }
        });
    }
}
