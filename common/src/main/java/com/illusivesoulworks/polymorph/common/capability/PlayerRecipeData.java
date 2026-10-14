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
import com.illusivesoulworks.polymorph.client.recipe.RecipesWidget;
import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import java.util.HashMap;
import java.util.Map;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class PlayerRecipeData extends AbstractRecipeData<Player> implements
    IPlayerRecipeData {

  private AbstractContainerMenu containerMenu;
  private Recipe<?> cachedSelection;
  private int lastAccessTick;
  // The player's namespace list takes the low ranks, the pack list is shifted above it.
  private static final int PACK_RANK_OFFSET = 1 << 20;
  private List<String> priorityNamespaces = java.util.Collections.emptyList();
  private Map<String, Integer> priorityRanks = java.util.Collections.emptyMap();
  private List<String> priorityRecipes = java.util.Collections.emptyList();
  private Map<String, Integer> recipeRanks = java.util.Collections.emptyMap();
  private ResourceLocation sessionChoice;
  private AbstractContainerMenu sessionMenu;

  public PlayerRecipeData(Player owner) {
    super(owner);
  }

  @Override
  public <T extends Recipe<C>, C extends Container> Optional<T> getRecipe(RecipeType<T> type,
                                                                          C inventory,
                                                                          Level level,
                                                                          List<T> recipesList) {

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
    Optional<T> maybeRecipe = super.getRecipe(type, inventory, level, recipesList);

    if (this.getContainerMenu() == this.getOwner().containerMenu) {
      this.syncPlayerRecipeData();
    }
    this.setContainerMenu(null);

    if (this.getOwner().tickCount != this.lastAccessTick) {
      this.lastAccessTick = this.getOwner().tickCount;
      this.cachedSelection = maybeRecipe.orElse(null);
    }
    return maybeRecipe;
  }

  @Override
  public void selectRecipe(@Nonnull Recipe<?> recipe) {
    this.cachedSelection = null;
    this.sessionChoice = recipe.getId();
    this.sessionMenu = this.getOwner().containerMenu;
    super.selectRecipe(recipe);
    this.syncPlayerRecipeData();
  }

  private void syncPlayerRecipeData() {

    if (this.getOwner() instanceof ServerPlayer) {
      PolymorphApi.common().getPacketDistributor()
          .sendPlayerSyncS2C((ServerPlayer) this.getOwner(), this.getRecipesList(),
              this.getSelectedRecipe().map(Recipe::getId).orElse(null));
    }
  }

  @Override
  public void sendRecipesListToListeners(boolean isEmpty) {

    if (this.getContainerMenu() == this.getOwner().containerMenu) {
      Pair<SortedSet<IRecipePair>, ResourceLocation> packetData =
          isEmpty ? new Pair<>(new TreeSet<>(), null) : this.getPacketData();
      Player player = this.getOwner();

      if (player.level.isClientSide()) {
        // Client-side resolution can run before the screen built its widget: queue the list.
        RecipesWidget.deliver(packetData.getFirst(), packetData.getSecond());
      } else if (player instanceof ServerPlayer) {
        PolymorphApi.common().getPacketDistributor()
            .sendRecipesListS2C((ServerPlayer) player, packetData.getFirst(),
                packetData.getSecond());
      }
    }
  }

  @Override
  public void setPriorityNamespaces(List<String> namespaces) {
    this.priorityNamespaces = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(namespaces));
    this.priorityRanks = ranks(this.priorityNamespaces);
  }

  @Override
  public List<String> getPriorityNamespaces() {
    return this.priorityNamespaces;
  }

  @Override
  public void setPriorityRecipes(List<String> recipes) {
    this.priorityRecipes = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(recipes));
    this.recipeRanks = ranks(this.priorityRecipes);
  }

  @Override
  public List<String> getPriorityRecipes() {
    return this.priorityRecipes;
  }

  private static Map<String, Integer> ranks(List<String> list) {
    Map<String, Integer> ranks = new HashMap<>();

    for (int i = 0; i < list.size(); i++) {
      ranks.putIfAbsent(list.get(i), i);
    }
    return java.util.Collections.unmodifiableMap(new java.util.HashMap<>(ranks));
  }

  // Menus that also resolve on the client never receive the uploaded lists there.
  private boolean useClientConfig() {
    return this.getOwner().level.isClientSide();
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
    String namespace = id.getNamespace();
    Integer own = this.useClientConfig() ? Integer.valueOf(PolymorphClientConfig.rankOf(namespace))
        : this.priorityRanks.get(namespace);

    if (own != null && own != Integer.MAX_VALUE) {
      return own;
    }
    int packRank = RecipePriority.packRank(namespace, this.useClientConfig());
    return packRank == Integer.MAX_VALUE ? Integer.MAX_VALUE : PACK_RANK_OFFSET + packRank;
  }

  // Closing a screen swaps in a different menu, so the session choice expires on its own.
  @Override
  protected ResourceLocation getSessionChoice() {

    if (this.sessionMenu == null || this.sessionMenu != this.getOwner().containerMenu) {
      this.sessionMenu = null;
      this.sessionChoice = null;
    }
    return this.sessionChoice;
  }

  @Override
  public Set<ServerPlayer> getListeners() {
    Player player = this.getOwner();

    if (player instanceof ServerPlayer) {
      return Collections.singleton((ServerPlayer) player);
    } else {
      return new HashSet<>();
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
