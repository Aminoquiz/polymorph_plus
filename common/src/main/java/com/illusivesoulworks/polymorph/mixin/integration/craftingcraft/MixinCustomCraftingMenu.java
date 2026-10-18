package com.illusivesoulworks.polymorph.mixin.integration.craftingcraft;

import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
@Mixin(targets = "net.blay09.mods.craftingcraft.container.CustomCraftingMenu", remap = false)
public abstract class MixinCustomCraftingMenu {

  // Pseudo target, no refmap entry for its override: SRG and intermediary names of
  // AbstractContainerMenu.slotsChanged, checked in the 1.18.2 to 1.20.1 mappings.
  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;",
          remap = true),
      method = {"slotsChanged", "m_6199_", "method_7609"})
  private <C extends Container, T extends Recipe<C>> Optional<T> polymorph$getRecipeFor(
      RecipeManager recipeManager, RecipeType<T> type, C container, Level level) {
    AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
    return RecipeSelection.getPlayerRecipe(menu, type, container, level, menu.slots);
  }
}
