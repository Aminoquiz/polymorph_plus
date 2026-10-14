package com.illusivesoulworks.polymorph.server.wrapper;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeWrapper {

  private final RecipeHolder<?> recipe;
  private final List<IngredientWrapper> ingredients;

  public RecipeWrapper(RecipeHolder<?> pRecipe) {
    this.recipe = pRecipe;
    this.ingredients = new ArrayList<>();

    for (Ingredient ingredient : this.recipe.value().getIngredients()) {
      IngredientWrapper wrapped = new IngredientWrapper(ingredient);
      this.ingredients.add(wrapped);
    }
  }

  public Recipe<?> getRecipe() {
    return this.recipe.value();
  }

  public ResourceLocation getId() {
    return this.recipe.id();
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
