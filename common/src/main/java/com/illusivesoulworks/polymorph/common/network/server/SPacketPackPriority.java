/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.network.server;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Pushes the datapack-shipped source-priority list to the client on join and on every
 * {@code /reload}.
 *
 * <p>The list is server-authoritative, but resolution is not exclusively server-side: AE2's
 * pattern encoding terminal computes its crafting output on the client, so a client that does
 * not know the pack order would preview a different recipe from the one the server encodes.
 * Sending it keeps both sides on the same ranking. An empty list is meaningful and must still
 * be sent, since it is how a client forgets the previous world's list.
 */
public record SPacketPackPriority(List<String> namespaces) implements CustomPacketPayload {

  public static final int MAX_ENTRIES = 256;
  public static final int MAX_LENGTH = 64;

  public static final Type<SPacketPackPriority> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "pack_priority"));
  public static final StreamCodec<FriendlyByteBuf, SPacketPackPriority> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.stringUtf8(MAX_LENGTH).apply(ByteBufCodecs.list(MAX_ENTRIES)),
          SPacketPackPriority::namespaces,
          SPacketPackPriority::new);

  public static void handle(SPacketPackPriority packet) {
    RecipePriority.setPackNamespaces(packet.namespaces());
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
