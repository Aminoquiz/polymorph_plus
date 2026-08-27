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

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.client.PolymorphWidgetsImpl;
import com.illusivesoulworks.polymorph.common.capability.CrafterRecipeData;
import com.illusivesoulworks.polymorph.common.capability.FurnaceRecipeData;
import com.illusivesoulworks.polymorph.common.integration.PolymorphIntegrations;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;

public class PolymorphCommonMod {

  public static void init() {
    PolymorphIntegrations.init();
  }

  public static void setup() {
    PolymorphApi api = PolymorphApi.getInstance();
    api.registerBlockEntity(blockEntity -> {
      if (blockEntity instanceof AbstractFurnaceBlockEntity) {
        return new FurnaceRecipeData((AbstractFurnaceBlockEntity) blockEntity);
      } else if (blockEntity instanceof CrafterBlockEntity) {
        return new CrafterRecipeData((CrafterBlockEntity) blockEntity);
      }
      return null;
    });
    api.registerMenu(menu -> {
      for (Slot inventorySlot : menu.slots) {
        Container inventory = inventorySlot.container;

        if (inventory instanceof BlockEntity) {
          return (BlockEntity) inventory;
        }
      }
      return null;
    });
    PolymorphIntegrations.setup();
  }

  public static void clientSetup() {
    PolymorphWidgetsImpl.setup();
    PolymorphIntegrations.clientSetup();
  }
}