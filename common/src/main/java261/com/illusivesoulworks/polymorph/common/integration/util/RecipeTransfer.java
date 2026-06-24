/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.integration.util;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * MC 26.1 fork. RecipeHolder.id() now returns ResourceKey<Recipe<?>>; unwrap with
 * identifier() before handing the recipe id to the widget.
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
    IRecipesWidget widget = PolymorphWidgets.getInstance().getCurrentWidget();

    if (widget != null) {
      widget.selectRecipe(id);
    }
  }
}
