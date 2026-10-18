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

package com.illusivesoulworks.polymorph.client.recipe.widget;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

public class BrewingStandRecipesWidget extends PersistentRecipesWidget {

  private static final int REAGENT_SLOT = 3;
  // Right of the brew arrow, level with the reagent slot: GUI (124, 17).
  private static final int REAGENT_X_SHIFT = 45;
  private static final int REAGENT_Y_SHIFT = 22;

  private final Slot reagentSlot;

  public BrewingStandRecipesWidget(AbstractContainerScreen<?> containerScreen) {
    super(containerScreen);
    this.reagentSlot = containerScreen.getMenu().slots.get(REAGENT_SLOT);
  }

  @Override
  public Slot getOutputSlot() {
    return this.reagentSlot;
  }

  @Override
  public int getXPos() {
    return super.getXPos() + REAGENT_X_SHIFT;
  }

  @Override
  public int getYPos() {
    return super.getYPos() + REAGENT_Y_SHIFT;
  }
}
