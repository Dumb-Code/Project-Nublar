package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.ClientRegistrationHolder;
import net.dumbcode.projectnublar.client.model.fossil.FossilModelLoader;
import net.dumbcode.projectnublar.client.model.testtube.TestTubeModelLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MODID,value= Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerBakedModels(ModelEvent.RegisterGeometryLoaders event) {
         event.register("fossil", new FossilModelLoader());
         event.register("test_tube", new TestTubeModelLoader());
    }
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event){
        ClientRegistrationHolder.entityRenderers().forEach((key, value) -> event.registerEntityRenderer(key.get(), value));
        ClientRegistrationHolder.getBlockEntityRenderers().forEach((key, value) -> event.registerBlockEntityRenderer(key.get(), value));
    }
    @SubscribeEvent
    public static void onFMLClient(FMLClientSetupEvent event) {
        ClientRegistrationHolder.menuScreens();
        ClientRegistrationHolder.registerItemProperties();
    }
}
