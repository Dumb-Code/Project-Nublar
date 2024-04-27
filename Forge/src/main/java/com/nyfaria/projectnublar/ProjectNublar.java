package com.nyfaria.projectnublar;

import com.nyfaria.projectnublar.config.FossilsConfig;
import com.nyfaria.projectnublar.datagen.ModBlockStateProvider;
import com.nyfaria.projectnublar.datagen.ModItemModelProvider;
import com.nyfaria.projectnublar.datagen.ModLangProvider;
import com.nyfaria.projectnublar.datagen.ModLootTableProvider;
import com.nyfaria.projectnublar.datagen.ModRecipeProvider;
import com.nyfaria.projectnublar.datagen.ModSoundProvider;
import com.nyfaria.projectnublar.datagen.ModTagProvider;
import com.nyfaria.projectnublar.datagen.ModWorldGenProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Constants.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ProjectNublar {
    
    public ProjectNublar() {
        Constants.LOG.info("Hello Forge world!");
        CommonClass.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FossilsConfig.CONFIG_SPEC,"projectnublar-fossils.toml");
    }

    @SubscribeEvent
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
        generator.addProvider(includeServer, new ModTagProvider.Blocks(packOutput,event.getLookupProvider(), existingFileHelper));
        generator.addProvider(includeServer, new ModTagProvider.Items(packOutput,event.getLookupProvider(), existingFileHelper));
        generator.addProvider(includeClient, new ModBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(includeClient, new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(includeClient, new ModLangProvider(packOutput));
    }
}