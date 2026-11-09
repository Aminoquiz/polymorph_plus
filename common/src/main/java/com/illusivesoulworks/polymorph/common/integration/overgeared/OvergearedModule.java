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

package com.illusivesoulworks.polymorph.common.integration.overgeared;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.IInWorldProvider;
import com.illusivesoulworks.polymorph.api.inworld.IndicatorSpot;
import com.illusivesoulworks.polymorph.api.inworld.PolymorphInWorld;
import com.illusivesoulworks.polymorph.common.capability.PlayerRecipeData;
import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class OvergearedModule extends AbstractCompatibilityModule {

  private static final String[] ANVILS =
      {"stone_anvil", "smithing_anvil", "tier_a_smithing_anvil", "tier_b_smithing_anvil"};

  private static final ThreadLocal<Entity> COOLING_ENTITY = new ThreadLocal<>();

  @Override
  protected void setup() {
    // Placeholder spot, off the centre where the anvil renderer draws the workpiece.
    IInWorldProvider anvil =
        PolymorphInWorld.recipeData(new IndicatorSpot(0.5f, 0.15f, 0.16f, 1.0f, true));

    for (String name : ANVILS) {
      Block block = BuiltInRegistries.BLOCK.get(
          ResourceLocation.fromNamespaceAndPath("overgeared", name));

      if (block != Blocks.AIR) {
        PolymorphInWorld.register(block, anvil);
      }
    }
  }

  public static void setCoolingEntity(Entity entity) {
    COOLING_ENTITY.set(entity);
  }

  /**
   * Overgeared cools an item into the first matching cooling recipe. With several matches, this
   * applies the cooling player's favourites, then the pack order, so the result no longer depends
   * on recipe load order.
   */
  public static Optional<?> pickCoolingRecipe(Stream<?> matches) {
    List<?> list = matches.toList();

    if (list.size() < 2) {
      return list.stream().findFirst();
    }
    PlayerRecipeData playerData = null;

    if (COOLING_ENTITY.get() instanceof Player player
        && PolymorphApi.getInstance().getPlayerRecipeData(player) instanceof PlayerRecipeData data) {
      playerData = data;
    }
    Object best = list.get(0);
    int bestFavourite = Integer.MAX_VALUE;
    int bestSource = Integer.MAX_VALUE;

    for (Object entry : list) {

      if (!(entry instanceof RecipeHolder<?> holder)) {
        continue;
      }
      ResourceLocation id = holder.id();
      int favourite = playerData != null ? playerData.favouriteRank(id) : Integer.MAX_VALUE;
      int source = playerData != null ? playerData.sourceRank(id)
          : RecipePriority.packRank(id.getNamespace());

      if (favourite < bestFavourite || (favourite == bestFavourite && source < bestSource)) {
        best = entry;
        bestFavourite = favourite;
        bestSource = source;
      }
    }
    return Optional.of(best);
  }
}
