package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.Dinosaurs;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.client.ClientRegistrationHolder;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.model.fossil.FossilModelLoader;
import net.dumbcode.projectnublar.client.model.testtube.TestTubeModelLoader;

import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.DinosaurInit;
import net.dumbcode.projectnublar.init.EntityInit;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.block.Blocks;
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
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event){
        ClientRegistrationHolder.registerEntityRenderers();
        ClientRegistrationHolder.registerBlockEntityRenderers();
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event){
        FossilCollection.getFossilOresForDinosaur(DinosaurInit.TYRANNOSAURUS_REX).forEach(block -> {
        FossilBlock fossilBlock = (FossilBlock) block.get();
            event.register( (state,level,pos,tintIndex) -> {
            if(fossilBlock.getBase().getBlock().equals(Blocks.SANDSTONE)){
                return 0xE5D38C;
            } else if(fossilBlock.getBase().getBlock().equals(Blocks.DEEPSLATE)){
                return 0x4F4F56;
            } else if(fossilBlock.getBase().getBlock().equals(Blocks.STONE)){
                return 0xFFFFFFF;
            } else return 0xFFFFFFF;
        },block.get());});
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
