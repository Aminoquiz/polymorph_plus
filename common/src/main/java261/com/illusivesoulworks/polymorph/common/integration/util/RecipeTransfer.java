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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Forked because RecipeHolder.id() returns {@code ResourceKey<Recipe<?>>} here, so it needs
 * identifier() unwrapping before the recipe id reaches the widget. selectRecipe also bypasses
 * the widget when it is null, so JEI/REI auto-fill works when fired from their own screens with
 * no crafting container open yet.
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
      api.getPlayerRecipeData(player).chooseRecipe(id);
      api.getNetwork().sendPlayerRecipeSelectionC2S(id);
    }
  }
}
