package net.dumbcode.projectnublar;

import dev.architectury.registry.registries.RegistrarManager;
import net.dumbcode.projectnublar.init.*;
import net.dumbcode.projectnublar.network.NetworkInit;
import net.dumbcode.projectnublar.worldgen.placement.ModifierTypes;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.config.RegistryBuilder;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ProjectNublar {
    public static void init() {
        RegistrarManager registrarManager = RegistrarManager.get(Constants.MODID);
        registrarManager.builder(GeneInit.GENE_KEY.location(), new GeneInit[0]).build();
        EntityInit.loadClass();
        ModifierTypes.loadClass();
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