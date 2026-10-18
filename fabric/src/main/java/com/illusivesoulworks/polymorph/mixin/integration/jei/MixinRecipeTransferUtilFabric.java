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

/**
 * JEI 24.x for 1.21.8 predates IRecipeTransferListener. The selection packet goes out after
 * JEI's transfer packet on the same connection, so the server fills first, then switches.
 * The descriptor names intermediary classes (class_1703 AbstractContainerMenu, class_1657
 * Player): remap = false keeps it as written, and JEI on Fabric runs against intermediary.
 */
@Mixin(value = RecipeTransferUtil.class, remap = false)
public class MixinRecipeTransferUtilFabric {

  @Inject(at = @At("RETURN"), method = "transferRecipe(Lmezz/jei/api/recipe/transfer/IRecipeTransferManager;Lnet/minecraft/class_1703;Lmezz/jei/api/gui/IRecipeLayoutDrawable;Lnet/minecraft/class_1657;Z)Z")
  private static void polymorph$transferRecipe(IRecipeTransferManager recipeTransferManager,
                                               AbstractContainerMenu containerMenu,
                                               IRecipeLayoutDrawable<?> recipeLayout, Player player,
                                               boolean maxTransfer,
                                               CallbackInfoReturnable<Boolean> cir) {

    if (cir.getReturnValueZ() && recipeLayout.getRecipe() instanceof RecipeHolder<?> recipeHolder) {
      RecipeTransfer.selectRecipe(recipeHolder);
    }
  }
}
