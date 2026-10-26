package net.dumbcode.projectnublar.platform.services;

import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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

}