package com.illusivesoulworks.polymorph.mixin.integration.refinedstorage;

import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import com.illusivesoulworks.polymorph.common.integration.refinedstorage.RefinedStorageModule;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The grid node holds one crafting matrix and recipe for every player at that grid, so the
// lookup is redirected to the player looking at it.
@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode", remap = false)
public abstract class MixinGridNetworkNode {

  @Shadow
  private CraftingRecipe currentRecipe;

  @Shadow
  @SuppressWarnings("rawtypes")
  private Set craftingListeners;

  // The node keeps its previous recipe while it still matches and skips the lookup.
  @Inject(at = @At("HEAD"), method = "onCraftingMatrixChanged")
  private void polymorph$forceLookup(CallbackInfo ci) {

    if (this.polymorph$viewer() != null) {
      this.currentRecipe = null;
    }
  }

  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;",
          remap = true),
      method = "onCraftingMatrixChanged")
  private <C extends Container, T extends Recipe<C>> Optional<T> polymorph$getRecipeFor(
      RecipeManager recipeManager, RecipeType<T> type, C container, Level level) {
    Player player = this.polymorph$viewer();

    if (player != null) {
      return RecipeSelection.getPlayerRecipe(player.containerMenu, type, container, level, player);
    }
    return recipeManager.getRecipeFor(type, container, level);
  }

  @Unique
  private Player polymorph$viewer() {

    if (this.craftingListeners == null) {
      return null;
    }

    for (Object listener : this.craftingListeners) {

      if (!(listener instanceof AbstractContainerMenu)) {
        continue;
      }
      AbstractContainerMenu menu = (AbstractContainerMenu) listener;

      for (Slot slot : menu.slots) {

        if (slot.container instanceof Inventory) {
          Player player = ((Inventory) slot.container).player;

          if (player.containerMenu == menu && RefinedStorageModule.isGridMenu(menu)) {
            return player;
          }
          break;
        }
      }
    }
    return null;
  }
}
