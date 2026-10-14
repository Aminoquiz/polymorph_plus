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

package com.illusivesoulworks.polymorph.common;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IStackRecipeData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;

public class PolymorphForgeCapabilities {

  @CapabilityInject(IPlayerRecipeData.class)
  public static Capability<IPlayerRecipeData> PLAYER_RECIPE_DATA = null;

  @CapabilityInject(IBlockEntityRecipeData.class)
  public static Capability<IBlockEntityRecipeData> BLOCK_ENTITY_RECIPE_DATA = null;

  @CapabilityInject(IStackRecipeData.class)
  public static Capability<IStackRecipeData> STACK_RECIPE_DATA = null;

  public static final ResourceLocation PLAYER_RECIPE_DATA_ID =
      new ResourceLocation(PolymorphApi.MOD_ID, "player_recipe_data");
  public static final ResourceLocation BLOCK_ENTITY_RECIPE_DATA_ID =
      new ResourceLocation(PolymorphApi.MOD_ID, "block_entity_recipe_data");
  public static final ResourceLocation STACK_RECIPE_DATA_ID =
      new ResourceLocation(PolymorphApi.MOD_ID, "stack_recipe_data");

  public static void register() {
    register(IPlayerRecipeData.class);
    register(IBlockEntityRecipeData.class);
    register(IStackRecipeData.class);
  }

  // Serialization goes through the providers, so the storage stays empty.
  private static <T> void register(Class<T> type) {
    CapabilityManager.INSTANCE.register(type, new Capability.IStorage<T>() {
      @Override
      public Tag writeNBT(Capability<T> capability, T instance, Direction side) {
        return null;
      }

      @Override
      public void readNBT(Capability<T> capability, T instance, Direction side, Tag nbt) {
      }
    }, () -> null);
  }
}
