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

package com.illusivesoulworks.polymorph.api.client.widgets;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.base.AbstractRecipesWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class PlayerRecipesWidget extends AbstractRecipesWidget {

  final Slot outputSlot;

  public PlayerRecipesWidget(AbstractContainerScreen<?> containerScreen, Slot outputSlot) {
    super(containerScreen);
    this.outputSlot = outputSlot;
  }

  @Override
  public void selectRecipe(ResourceLocation resourceLocation) {
    PolymorphApi api = PolymorphApi.getInstance();
    Player player = Minecraft.getInstance().player;

    if (player != null) {
      player.level().getRecipeManager().byKey(resourceLocation)
          .ifPresent(recipe -> api.getPlayerRecipeData(player).selectRecipe(recipe));
    }
    api.getNetwork().sendPlayerRecipeSelectionC2S(resourceLocation);
  }

  @Override
  public Slot getOutputSlot() {
    return this.outputSlot;
  }
}
