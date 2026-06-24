package com.illusivesoulworks.polymorph.common.network.server;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SPacketRecipeHandshake() implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<SPacketRecipeHandshake> TYPE =
      new CustomPacketPayload.Type<>(
          ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "recipe_handshake"));

  public static final SPacketRecipeHandshake INSTANCE = new SPacketRecipeHandshake();

  public static final StreamCodec<FriendlyByteBuf, SPacketRecipeHandshake> STREAM_CODEC =
      StreamCodec.unit(INSTANCE);

  public static void handle(SPacketRecipeHandshake packet) {
    ClientPacketHandler.handle(packet);
  }

  @Nonnull
  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
