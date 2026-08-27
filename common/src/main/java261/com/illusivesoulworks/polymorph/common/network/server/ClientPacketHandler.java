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
import net.minecraft.resources.Identifier;

/**
 * MC 26.1 fork. The client holds no full {@code RecipeManager} — only a {@code RecipeAccess}
 * surface (property sets + stonecutter) — and {@code ClientLevel.getServer()} is null even in
 * single player, so a holder cannot be looked up here at all. The selection is therefore stored
 * by {@link Identifier} and matched against the candidates during resolution instead.
 */
public class ClientPacketHandler {

  public static void handle(SPacketPlayerRecipeSync packet) {
    LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;

    if (clientPlayerEntity != null) {
      IPlayerRecipeData recipeData =
          PolymorphApi.getInstance().getPlayerRecipeData(clientPlayerEntity);

      if (recipeData != null) {
        recipeData.setRecipesList(sort(packet.recipeList().orElse(new HashSet<>())));
        recipeData.setSelectedRecipeId(packet.selected().orElse(null));
      }
    }
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

    if (mc.screen instanceof SmithingScreen smithingScreen) {
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
