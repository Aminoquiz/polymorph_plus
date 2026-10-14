package com.shiver.polymorphlegacy.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public final class OpenViewPacket implements IMessage {
    public int windowId;
    public int viewId;

    public OpenViewPacket() {
    }

    public OpenViewPacket(int windowId, int viewId) {
        this.windowId = windowId;
        this.viewId = viewId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        viewId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeInt(viewId);
    }
}
