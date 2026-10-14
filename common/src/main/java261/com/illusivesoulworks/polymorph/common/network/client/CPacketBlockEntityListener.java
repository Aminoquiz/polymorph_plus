/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.network.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.common.util.BlockEntityTicker;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * MC 26.1 fork. RecipeHolder.id() returns {@code ResourceKey<Recipe<?>>} — unwrap with
 * {@code identifier()} before handing the recipe id to the network payload.
 */
public record CPacketBlockEntityListener(boolean add) implements CustomPacketPayload {

  public static final Type<CPacketBlockEntityListener> TYPE =
      new Type<>(
          Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "block_entity_listener"));
  public static final StreamCodec<FriendlyByteBuf, CPacketBlockEntityListener> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.BOOL,
          CPacketBlockEntityListener::add,
          CPacketBlockEntityListener::new);

  public static void handle(CPacketBlockEntityListener packet, ServerPlayer player) {

    if (player != null) {

      if (packet.add) {
        AbstractContainerMenu container = player.containerMenu;
        PolymorphApi api = PolymorphApi.getInstance();
        IBlockEntityRecipeData recipeData = api.getBlockEntityRecipeData(container);

        if (recipeData != null) {
          BlockEntityTicker.add(player, recipeData);
          Identifier id = null;
          RecipeHolder<?> recipeHolder = recipeData.getSelectedRecipe();

          if (recipeHolder != null) {
            id = recipeHolder.id().identifier();
          }
          api.getNetwork().sendRecipesListS2C(player,
              recipeData.isEmpty() ? new TreeSet<>() : recipeData.getRecipesList(),
              id);
        }
      } else {
        BlockEntityTicker.remove(player);
      }
    }
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
