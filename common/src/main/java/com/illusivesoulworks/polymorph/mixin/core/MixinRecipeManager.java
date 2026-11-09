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

package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(value = RecipeManager.class, priority = 900)
public class MixinRecipeManager implements IRecipeContext {

  @Unique
  Object polymorph$context;

  @Inject(
      at = @At("HEAD"),
      method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;",
      cancellable = true)
  private <C extends Container, T extends Recipe<C>> void polymorph$getRecipe(
      RecipeType<T> recipeType, C inventory, Level level, ResourceLocation resourceLocation,
      CallbackInfoReturnable<Optional<Pair<ResourceLocation, T>>> cb) {

    BlockEntity blockEntity = this.polymorph$blockEntity(inventory, level);

    if (blockEntity != null) {
      RecipeSelection.getBlockEntityRecipe(recipeType, inventory, level, blockEntity)
          .ifPresent(recipe -> cb.setReturnValue(Optional.of(Pair.of(resourceLocation, recipe))));
    }
  }

  @Inject(
      at = @At("HEAD"),
      method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;",
      cancellable = true)
  private <C extends Container, T extends Recipe<C>> void polymorph$getRecipe(
      RecipeType<T> recipeType, C inventory, Level level, CallbackInfoReturnable<Optional<T>> cb) {

    BlockEntity blockEntity = this.polymorph$blockEntity(inventory, level);

    if (blockEntity != null) {
      RecipeSelection.getBlockEntityRecipe(recipeType, inventory, level, blockEntity)
          .ifPresent(recipe -> cb.setReturnValue(Optional.of(recipe)));
    }
  }

  @Unique
  @Nullable
  private BlockEntity polymorph$blockEntity(Container inventory, Level level) {

    if (inventory instanceof BlockEntity blockEntity) {
      return blockEntity;
    }

    // Only a context on the same level counts, so one left set by a failed call cannot reach
    // another dimension's lookups.
    if (this.polymorph$context instanceof BlockEntity blockEntity &&
        blockEntity.getLevel() == level &&
        PolymorphApi.common().getRecipeData(blockEntity).isPresent()) {
      return blockEntity;
    }
    return null;
  }

  @Nullable
  @Override
  public Object polymorph$getContext() {
    return this.polymorph$context;
  }

  @Override
  public void polymorph$setContext(Object context) {
    this.polymorph$context = context;
  }
}
