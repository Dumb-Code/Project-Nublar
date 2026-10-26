package net.dumbcode.projectnublar.platform.services;

import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.MenuType;

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

}