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

package com.illusivesoulworks.polymorph.api.common.capability;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public interface IRecipeData<E> {

  <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getRecipe(RecipeType<T> type,
                                                                         I inventory, Level level,
                                                                         List<RecipeHolder<T>> recipes);

  void selectRecipe(@Nonnull RecipeHolder<?> recipe);

  RecipeHolder<?> getSelectedRecipe();

  void setSelectedRecipe(RecipeHolder<?> recipe);

  @Nonnull
  SortedSet<IRecipePair> getRecipesList();

  void setRecipesList(@Nonnull SortedSet<IRecipePair> recipesList);

  Collection<ServerPlayer> getListeners();

  void addListener(@Nonnull ServerPlayer player);

  void removeListener(@Nonnull ServerPlayer player);

  void clearListeners();

  void sendRecipesListToListeners();

  E getOwner();

  @Nonnull
  CompoundTag writeNBT(HolderLookup.Provider provider);

  void readNBT(HolderLookup.Provider provider, CompoundTag compound);
}
