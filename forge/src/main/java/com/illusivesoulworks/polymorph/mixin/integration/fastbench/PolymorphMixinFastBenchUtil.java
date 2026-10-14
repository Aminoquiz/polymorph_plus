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

package com.illusivesoulworks.polymorph.mixin.integration.fastbench;

import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import shadows.fastbench.api.ICraftingContainer;
import shadows.fastbench.util.CraftingInventoryExt;
import shadows.fastbench.util.FastBenchUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(FastBenchUtil.class)
public class PolymorphMixinFastBenchUtil {

  // FastWorkbench keeps the previous recipe while it still matches and skips the lookup. The
  // candidate list is only rebuilt inside the lookup, so it has to run on every grid update.
  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/Recipe;matches(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z",
          remap = true),
      method = "slotChangedCraftingGrid",
      remap = false)
  private static boolean polymorph$matches(Recipe<Container> recipe, Container container,
                                           Level world, Level level, Player player,
                                           CraftingInventoryExt inv, ResultContainer result) {
    return !polymorph$isOpenGrid(player, result) && recipe.matches(container, world);
  }

  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "shadows/fastbench/util/FastBenchUtil.findRecipe(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/item/crafting/Recipe;"),
      method = "slotChangedCraftingGrid",
      remap = false)
  private static Recipe<CraftingContainer> polymorph$findRecipe(CraftingContainer inv, Level world,
                                                                Level unused1, Player player,
                                                                CraftingInventoryExt unused2,
                                                                ResultContainer result) {

    if (!polymorph$isOpenGrid(player, result)) {
      return FastBenchUtil.findRecipe(inv, world);
    }
    return RecipeSelection.getPlayerRecipe(player.containerMenu, RecipeType.CRAFTING, inv, world,
        player).orElse(null);
  }

  // The update is queued and runs up to a tick later, by which time the player may have closed
  // the menu the grid belongs to.
  @Unique
  private static boolean polymorph$isOpenGrid(Player player, ResultContainer result) {
    return player.containerMenu instanceof ICraftingContainer container
        && container.getResult() == result;
  }
}
