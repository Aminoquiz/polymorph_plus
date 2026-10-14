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

package com.illusivesoulworks.polymorph.common.capability;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import com.illusivesoulworks.polymorph.client.RecipesWidget;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class PlayerRecipeData extends AbstractRecipeData<Player> implements
    IPlayerRecipeData {

  private AbstractContainerMenu containerMenu;
  private RecipeHolder<?> cachedSelection;
  private int lastAccessTick;

  public PlayerRecipeData(Player owner) {
    super(owner);
  }

  @Override
  public <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getRecipe(
      RecipeType<T> type, I recipeInput, Level level, List<RecipeHolder<T>> recipesList) {

    // Workaround for crafting remainders where the recipe output is called once without it and then
    // once with it, resulting in a cache needed for repeated access during the same tick to get the
    // true final result, taking into consideration previous selections
    if (this.getOwner().tickCount == this.lastAccessTick) {

      if (this.cachedSelection != null) {
        this.setSelectedRecipe(this.cachedSelection);
      }
    } else {
      this.cachedSelection = null;
    }
    RecipeHolder<T> result = super.getRecipe(type, recipeInput, level, recipesList);

    if (this.getContainerMenu() == this.getOwner().containerMenu) {
      this.syncPlayerRecipeData();
    }
    this.setContainerMenu(null);

    if (this.getOwner().tickCount != this.lastAccessTick) {
      this.lastAccessTick = this.getOwner().tickCount;

      // Prevent caching selections coming from outside the main thread, this is usually not desired
      // and can lead to race conditions
      if (validateThread(level)) {
        this.cachedSelection = result;
      }
    }
    return result;
  }

  private boolean validateThread(Level level) {
    MinecraftServer server = level.getServer();
    return server != null && server.getRunningThread() == Thread.currentThread();
  }

  @Override
  public void selectRecipe(@Nonnull RecipeHolder<?> recipe) {
    this.cachedSelection = null;
    super.selectRecipe(recipe);
    this.syncPlayerRecipeData();
  }

  private void syncPlayerRecipeData() {

    if (this.getOwner() instanceof ServerPlayer) {
      ResourceLocation resourceLocation =
          this.getSelectedRecipe() != null ? this.getSelectedRecipe().id() : null;
      PolymorphApi.getInstance().getNetwork()
          .sendPlayerSyncS2C((ServerPlayer) this.getOwner(), this.getRecipesList(),
              resourceLocation);
    }
  }

  @Override
  public void sendRecipesListToListeners() {

    if (this.getContainerMenu() == this.getOwner().containerMenu) {
      ResourceLocation resourceLocation =
          this.getSelectedRecipe() != null ? this.getSelectedRecipe().id() : null;
      Pair<SortedSet<IRecipePair>, ResourceLocation> packetData =
          new Pair<>(this.getRecipesList(), resourceLocation);
      Player player = this.getOwner();

      if (player.level().isClientSide()) {
        RecipesWidget.get().ifPresent(
            widget -> widget.setRecipesList(packetData.getFirst(), packetData.getSecond()));
      } else if (player instanceof ServerPlayer) {
        PolymorphApi.getInstance().getNetwork()
            .sendRecipesListS2C((ServerPlayer) player, packetData.getFirst(),
                packetData.getSecond());
      }
    }
  }

  @Override
  public void setContainerMenu(AbstractContainerMenu containerMenu) {
    this.containerMenu = containerMenu;
  }

  @Override
  public AbstractContainerMenu getContainerMenu() {
    return this.containerMenu;
  }
}
