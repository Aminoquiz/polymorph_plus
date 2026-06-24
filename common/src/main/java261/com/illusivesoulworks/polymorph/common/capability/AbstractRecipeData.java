/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.capability;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.api.common.capability.IRecipeData;
import com.illusivesoulworks.polymorph.common.util.RecipePair;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * MC 26.1 fork. Key API shifts threaded through this class:
 * <ul>
 *   <li>{@code Level.getRecipeManager()} gone — server-side use
 *       {@code level.getServer().getRecipeManager()}.</li>
 *   <li>{@code RecipeHolder.id()} now returns {@code ResourceKey<Recipe<?>>};
 *       persisted state still stores the {@link Identifier} (string form).</li>
 *   <li>{@code Recipe#assemble} is single-arg ({@code RecipeInput}) — registry
 *       access is no longer threaded through.</li>
 *   <li>{@code CompoundTag.getString} returns {@code Optional<String>}.</li>
 * </ul>
 */
public abstract class AbstractRecipeData<E> implements IRecipeData<E> {

  private final SortedSet<IRecipePair> recipesList;
  private final E owner;
  private final RecipeCache recipeCache;
  private final Map<UUID, ServerPlayer> listeners;

  private RecipeHolder<?> selectedRecipe;
  private Identifier loadedRecipe;

  public AbstractRecipeData(E owner) {
    this.recipesList = new TreeSet<>();
    this.owner = owner;
    this.recipeCache = new RecipeCache(10);
    this.listeners = new HashMap<>();
  }

  @Override
  public <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getRecipe(
      RecipeType<T> type, I recipeInput, Level level, List<RecipeHolder<T>> recipesListIn) {
    List<RecipeHolder<T>> recipes =
        recipesListIn.isEmpty() ? this.recipeCache.get(level, type, recipeInput) : recipesListIn;

    if (recipes.isEmpty()) {
      this.updateRecipesList(new TreeSet<>());
      return null;
    }

    if (this.loadedRecipe != null && this.getSelectedRecipe() == null) {
      RecipeManager rm = serverRecipeManager(level);
      if (rm != null) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, this.loadedRecipe);
        rm.byKey(key).ifPresent(this::setSelectedRecipe);
      }
    }
    this.loadedRecipe = null;
    RecipeHolder<T> firstResult = null;
    RecipeHolder<T> selected = null;
    SortedSet<IRecipePair> recipesList = new TreeSet<>();

    for (RecipeHolder<T> entry : recipes) {
      T recipe = entry.value();
      Identifier id = entry.id().identifier();
      ItemStack output = recipe.assemble(recipeInput, level.registryAccess());

      if (output.isEmpty()) {
        continue;
      }

      if (firstResult == null) {
        firstResult = entry;
      }
      boolean flag = false;

      if (selected == null && this.getSelectedRecipe() != null
          && this.getSelectedRecipe().id().identifier().equals(id)) {
        selected = entry;
        flag = true;
      }

      if (recipesList.size() < 15 || flag) {
        recipesList.add(new RecipePair(id, output));
      }
    }

    if (selected == null) {
      selected = firstResult;
      this.setSelectedRecipe(selected);
    }
    this.updateRecipesList(recipesList);
    return selected;
  }

  protected void updateRecipesList(SortedSet<IRecipePair> recipesList) {
    this.setRecipesList(recipesList);
    this.sendRecipesListToListeners();
  }

  @Override
  public RecipeHolder<?> getSelectedRecipe() {
    return this.selectedRecipe;
  }

  @Override
  public void setSelectedRecipe(RecipeHolder<?> recipe) {
    this.selectedRecipe = recipe;
  }

  @Nonnull
  @Override
  public SortedSet<IRecipePair> getRecipesList() {
    return this.recipesList;
  }

  @Override
  public void setRecipesList(@Nonnull SortedSet<IRecipePair> recipesList) {
    this.recipesList.clear();
    this.recipesList.addAll(recipesList);
  }

  @Override
  public E getOwner() {
    return this.owner;
  }

  @Override
  public void selectRecipe(@Nonnull RecipeHolder<?> recipe) {
    this.setSelectedRecipe(recipe);
  }

  @Override
  public Collection<ServerPlayer> getListeners() {
    return Collections.unmodifiableCollection(this.listeners.values());
  }

  @Override
  public void addListener(@Nonnull ServerPlayer serverPlayer) {
    this.listeners.put(serverPlayer.getUUID(), serverPlayer);
  }

  @Override
  public void removeListener(@NotNull ServerPlayer serverPlayer) {
    this.listeners.remove(serverPlayer.getUUID());
  }

  @Override
  public void clearListeners() {
    this.listeners.clear();
  }

  @Override
  public void sendRecipesListToListeners() {
    Identifier selectedId =
        this.getSelectedRecipe() != null ? this.getSelectedRecipe().id().identifier() : null;
    Pair<SortedSet<IRecipePair>, Identifier> packetData =
        new Pair<>(this.getRecipesList(), selectedId);

    for (ServerPlayer listener : this.getListeners()) {
      PolymorphApi.getInstance().getNetwork()
          .sendRecipesListS2C(listener, packetData.getFirst(), packetData.getSecond());
    }
  }

  @Override
  public void readNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
    Optional<String> raw = compoundTag.getString("SelectedRecipe");
    raw.ifPresent(s -> this.loadedRecipe = Identifier.tryParse(s));
  }

  @Nonnull
  @Override
  public CompoundTag writeNBT(HolderLookup.Provider provider) {
    CompoundTag nbt = new CompoundTag();

    if (this.selectedRecipe != null) {
      nbt.putString("SelectedRecipe", this.selectedRecipe.id().identifier().toString());
    }
    return nbt;
  }

  private static RecipeManager serverRecipeManager(Level level) {
    if (level instanceof ServerLevel sl) {
      return sl.recipeAccess();
    }
    MinecraftServer server = level.getServer();
    return server != null ? server.getRecipeManager() : null;
  }
}
