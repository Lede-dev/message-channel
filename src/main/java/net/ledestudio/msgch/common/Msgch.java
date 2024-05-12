package net.ledestudio.msgch.common;

import net.ledestudio.msgch.client.MsgchClient;
import net.ledestudio.msgch.server.MsgchServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class Msgch {

    private static final class PacketRegistryHolder {
        private static final PacketRegistry REGISTRY = new PacketRegistry();
    }

    public static @NotNull PacketRegistry getSingletonRegistry() {
        return PacketRegistryHolder.REGISTRY;
    }

    public static @NotNull PacketRegistry newPacketRegistry() {
        return new PacketRegistry();
    }

    public static MsgchClient newClient(@NotNull String address, @Range(from = 0, to = 65535) int port) {
        return new MsgchClient(address, port, 1);
    }

    public static MsgchServer newServer(@Range(from = 0, to = 65535) int port, @NotNull ChannelIdentifier identifier) {
        return new MsgchServer(port, 4, identifier);
    }

}
