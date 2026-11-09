package com.illusivesoulworks.polymorph.common.network.server;

import com.illusivesoulworks.polymorph.api.inworld.Option;
import com.illusivesoulworks.polymorph.client.inworld.ClientInWorldStates;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * The options shown at one index of a position. Empty options clear that index.
 */
public record SPacketInWorldState(BlockPos pos, int index, ResourceLocation key,
                                  List<Option> options, int shown) {

  public static final int MAX_OPTIONS = 64;

  public static void encode(SPacketInWorldState packet, FriendlyByteBuf buffer) {
    buffer.writeBlockPos(packet.pos);
    buffer.writeVarInt(packet.index);
    buffer.writeResourceLocation(packet.key);
    int size = Math.min(packet.options.size(), MAX_OPTIONS);
    buffer.writeVarInt(size);

    for (int i = 0; i < size; i++) {
      Option option = packet.options.get(i);
      buffer.writeResourceLocation(option.id());
      buffer.writeItem(option.icon());
    }
    buffer.writeVarInt(packet.shown);
  }

  public static SPacketInWorldState decode(FriendlyByteBuf buffer) {
    BlockPos pos = buffer.readBlockPos();
    int index = buffer.readVarInt();
    ResourceLocation key = buffer.readResourceLocation();
    int size = buffer.readVarInt();

    if (size < 0 || size > MAX_OPTIONS) {
      throw new IllegalArgumentException("Too many in-world options: " + size);
    }
    List<Option> options = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      options.add(new Option(buffer.readResourceLocation(), buffer.readItem()));
    }
    return new SPacketInWorldState(pos, index, key, options, buffer.readVarInt());
  }

  public static void handle(SPacketInWorldState packet) {
    ClientInWorldStates.apply(packet);
  }
}
