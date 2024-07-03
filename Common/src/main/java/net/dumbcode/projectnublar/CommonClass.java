package net.dumbcode.projectnublar;

import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.CreativeTabInit;
import net.dumbcode.projectnublar.init.DataSerializerInit;
import net.dumbcode.projectnublar.init.EntityInit;
import net.dumbcode.projectnublar.init.FeatureInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.init.LootFunctionInit;
import net.dumbcode.projectnublar.init.MenuTypeInit;
import net.dumbcode.projectnublar.network.NetworkInit;
import net.minecraft.util.random.SimpleWeightedRandomList;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CommonClass {
    public static Map<String, Map<String, SimpleWeightedRandomList.Builder<String>>> WEIGHTED_PERIOD_BIOME_FOSSIL_MAP = new HashMap<>();
    public static Map<String, SimpleWeightedRandomList<FossilPiece>> WEIGHTED_FOSSIL_BLOCKS_MAP = new HashMap<>();

    public static void init() {

        ItemInit.loadClass();
        BlockInit.loadClass();
        EntityInit.loadClass();
        LootFunctionInit.loadClass();
        FeatureInit.loadClass();
        MenuTypeInit.loadClass();
        CreativeTabInit.loadClass();
        DataSerializerInit.loadClass();
        NetworkInit.registerPackets();
    }
    public static String checkReplace(String registryObject) {
        return Arrays.stream(registryObject.split("_"))
                .map(StringUtils::capitalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }
}