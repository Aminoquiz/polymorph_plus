package com.illusivesoulworks.polymorph.common.integration.jei;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IPolymorphCommon;
import com.illusivesoulworks.polymorph.client.recipe.RecipesWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class RecipeTransfer {

  public static void selectRecipe(ResourceLocation id) {

    if (RecipesWidget.get().isPresent()) {
      RecipesWidget.get().get().selectRecipe(id);
      return;
    }
    IPolymorphCommon commonApi = PolymorphApi.common();
    Player player = Minecraft.getInstance().player;

    if (player != null) {
      player.level().getRecipeManager().byKey(id).ifPresent(
          recipe -> commonApi.getRecipeData(player)
              .ifPresent(recipeData -> recipeData.selectRecipe(recipe)));
    }
    commonApi.getPacketDistributor().sendPlayerRecipeSelectionC2S(id);
  }
}
