package net.dumbcode.projectnublar;

import dev.architectury.registry.registries.RegistrarManager;
import net.dumbcode.projectnublar.registry.*;
import net.dumbcode.projectnublar.network.NetworkInit;
import net.minecraft.util.random.SimpleWeightedRandomList;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectNublar {
    public static Map<String, Map<String, SimpleWeightedRandomList.Builder<String>>> WEIGHTED_PERIOD_BIOME_FOSSIL_MAP = new HashMap<>();;

    public static void init() {
        RegistrarManager registrarManager = RegistrarManager.get(Constants.MODID);
        registrarManager.builder(GeneInit.GENE_KEY.location(), new GeneInit[0]).build();
        EntityInit.loadClass();
        BlockInit.loadClass();
        ItemInit.loadClass();
        LootFunctionInit.loadClass();
        FeatureInit.loadClass();
        MenuTypeInit.loadClass();
        CreativeTabInit.loadClass();
        DataSerializerInit.loadClass();
        NetworkInit.registerPackets();
        RecipeInit.loadClass();
        AttributesInit.loadClass();
        GeneInit.loadClass();
        MemoryModuleTypeInit.loadClass();
        SoundInit.loadClass();
    }
    public static String checkReplace(String registryObject) {
        return Arrays.stream(registryObject.split("_"))
                .map(StringUtils::capitalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }
}