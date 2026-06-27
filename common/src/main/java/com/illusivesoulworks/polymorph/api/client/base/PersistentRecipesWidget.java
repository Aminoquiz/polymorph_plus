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

package com.illusivesoulworks.polymorph.api.client.base;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

public abstract class PersistentRecipesWidget extends AbstractRecipesWidget {

  public PersistentRecipesWidget(AbstractContainerScreen<?> containerScreen) {
    super(containerScreen);
  }

  @Override
  public void selectRecipe(ResourceLocation resourceLocation) {
    // Eager local highlight so the selector reflects the click immediately. The C2S round
    // trip updates the block entity, but server doesn't always echo back a SPacketRecipesList
    // to refresh the highlight, leaving the old selection visually marked until the screen
    // is closed and reopened.
    this.highlightRecipe(resourceLocation);
    PolymorphApi.getInstance().getNetwork().sendPersistentRecipeSelectionC2S(resourceLocation);
  }
}
