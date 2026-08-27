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
import com.illusivesoulworks.polymorph.client.PolymorphClientConfig;
import com.illusivesoulworks.polymorph.client.RecipesWidget;
import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  // Namespace lists share one integer scale: the player's own list occupies the low range, the
  // pack list is shifted above it. Favourites are ranked separately, on their own axis, because
  // they sit above the remembered click rather than below it.
  private static final int PACK_RANK_OFFSET = 1 << 20;

  private AbstractContainerMenu containerMenu;
  private RecipeHolder<?> cachedSelection;
  private int lastAccessTick;
  private List<String> priorityNamespaces = List.of();
  private Map<String, Integer> priorityRanks = Map.of();
  private List<String> priorityRecipes = List.of();
  private Map<String, Integer> recipeRanks = Map.of();
  private ResourceLocation sessionChoice;
  private AbstractContainerMenu sessionMenu;

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
    this.sessionChoice = recipe.id();
    this.sessionMenu = this.getOwner().containerMenu;
    super.selectRecipe(recipe);
    this.syncPlayerRecipeData();
  }

  @Override
  public void setSelectedRecipeId(ResourceLocation id) {
    this.cachedSelection = null;
    super.setSelectedRecipeId(id);
  }

  private void syncPlayerRecipeData() {

    if (this.getOwner() instanceof ServerPlayer) {
      PolymorphApi.getInstance().getNetwork()
          .sendPlayerSyncS2C((ServerPlayer) this.getOwner(), this.getRecipesList(),
              this.getSelectedRecipeId());
    }
  }

  @Override
  public void sendRecipesListToListeners() {

    if (this.getContainerMenu() == this.getOwner().containerMenu) {
      Pair<SortedSet<IRecipePair>, ResourceLocation> packetData =
          new Pair<>(this.getRecipesList(), this.getSelectedRecipeId());
      Player player = this.getOwner();

      if (player.level().isClientSide()) {
        // Client-side resolution (AE2's pattern terminal computes its output there) can run
        // before the screen has built its widget, so queue the list instead of dropping it.
        RecipesWidget.get().ifPresentOrElse(
            widget -> widget.setRecipesList(packetData.getFirst(), packetData.getSecond()),
            () -> RecipesWidget.enqueueRecipesList(packetData.getFirst(),
                packetData.getSecond()));
      } else if (player instanceof ServerPlayer) {
        PolymorphApi.getInstance().getNetwork()
            .sendRecipesListS2C((ServerPlayer) player, packetData.getFirst(),
                packetData.getSecond());
      }
    }
  }

  @Override
  public void setPriorityNamespaces(List<String> namespaces) {
    List<String> copy = List.copyOf(namespaces);
    Map<String, Integer> ranks = new HashMap<>();

    for (int i = 0; i < copy.size(); i++) {
      ranks.putIfAbsent(copy.get(i), i);
    }
    this.priorityNamespaces = copy;
    this.priorityRanks = Map.copyOf(ranks);
  }

  @Override
  public List<String> getPriorityNamespaces() {
    return this.priorityNamespaces;
  }

  @Override
  public void setPriorityRecipes(List<String> recipes) {
    List<String> copy = List.copyOf(recipes);
    Map<String, Integer> ranks = new HashMap<>();

    for (int i = 0; i < copy.size(); i++) {
      ranks.putIfAbsent(copy.get(i), i);
    }
    this.priorityRecipes = copy;
    this.recipeRanks = Map.copyOf(ranks);
  }

  @Override
  public List<String> getPriorityRecipes() {
    return this.priorityRecipes;
  }

  /**
   * A menu that resolves its output on both sides, such as the AE2 crafting terminal, runs this
   * on the client too, where the uploaded lists never land: only the server-bound handler fills
   * them. Reading the config directly there keeps the two sides on the same answer instead of
   * letting the client draw a candidate the server is about to replace.
   */
  private boolean useClientConfig() {
    return this.getOwner().level().isClientSide();
  }

  @Override
  protected int favouriteRank(ResourceLocation id) {

    if (this.useClientConfig()) {
      return PolymorphClientConfig.recipeRankOf(id.toString());
    }
    Integer rank = this.recipeRanks.get(id.toString());
    return rank == null ? Integer.MAX_VALUE : rank;
  }

  @Override
  protected int sourceRank(ResourceLocation id) {
    int namespaceRank = this.namespaceRank(id.getNamespace());

    if (namespaceRank != Integer.MAX_VALUE) {
      return namespaceRank;
    }
    int packRank = RecipePriority.packRank(id.getNamespace());
    return packRank == Integer.MAX_VALUE ? Integer.MAX_VALUE : PACK_RANK_OFFSET + packRank;
  }

  private int namespaceRank(String namespace) {

    if (this.useClientConfig()) {
      return PolymorphClientConfig.rankOf(namespace);
    }
    Integer rank = this.priorityRanks.get(namespace);
    return rank == null ? Integer.MAX_VALUE : rank;
  }

  /**
   * Valid only while the menu it was made on is still the one the player has open. Comparing
   * menu instances is enough: closing a screen swaps in a different menu, so the override
   * expires on its own with no close event to hook.
   */
  @Override
  protected ResourceLocation getSessionChoice() {

    if (this.sessionMenu == null || this.sessionMenu != this.getOwner().containerMenu) {
      this.sessionMenu = null;
      this.sessionChoice = null;
    }
    return this.sessionChoice;
  }

  @Override
  public void chooseRecipe(ResourceLocation id) {
    this.cachedSelection = null;
    this.sessionChoice = id;
    this.sessionMenu = this.getOwner().containerMenu;
    super.chooseRecipe(id);
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
