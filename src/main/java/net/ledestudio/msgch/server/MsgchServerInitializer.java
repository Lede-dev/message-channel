package net.ledestudio.msgch.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class MsgchServerInitializer extends ChannelInitializer<SocketChannel> {

    private final MsgchServer server;
    private final Logger logger;

    public MsgchServerInitializer(MsgchServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    protected void initChannel(@NotNull SocketChannel ch) {
        ch.pipeline().addLast(new MsgchServerHandler(server, logger));
    }

}
