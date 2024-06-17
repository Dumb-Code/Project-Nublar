package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.CommonClass;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilPieces;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.data.GeneDataReloadListener;
import net.dumbcode.projectnublar.init.EntityInit;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    private final static List<String> PERIODS = List.of("carboniferous", "jurassic", "cretaceous");

    @SubscribeEvent
    public static void attribcage(EntityAttributeCreationEvent e) {
        EntityInit.attributeSuppliers.forEach(p -> e.put(p.entityTypeSupplier().get(), p.factory().get().build()));
    }
    @SubscribeEvent
    public static void onConfigLoaded(ModConfigEvent.Loading e) {

        FossilsConfig.getFossils().forEach((type, fossil) -> {
            FossilCollection collection = FossilCollection.COLLECTIONS.get(type);
            List<String> periods = fossil.getPeriods().get();
            List<String> biomes = fossil.getBiomes().get();
            SimpleWeightedRandomList.Builder<FossilPiece> blockStates = new SimpleWeightedRandomList.Builder<>();
            FossilsConfig.Set set = FossilsConfig.getSet(fossil.getPieces().get());
            for(int i = 0; i < set.pieces().get().size(); i++) {
                String piece = set.pieces().get().get(i);
                int weight = set.weights().get().get(i);
                blockStates.add(FossilPieces.getPieceByName(piece), weight);
            }
            CommonClass.WEIGHTED_FOSSIL_BLOCKS_MAP.put(type, blockStates.build());
            for (String period : periods) {
                for (String biome : biomes) {
                    if (!CommonClass.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.containsKey(period)) {
                        CommonClass.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.put(period, new HashMap<>());
                    }
                    if(!CommonClass.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).containsKey(biome)){
                        CommonClass.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).put(biome, new SimpleWeightedRandomList.Builder<>());
                    }
                    CommonClass.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).get(biome).add(type, fossil.getWeight().get());
                }
            }
        });
        boolean breakHere = true;
    }

}
