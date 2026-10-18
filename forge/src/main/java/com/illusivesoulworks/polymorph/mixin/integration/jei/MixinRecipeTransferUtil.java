package com.illusivesoulworks.polymorph.mixin.integration.jei;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IPolymorphCommon;
import com.illusivesoulworks.polymorph.client.recipe.RecipesWidget;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.transfer.RecipeTransferUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * JEI 7.x predates IRecipeTransferListener. The selection packet goes out after JEI's transfer
 * packet on the same connection, so the server fills first, then switches.
 */
@Mixin(value = RecipeTransferUtil.class, remap = false)
public class MixinRecipeTransferUtil {

  @Inject(at = @At("RETURN"), method = "transferRecipe")
  private static void polymorph$transferRecipe(RecipeTransferManager recipeTransferManager,
                                               AbstractContainerMenu container,
                                               RecipeLayout<?> recipeLayout, Player player,
                                               boolean maxTransfer,
                                               CallbackInfoReturnable<Boolean> cir) {

    if (!cir.getReturnValueZ() || !(recipeLayout.getRecipe() instanceof Recipe)) {
      return;
    }
    ResourceLocation id = ((Recipe<?>) recipeLayout.getRecipe()).getId();

    if (RecipesWidget.get().isPresent()) {
      RecipesWidget.get().get().selectRecipe(id);
      return;
    }
    IPolymorphCommon commonApi = PolymorphApi.common();
    Player clientPlayer = Minecraft.getInstance().player;

    if (clientPlayer != null) {
      clientPlayer.level.getRecipeManager().byKey(id).ifPresent(
          recipe -> commonApi.getRecipeData(clientPlayer)
              .ifPresent(recipeData -> recipeData.selectRecipe(recipe)));
    }
    commonApi.getPacketDistributor().sendPlayerRecipeSelectionC2S(id);
  }
}
