package net.dumbcode.projectnublar;

import dev.architectury.platform.forge.EventBuses;

import net.dumbcode.projectnublar.datagen.GeneDataProvider;
import net.dumbcode.projectnublar.datagen.ModBlockStateProvider;
import net.dumbcode.projectnublar.datagen.ModItemModelProvider;
import net.dumbcode.projectnublar.datagen.ModLangProvider;
import net.dumbcode.projectnublar.datagen.ModLootTableProvider;
import net.dumbcode.projectnublar.datagen.ModRecipeProvider;
import net.dumbcode.projectnublar.datagen.ModSoundProvider;
import net.dumbcode.projectnublar.datagen.ModTagProvider;
import net.dumbcode.projectnublar.datagen.ModWorldGenProvider;
import net.dumbcode.projectnublar.registry.SensorTypesInit;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MODID)
public class ProjectNublarForge {
    
    public ProjectNublarForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ProjectNublarForge::onGatherData);
        EventBuses.registerModEventBus(Constants.MODID,bus);
        ProjectNublar.init();
        SensorTypesInit.init();
      //  ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FossilsConfig.CONFIG_SPEC,"projectnublar-fossils.toml");
    }


    public static void onGatherData(GatherDataEvent event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        boolean includeServer = event.includeServer();
        boolean includeClient = event.includeClient();
        generator.addProvider(includeServer, new ModRecipeProvider(packOutput));
        generator.addProvider(includeServer, new ModLootTableProvider(packOutput));
        generator.addProvider(includeServer, new ModSoundProvider(packOutput, existingFileHelper));
        generator.addProvider(includeServer, new ModWorldGenProvider(packOutput, event.getLookupProvider()));
        generator.addProvider(includeServer, new ModTagProvider.BlockTag(packOutput,event.getLookupProvider(), existingFileHelper));
        generator.addProvider(includeServer, new ModTagProvider.ItemTag(packOutput,event.getLookupProvider(), existingFileHelper));
        generator.addProvider(includeServer, new ModTagProvider.EntityTypeTag(packOutput,event.getLookupProvider(), existingFileHelper));
        generator.addProvider(includeClient, new ModBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(includeClient, new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(includeClient, new ModLangProvider(packOutput));
        generator.addProvider(includeServer, new GeneDataProvider(packOutput, existingFileHelper));
    }
}