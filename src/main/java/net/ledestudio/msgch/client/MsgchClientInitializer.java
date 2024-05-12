package net.ledestudio.msgch.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class MsgchClientInitializer extends ChannelInitializer<SocketChannel> {

    private final MsgchClient client;
    private final Logger logger;

    public MsgchClientInitializer(MsgchClient client, Logger logger) {
        this.client = client;
        this.logger = logger;
    }

    @Override
    protected void initChannel(@NotNull SocketChannel ch) {
        ch.pipeline().addLast(new MsgchClientHandler(client, logger));
    }

}
