package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.data.GeneDataReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonForgeEvents {
    @SubscribeEvent
    public static void onReloadListeners(AddReloadListenerEvent event){
        event.addListener(new GeneDataReloadListener());
    }
}
