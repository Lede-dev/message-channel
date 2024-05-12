package net.ledestudio.msgch.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class MsgchClientHandler extends ChannelInboundHandlerAdapter {

    private final MsgchClient client;
    private final Logger logger;

    public MsgchClientHandler(MsgchClient client, Logger logger) {
        this.client = client;
        this.logger = logger;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        logger.info("channel activated.");
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        logger.info("channel inactivated.");
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        try {
            final ByteBuf buf = (ByteBuf) msg;
            final int id = client.getPacketRegistry().readPacketFromByteBuf(buf);

            if (client.isLogPacket()) {
                logger.info(String.format("Packet READ | id = %d", id));
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        client.restartAsync();
    }

}
