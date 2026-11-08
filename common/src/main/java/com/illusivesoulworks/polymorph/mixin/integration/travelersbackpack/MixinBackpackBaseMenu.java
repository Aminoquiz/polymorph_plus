package com.illusivesoulworks.polymorph.mixin.integration.travelersbackpack;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.integration.travelersbackpack.TravelersBackpackModule;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
@Mixin(targets = "com.tiviacz.travelersbackpack.inventory.menu.BackpackBaseMenu", remap = false)
public class MixinBackpackBaseMenu {

  // The grid keeps its previous recipe while it still matches; the candidate list is only
  // rebuilt by the lookup, so it has to run on every grid update.
  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;matches(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Z",
          remap = true),
      method = "slotChangedCraftingGrid")
  private boolean polymorph$matches(CraftingRecipe recipe, RecipeInput input, Level level) {
    return false;
  }

  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;",
          remap = true),
      method = "slotChangedCraftingGrid")
  private <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> polymorph$getRecipeFor(
      RecipeManager recipeManager, RecipeType<T> type, I input, Level level) {
    AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
    Player player = TravelersBackpackModule.getPlayer(menu);

    if (player == null) {
      return recipeManager.getRecipeFor(type, input, level);
    }
    return PolymorphApi.getInstance().getRecipeManager()
        .getPlayerRecipe(menu, type, input, level, player);
  }
}
