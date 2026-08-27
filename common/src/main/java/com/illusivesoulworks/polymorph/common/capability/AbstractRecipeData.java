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
import com.illusivesoulworks.polymorph.api.common.capability.IRecipeData;
import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import com.illusivesoulworks.polymorph.common.util.RecipePair;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRecipeData<E> implements IRecipeData<E> {

  private final SortedSet<IRecipePair> recipesList;
  private final E owner;
  private final RecipeCache recipeCache;
  private final Map<UUID, ServerPlayer> listeners;
  private static final int MAX_SENT_CANDIDATES = 15;

  // Three distinct things that used to share one field, which is why a favourite could be
  // permanently shadowed by a later click:
  //   selectedRecipe / selectedRecipeId - what resolution landed on, for display and sync
  //   chosenRecipeId                    - the last deliberate pick, persisted to NBT
  // plus, for players only, a session choice that lives as long as the open menu.
  private RecipeHolder<?> selectedRecipe;
  private ResourceLocation selectedRecipeId;
  private ResourceLocation chosenRecipeId;

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

    RegistryAccess registryAccess = level.registryAccess();
    ResourceLocation sessionChoice = this.getSessionChoice();
    RecipeHolder<T> firstResult = null;
    RecipeHolder<T> sessionMatch = null;
    RecipeHolder<T> chosenMatch = null;
    RecipeHolder<T> favourite = null;
    RecipeHolder<T> sourceMatch = null;
    int favouriteRank = Integer.MAX_VALUE;
    int sourceRank = Integer.MAX_VALUE;
    List<IRecipePair> candidates = new ArrayList<>();

    for (RecipeHolder<T> entry : recipes) {
      T recipe = entry.value();
      ResourceLocation id = entry.id();
      ItemStack output = recipe.getResultItem(registryAccess);

      // noinspection ConstantConditions
      if (output == null || output.isEmpty() || entry.value() instanceof CustomRecipe) {
        output = recipe.assemble(recipeInput, registryAccess);
      }

      if (output.isEmpty()) {
        continue;
      }

      if (firstResult == null) {
        firstResult = entry;
      }

      if (sessionMatch == null && id.equals(sessionChoice)) {
        sessionMatch = entry;
      }

      if (chosenMatch == null && id.equals(this.chosenRecipeId)) {
        chosenMatch = entry;
      }
      int entryFavouriteRank = this.favouriteRank(id);

      if (entryFavouriteRank < favouriteRank) {
        favouriteRank = entryFavouriteRank;
        favourite = entry;
      }
      int entrySourceRank = this.sourceRank(id);

      if (entrySourceRank < sourceRank) {
        sourceRank = entrySourceRank;
        sourceMatch = entry;
      }
      candidates.add(new RecipePair(id, output));
    }

    // Precedence, strongest first. The session choice is what the player clicked in the menu
    // that is open right now, so it always shows. Below it the favourite wins over the last
    // remembered click: that is the whole point of marking one, it has to come back after the
    // GUI is closed. The remembered click is still there for anyone who never marks anything,
    // which is upstream's behaviour.
    RecipeHolder<T> selected = sessionMatch != null ? sessionMatch
        : favourite != null ? favourite
            : chosenMatch != null ? chosenMatch
                : sourceMatch != null ? sourceMatch : firstResult;
    this.setSelectedRecipe(selected);
    this.updateRecipesList(trim(candidates, this.selectedRecipeId));
    return selected;
  }

  /**
   * Upstream caps what is sent to the client. The resolved recipe is force-included, otherwise
   * a conflict with more candidates than the cap could resolve to something the selector cannot
   * even show.
   */
  private static SortedSet<IRecipePair> trim(List<IRecipePair> candidates, ResourceLocation selected) {
    SortedSet<IRecipePair> out = new TreeSet<>();

    for (IRecipePair candidate : candidates) {

      if (out.size() < MAX_SENT_CANDIDATES) {
        out.add(candidate);
      } else if (candidate.getResourceLocation().equals(selected)) {
        out.add(candidate);
      }
    }
    return out;
  }

  /**
   * The recipe the player picked in the menu they currently have open, if any. Lives only as
   * long as that menu: a deliberate one-off override should not outlive the screen it was made
   * on, or it would shadow the favourite forever.
   */
  protected ResourceLocation getSessionChoice() {
    return null;
  }

  /**
   * Rank of a candidate in the player's favourites. Lower wins, {@link Integer#MAX_VALUE} means
   * it is not a favourite.
   */
  protected int favouriteRank(ResourceLocation id) {
    return Integer.MAX_VALUE;
  }

  /**
   * Rank of a candidate by source (namespace lists, pack list). Lower wins;
   * {@link Integer#MAX_VALUE} means no preference applies.
   */
  protected int sourceRank(ResourceLocation id) {
    return RecipePriority.packRank(id.getNamespace());
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
    this.selectedRecipeId = recipe != null ? recipe.id() : null;
  }

  @Override
  public void setSelectedRecipeId(ResourceLocation id) {
    this.selectedRecipeId = id;

    if (this.selectedRecipe != null && !this.selectedRecipe.id().equals(id)) {
      this.selectedRecipe = null;
    }
  }

  /**
   * Records a deliberate pick, as opposed to {@link #setSelectedRecipeId} which only mirrors
   * what resolution landed on. This is the value that survives to NBT.
   */
  @Override
  public void chooseRecipe(ResourceLocation id) {
    this.chosenRecipeId = id;
    this.setSelectedRecipeId(id);
  }

  public ResourceLocation getChosenRecipeId() {
    return this.chosenRecipeId;
  }

  @Override
  public ResourceLocation getSelectedRecipeId() {
    return this.selectedRecipeId;
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
    this.chosenRecipeId = recipe.id();
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
    Pair<SortedSet<IRecipePair>, ResourceLocation> packetData =
        new Pair<>(this.getRecipesList(), this.getSelectedRecipeId());

    for (ServerPlayer listener : this.getListeners()) {
      PolymorphApi.getInstance().getNetwork()
          .sendRecipesListS2C(listener, packetData.getFirst(), packetData.getSecond());
    }
  }

  @Override
  public void readNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
    if (compoundTag.contains("SelectedRecipe")) {
      this.chosenRecipeId = ResourceLocation.tryParse(compoundTag.getString("SelectedRecipe"));
    }
  }

  @Nonnull
  @Override
  public CompoundTag writeNBT(HolderLookup.Provider provider) {
    CompoundTag nbt = new CompoundTag();

    if (this.chosenRecipeId != null) {
      nbt.putString("SelectedRecipe", this.chosenRecipeId.toString());
    }
    return nbt;
  }

}
