package com.illusivesoulworks.polymorph.mixin.integration.craftingslots;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "net.blay09.mods.craftingslots.menu.CustomCraftingMenu", remap = false)
public abstract class MixinCustomCraftingMenu {

  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;",
          remap = true),
      method = "slotChangedCraftingGrid")
  private <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> polymorph$getRecipeFor(
      RecipeManager recipeManager, RecipeType<T> type, I input, Level level,
      RecipeHolder<T> lastRecipe, AbstractContainerMenu menu, ServerLevel serverLevel,
      Player player, CraftingContainer craftingContainer, ResultContainer resultContainer,
      RecipeHolder<CraftingRecipe> recipeHint) {
    return PolymorphApi.getInstance().getRecipeManager()
        .getPlayerRecipe(menu, type, input, level, player);
  }
}
