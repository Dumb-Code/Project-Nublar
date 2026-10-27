package net.dumbcode.projectnublar.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * The Fabric {@link PlatformNetwork}, loaded by {@code Services.NETWORK} through
 * {@code META-INF/services}. {@code ProjectNublarFabric} registers the payloads of
 * {@link NublarPayloads}.
 *
 * <p>{@link #sendToServer} names the client-only {@code ClientPlayNetworking} inside its body
 * only, so a dedicated server never loads that class.
 */
public class FabricNetwork implements PlatformNetwork {

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
