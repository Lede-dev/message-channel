package net.ledestudio.msgch.common;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public abstract class Packet {

    public abstract void write(@NotNull ByteBuf buf);

    public abstract void onPacketRead();

}
