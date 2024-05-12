package net.ledestudio.msgch.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.ledestudio.msgch.common.ChannelIdentifier;
import net.ledestudio.msgch.common.Msgch;
import net.ledestudio.msgch.common.Packet;
import net.ledestudio.msgch.common.PacketRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class MsgchServer {

    private static final Logger LOGGER = Logger.getLogger("msgch-server");

    private final @Range(from = 0, to = 65535) int port;
    private final @Range(from = 0, to = Integer.MAX_VALUE) int nThreads;
    private final @NotNull ChannelIdentifier identifier;
    private final @NotNull PacketRegistry packetRegistry;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final MsgchChannelGroup channels = new MsgchChannelGroup();

    private boolean logPacket = false;

    public MsgchServer(int port, int nThreads, @NotNull ChannelIdentifier identifier) {
        this(port, nThreads, identifier, Msgch.getSingletonRegistry());
    }

    public MsgchServer(int port, int nThreads, @NotNull ChannelIdentifier identifier, @NotNull PacketRegistry packetRegistry) {
        this.port = port;
        this.nThreads = nThreads;
        this.identifier = identifier;
        this.packetRegistry = packetRegistry;
    }

    public void runAsync() {
        Executors.newSingleThreadExecutor().execute(this::run);
    }

    public void run() {
        try {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup(nThreads);

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MsgchServerInitializer(this, LOGGER))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            final ChannelFuture future = bootstrap.bind(port).sync();
            LOGGER.info("run msgch server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeAsync() {
        Executors.newSingleThreadExecutor().execute(this::close);
    }

    public void close() {
        try {
            channels.close();
            channels.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully().awaitUninterruptibly();
            workerGroup = null;
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully().awaitUninterruptibly();
            bossGroup = null;
        }
    }

    public boolean isRunning() {
        return !bossGroup.isShutdown() && !workerGroup.isShutdown();
    }

    void addChannel(@NotNull UUID uuid, @NotNull Channel channel) {
        channels.put(uuid, channel);
    }

    void removeChannel(@NotNull UUID uuid) {
        channels.remove(uuid);
    }

    void removeChannel(@NotNull Channel channel) {
        channels.remove(channel);
    }

    public void writeAndFlush(@NotNull Packet packet) {
        final ByteBuf buf = Unpooled.buffer();
        final int id = packetRegistry.writePacketToByteBuf(packet, buf);

        if (isLogPacket()) {
            LOGGER.info(String.format("Packet Write | id = %d | Target = ALL", id));
        }

        channels.writeAndFlush(buf);
    }

    public void writeAndFlush(@NotNull Packet packet, @NotNull UUID... targets) {
        final ByteBuf buf = Unpooled.buffer();
        final int id = packetRegistry.writePacketToByteBuf(packet, buf);

        if (isLogPacket()) {
            LOGGER.info(String.format("Packet Write | id = %d | Target = %s", id, Arrays.toString(targets)));
        }

        channels.writeAndFlush(buf, targets);
    }

    public void writeAndFlush(@NotNull Packet packet, @NotNull Collection<UUID> targets) {
        final ByteBuf buf = Unpooled.buffer();
        final int id = packetRegistry.writePacketToByteBuf(packet, buf);

        if (isLogPacket()) {
            LOGGER.info(String.format("Packet Write | id = %d | Target = %s", id, targets));
        }

        channels.writeAndFlush(buf, targets);
    }

    public void setLogPacket(boolean logPacket) {
        this.logPacket = logPacket;
    }

    public boolean isLogPacket() {
        return logPacket;
    }

    public @NotNull ChannelIdentifier getIdentifier() {
        return identifier;
    }

    public @NotNull PacketRegistry getPacketRegistry() {
        return packetRegistry;
    }

}
