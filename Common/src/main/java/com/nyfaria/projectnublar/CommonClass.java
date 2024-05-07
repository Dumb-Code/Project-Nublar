package com.nyfaria.projectnublar;

import com.nyfaria.projectnublar.api.FossilPiece;
import com.nyfaria.projectnublar.api.NublarMath;
import com.nyfaria.projectnublar.init.BlockInit;
import com.nyfaria.projectnublar.init.EntityInit;
import com.nyfaria.projectnublar.init.FeatureInit;
import com.nyfaria.projectnublar.init.ItemInit;
import com.nyfaria.projectnublar.init.LootFunctionInit;
import com.nyfaria.projectnublar.init.MenuTypeInit;
import net.minecraft.util.random.SimpleWeightedRandomList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    }

}