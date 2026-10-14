/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.network.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.common.integration.PolymorphIntegrations;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

/**
 * MC 26.1 fork. {@code RecipeManager.byKey} now takes a {@code ResourceKey<Recipe<?>>};
 * wrap the payload's {@link Identifier} before lookup. {@code Level.getRecipeManager()}
 * is gone — server-side route through {@code ServerLevel.recipeAccess()}.
 */
public record CPacketPersistentRecipeSelection(Identifier recipe) implements
    CustomPacketPayload {

  public static final Type<CPacketPersistentRecipeSelection> TYPE =
      new Type<>(Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID,
          "persistent_recipe_selection"));
  public static final StreamCodec<FriendlyByteBuf, CPacketPersistentRecipeSelection> STREAM_CODEC =
      StreamCodec.composite(
          Identifier.STREAM_CODEC,
          CPacketPersistentRecipeSelection::recipe,
          CPacketPersistentRecipeSelection::new);

  public static void handle(CPacketPersistentRecipeSelection packet, ServerPlayer player) {
    Level world = player.level();
    RecipeManager rm = serverRecipeManager(world);

    if (rm == null) {
      return;
    }
    ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, packet.recipe);
    Optional<RecipeHolder<?>> maybeRecipe = rm.byKey(key);
    maybeRecipe.ifPresent(recipe -> {
      AbstractContainerMenu container = player.containerMenu;
      IBlockEntityRecipeData recipeData =
          PolymorphApi.getInstance().getBlockEntityRecipeData(container);

      if (recipeData != null) {
        recipeData.selectRecipe(recipe);
        PolymorphIntegrations.selectRecipe(recipeData.getOwner(), container, recipe);
      }
    });
  }

  private static RecipeManager serverRecipeManager(Level level) {
    if (level instanceof ServerLevel sl) {
      return sl.recipeAccess();
    }
    MinecraftServer server = level.getServer();
    return server != null ? server.getRecipeManager() : null;
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
