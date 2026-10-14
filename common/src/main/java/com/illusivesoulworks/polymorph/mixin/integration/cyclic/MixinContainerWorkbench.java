package com.illusivesoulworks.polymorph.mixin.integration.cyclic;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;
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
          target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"),
      method = "updateCraftingResult")
  private static <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> polymorph$getRecipeFor(
      RecipeManager recipeManager, RecipeType<T> type, I input, Level level,
      @Coerce AbstractContainerMenu menu, ServerLevel world, Player player, CraftingContainer inventory, ResultContainer result) {

    if (player != null && player.containerMenu != null) {
      return PolymorphApi.getInstance().getRecipeManager()
          .getPlayerRecipe(player.containerMenu, type, input, level, player);
    }
    return recipeManager.getRecipeFor(type, input, level);
  }
}
