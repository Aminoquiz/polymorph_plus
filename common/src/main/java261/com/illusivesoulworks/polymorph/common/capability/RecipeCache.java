/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.capability;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * MC 26.1 fork. {@code Level.getRecipeManager()} is gone — server-side route through
 * {@code ServerLevel.recipeAccess()} or {@code level.getServer().getRecipeManager()}.
 * The public {@code RecipeManager.getRecipesFor(...)} that returned the typed list is
 * also gone; only {@code getRecipeFor} (singular Optional) and {@code getRecipes()} (full
 * collection) remain. Polymorph still needs the FULL match list to present a choice, so
 * we filter {@code getRecipes()} by type + {@code Recipe#matches} ourselves.
 */
public class RecipeCache {

  private final Entry<? extends RecipeInput, ? extends Recipe<?>>[] entries;
  private WeakReference<RecipeManager> cachedRecipeManager = new WeakReference<>(null);

  public RecipeCache(int size) {
    this.entries = new Entry<?, ?>[size];
  }

  @SuppressWarnings("unchecked")
  public <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> get(Level level,
                                                                                RecipeType<T> recipeType,
                                                                                I recipeInput) {

    if (recipeInput.isEmpty()) {
      return List.of();
    } else {
      this.validateRecipeManager(level);

      for (int i = 0; i < this.entries.length; i++) {
        Entry<?, ?> entry = this.entries[i];

        if (entry != null && entry.matches(recipeInput)) {
          this.moveEntryToFront(i);
          return (List<RecipeHolder<T>>) (Object) entry.recipes;
        }
      }
      return this.compute(level, recipeType, recipeInput);
    }
  }

  private void validateRecipeManager(Level level) {
    RecipeManager recipeManager = serverRecipeManager(level);

    if (recipeManager != this.cachedRecipeManager.get()) {
      this.cachedRecipeManager = new WeakReference<>(recipeManager);
      Arrays.fill(this.entries, null);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> compute(Level level,
                                                                                     RecipeType<T> recipeType,
                                                                                     I recipeInput) {
    RecipeManager rm = serverRecipeManager(level);
    List<RecipeHolder<T>> list;

    if (rm == null) {
      list = List.of();
    } else {
      list = rm.getRecipes().stream()
          .filter(h -> h.value().getType() == recipeType)
          .filter(h -> ((Recipe) h.value()).matches(recipeInput, level))
          .map(h -> (RecipeHolder<T>) h)
          .toList();
    }
    this.insert(recipeInput, list);
    return list;
  }

  private void moveEntryToFront(int index) {

    if (index > 0) {
      Entry<?, ?> entry = this.entries[index];
      System.arraycopy(this.entries, 0, this.entries, 1, index);
      this.entries[0] = entry;
    }
  }

  private <I extends RecipeInput, T extends Recipe<I>> void insert(I recipeInput,
                                                                   List<RecipeHolder<T>> recipes) {
    NonNullList<ItemStack> list = NonNullList.withSize(recipeInput.size(), ItemStack.EMPTY);

    for (int i = 0; i < recipeInput.size(); i++) {
      list.set(i, recipeInput.getItem(i).copyWithCount(1));
    }
    System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
    Entry<I, T> entry;

    if (recipeInput instanceof CraftingInput craftingInput) {
      entry = new CraftingEntry<>(list, craftingInput.width(), craftingInput.height(),
          recipeInput.size(), recipes);
    } else {
      entry = new Entry<>(list, recipeInput.size(), recipes);
    }
    this.entries[0] = entry;
  }

  private static RecipeManager serverRecipeManager(Level level) {
    if (level instanceof ServerLevel sl) {
      return sl.recipeAccess();
    }
    MinecraftServer server = level.getServer();
    return server != null ? server.getRecipeManager() : null;
  }

  static class CraftingEntry<I extends RecipeInput, T extends Recipe<I>> extends Entry<I, T> {

    private final int width;
    private final int height;

    public CraftingEntry(NonNullList<ItemStack> list, int width, int height, int size,
                         List<RecipeHolder<T>> recipes) {
      super(list, size, recipes);
      this.width = width;
      this.height = height;
    }

    @Override
    public boolean matches(RecipeInput recipeInput) {

      if (recipeInput instanceof CraftingInput craftingInput &&
          craftingInput.width() == this.width && craftingInput.height() == this.height) {
        return super.matches(recipeInput);
      }
      return false;
    }
  }

  static class Entry<I extends RecipeInput, T extends Recipe<I>> {

    private final NonNullList<ItemStack> key;
    private final int size;
    private final List<RecipeHolder<T>> recipes;

    public Entry(NonNullList<ItemStack> list, int size, List<RecipeHolder<T>> recipes) {
      this.key = list;
      this.size = size;
      this.recipes = recipes;
    }

    public boolean matches(RecipeInput recipeInput) {

      if (this.size == recipeInput.size()) {

        for (int i = 0; i < this.key.size(); i++) {

          if (!ItemStack.isSameItemSameComponents(this.key.get(i), recipeInput.getItem(i))) {
            return false;
          }
        }
        return true;
      } else {
        return false;
      }
    }
  }
}
