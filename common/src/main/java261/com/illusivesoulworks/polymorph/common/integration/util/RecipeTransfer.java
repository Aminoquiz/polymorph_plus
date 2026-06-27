/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.integration.util;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * MC 26.1 fork. RecipeHolder.id() now returns ResourceKey<Recipe<?>>; unwrap with
 * identifier() before handing the recipe id to the widget. selectRecipe also bypasses the
 * widget when it's null so JEI/REI auto-fill works even when fired from their own screens
 * (no crafting container open yet).
 */
public class RecipeTransfer {

  private static Identifier transfer = null;

  public static void enqueueTransfer(Identifier id) {
    transfer = id;
  }

  public static Identifier getTransfer() {
    return transfer;
  }

  public static void selectRecipe(RecipeHolder<?> recipe) {
    selectRecipe(recipe.id().identifier());
  }

  public static void selectRecipe(Identifier id) {
    if (id == null) {
      return;
    }
    IRecipesWidget widget = PolymorphWidgets.getInstance().getCurrentWidget();

    if (widget != null) {
      widget.selectRecipe(id);
      return;
    }
    PolymorphApi api = PolymorphApi.getInstance();
    Player player = Minecraft.getInstance().player;

    if (player != null) {
      MinecraftServer server = player.level().getServer();

      if (server != null) {
        RecipeManager rm = server.getRecipeManager();
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
        rm.byKey(key).ifPresent(recipe -> api.getPlayerRecipeData(player).selectRecipe(recipe));
      }
      api.getNetwork().sendPlayerRecipeSelectionC2S(id);
    }
  }
}
