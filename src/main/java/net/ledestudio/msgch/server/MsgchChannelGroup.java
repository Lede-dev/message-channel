package net.ledestudio.msgch.server;

import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MsgchChannelGroup extends ConcurrentHashMap<UUID, Channel> {

    public void close() throws IOException {
        for (Channel channel : values()) {
            channel.close();
        }
    }

    public void remove(@Nullable Channel channel) {
        for (Channel value : Sets.newHashSet(values())) {
            if (value.equals(channel)) {
                remove(value);
            }
        }
    }

    public void removeInactive() {
        for (Channel value : Sets.newHashSet(values())) {
            if (!value.isActive()) {
                remove(value);
            }
        }
    }

    public void writeAndFlush(@NotNull ByteBuf buf) {
        values().forEach(channel -> channel.writeAndFlush(buf));
    }

    public void writeAndFlush(@NotNull ByteBuf buf, @NotNull UUID... uuids) {
        for (UUID uuid : uuids) {
            final Channel channel = get(uuid);
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(buf);
            }
        }
    }

    public void writeAndFlush(@NotNull ByteBuf buf, @NotNull Collection<UUID> uuids) {
        uuids.forEach(uuid -> {
            final Channel channel = get(uuid);
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(buf);
            }
        });
    }

}
