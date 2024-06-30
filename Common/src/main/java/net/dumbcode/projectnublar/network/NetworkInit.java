package net.dumbcode.projectnublar.network;

import commonnetwork.api.Network;
import net.dumbcode.projectnublar.network.c2s.UpdateEditInfoPacket;

public class NetworkInit {
    public static void registerPackets(){
        Network.registerPacket(UpdateEditInfoPacket.ID, UpdateEditInfoPacket.class,UpdateEditInfoPacket::encode, UpdateEditInfoPacket::decode, UpdateEditInfoPacket::handle);
    }
}
