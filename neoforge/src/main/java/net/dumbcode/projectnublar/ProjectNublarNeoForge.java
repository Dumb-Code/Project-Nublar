package net.dumbcode.projectnublar;

import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.registry.SensorTypesInit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

/**
 * The NeoForge entry point. FML constructs it once; the event listeners live in
 * {@code NeoForgeModEvents}, {@code NeoForgeGameEvents} and {@code NeoForgeClientEvents}, which
 * FML finds by their {@code @EventBusSubscriber} annotation.
 */
@Mod(Constants.MODID)
public class ProjectNublarNeoForge {

    /**
     * Runs the common initialisation (Architectury's deferred registers attach to the mod bus by
     * themselves), registers SmartBrainLib's sensor types and the fossils config.
     *
     * @param modBus    this mod's event bus
     * @param container this mod's container, which owns the config
     */
    public ProjectNublarNeoForge(IEventBus modBus, ModContainer container) {
        ProjectNublar.init();
        SensorTypesInit.init();
        container.registerConfig(ModConfig.Type.COMMON, FossilsConfig.CONFIG_SPEC, "projectnublar-fossils.toml");
    }
}
