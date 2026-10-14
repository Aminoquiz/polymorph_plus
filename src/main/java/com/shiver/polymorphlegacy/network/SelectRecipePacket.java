package com.shiver.polymorphlegacy.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public final class SelectRecipePacket implements IMessage {
    public int windowId;
    public int viewId;
    public ResourceLocation recipe;

    public SelectRecipePacket() {
    }

    public SelectRecipePacket(int windowId, int viewId, ResourceLocation recipe) {
        this.windowId = windowId;
        this.viewId = viewId;
        this.recipe = recipe;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        viewId = buf.readInt();
        recipe = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeInt(viewId);
        ByteBufUtils.writeUTF8String(buf, recipe.toString());
    }
}
