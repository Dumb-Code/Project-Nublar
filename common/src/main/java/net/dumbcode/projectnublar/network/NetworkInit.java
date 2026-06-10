package net.dumbcode.projectnublar.network;

import commonnetwork.api.Network;
import net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorPacket;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket;

/** Registers the mod's packets. Packet ids and wire formats are frozen contracts. */
public class NetworkInit {
    public static void registerPackets() {
        Network.registerPacket(UpdateEditInfoPacket.ID, UpdateEditInfoPacket.class,
                UpdateEditInfoPacket::encode, UpdateEditInfoPacket::decode, UpdateEditInfoPacket::handle);
        Network.registerPacket(UpdateIncubatorSlotPacket.ID, UpdateIncubatorSlotPacket.class,
                UpdateIncubatorSlotPacket::encode, UpdateIncubatorSlotPacket::decode,
                UpdateIncubatorSlotPacket::handle);
        Network.registerPacket(UpdateIncubatorPacket.ID, UpdateIncubatorPacket.class,
                UpdateIncubatorPacket::encode, UpdateIncubatorPacket::decode, UpdateIncubatorPacket::handle);
    }
}
