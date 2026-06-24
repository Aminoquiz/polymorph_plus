/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IPolymorphRecipeManager;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IRecipeData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * MC 26.1 fork. {@code Level.getRecipeManager()} gone — route through
 * {@code ServerLevel.recipeAccess()} server-side, or {@code MinecraftServer.getRecipeManager()}
 * on integrated SP. {@code RecipeManager.getRecipesFor(type,input,level)} renamed to
 * {@code getRecipeFor(...)} and now returns {@code Optional<RecipeHolder<T>>} directly.
 */
public class PolymorphRecipeManager implements IPolymorphRecipeManager {

  public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getPlayerRecipe(
      AbstractContainerMenu containerMenu, RecipeType<T> type, I inventory, Level level,
      Player player, List<RecipeHolder<T>> recipes) {
    IPlayerRecipeData recipeData = PolymorphApi.getInstance().getPlayerRecipeData(player);

    if (recipeData != null) {
      recipeData.setContainerMenu(containerMenu);
    }
    return getRecipe(type, inventory, level, recipeData, recipes);
  }

  public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getBlockEntityRecipe(
      RecipeType<T> type, I inventory, Level level, BlockEntity blockEntity) {
    return getRecipe(type, inventory, level,
        PolymorphApi.getInstance().getBlockEntityRecipeData(blockEntity), new ArrayList<>());
  }

  private static <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipe(
      RecipeType<T> type, I inventory, Level level, IRecipeData<?> recipeData,
      List<RecipeHolder<T>> recipes) {

    if (recipeData != null) {
      return Optional.ofNullable(recipeData.getRecipe(type, inventory, level, recipes));
    }
    RecipeManager rm = serverRecipeManager(level);
    return rm != null ? rm.getRecipeFor(type, inventory, level) : Optional.empty();
  }

  private static RecipeManager serverRecipeManager(Level level) {
    if (level instanceof ServerLevel sl) {
      return sl.recipeAccess();
    }
    MinecraftServer server = level.getServer();
    return server != null ? server.getRecipeManager() : null;
  }
}
