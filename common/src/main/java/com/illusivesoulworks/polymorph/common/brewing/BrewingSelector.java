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

package com.illusivesoulworks.polymorph.common.brewing;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

/**
 * One pick per stand. It applies to every bottle whose mixes include it; the others brew as
 * vanilla would. The list shown is the first bottle with a conflict, else the first that brews.
 */
public class BrewingSelector {

  private static final int REAGENT_SLOT = 3;

  private final BrewingStandBlockEntity owner;

  private ResourceLocation chosen;
  private List<ResourceLocation> shownIds = Collections.emptyList();
  private ResourceLocation shownSelected;
  private Set<UUID> sentTo = Collections.emptySet();

  public BrewingSelector(BrewingStandBlockEntity owner) {
    this.owner = owner;
  }

  @Nullable
  public ItemStack resolve(ItemStack bottle, ItemStack reagent) {
    List<IRecipePair> candidates = BrewingCandidates.candidates(bottle, reagent);

    if (candidates.size() < 2) {
      return null;
    }
    IRecipePair pick = this.pick(candidates);
    return pick == null ? null : pick.getOutput().copy();
  }

  @Nullable
  private IRecipePair pick(List<IRecipePair> candidates) {

    if (this.chosen != null) {

      for (IRecipePair candidate : candidates) {

        if (candidate.getResourceLocation().equals(this.chosen)) {
          return candidate;
        }
      }
    }
    IRecipePair best = null;
    int bestRank = Integer.MAX_VALUE;

    for (IRecipePair candidate : candidates) {
      int rank = RecipePriority.packRank(candidate.getResourceLocation().getNamespace());

      if (rank < bestRank) {
        bestRank = rank;
        best = candidate;
      }
    }
    return best;
  }

  public void tick(Set<ServerPlayer> listeners) {
    ItemStack reagent = this.owner.getItem(REAGENT_SLOT);
    List<IRecipePair> shown = null;
    List<IRecipePair> first = null;

    for (int i = 0; i < REAGENT_SLOT; i++) {
      List<IRecipePair> candidates =
          BrewingCandidates.candidates(this.owner.getItem(i), reagent);

      if (candidates.isEmpty()) {
        continue;
      }

      if (first == null) {
        first = candidates;
      }

      if (candidates.size() > 1) {
        shown = candidates;
        break;
      }
    }

    if (shown == null) {
      shown = first == null ? Collections.emptyList() : first;
    }
    List<ResourceLocation> ids = new ArrayList<>();
    shown.forEach(pair -> ids.add(pair.getResourceLocation()));
    IRecipePair pick = this.pick(shown);
    ResourceLocation selected =
        pick != null ? pick.getResourceLocation() : ids.isEmpty() ? null : ids.get(0);
    Set<UUID> players = new HashSet<>();
    listeners.forEach(player -> players.add(player.getUUID()));

    if (ids.equals(this.shownIds) && Objects.equals(selected, this.shownSelected) &&
        players.equals(this.sentTo)) {
      return;
    }
    this.shownIds = ids;
    this.shownSelected = selected;
    this.sentTo = players;
    SortedSet<IRecipePair> set = new TreeSet<>(shown);

    for (ServerPlayer listener : listeners) {
      PolymorphApi.common().getPacketDistributor().sendRecipesListS2C(listener, set, selected);
    }
  }

  public void choose(ResourceLocation id) {
    this.chosen = id;
    this.shownSelected = null;
    this.owner.setChanged();
  }

  public void read(CompoundTag tag) {

    if (tag.contains("SelectedRecipe")) {
      this.chosen = ResourceLocation.tryParse(tag.getString("SelectedRecipe"));
    }
  }

  public void write(CompoundTag tag) {

    if (this.chosen != null) {
      tag.putString("SelectedRecipe", this.chosen.toString());
    }
  }
}
