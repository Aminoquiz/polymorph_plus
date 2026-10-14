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

package com.illusivesoulworks.polymorph.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import com.illusivesoulworks.polymorph.api.client.widgets.PlayerRecipesWidget;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.SortedSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public class RecipesWidget {

  private static IRecipesWidget widget = null;
  private static Screen lastScreen = null;
  private static Pair<SortedSet<IRecipePair>, ResourceLocation> pendingData = null;
  private static Pair<SortedSet<IRecipePair>, ResourceLocation> lastData = null;
  private static Screen trackedScreen = null;
  private static int trackedSlotCount = -1;

  public static Optional<IRecipesWidget> get() {
    return Optional.ofNullable(widget);
  }

  public static void enqueueRecipesList(SortedSet<IRecipePair> recipesList,
                                        ResourceLocation resourceLocation) {
    pendingData = new Pair<>(recipesList, resourceLocation);
  }

  public static void deliver(SortedSet<IRecipePair> recipesList, ResourceLocation selected) {
    lastData = new Pair<>(recipesList, selected);

    if (widget != null) {
      widget.setRecipesList(recipesList, selected);
    } else {
      enqueueRecipesList(recipesList, selected);
    }
  }

  /**
   * Traveler's Backpack replaces its crafting slots (new Slot objects) when a tab opens or an
   * upgrade changes, without re-initialising the screen. A widget bound to the removed result
   * slot never draws, so rebuild it against the current slots.
   */
  public static void revalidate(AbstractContainerScreen<?> containerScreen) {

    if (containerScreen != trackedScreen) {
      return;
    }
    int slotCount = containerScreen.getMenu().slots.size();
    boolean stale;

    if (widget != null) {
      Slot slot = widget.getOutputSlot();
      stale = slot != null && !containerScreen.getMenu().slots.contains(slot);
    } else {
      stale = slotCount != trackedSlotCount;
    }

    if (stale) {
      Pair<SortedSet<IRecipePair>, ResourceLocation> carried = lastData;
      widget = null;
      lastScreen = null;
      create(containerScreen);

      if (widget != null && carried != null) {
        lastData = carried;
        widget.setRecipesList(carried.getFirst(), carried.getSecond());
      }
    }
  }

  public static void create(AbstractContainerScreen<?> containerScreen) {

    if (containerScreen == lastScreen && widget != null) {
      return;
    }
    if (containerScreen != trackedScreen) {
      lastData = null;
    }
    trackedScreen = containerScreen;
    trackedSlotCount = containerScreen.getMenu().slots.size();
    PolymorphWidgets polymorphWidgets = PolymorphWidgets.getInstance();
    widget = polymorphWidgets.getWidgetOrDefault(containerScreen);

    if (widget != null) {

      if (widget instanceof PersistentRecipesWidget &&
          Minecraft.getInstance().getConnection() != null) {
        PolymorphApi.getInstance().getNetwork().sendBlockEntityListenerC2S(true);
      }
      widget.initChildWidgets();
      lastScreen = containerScreen;

      if (pendingData != null) {
        widget.setRecipesList(pendingData.getFirst(), pendingData.getSecond());
      }
    } else {
      lastScreen = null;
    }
    pendingData = null;
  }

  public static void clear() {

    if (widget instanceof PersistentRecipesWidget &&
        Minecraft.getInstance().getConnection() != null) {
      PolymorphApi.getInstance().getNetwork().sendBlockEntityListenerC2S(false);
    }
    widget = null;
    lastScreen = null;
    trackedScreen = null;
    lastData = null;
  }
}
