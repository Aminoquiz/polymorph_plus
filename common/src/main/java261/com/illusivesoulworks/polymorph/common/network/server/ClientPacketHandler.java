/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.network.server;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import com.illusivesoulworks.polymorph.client.RecipesWidget;
import com.illusivesoulworks.polymorph.common.integration.util.RecipeTransfer;
import com.illusivesoulworks.polymorph.mixin.core.AccessorSmithingScreen;
import java.util.HashSet;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * MC 26.1 fork. The client no longer holds a full {@code RecipeManager} — only a
 * {@code RecipeAccess} surface (property sets + stonecutter). For SP we can still reach
 * the integrated server's full RecipeManager via {@code level().getServer()}; on a
 * remote dedicated server the selected-recipe sync degrades to no-op until we lift the
 * IPlayerRecipeData API onto Identifier-only persistence (a follow-up). {@code
 * byKey} also now takes {@code ResourceKey<Recipe<?>>}, not {@code Identifier}.
 */
public class ClientPacketHandler {

  public static void handle(SPacketPlayerRecipeSync packet) {
    LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;

    if (clientPlayerEntity != null) {
      IPlayerRecipeData recipeData =
          PolymorphApi.getInstance().getPlayerRecipeData(clientPlayerEntity);

      if (recipeData != null) {
        recipeData.setRecipesList(sort(packet.recipeList().orElse(new HashSet<>())));
        packet.selected().flatMap(ClientPacketHandler::lookup)
            .ifPresent(recipeData::setSelectedRecipe);
      }
    }
  }

  private static java.util.Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> lookup(
      Identifier id) {
    LocalPlayer player = Minecraft.getInstance().player;

    if (player == null) {
      return Optional.empty();
    }
    MinecraftServer server = player.level().getServer();

    if (server == null) {
      // Remote multiplayer: no full RecipeManager on the client in 26.1.
      return Optional.empty();
    }
    RecipeManager rm = server.getRecipeManager();
    ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
    return rm.byKey(key);
  }

  public static void handle(SPacketRecipesList packet) {
    LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;

    if (clientPlayerEntity != null) {
      Optional<IRecipesWidget> maybeWidget = RecipesWidget.get();
      maybeWidget.ifPresent(
          widget -> widget.setRecipesList(sort(packet.recipeList().orElse(new HashSet<>())),
              packet.selected().orElse(null)));

      if (maybeWidget.isEmpty()) {
        RecipesWidget.enqueueRecipesList(sort(packet.recipeList().orElse(new HashSet<>())),
            packet.selected().orElse(null));
      }
    }
  }

  public static void handle(SPacketHighlightRecipe packet) {
    LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;

    if (clientPlayerEntity != null) {
      RecipesWidget.get().ifPresent(widget -> widget.highlightRecipe(packet.recipe()));
    }
  }

  private static SortedSet<IRecipePair> sort(HashSet<IRecipePair> set) {
    return new TreeSet<>(set);
  }

  public static void handle(SPacketUpdatePreview unused) {
    Minecraft mc = Minecraft.getInstance();

    if (mc.gui.screen() instanceof SmithingScreen smithingScreen) {
      ((AccessorSmithingScreen) smithingScreen).callUpdateArmorStandPreview(
          smithingScreen.getMenu().getSlot(3).getItem());
    }
  }

  public static void handle(SPacketRecipeHandshake unused) {
    IRecipesWidget widget = PolymorphWidgets.getInstance().getCurrentWidget();
    Identifier id = RecipeTransfer.getTransfer();

    if (widget != null && id != null) {
      widget.selectRecipe(id);
      RecipeTransfer.enqueueTransfer(null);
    }
  }
}
