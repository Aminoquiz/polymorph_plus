/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.server.wrapper;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * MC 26.1 fork. {@code Recipe.getIngredients()} is gone — ingredients now live behind
 * {@code recipe.placementInfo().ingredients()}. {@code RecipeHolder.id()} now returns
 * {@code ResourceKey<Recipe<?>>}; unwrap with {@code identifier()} to get the underlying
 * {@link Identifier} this wrapper exposes.
 */
public class RecipeWrapper {

  private final RecipeHolder<?> recipe;
  private final List<IngredientWrapper> ingredients;

  public RecipeWrapper(RecipeHolder<?> pRecipe) {
    this.recipe = pRecipe;
    this.ingredients = new ArrayList<>();

    for (Ingredient ingredient : this.recipe.value().placementInfo().ingredients()) {
      IngredientWrapper wrapped = new IngredientWrapper(ingredient);
      this.ingredients.add(wrapped);
    }
  }

  public Recipe<?> getRecipe() {
    return this.recipe.value();
  }

  public Identifier getId() {
    return this.recipe.id().identifier();
  }

  public List<IngredientWrapper> getIngredients() {
    return this.ingredients;
  }

  public boolean conflicts(RecipeWrapper pOther) {

    if (pOther == null) {
      return false;
    } else if (this.getId().equals(pOther.getId())) {
      return true;
    } else if (this.ingredients.size() != pOther.getIngredients().size()) {
      return false;
    } else {
      List<IngredientWrapper> otherIngredients = pOther.getIngredients();

      for (int i = 0; i < otherIngredients.size(); i++) {

        if (!otherIngredients.get(i).matches(this.getIngredients().get(i))) {
          return false;
        }
      }
      return true;
    }
  }
}
