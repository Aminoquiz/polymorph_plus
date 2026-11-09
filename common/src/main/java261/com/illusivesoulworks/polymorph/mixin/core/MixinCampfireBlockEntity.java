/*
 * Copyright (C) 2020-2026 Illusive Soulworks
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
import com.illusivesoulworks.polymorph.api.inworld.PolymorphInWorld;
import com.illusivesoulworks.polymorph.common.capability.CampfireRecipeData;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlockEntity.class)
public class MixinCampfireBlockEntity {

  @Unique
  private Object polymorph$previousContext;

  // placeFood looks the recipe up itself, outside the block entity tick where MixinLevelChunk
  // sets the context. With the context set, the candidates exist as soon as the food lands, so
  // the indicator shows before the first item finishes cooking.
  @Inject(method = "placeFood", at = @At("HEAD"))
  private void polymorph$prePlaceFood(ServerLevel level, @Nullable LivingEntity entity,
                                      ItemStack stack, CallbackInfoReturnable<Boolean> cir) {

    if (level.recipeAccess() instanceof IRecipeContext recipeContext) {
      this.polymorph$previousContext = recipeContext.polymorph$getContext();
      recipeContext.polymorph$setContext(this);
    }
  }

  // The lookup runs before the item is stored, so the refresh it fired saw an empty campfire
  // and sent no indicator. Refresh again once the food is in.
  @Inject(method = "placeFood", at = @At("RETURN"))
  private void polymorph$postPlaceFood(ServerLevel level, @Nullable LivingEntity entity,
                                       ItemStack stack, CallbackInfoReturnable<Boolean> cir) {

    if (level.recipeAccess() instanceof IRecipeContext recipeContext) {
      recipeContext.polymorph$setContext(this.polymorph$previousContext);
      this.polymorph$previousContext = null;
    }

    if (cir.getReturnValueZ()) {
      CampfireBlockEntity campfire = (CampfireBlockEntity) (Object) this;
      PolymorphInWorld.refresh(level, campfire.getBlockPos());
    }
  }

  // A new item must not inherit the pick of the item that sat in its slot before.
  @Inject(method = "placeFood", at = @At("HEAD"))
  private void polymorph$beforePlaceFood(ServerLevel level, @Nullable LivingEntity entity,
                                         ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
    CampfireBlockEntity campfire = (CampfireBlockEntity) (Object) this;

    if (PolymorphApi.getInstance().getBlockEntityRecipeData(campfire) instanceof
        CampfireRecipeData data) {
      data.syncSlots();
    }
  }

  // The indicator of a slot goes once its item is cooked, and so does that slot's pick.
  @Inject(method = "cookTick", at = @At("TAIL"))
  private static void polymorph$afterCookTick(ServerLevel level, BlockPos pos, BlockState state,
                                              CampfireBlockEntity campfire,
                                              RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipeCache,
                                              CallbackInfo ci) {

    if (PolymorphApi.getInstance().getBlockEntityRecipeData(campfire) instanceof
        CampfireRecipeData data && data.syncSlots()) {
      PolymorphInWorld.refresh(level, pos);
    }
  }
}
