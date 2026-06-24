/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.server.wrapper;

import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * MC 26.1 fork. The 1.21.1 source compared via {@code Ingredient.getItems(): ItemStack[]}
 * and {@code ItemStack.matches} (data-component-aware). On 26.x ingredients no longer
 * expose generated example stacks — {@link Ingredient#items()} now returns a
 * {@code Stream<Holder<Item>>} of the matchable items, and data-component matching is
 * separate from the ingredient predicate. Two ingredients are treated as overlapping if
 * they share at least one accepted {@link Item}.
 */
public class IngredientWrapper {

  private final Ingredient ingredient;

  public IngredientWrapper(Ingredient pIngredient) {
    this.ingredient = pIngredient;
  }

  public Ingredient getIngredient() {
    return this.ingredient;
  }

  public boolean matches(IngredientWrapper pIngredient) {

    if (pIngredient == null) {
      return false;
    }
    Ingredient otherIngredient = pIngredient.getIngredient();

    if (otherIngredient == null) {
      return false;
    }
    Set<Item> mine = this.ingredient.items().map(Holder::value).collect(Collectors.toSet());

    if (mine.isEmpty()) {
      return otherIngredient.items().findAny().isEmpty();
    }
    return otherIngredient.items().map(Holder::value).anyMatch(mine::contains);
  }
}
