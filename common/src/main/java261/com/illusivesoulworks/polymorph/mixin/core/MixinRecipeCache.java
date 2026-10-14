/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * 1.21.8 fork. Instead of using @Shadow on the private `compute` method (which fails to
 * resolve at runtime without a refmap on 1.21.8), we @Inject at HEAD of `get` and bypass
 * the cache directly by calling RecipeManager.getRecipeFor when there is a polymorph
 * context. Cache-hit path returns stale results after a selection change, so bypassing
 * the cache is required to make the Crafter respect the selected recipe.
 */
package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import java.util.Optional;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeCache.class)
public class MixinRecipeCache {

  @Inject(at = @At("HEAD"), method = "get", cancellable = true)
  private void polymorph$get(ServerLevel level, CraftingInput craftingInput,
                             CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
    RecipeManager rm = level.recipeAccess();
    if (rm instanceof IRecipeContext ctx && ctx.polymorph$getContext() != null) {
      cir.setReturnValue(rm.getRecipeFor(RecipeType.CRAFTING, craftingInput, level));
    }
  }
}
