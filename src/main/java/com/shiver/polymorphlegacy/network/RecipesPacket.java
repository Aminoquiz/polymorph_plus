package com.shiver.polymorphlegacy.network;

import com.shiver.polymorphlegacy.api.RecipeChoice;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public final class RecipesPacket implements IMessage {
    public int windowId;
    public int viewId;
    public List<RecipeChoice> choices;
    public ResourceLocation selected;

    public RecipesPacket() {
    }

    public RecipesPacket(int windowId, int viewId, List<RecipeChoice> choices,
            @Nullable ResourceLocation selected) {
        this.windowId = windowId;
        this.viewId = viewId;
        this.choices = new ArrayList<>(choices);
        this.selected = selected;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        viewId = buf.readInt();
        int count = ByteBufUtils.readVarInt(buf, 3);
        choices = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
            choices.add(new RecipeChoice(id, ByteBufUtils.readItemStack(buf)));
        }
        selected = buf.readBoolean() ? new ResourceLocation(ByteBufUtils.readUTF8String(buf)) : null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeInt(viewId);
        ByteBufUtils.writeVarInt(buf, choices.size(), 3);
        for (RecipeChoice choice : choices) {
            ByteBufUtils.writeUTF8String(buf, choice.getId().toString());
            ByteBufUtils.writeItemStack(buf, choice.getOutput());
        }
        buf.writeBoolean(selected != null);
        if (selected != null) {
            ByteBufUtils.writeUTF8String(buf, selected.toString());
        }
    }
}
