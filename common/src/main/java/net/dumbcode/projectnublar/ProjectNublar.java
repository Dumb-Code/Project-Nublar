package net.dumbcode.projectnublar;

import dev.architectury.registry.registries.RegistrarManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.dumbcode.projectnublar.network.NetworkInit;
import net.dumbcode.projectnublar.registry.AttributesInit;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.CreativeTabInit;
import net.dumbcode.projectnublar.registry.DataSerializerInit;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.FeatureInit;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.registry.LootFunctionInit;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.registry.MenuTypeInit;
import net.dumbcode.projectnublar.registry.RecipeInit;
import net.dumbcode.projectnublar.registry.SoundInit;
import net.minecraft.util.random.SimpleWeightedRandomList;
import org.apache.commons.lang3.StringUtils;

/**
 * Common entry point: builds the custom gene registry and triggers every deferred register.
 *
 * <p>The {@code loadClass()} call order below determines registration (and therefore class
 * initialization) order and must not be changed.
 */
public class ProjectNublar {
    /** Period name -> biome name -> weighted fossil ids, filled by the fossil config reload listener. */
    public static Map<String, Map<String, SimpleWeightedRandomList.Builder<String>>>
            WEIGHTED_PERIOD_BIOME_FOSSIL_MAP = new HashMap<>();

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

    /** Turns a registry path like {@code tyrannosaurus_rex} into display text ("Tyrannosaurus Rex"). This should be moved to a common library. */
    public static String checkReplace(String registryObject) {
        return Arrays.stream(registryObject.split("_"))
                .map(StringUtils::capitalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }
}