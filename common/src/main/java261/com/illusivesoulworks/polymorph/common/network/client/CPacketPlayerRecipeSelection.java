/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.network.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import com.illusivesoulworks.polymorph.common.integration.PolymorphIntegrations;
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
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

/**
 * MC 26.1 fork. {@code RecipeManager.byKey} now takes {@code ResourceKey<Recipe<?>>};
 * wrap the payload Identifier before lookup. {@code Level.getRecipeManager()} gone —
 * route through {@code ServerLevel.recipeAccess()}.
 */
public record CPacketPlayerRecipeSelection(Identifier recipe) implements CustomPacketPayload {

  public static final Type<CPacketPlayerRecipeSelection> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "player_recipe_selection"));
  public static final StreamCodec<FriendlyByteBuf, CPacketPlayerRecipeSelection> STREAM_CODEC =
      StreamCodec.composite(
          Identifier.STREAM_CODEC,
          CPacketPlayerRecipeSelection::recipe,
          CPacketPlayerRecipeSelection::new);

  public static void handle(CPacketPlayerRecipeSelection packet, ServerPlayer player) {
    AbstractContainerMenu container = player.containerMenu;
    RecipeManager rm = serverRecipeManager(player.level());

    if (rm == null) {
      return;
    }
    ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, packet.recipe);
    rm.byKey(key).ifPresent(recipe -> {
      PolymorphApi api = PolymorphApi.getInstance();
      IPlayerRecipeData recipeData = api.getPlayerRecipeData(player);

      if (recipeData != null) {
        recipeData.selectRecipe(recipe);
      }
      PolymorphIntegrations.selectRecipe(container, recipe);
      container.slotsChanged(player.getInventory());

      if (container instanceof ItemCombinerMenu) {
        ((ItemCombinerMenu) container).createResult();
        container.broadcastChanges();
        api.getNetwork().sendUpdatePreviewS2C(player);
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
