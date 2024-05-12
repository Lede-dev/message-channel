package net.ledestudio.msgch.common;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class PacketRegistry {

    private final Map<Integer, Class<? extends Packet>> idTypeMap = Maps.newConcurrentMap();
    private final Map<Class<? extends Packet>, Integer> typeIdMap = Maps.newConcurrentMap();
    private final Map<Class<? extends Packet>, Constructor<? extends Packet>> constructorMap = Maps.newConcurrentMap();

    PacketRegistry() {}

    public void register(@Range(from = 0, to = Integer.MAX_VALUE) int id, @NotNull Packet packet) {
        try {
            final Class<? extends Packet> clazz = packet.getClass();
            final Constructor<? extends Packet> constructor = clazz.getConstructor(ByteBuf.class);

            idTypeMap.put(id, clazz);
            typeIdMap.put(clazz, id);
            constructorMap.put(clazz, constructor);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void unregister(@Range(from = 0, to = Integer.MAX_VALUE) int id) {
        final Class<? extends Packet> type = idTypeMap.remove(id);
        if (type != null) {
            typeIdMap.remove(type);
            constructorMap.remove(type);
        }
    }

    public @Range(from = -1, to = Integer.MAX_VALUE) int getPacketId(@NotNull Class<? extends Packet> type) {
        return typeIdMap.getOrDefault(type, -1);
    }

    public @Range(from = -1, to = Integer.MAX_VALUE) int getPacketId(@NotNull Packet packet) {
        return typeIdMap.getOrDefault(packet.getClass(), -1);
    }

    public int writePacketToByteBuf(@NotNull Packet packet, @NotNull ByteBuf buf) throws IllegalArgumentException {
        final Class<? extends Packet> type = packet.getClass();
        final int id = typeIdMap.get(type);
        if (id < 0) {
            throw new IllegalArgumentException(id + " is not registered packet id.");
        }

        buf.writeInt(id);
        packet.write(buf);

        return id;
    }

    public int readPacketFromByteBuf(@NotNull ByteBuf buf) {
        // Read Packet ID
        final int id = buf.readInt();

        // Load Packet Type
        final Class<? extends Packet> type = idTypeMap.get(id);
        if (type == null) {
            throw new IllegalArgumentException(id + " is not registered packet id.");
        }

        // Load Packet Constructor
        final Constructor<? extends Packet> constructor = constructorMap.get(type);
        if (constructor == null) {
            throw new IllegalArgumentException(type + " is not registered packet type.");
        }

        // Create and Call Read Event
        try {
            final Packet packet = constructor.newInstance(buf);
            packet.onPacketRead();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return id;
    }

}
