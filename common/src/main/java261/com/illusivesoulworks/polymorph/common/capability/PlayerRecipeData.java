/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
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
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * MC 26.1 fork. Only difference vs 1.21.1 is unwrapping {@code RecipeHolder.id()} —
 * a {@code ResourceKey<Recipe<?>>} — to the {@link Identifier} the network layer
 * still consumes.
 */
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
      Identifier id =
          this.getSelectedRecipe() != null ? this.getSelectedRecipe().id().identifier() : null;
      PolymorphApi.getInstance().getNetwork()
          .sendPlayerSyncS2C((ServerPlayer) this.getOwner(), this.getRecipesList(), id);
    }
  }

  @Override
  public void sendRecipesListToListeners() {

    if (this.getContainerMenu() == this.getOwner().containerMenu) {
      Identifier id =
          this.getSelectedRecipe() != null ? this.getSelectedRecipe().id().identifier() : null;
      Pair<SortedSet<IRecipePair>, Identifier> packetData =
          new Pair<>(this.getRecipesList(), id);
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
