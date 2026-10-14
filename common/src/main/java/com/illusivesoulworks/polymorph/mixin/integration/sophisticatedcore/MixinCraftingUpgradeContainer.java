package com.illusivesoulworks.polymorph.mixin.integration.sophisticatedcore;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import com.illusivesoulworks.polymorph.common.integration.sophisticatedcore.SophisticatedCraftingContainer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Shared by Sophisticated Backpacks and Sophisticated Storage. The mod keeps its own arrows as
// the UI; Polymorph only decides which candidate comes first and remembers arrow picks.
@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer", remap = false)
public abstract class MixinCraftingUpgradeContainer implements SophisticatedCraftingContainer {

  @Shadow
  private List<RecipeHolder<CraftingRecipe>> matchedCraftingRecipes;

  @Shadow
  public abstract void selectCraftingResult(int resultIndex);

  @Unique
  private Player polymorph$player;

  @ModifyVariable(at = @At("STORE"), method = "updateCraftingResult", ordinal = 0)
  private List<RecipeHolder<CraftingRecipe>> polymorph$order(
      List<RecipeHolder<CraftingRecipe>> recipes, Level level, Player player,
      CraftingContainer inventory, ResultContainer result, ResultSlot slot) {
    this.polymorph$player = player;

    if (recipes.size() < 2) {
      return recipes;
    }
    return PolymorphApi.getInstance().getRecipeManager()
        .getPlayerRecipe(player.containerMenu, RecipeType.CRAFTING, inventory.asCraftInput(), level,
            player)
        .map(picked -> {
          List<RecipeHolder<CraftingRecipe>> ordered = new ArrayList<>(recipes);

          for (int i = 0; i < ordered.size(); i++) {

            if (ordered.get(i).id().equals(picked.id())) {
              ordered.add(0, ordered.remove(i));
              break;
            }
          }
          return ordered;
        }).orElse(recipes);
  }

  @Inject(at = @At("TAIL"), method = "selectCraftingResult")
  private void polymorph$remember(int resultIndex, CallbackInfo ci) {

    if (this.polymorph$player instanceof ServerPlayer player && resultIndex >= 0 &&
        resultIndex < this.matchedCraftingRecipes.size()) {
      IPlayerRecipeData data = PolymorphApi.getInstance().getPlayerRecipeData(player);

      if (data != null) {
        data.selectRecipe(this.matchedCraftingRecipes.get(resultIndex));
      }
    }
  }

  @Override
  public boolean polymorph$select(RecipeHolder<?> recipe) {

    for (int i = 0; i < this.matchedCraftingRecipes.size(); i++) {

      if (this.matchedCraftingRecipes.get(i).id().equals(recipe.id())) {
        this.selectCraftingResult(i);
        return true;
      }
    }
    return false;
  }
}
