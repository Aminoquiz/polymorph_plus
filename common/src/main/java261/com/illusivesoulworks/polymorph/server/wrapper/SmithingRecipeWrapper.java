/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.server.wrapper;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

/**
 * MC 26.1 fork. The 1.21.1 source pulled template/base/addition via {@code Accessor*}
 * mixins on package-private fields. On 26.x those fields are exposed by PUBLIC accessors
 * ({@code templateIngredient() / baseIngredient() / additionIngredient()}), and the
 * template/addition fields are now {@code Optional<Ingredient>}. Conflict logic
 * preserved 1:1 from upstream, just routed through the new accessors.
 */
public class SmithingRecipeWrapper extends RecipeWrapper {

  public SmithingRecipeWrapper(RecipeHolder<?> pRecipe) {
    super(pRecipe);
  }

  @Override
  public boolean conflicts(RecipeWrapper pOther) {
    Components self = extract(this.getRecipe());
    Components other = extract(pOther.getRecipe());

    if (self == null || other == null) {
      return super.conflicts(pOther);
    }
    IngredientWrapper baseWrapper = new IngredientWrapper(self.base);
    IngredientWrapper otherBaseWrapper = new IngredientWrapper(other.base);
    IngredientWrapper additionWrapper = self.addition == null ? null : new IngredientWrapper(self.addition);
    IngredientWrapper otherAdditionWrapper = other.addition == null ? null : new IngredientWrapper(other.addition);
    IngredientWrapper templateWrapper = self.template == null ? null : new IngredientWrapper(self.template);
    IngredientWrapper otherTemplateWrapper = other.template == null ? null : new IngredientWrapper(other.template);

    boolean addMatch = additionWrapper == null
        ? otherAdditionWrapper == null
        : additionWrapper.matches(otherAdditionWrapper);
    boolean tplMatch = templateWrapper == null
        ? otherTemplateWrapper == null
        : templateWrapper.matches(otherTemplateWrapper);

    return super.conflicts(pOther)
        && baseWrapper.matches(otherBaseWrapper)
        & addMatch
        && tplMatch;
  }

  private static Components extract(Recipe<?> recipe) {

    if (recipe instanceof SmithingTrimRecipe trim) {
      return new Components(
          trim.templateIngredient().orElse(null),
          trim.baseIngredient(),
          trim.additionIngredient().orElse(null)
      );
    } else if (recipe instanceof SmithingTransformRecipe transform) {
      return new Components(
          transform.templateIngredient().orElse(null),
          transform.baseIngredient(),
          transform.additionIngredient().orElse(null)
      );
    }
    return null;
  }

  private record Components(Ingredient template, Ingredient base, Ingredient addition) {}
}
