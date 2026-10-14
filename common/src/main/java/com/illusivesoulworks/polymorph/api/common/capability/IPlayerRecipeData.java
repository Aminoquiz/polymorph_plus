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

package com.illusivesoulworks.polymorph.api.common.capability;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface IPlayerRecipeData extends IRecipeData<Player> {

  void setContainerMenu(AbstractContainerMenu container);

  AbstractContainerMenu getContainerMenu();

  /**
   * The player's own source-preference list, most preferred first, as uploaded from the
   * client. Default implementations keep addons that implement this interface source
   * compatible: without an override the player simply has no preference.
   */
  default void setPriorityNamespaces(List<String> namespaces) {
  }

  default List<String> getPriorityNamespaces() {
    return List.of();
  }

  /**
   * The player's per-recipe preference list, most preferred first, as uploaded from the client.
   * Outranks {@link #getPriorityNamespaces()}: a craft has one outcome, so a namespace rule
   * cannot decide between candidates that share a namespace.
   */
  default void setPriorityRecipes(List<String> recipes) {
  }

  default List<String> getPriorityRecipes() {
    return List.of();
  }
}
