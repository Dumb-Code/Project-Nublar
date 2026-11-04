package net.dumbcode.projectnublar;

import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.datagen.GeneDataProvider;
import net.dumbcode.projectnublar.datagen.ModGameTestProvider;
import net.dumbcode.projectnublar.datagen.ModLangProvider;
import net.dumbcode.projectnublar.datagen.ModLootTableProvider;
import net.dumbcode.projectnublar.datagen.ModModelProvider;
import net.dumbcode.projectnublar.datagen.ModRecipeProvider;
import net.dumbcode.projectnublar.datagen.ModSoundProvider;
import net.dumbcode.projectnublar.datagen.ModTagProvider;
import net.dumbcode.projectnublar.datagen.ModWorldGenProvider;
import net.dumbcode.projectnublar.registry.SensorTypesInit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * The NeoForge entry point. FML constructs it once; the event listeners live in
 * {@code NeoForgeModEvents}, {@code NeoForgeGameEvents}, {@code NeoForgeClientEvents},
 * {@code NeoForgeClientGameEvents} and {@code NeoForgeMachineEnergy} (the energy lookup at every
 * block of a machine), which FML finds by their {@code @EventBusSubscriber} annotation. The data
 * generation listener is the one listener added here.
 */
@Mod(Constants.MODID)
public class ProjectNublarNeoForge {

    /**
     * Runs the common initialisation (Architectury's deferred registers attach to the mod bus by
     * themselves), registers SmartBrainLib's sensor types and the fossils config, and adds the data
     * generation listener.
     *
     * @param modBus    this mod's event bus; it fires {@link GatherDataEvent.Client}, so the data
     *                  generation listener is added to it
     * @param container this mod's container, which owns the config
     */
    public ProjectNublarNeoForge(IEventBus modBus, ModContainer container) {
        ProjectNublar.init();
        SensorTypesInit.init();
        container.registerConfig(ModConfig.Type.COMMON, FossilsConfig.CONFIG_SPEC, "projectnublar-fossils.toml");
        modBus.addListener(GatherDataEvent.Client.class, ProjectNublarNeoForge::gatherData);
    }

    /**
     * Adds every data provider, the client ones (models, language, sounds) and the data ones
     * (recipes, loot tables, tags, world gen, gene data, GameTest instances and environments).
     *
     * <p>There is no {@link GatherDataEvent.Server} listener: the mod's one data run ({@code data}
     * in {@code neoforge/build.gradle}, a {@code clientData()} run with {@code --all}) posts only
     * {@link GatherDataEvent.Client} (NeoForge's patched {@code net.minecraft.client.data.Main} hands
     * {@code GatherDataEvent.Client::new} to {@code DatagenModLoader}; only the {@code serverData()}
     * main posts {@code Server}). NeoForge registers its own data providers on {@code Client} the
     * same way ({@code ClientNeoForgeMod.onGatherData}: "a single clientData run").
     *
     * <p>The providers are named only inside this body, so a dedicated server never loads them.
     */
    private static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModLootTableProvider::new);
        event.createProvider(ModSoundProvider::new);
        event.createProvider(ModWorldGenProvider::new);
        event.createProvider(ModTagProvider.BlockTag::new);
        event.createProvider(ModTagProvider.ItemTag::new);
        event.createProvider(ModTagProvider.EntityTypeTag::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModLangProvider::new);
        event.createProvider(GeneDataProvider::new);
        event.createProvider(ModGameTestProvider::new);
    }
}
