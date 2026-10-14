package com.illusivesoulworks.polymorph.mixin.integration.cyclic;

import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "com.lothrazar.cyclic.block.workbench.ContainerWorkbench", remap = false)
public abstract class MixinContainerWorkbench {

  // updateCraftingResult is static and runs from slotsChanged, so the open menu is the workbench.
  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;",
          remap = true),
      method = "updateCraftingResult")
  private static <C extends Container, T extends Recipe<C>> Optional<T> polymorph$getRecipeFor(
      RecipeManager recipeManager, RecipeType<T> type, C container, Level level,
      int id, Level world, Player player, CraftingContainer inventory, ResultContainer result) {

    if (player != null && player.containerMenu != null) {
      return RecipeSelection.getPlayerRecipe(player.containerMenu, type, container, level, player);
    }
    return recipeManager.getRecipeFor(type, container, level);
  }
}
