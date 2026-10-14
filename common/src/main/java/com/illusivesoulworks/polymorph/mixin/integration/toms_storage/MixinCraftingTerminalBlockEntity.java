package com.illusivesoulworks.polymorph.mixin.integration.toms_storage;

import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import com.illusivesoulworks.polymorph.common.integration.toms_storage.TomsCraftingTerminal;
import com.illusivesoulworks.polymorph.common.integration.toms_storage.TomsStorageModule;
import java.util.HashSet;
import java.util.Optional;
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

// The terminal resolves one recipe per block entity, so the lookup is redirected to the player
// looking at the grid.
@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "com.tom.storagemod.tile.TileEntityCraftingTerminal", remap = false)
public abstract class MixinCraftingTerminalBlockEntity implements TomsCraftingTerminal {

  @Shadow
  private CraftingRecipe currentRecipe;

  @Shadow
  private HashSet<AbstractContainerMenu> craftingListeners;

  @Shadow
  protected abstract void onCraftingMatrixChanged();

  // The terminal keeps its previous recipe while it still matches and skips the lookup. The
  // candidate list is only rebuilt inside the lookup, so it has to run on every grid update.
  @Inject(at = @At("HEAD"), method = "onCraftingMatrixChanged")
  private void polymorph$forceLookup(CallbackInfo ci) {

    if (this.polymorph$viewer() != null) {
      this.currentRecipe = null;
    }
  }

  @Override
  public void polymorph$refresh() {
    this.currentRecipe = null;
    this.onCraftingMatrixChanged();
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

    for (AbstractContainerMenu menu : this.craftingListeners) {

      for (Slot slot : menu.slots) {

        if (slot.container instanceof Inventory) {
          Player player = ((Inventory) slot.container).player;

          if (player.containerMenu == menu &&
              TomsStorageModule.isCraftingTerminal(player.containerMenu)) {
            return player;
          }
          break;
        }
      }
    }
    return null;
  }
}
