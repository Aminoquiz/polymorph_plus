package com.illusivesoulworks.polymorph.common.network.client;

import com.illusivesoulworks.polymorph.common.inworld.InWorldServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record CPacketInWorldCycle(BlockPos pos, int index) {

  public static void encode(CPacketInWorldCycle packet, FriendlyByteBuf buffer) {
    buffer.writeBlockPos(packet.pos);
    buffer.writeVarInt(packet.index);
  }

  public static CPacketInWorldCycle decode(FriendlyByteBuf buffer) {
    return new CPacketInWorldCycle(buffer.readBlockPos(), buffer.readVarInt());
  }

  public static void handle(CPacketInWorldCycle packet, ServerPlayer player) {

    if (player != null) {
      InWorldServer.cycle(player, packet.pos, packet.index);
    }
  }
}
