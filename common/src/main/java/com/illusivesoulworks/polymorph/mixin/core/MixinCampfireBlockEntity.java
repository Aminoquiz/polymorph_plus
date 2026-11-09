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
import com.illusivesoulworks.polymorph.api.inworld.PolymorphInWorld;
import com.illusivesoulworks.polymorph.common.capability.CampfireRecipeData;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
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

  // CampfireBlock asks this when a player places food, outside the block entity tick where
  // MixinLevelChunk sets the context. With the context set, the candidates exist as soon as the
  // food lands, so the indicator shows before the first item finishes cooking.
  @Inject(method = "getCookableRecipe", at = @At("HEAD"))
  private void polymorph$preCookable(ItemStack stack,
                                     CallbackInfoReturnable<Optional<RecipeHolder<CampfireCookingRecipe>>> cir) {
    IRecipeContext recipeContext = this.polymorph$recipeContext();

    if (recipeContext != null) {
      this.polymorph$previousContext = recipeContext.polymorph$getContext();
      recipeContext.polymorph$setContext(this);
    }
  }

  @Inject(method = "getCookableRecipe", at = @At("RETURN"))
  private void polymorph$postCookable(ItemStack stack,
                                      CallbackInfoReturnable<Optional<RecipeHolder<CampfireCookingRecipe>>> cir) {
    IRecipeContext recipeContext = this.polymorph$recipeContext();

    if (recipeContext != null) {
      recipeContext.polymorph$setContext(this.polymorph$previousContext);
      this.polymorph$previousContext = null;
    }
  }

  // CampfireBlock looks the recipe up before placeFood stores the item, so the refresh fired from
  // that lookup saw an empty campfire and sent no indicator. Refresh again once the food is in.
  @Inject(method = "placeFood", at = @At("RETURN"))
  private void polymorph$afterPlaceFood(@Nullable LivingEntity entity, ItemStack stack, int cookTime,
                                        CallbackInfoReturnable<Boolean> cir) {
    CampfireBlockEntity campfire = (CampfireBlockEntity) (Object) this;
    Level level = campfire.getLevel();

    if (cir.getReturnValueZ() && level != null && !level.isClientSide()) {
      PolymorphInWorld.refresh(level, campfire.getBlockPos());
    }
  }

  // A new item must not inherit the pick of the item that sat in its slot before.
  @Inject(method = "placeFood", at = @At("HEAD"))
  private void polymorph$beforePlaceFood(@Nullable LivingEntity entity, ItemStack stack,
                                         int cookTime, CallbackInfoReturnable<Boolean> cir) {
    CampfireBlockEntity campfire = (CampfireBlockEntity) (Object) this;

    if (PolymorphApi.getInstance().getBlockEntityRecipeData(campfire) instanceof
        CampfireRecipeData data) {
      data.syncSlots();
    }
  }

  // The indicator of a slot goes once its item is cooked, and so does that slot's pick.
  @Inject(method = "cookTick", at = @At("TAIL"))
  private static void polymorph$afterCookTick(Level level, BlockPos pos, BlockState state,
                                              CampfireBlockEntity campfire, CallbackInfo ci) {

    if (PolymorphApi.getInstance().getBlockEntityRecipeData(campfire) instanceof
        CampfireRecipeData data && data.syncSlots()) {
      PolymorphInWorld.refresh(level, pos);
    }
  }

  @Unique
  private IRecipeContext polymorph$recipeContext() {
    Level level = ((CampfireBlockEntity) (Object) this).getLevel();
    return level != null && !level.isClientSide()
        && level.getRecipeManager() instanceof IRecipeContext context ? context :
        null;
  }
}
