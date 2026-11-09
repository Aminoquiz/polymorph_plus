package com.illusivesoulworks.polymorph.common.network.client;

import com.illusivesoulworks.polymorph.common.network.InWorldNetwork;
import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.inworld.InWorldServer;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record CPacketInWorldCycle(BlockPos pos, int index) implements CustomPacketPayload {

  public static final Type<CPacketInWorldCycle> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "inworld_cycle_" + InWorldNetwork.FORMAT));
  public static final StreamCodec<FriendlyByteBuf, CPacketInWorldCycle> STREAM_CODEC =
      StreamCodec.composite(
          BlockPos.STREAM_CODEC,
          CPacketInWorldCycle::pos,
          ByteBufCodecs.VAR_INT,
          CPacketInWorldCycle::index,
          CPacketInWorldCycle::new);

  public static void handle(CPacketInWorldCycle packet, ServerPlayer player) {

    if (player != null) {
      InWorldServer.cycle(player, packet.pos, packet.index);
    }
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
