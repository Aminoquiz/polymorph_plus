package com.illusivesoulworks.polymorph.mixin.integration.jei;

import com.illusivesoulworks.polymorph.common.integration.util.RecipeTransfer;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.transfer.RecipeTransferUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeTransferUtil.class)
public class MixinRecipeTransferUtil {

  @Inject(
      at = @At("HEAD"),
      method = "transferRecipe(Lmezz/jei/api/recipe/transfer/IRecipeTransferManager;Lnet/minecraft/world/inventory/AbstractContainerMenu;Lmezz/jei/api/gui/IRecipeLayoutDrawable;Lnet/minecraft/world/entity/player/Player;Z)Z"
  )
  private static void polymorph$transferRecipe(IRecipeTransferManager recipeTransferManager,
                                               AbstractContainerMenu containerMenu,
                                               IRecipeLayoutDrawable<?> recipeLayout, Player player,
                                               boolean maxTransfer,
                                               CallbackInfoReturnable<Boolean> cir) {

    if (recipeLayout.getRecipe() instanceof RecipeHolder<?> recipeHolder) {
      RecipeTransfer.enqueueTransfer(recipeHolder.id());
    }
  }
}
