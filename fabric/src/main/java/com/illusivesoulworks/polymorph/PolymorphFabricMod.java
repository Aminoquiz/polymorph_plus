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

package com.illusivesoulworks.polymorph;

import com.illusivesoulworks.polymorph.common.CommonEventsListener;
import com.illusivesoulworks.polymorph.common.PolymorphFabricNetwork;
import com.illusivesoulworks.polymorph.common.brewing.BrewingCandidates;
import com.illusivesoulworks.polymorph.common.components.PolymorphFabricComponents;
import com.illusivesoulworks.polymorph.mixin.core.AccessorPotionBrewingMix;
import com.illusivesoulworks.polymorph.server.PolymorphCommands;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class PolymorphFabricMod implements ModInitializer {

  @Override
  public void onInitialize() {
    PolymorphCommonMod.init();
    PolymorphCommonMod.setup();
    PolymorphFabricNetwork.setup();
    BrewingCandidates.setup(new BrewingCandidates.MixReader() {
      @Override
      public Object from(Object mix) {
        return ((AccessorPotionBrewingMix) mix).getFrom();
      }

      @Override
      public Ingredient ingredient(Object mix) {
        return ((AccessorPotionBrewingMix) mix).getIngredient();
      }

      @Override
      public Object to(Object mix) {
        return ((AccessorPotionBrewingMix) mix).getTo();
      }
    }, null);
    PolymorphFabricComponents.setup();
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, dedicated) -> PolymorphCommands.register(dispatcher));
    CommonEventsListener.setup();
  }
}
