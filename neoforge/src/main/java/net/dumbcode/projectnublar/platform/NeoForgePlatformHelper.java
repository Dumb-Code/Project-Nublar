package net.dumbcode.projectnublar.platform;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.dumbcode.projectnublar.platform.services.IPlatformHelper;
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
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The NeoForge {@link IPlatformHelper}, loaded by {@code Services.PLATFORM} through
 * {@code META-INF/services}.
 *
 * <p>NeoForge takes reload listeners and entity data serializers only inside its events, so this
 * class collects them while the mod initialises: {@code NeoForgeGameEvents} adds the listeners in
 * {@code AddServerReloadListenersEvent} and {@code NeoForgeModEvents} registers the serializers in
 * {@code RegisterEvent}.
 */
public class NeoForgePlatformHelper implements IPlatformHelper {

    private static final Map<Identifier, PreparableReloadListener> SERVER_RELOAD_LISTENERS = new LinkedHashMap<>();
    private static final Map<Identifier, EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = new LinkedHashMap<>();

    /** Creates the service; {@code ServiceLoader} calls it for {@code Services.PLATFORM}. */
    public NeoForgePlatformHelper() {
    }

    /**
     * The server reload listeners by id, in the order the mod registered them.
     *
     * @return a read-only view of the listeners
     */
    public static Map<Identifier, PreparableReloadListener> serverReloadListeners() {
        return Collections.unmodifiableMap(SERVER_RELOAD_LISTENERS);
    }

    /**
     * The entity data serializers by registry id, in the order the mod registered them.
     *
     * @return a read-only view of the serializers
     */
    public static Map<Identifier, EntityDataSerializer<?>> entityDataSerializers() {
        return Collections.unmodifiableMap(ENTITY_DATA_SERIALIZERS);
    }

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.isProduction();
    }

    @Override
    public MenuType<SequencerMenu> registerSequenceMenu() {
        return IMenuTypeExtension.create((id, inventory, buf) -> new SequencerMenu(id, inventory, buf.readBlockPos()));
    }

    @Override
    public MenuType<IncubatorMenu> registerIncubatorMenu() {
        return IMenuTypeExtension.create((id, inventory, buf) -> new IncubatorMenu(id, inventory, buf.readBlockPos()));
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(provider, buf -> buf.writeBlockPos(pos));
    }

    /**
     * Inserts through the block's {@code Capabilities.Energy.BLOCK} handler on {@code side}, in a
     * root transaction that commits only when {@code simulate} is false.
     */
    @Override
    public int insertEnergy(Level level, BlockPos pos, Direction side, int amount, boolean simulate) {
        EnergyHandler handler = level.getCapability(Capabilities.Energy.BLOCK, pos, side);
        if (handler == null) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int accepted = handler.insert(amount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return accepted;
        }
    }

    /** Invalidates the capabilities at {@code pos}, so the capability caches there ask again. */
    @Override
    public void invalidateCapabilities(ServerLevel level, BlockPos pos) {
        level.invalidateCapabilities(pos);
    }

    @Override
    public void registerServerReloadListener(Identifier id, PreparableReloadListener listener) {
        if (SERVER_RELOAD_LISTENERS.putIfAbsent(id, listener) != null) {
            throw new IllegalArgumentException("Duplicate reload listener id " + id);
        }
    }

    @Override
    public void registerEntityDataSerializer(Identifier id, EntityDataSerializer<?> serializer) {
        if (ENTITY_DATA_SERIALIZERS.putIfAbsent(id, serializer) != null) {
            throw new IllegalArgumentException("Duplicate entity data serializer id " + id);
        }
    }
}
