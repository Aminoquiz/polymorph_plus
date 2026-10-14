package com.illusivesoulworks.polymorph.mixin.integration.travelersbackpack;

import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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

// Fabric build: a static grid update that always runs the lookup.
@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "com.tiviacz.travelersbackpack.inventory.screen.TravelersBackpackBaseScreenHandler", remap = false)
public class MixinTravelersBackpackBaseScreenHandler {

  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;",
          remap = true),
      method = "slotChangedCraftingGrid")
  private static <C extends Container, T extends Recipe<C>> Optional<T> polymorph$getRecipeFor(
      RecipeManager recipeManager, RecipeType<T> type, C container, Level level,
      AbstractContainerMenu menu, Level menuLevel, Player player, CraftingContainer craftSlots,
      ResultContainer resultSlots) {
    return RecipeSelection.getPlayerRecipe(menu, type, container, level, player);
  }
}
