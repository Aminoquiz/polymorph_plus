package com.illusivesoulworks.polymorph.mixin.integration.emi;

import com.illusivesoulworks.polymorph.common.integration.util.RecipeTransfer;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EmiRecipeFiller.class, remap = false)
public class MixinEmiRecipeFiller {

  @Inject(
      at = @At("HEAD"),
      method = "performFill"
  )
  private static <T extends AbstractContainerMenu> void polymorph$performFill(EmiRecipe recipe,
                                                                              AbstractContainerScreen<T> screen,
                                                                              EmiCraftContext.Type type,
                                                                              EmiCraftContext.Destination destination,
                                                                              int amount,
                                                                              CallbackInfoReturnable<Boolean> cir) {
    RecipeHolder<?> recipeHolder = recipe.getBackingRecipe();

    if (recipeHolder != null) {
      RecipeTransfer.selectRecipe(recipeHolder);
    }
  }
}
