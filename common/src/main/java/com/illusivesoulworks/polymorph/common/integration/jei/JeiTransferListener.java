package com.illusivesoulworks.polymorph.common.integration.jei;

import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferListener;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Kept out of PolymorphJeiPlugin so old JEI builds never load IRecipeTransferListener.
 */
final class JeiTransferListener implements IRecipeTransferListener {

  static void register(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferListener(new JeiTransferListener());
  }

  @Override
  public void afterRecipeTransfer(IRecipeTransferContext<?, ?> context,
                                  RecipeTransferResult result) {
    if (result == RecipeTransferResult.SUCCESS && context.getRecipe() instanceof Recipe<?> recipe) {
      RecipeTransfer.selectRecipe(recipe.getId());
    }
  }
}
