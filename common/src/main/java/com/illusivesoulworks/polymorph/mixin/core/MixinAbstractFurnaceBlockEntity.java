/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class MixinAbstractFurnaceBlockEntity {

  @Unique
  private Object polymorph$previousContext;

  // setItem resolves the recipe for the cook time outside the block entity tick, where
  // MixinLevelChunk has no context set. FastFurnace caches that lookup in curRecipe and never
  // asks the RecipeManager again, so without a context here Polymorph never sees the furnace.
  // A before/after pair instead of WrapOperation: this tree has no MixinExtras on the common
  // classpath.
  @Inject(
      method = "setItem(ILnet/minecraft/world/item/ItemStack;)V",
      require = 0,
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;getTotalCookTime(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I"
      )
  )
  private void polymorph$preCookTime(int slot, ItemStack stack, CallbackInfo ci) {
    IRecipeContext recipeContext = this.polymorph$recipeContext();

    if (recipeContext != null) {
      this.polymorph$previousContext = recipeContext.polymorph$getContext();
      recipeContext.polymorph$setContext(this);
    }
  }

  @Inject(
      method = "setItem(ILnet/minecraft/world/item/ItemStack;)V",
      require = 0,
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;getTotalCookTime(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I",
          shift = At.Shift.AFTER
      )
  )
  private void polymorph$postCookTime(int slot, ItemStack stack, CallbackInfo ci) {
    IRecipeContext recipeContext = this.polymorph$recipeContext();

    if (recipeContext != null) {
      recipeContext.polymorph$setContext(this.polymorph$previousContext);
      this.polymorph$previousContext = null;
    }
  }

  @Unique
  private IRecipeContext polymorph$recipeContext() {
    Level level = ((AbstractFurnaceBlockEntity) (Object) this).getLevel();
    return level != null && level.getRecipeManager() instanceof IRecipeContext context ? context :
        null;
  }
}
