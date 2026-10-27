package net.dumbcode.projectnublar.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The NeoForge {@link PlatformNetwork}, loaded by {@code Services.NETWORK} through
 * {@code META-INF/services}. {@code NeoForgeModEvents} registers the payloads of
 * {@link NublarPayloads}.
 *
 * <p>{@link #sendToServer} names the client-only {@code ClientPacketDistributor} inside its body
 * only, so a dedicated server never loads that class.
 */
public class NeoForgeNetwork implements PlatformNetwork {

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
