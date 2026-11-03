package net.dumbcode.projectnublar.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sends the mod's payloads through the loader's networking. Loaded by
 * {@code Services.NETWORK}; each loader module provides one implementation and registers the
 * payloads of {@link NublarPayloads}. Common code sends through this service and never calls a
 * loader networking API directly.
 */
public interface PlatformNetwork {

    /**
     * Sends {@code payload} from the client to the server. Client only: call it from client code
     * (screens, client-side menus); on a dedicated server the loader's client classes are absent.
     *
     * @param payload a payload of {@link NublarPayloads#SERVERBOUND}
     */
    void sendToServer(CustomPacketPayload payload);

    /**
     * Sends {@code payload} from the server to one player. No payload uses it today; it is here
     * for the planned incubator sync packet.
     *
     * @param player  the player who receives it
     * @param payload the payload to send
     */
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
}
