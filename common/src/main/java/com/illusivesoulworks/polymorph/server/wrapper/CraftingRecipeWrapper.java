package com.illusivesoulworks.polymorph.server.wrapper;

import com.illusivesoulworks.polymorph.platform.Services;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CraftingRecipeWrapper extends RecipeWrapper {

  private final boolean shaped;

  public CraftingRecipeWrapper(RecipeHolder<?> recipe) {
    super(recipe);
    this.shaped = Services.PLATFORM.isShaped(recipe.value());
  }

  public boolean isShaped() {
    return this.shaped;
  }

  @Override
  public boolean conflicts(RecipeWrapper pOther) {
    return super.conflicts(pOther) && isSameShape((CraftingRecipeWrapper) pOther);
  }

  private boolean isSameShape(CraftingRecipeWrapper other) {

    if (this.shaped && other.isShaped()) {
      return Services.PLATFORM.isSameShape(this.getRecipe(), other.getRecipe());
    }
    return true;
  }
}
