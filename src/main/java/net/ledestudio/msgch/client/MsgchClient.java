package net.ledestudio.msgch.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.ledestudio.msgch.common.Msgch;
import net.ledestudio.msgch.common.Packet;
import net.ledestudio.msgch.common.PacketRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class MsgchClient {

    private static final Logger LOGGER = Logger.getLogger("msgch-client");

    private final String address;
    private final int port;
    private final int nThreads;
    private final @NotNull PacketRegistry packetRegistry;

    private boolean logPacket = false;

    private EventLoopGroup workerGroup;
    private Channel channel;

    public MsgchClient(@NotNull String address, int port, int nThreads) {
        this(address, port, nThreads, Msgch.getSingletonRegistry());
    }

    public MsgchClient(String address, int port, int nThreads, @NotNull PacketRegistry packetRegistry) {
        this.address = address;
        this.port = port;
        this.nThreads = nThreads;
        this.packetRegistry = packetRegistry;
    }

    public void runAsync() {
        Executors.newSingleThreadExecutor().execute(this::run);
    }

    public void run() {
        try {
            workerGroup = new NioEventLoopGroup(nThreads);

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new MsgchClientInitializer(this, LOGGER));

            ChannelFuture future = bootstrap.connect(address, port).sync();
            channel = future.channel();
            LOGGER.info("run msgch client.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeAsync() {
        Executors.newSingleThreadExecutor().execute(this::close);
    }

    public void close() {
        if (channel != null) {
            channel.close().awaitUninterruptibly();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully().awaitUninterruptibly();
        }
    }

    public void restartAsync() {
        Executors.newSingleThreadExecutor().execute(this::restart);
    }

    public void restart() {
        close();
        run();
    }

    public boolean isRunning() {
        return !workerGroup.isShutdown() && channel != null && channel.isActive();
    }

    public void writeAndFlush(@NotNull Packet packet) {
        if (channel != null) {
            final ByteBuf buf = Unpooled.buffer();
            final int id = packetRegistry.writePacketToByteBuf(packet, buf);

            if (isLogPacket()) {
                LOGGER.info(String.format("Packet Write | id = %d | Target = Server", id));
            }

            channel.writeAndFlush(buf);
        }
    }

    public boolean isLogPacket() {
        return logPacket;
    }

    public void setLogPacket(boolean logPacket) {
        this.logPacket = logPacket;
    }

    public @NotNull PacketRegistry getPacketRegistry() {
        return packetRegistry;
    }

}
