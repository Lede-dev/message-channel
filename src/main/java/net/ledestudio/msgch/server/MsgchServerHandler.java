package net.ledestudio.msgch.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class MsgchServerHandler extends ChannelInboundHandlerAdapter {

    private final MsgchServer server;
    private final Logger logger;

    public MsgchServerHandler(MsgchServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        server.addChannel(server.getIdentifier().getIdentifier(channel), channel);
        logger.info("new channel activated. address = " + channel.remoteAddress().toString());
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        server.removeChannel(channel);
        logger.info("channel inactivated. address = " + channel.remoteAddress().toString());
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        try {
            final ByteBuf buf = (ByteBuf) msg;
            final int id = server.getPacketRegistry().readPacketFromByteBuf(buf);

            if (server.isLogPacket()) {
                logger.info(String.format("Packet READ | id = %d", id));
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
