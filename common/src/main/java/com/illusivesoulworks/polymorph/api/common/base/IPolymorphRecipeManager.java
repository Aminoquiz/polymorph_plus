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

package com.illusivesoulworks.polymorph.api.common.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IPolymorphRecipeManager {

  default <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getPlayerRecipe(
      AbstractContainerMenu containerMenu, RecipeType<T> type, I inventory, Level level,
      Player player) {
    return this.getPlayerRecipe(containerMenu, type, inventory, level, player, new ArrayList<>());
  }

  <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getPlayerRecipe(
      AbstractContainerMenu containerMenu, RecipeType<T> type, I inventory, Level level,
      Player player, List<RecipeHolder<T>> recipes);

  <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getBlockEntityRecipe(
      RecipeType<T> type, I inventory, Level level, BlockEntity blockEntity);
}
