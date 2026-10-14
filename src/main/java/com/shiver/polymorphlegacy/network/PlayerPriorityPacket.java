package com.shiver.polymorphlegacy.network;

import com.shiver.polymorphlegacy.priority.PlayerPreferences;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/** The client's favourite recipes and preferred namespaces, from its own config file. */
public final class PlayerPriorityPacket implements IMessage {
    private static final int MAX_LENGTH = 256;
    public List<String> namespaces;
    public List<String> recipes;

    public PlayerPriorityPacket() {
    }

    public PlayerPriorityPacket(List<String> namespaces, List<String> recipes) {
        this.namespaces = namespaces;
        this.recipes = recipes;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        namespaces = read(buf, PlayerPreferences.MAX_NAMESPACES);
        recipes = read(buf, PlayerPreferences.MAX_RECIPES);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        write(buf, namespaces, PlayerPreferences.MAX_NAMESPACES);
        write(buf, recipes, PlayerPreferences.MAX_RECIPES);
    }

    private static List<String> read(ByteBuf buf, int max) {
        int count = Math.min(ByteBufUtils.readVarInt(buf, 3), max);
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String value = ByteBufUtils.readUTF8String(buf);
            if (value.length() <= MAX_LENGTH) {
                list.add(value);
            }
        }
        return list;
    }

    private static void write(ByteBuf buf, List<String> list, int max) {
        int count = Math.min(list.size(), max);
        ByteBufUtils.writeVarInt(buf, count, 3);
        for (int i = 0; i < count; i++) {
            ByteBufUtils.writeUTF8String(buf, list.get(i));
        }
    }
}
