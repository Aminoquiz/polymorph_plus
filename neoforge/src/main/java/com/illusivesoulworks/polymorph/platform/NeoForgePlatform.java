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

package com.illusivesoulworks.polymorph.platform;

import com.illusivesoulworks.polymorph.api.common.base.IPolymorphNetwork;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import com.illusivesoulworks.polymorph.common.PolymorphNeoForgeCapabilities;
import com.illusivesoulworks.polymorph.common.network.PolymorphNeoForgePacketDistributor;
import com.illusivesoulworks.polymorph.platform.services.IPlatform;
import java.nio.file.Path;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

public class NeoForgePlatform implements IPlatform {

  private static final IPolymorphNetwork PACKET_DISTRIBUTOR =
      new PolymorphNeoForgePacketDistributor();

  @Override
  public Path getGameDir() {
    return FMLPaths.GAMEDIR.get();
  }

  @Override
  public Path getConfigDir() {
    return FMLPaths.CONFIGDIR.get();
  }

  @Override
  public boolean isModLoaded(String id) {
    return ModList.get().isLoaded(id);
  }

  @Override
  public boolean isModFileLoaded(String id) {
    return FMLLoader.getLoadingModList().getModFileById(id) != null;
  }

  @Override
  public boolean isShaped(Recipe<?> recipe) {
    return recipe instanceof ShapedRecipe;
  }

  @Override
  public boolean isSameShape(Recipe<?> recipe1, Recipe<?> recipe2) {

    if (isShaped(recipe1) && isShaped(recipe2)) {
      ShapedRecipe shaped = (ShapedRecipe) recipe1;
      ShapedRecipe otherShaped = (ShapedRecipe) recipe2;
      return shaped.getHeight() == otherShaped.getHeight() &&
          shaped.getWidth() == otherShaped.getWidth();
    }
    return true;
  }

  @Override
  public IPlayerRecipeData getRecipeData(Player player) {
    return (IPlayerRecipeData) player.getData(PolymorphNeoForgeCapabilities.RECIPE_DATA.get())
        .getRecipeData();
  }

  @Override
  public IBlockEntityRecipeData getRecipeData(BlockEntity blockEntity) {
    return (IBlockEntityRecipeData) blockEntity.getData(
        PolymorphNeoForgeCapabilities.RECIPE_DATA.get()).getRecipeData();
  }

  @Override
  public IPolymorphNetwork getPacketDistributor() {
    return PACKET_DISTRIBUTOR;
  }
}
