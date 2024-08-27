package net.dumbcode.projectnublar.platform.services;

import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IPlatformHelper<T extends AbstractContainerMenu> {

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


    MenuType<SequencerMenu> registerSequenceMenu();
    MenuType<IncubatorMenu> registerIncubatorMenu();
    void openMenu(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> buf);
}