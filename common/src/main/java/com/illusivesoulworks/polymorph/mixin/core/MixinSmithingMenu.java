/*
 * Copyright (C) 2020-2022 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Polymorph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SuppressWarnings("unused")
@Mixin(SmithingMenu.class)
public abstract class MixinSmithingMenu extends ItemCombinerMenu {

  @Unique
  private List<RecipeHolder<SmithingRecipe>> polymorph$matchingRecipes;

  @Shadow
  private RecipeHolder<SmithingRecipe> selectedRecipe;

  @Shadow
  protected abstract SmithingRecipeInput createRecipeInput();

  public MixinSmithingMenu(@Nullable MenuType<?> p_i231587_1_, int p_i231587_2_,
                           Inventory p_i231587_3_, ContainerLevelAccess p_i231587_4_) {
    super(p_i231587_1_, p_i231587_2_, p_i231587_3_, p_i231587_4_);
  }

  @ModifyVariable(
      at = @At(
          value = "INVOKE_ASSIGN",
          target = "net/minecraft/world/item/crafting/RecipeManager.getRecipesFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/List;"),
      method = "createResult")
  private List<RecipeHolder<SmithingRecipe>> polymorph$getRecipes(
      List<RecipeHolder<SmithingRecipe>> recipes) {
    this.polymorph$matchingRecipes = recipes;

    if (this.player instanceof ServerPlayer && recipes.isEmpty()) {
      PolymorphApi.getInstance().getNetwork().sendRecipesListS2C((ServerPlayer) this.player);
    }
    return recipes;
  }

  @ModifyVariable(
      at = @At(
          value = "INVOKE_ASSIGN",
          target = "java/util/List.get(I)Ljava/lang/Object;",
          shift = At.Shift.BY,
          by = 3),
      method = "createResult")
  private RecipeHolder<SmithingRecipe> polymorph$updateRepairOutput(
      RecipeHolder<SmithingRecipe> smithingRecipe) {
    return PolymorphApi.getInstance().getRecipeManager()
        .getPlayerRecipe((SmithingMenu) (Object) this, RecipeType.SMITHING,
            this.createRecipeInput(), this.player.level(), this.player,
            this.polymorph$matchingRecipes).orElse(smithingRecipe);
  }
}
