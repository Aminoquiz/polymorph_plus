/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.network.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Uploads the player's preference lists so conflict resolution can consult them server-side,
 * where the recipe choice is actually made.
 *
 * <p>Two lists, both most-preferred first. {@code recipes} holds recipe ids and is what
 * shift-clicking an output writes: a craft yields one outcome, so a per-mod rule cannot settle
 * a conflict whose candidates share a mod. {@code namespaces} stays as a coarser hand-edited
 * fallback for blanket "this mod over that one" rules.
 *
 * <p>Sent once on join and again on every change. Both stay client-side files so they follow
 * the player between servers; the server only keeps an in-memory copy for the session. Bounded
 * on both ends since this is client-supplied input.
 */
public record CPacketPlayerPriority(List<String> namespaces,
                                    List<String> recipes) implements CustomPacketPayload {

  public static final int MAX_ENTRIES = 64;
  public static final int MAX_LENGTH = 64;
  public static final int MAX_RECIPES = 256;
  public static final int MAX_RECIPE_LENGTH = 256;

  public static final Type<CPacketPlayerPriority> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "player_priority"));
  public static final StreamCodec<FriendlyByteBuf, CPacketPlayerPriority> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.stringUtf8(MAX_LENGTH).apply(ByteBufCodecs.list(MAX_ENTRIES)),
          CPacketPlayerPriority::namespaces,
          ByteBufCodecs.stringUtf8(MAX_RECIPE_LENGTH).apply(ByteBufCodecs.list(MAX_RECIPES)),
          CPacketPlayerPriority::recipes,
          CPacketPlayerPriority::new);

  public static void handle(CPacketPlayerPriority packet, ServerPlayer player) {
    IPlayerRecipeData recipeData = PolymorphApi.getInstance().getPlayerRecipeData(player);

    if (recipeData != null) {
      recipeData.setPriorityNamespaces(packet.namespaces());
      recipeData.setPriorityRecipes(packet.recipes());
    }
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
