package net.ledestudio.msgch.common;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ChannelIdentifier {

    @NotNull UUID getIdentifier(@NotNull Channel channel);

}
