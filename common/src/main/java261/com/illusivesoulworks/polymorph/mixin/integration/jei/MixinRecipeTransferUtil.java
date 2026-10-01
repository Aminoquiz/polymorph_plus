/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * MC 26.1 fork. RecipeHolder.id() now returns ResourceKey<Recipe<?>>; unwrap with
 * .location() to get the Identifier we hand to RecipeTransfer.
 */
package com.illusivesoulworks.polymorph.mixin.integration.jei;

import com.illusivesoulworks.polymorph.common.integration.util.RecipeTransfer;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.common.transfer.RecipeTransferService;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// JEI 31 moved the client transfer entry point from RecipeTransferUtil to RecipeTransferService.
@Mixin(value = RecipeTransferService.class, remap = false)
public class MixinRecipeTransferUtil {

  @Inject(
      at = @At("HEAD"),
      method = "transferRecipe(Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;Lmezz/jei/api/gui/IRecipeLayoutDrawable;Lnet/minecraft/world/entity/player/Player;Z)Z"
  )
  private void polymorph$transferRecipe(AbstractContainerScreen<?> containerScreen,
                                        IRecipeLayoutDrawable<?> recipeLayout, Player player,
                                        boolean maxTransfer,
                                        CallbackInfoReturnable<Boolean> cir) {

    if (recipeLayout.getRecipe() instanceof RecipeHolder<?> recipeHolder) {
      RecipeTransfer.enqueueTransfer(recipeHolder.id().identifier());
    }
  }
}
