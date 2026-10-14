package com.illusivesoulworks.polymorph.common.capability;

import com.illusivesoulworks.polymorph.mixin.core.AccessorCrafterMenu;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;

public class CrafterRecipeData extends AbstractBlockEntityRecipeData<CrafterBlockEntity> {

  public CrafterRecipeData(CrafterBlockEntity owner) {
    super(owner);
  }

  @Override
  public void selectRecipe(@Nonnull RecipeHolder<?> recipe) {
    super.selectRecipe(recipe);

    for (ServerPlayer listeningPlayer : this.getListeners()) {

      if (listeningPlayer.containerMenu instanceof CrafterMenu crafterMenu) {
        ((AccessorCrafterMenu) crafterMenu).callRefreshRecipeResult();
      }
    }
  }

  @Override
  protected NonNullList<ItemStack> getInput() {
    return this.getOwner().getItems();
  }
}
