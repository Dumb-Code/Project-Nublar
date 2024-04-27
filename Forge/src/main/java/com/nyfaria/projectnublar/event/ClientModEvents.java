package com.nyfaria.projectnublar.event;

import com.nyfaria.projectnublar.Constants;
import com.nyfaria.projectnublar.client.model.FossilModelLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID,value= Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerBakedModels(ModelEvent.RegisterGeometryLoaders event) {
         event.register("fossil", new FossilModelLoader());
    }
}
