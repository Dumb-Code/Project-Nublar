package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.fossil.FossilCollection;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.client.ClientRegistrationHolder;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.model.fossil.FossilModelLoader;
import net.dumbcode.projectnublar.client.model.testtube.TestTubeModelLoader;

import net.minecraft.client.color.block.BlockColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
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

    /**
     * Tints the fossil overlay (tint index 0 in {@code block/fossil_base}) to match the host stone,
     * so the bone reads as part of the rock.
     */
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColor byBase = (state, level, pos, tintIndex) -> switch (((FossilBlock) state.getBlock()).getBase()) {
            case SANDSTONE -> 0xE5D38C;
            case DEEPSLATE -> 0x4F4F56;
            case STONE -> 0xFFFFFF;
        };
        FossilCollection.COLLECTIONS.values().forEach(collection ->
                collection.blocks().forEach(block -> event.register(byBase, block.get())));
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
