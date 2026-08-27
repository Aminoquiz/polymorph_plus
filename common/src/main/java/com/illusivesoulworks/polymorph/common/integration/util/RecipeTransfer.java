package com.illusivesoulworks.polymorph.common.integration.util;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * selectRecipe bypasses the widget when it is null, so JEI/REI auto-fill works when fired from
 * their own screens with no crafting container open yet.
 */
public class RecipeTransfer {

  private static ResourceLocation transfer = null;

  public static void enqueueTransfer(ResourceLocation resourceLocation) {
    transfer = resourceLocation;
  }

  public static ResourceLocation getTransfer() {
    return transfer;
  }

  public static void selectRecipe(RecipeHolder<?> recipe) {
    selectRecipe(recipe.id());
  }

  public static void selectRecipe(ResourceLocation id) {
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
