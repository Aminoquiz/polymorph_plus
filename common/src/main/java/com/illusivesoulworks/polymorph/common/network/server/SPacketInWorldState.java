package com.illusivesoulworks.polymorph.common.network.server;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.Option;
import com.illusivesoulworks.polymorph.client.inworld.ClientInWorldStates;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * The options shown at one index of a position. Empty options clear that index.
 */
public record SPacketInWorldState(BlockPos pos, int index, ResourceLocation key,
                                  List<Option> options, int shown) implements CustomPacketPayload {

  public static final Type<SPacketInWorldState> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "inworld_state"));

  private static final StreamCodec<RegistryFriendlyByteBuf, Option> OPTION_CODEC =
      StreamCodec.composite(
          ResourceLocation.STREAM_CODEC,
          Option::id,
          ItemStack.OPTIONAL_STREAM_CODEC,
          Option::icon,
          Option::new);

  public static final StreamCodec<RegistryFriendlyByteBuf, SPacketInWorldState> STREAM_CODEC =
      StreamCodec.composite(
          BlockPos.STREAM_CODEC,
          SPacketInWorldState::pos,
          ByteBufCodecs.VAR_INT,
          SPacketInWorldState::index,
          ResourceLocation.STREAM_CODEC,
          SPacketInWorldState::key,
          OPTION_CODEC.apply(ByteBufCodecs.list()),
          SPacketInWorldState::options,
          ByteBufCodecs.VAR_INT,
          SPacketInWorldState::shown,
          SPacketInWorldState::new);

  public static void handle(SPacketInWorldState packet) {
    ClientInWorldStates.apply(packet);
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
