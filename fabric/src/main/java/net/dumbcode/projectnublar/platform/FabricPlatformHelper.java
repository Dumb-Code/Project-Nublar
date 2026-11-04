package net.dumbcode.projectnublar.platform;

import net.dumbcode.projectnublar.energy.TeamRebornEnergySwitch;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.dumbcode.projectnublar.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;

/**
 * The Fabric {@link IPlatformHelper}, loaded by {@code Services.PLATFORM} through
 * {@code META-INF/services}. Fabric API takes reload listeners and entity data serializers at any
 * time while the mod initialises, so this class hands them over at once.
 */
public class FabricPlatformHelper implements IPlatformHelper {

    /** Creates the service; {@code ServiceLoader} calls it for {@code Services.PLATFORM}. */
    public FabricPlatformHelper() {
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public MenuType<SequencerMenu> registerSequenceMenu() {
        return new ExtendedMenuType<>((id, inventory, pos) -> new SequencerMenu(id, inventory, pos), BlockPos.STREAM_CODEC);
    }

    @Override
    public MenuType<IncubatorMenu> registerIncubatorMenu() {
        return new ExtendedMenuType<>((id, inventory, pos) -> new IncubatorMenu(id, inventory, pos), BlockPos.STREAM_CODEC);
    }

    /**
     * Opens the menu through Fabric's extended menu provider, which sends {@code pos} with the
     * open-screen packet; the name and the server menu come from {@code provider}.
     */
    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(new ExtendedMenuProvider<BlockPos>() {
            @Override
            public BlockPos getScreenOpeningData(ServerPlayer opener) {
                return pos;
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return provider.createMenu(containerId, inventory, menuPlayer);
            }
        });
    }

    /**
     * Inserts through the block's Team Reborn Energy {@code EnergyStorage.SIDED} storage on
     * {@code side}, in an outer transaction that commits only when {@code simulate} is false.
     * Returns 0 when {@code compat.team_reborn_energy_bridge} was false at game start (the value
     * {@link TeamRebornEnergySwitch} read): the bridge is off in both directions.
     */
    @Override
    public int insertEnergy(Level level, BlockPos pos, Direction side, int amount, boolean simulate) {
        if (!TeamRebornEnergySwitch.bridgeEnabled()) {
            return 0;
        }
        EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, side);
        if (storage == null) {
            return 0;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long accepted = storage.insert(amount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return (int) accepted;
        }
    }

    /**
     * Does nothing. Fabric API's {@code BlockApiCache} reads the block state at each lookup and
     * asks the block's provider again when the state changed, so no cache keeps a stale answer.
     */
    @Override
    public void invalidateCapabilities(ServerLevel level, BlockPos pos) {
    }

    /**
     * Registers the listener with Fabric API's server data resource loader at once. Fabric runs
     * it after the vanilla listeners and orders the mod listeners by id (see
     * {@link IPlatformHelper#registerServerReloadListener}).
     */
    @Override
    public void registerServerReloadListener(Identifier id, PreparableReloadListener listener) {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(id, listener);
    }

    /**
     * Registers the serializer in Fabric API's entity data registry; Fabric API refuses vanilla's
     * {@code EntityDataSerializers.registerSerializer} for mod serializers in development.
     */
    @Override
    public void registerEntityDataSerializer(Identifier id, EntityDataSerializer<?> serializer) {
        FabricEntityDataRegistry.register(id, serializer);
    }
}
