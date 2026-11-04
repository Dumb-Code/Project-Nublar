package net.dumbcode.projectnublar.platform.services;

import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

/** The loader-specific services that common code calls through {@code Services.PLATFORM}. */
public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }


    /** Creates the sequencer menu type; its client factory reads the machine's {@link BlockPos}. */
    MenuType<SequencerMenu> registerSequenceMenu();

    /** Creates the incubator menu type; its client factory reads the machine's {@link BlockPos}. */
    MenuType<IncubatorMenu> registerIncubatorMenu();

    /**
     * Opens a menu for the player and sends {@code pos} to the client, where the sequencer or
     * incubator menu type's client factory reads it.
     */
    void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos);

    /**
     * Offers energy to a block of another mod through the loader's energy lookup. The generator
     * calls this only for a neighbour that is not a {@code BalmEnergyStorageProvider}; Nublar
     * blocks are filled directly in common code.
     *
     * <p>NeoForge reads {@code Capabilities.Energy.BLOCK}. Fabric reads Team Reborn Energy's
     * {@code EnergyStorage.SIDED} and returns 0 when {@code compat.team_reborn_energy_bridge} is
     * false or Team Reborn Energy is absent.
     *
     * @param level    the level of the receiving block
     * @param pos      the position of the receiving block
     * @param side     the face of the receiving block that the energy enters
     * @param amount   the most energy to offer
     * @param simulate true to only compute the result, false to move the energy
     * @return the energy that the block accepted, 0 when it has no energy storage on that side
     */
    int insertEnergy(Level level, BlockPos pos, Direction side, int amount, boolean simulate);

    /**
     * Tells the loader that what a block offers to other mods (energy, items, fluids) may have
     * changed at {@code pos}, so their cached lookups ask again. A machine multiblock calls it for
     * each of its blocks when that block is placed or removed: every block of a machine answers a
     * lookup for the core, but only the core has a block entity, and a loader refreshes on its own
     * only where a block entity comes or goes.
     *
     * <p>NeoForge calls {@code level.invalidateCapabilities(pos)}, which its
     * {@code BlockCapabilityCache} needs. Fabric does nothing: Fabric API's block lookup cache
     * reads the block at each lookup.
     *
     * @param level the server level of the block
     * @param pos   the position of the block that was placed or removed
     */
    void invalidateCapabilities(ServerLevel level, BlockPos pos);

    /**
     * Adds a server data reload listener. It runs on every data pack load and reload, after the
     * vanilla listeners. The order among the mod's own listeners is not fixed: NeoForge keeps the
     * order of the calls, Fabric API sorts them by id. So a listener must not depend on another
     * one. Call it once per listener while the mod initialises.
     *
     * @param id       the listener's id, unique among all reload listeners
     * @param listener the listener; one instance serves every reload, so it keeps no state of its own
     */
    void registerServerReloadListener(Identifier id, PreparableReloadListener listener);

    /**
     * Registers an entity data serializer so that client and server agree on its network id.
     * Both loaders refuse mod serializers in vanilla's {@code EntityDataSerializers} list and key
     * them by id instead: NeoForge in its synced {@code entity_data_serializers} registry, Fabric
     * API in {@code FabricEntityDataRegistry}. Call it once per serializer while the mod
     * initialises.
     *
     * @param id         the serializer's id
     * @param serializer the serializer
     */
    void registerEntityDataSerializer(Identifier id, EntityDataSerializer<?> serializer);

}