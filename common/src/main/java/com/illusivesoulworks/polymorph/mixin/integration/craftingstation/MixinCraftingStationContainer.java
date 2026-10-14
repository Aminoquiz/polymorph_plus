package com.illusivesoulworks.polymorph.mixin.integration.craftingstation;

import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "com.tfar.craftingstation.CraftingStationContainer", remap = false)
public abstract class MixinCraftingStationContainer {

  @Inject(at = @At("HEAD"), method = "findRecipe", cancellable = true)
  private static void polymorph$findRecipe(CraftingContainer inv, Level level, Player player,
                                           CallbackInfoReturnable<Recipe<CraftingContainer>> cir) {

    if (player != null) {
      cir.setReturnValue(RecipeSelection.getPlayerRecipe(player.containerMenu, RecipeType.CRAFTING,
          inv, level, player).orElse(null));
    }
  }

  // The station keeps its last recipe while it still matches, which would hide the player's pick.
  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/Recipe;matches(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z",
          remap = true),
      method = "slotChangedCraftingGrid")
  private boolean polymorph$skipLastRecipe(Recipe<?> recipe, Container container, Level level) {
    return false;
  }
}
