package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.Dinosaurs;
import net.dumbcode.projectnublar.client.ClientRegistrationHolder;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.model.fossil.FossilModelLoader;
import net.dumbcode.projectnublar.client.model.testtube.TestTubeModelLoader;

import net.dumbcode.projectnublar.init.DinosaurInit;
import net.dumbcode.projectnublar.init.EntityInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = Constants.MODID,value= Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEventsForge {

    @SubscribeEvent
    public static void registerBakedModels(ModelEvent.RegisterGeometryLoaders event) {
         event.register("fossil", new FossilModelLoader());
         event.register("test_tube", new TestTubeModelLoader());
    }
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event){
        ClientRegistrationHolder.registerEntityRenderers();
        ClientRegistrationHolder.registerBlockEntityRenderers();
    }
    @SubscribeEvent
    public static void onFMLClient(FMLClientSetupEvent event)  {
        ClientRegistrationHolder.menuScreens();
        ClientRegistrationHolder.registerItemProperties();
        CommonClientClass.initClient();
//        try {
//            CommonClientClass.testingTextures();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
