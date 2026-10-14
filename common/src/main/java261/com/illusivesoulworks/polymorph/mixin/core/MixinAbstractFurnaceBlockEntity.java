/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceBlockEntity.class)
public class MixinAbstractFurnaceBlockEntity {

  // setItem resolves the recipe for the cook time outside the block entity tick, where
  // MixinLevelChunk has no context set. FastFurnace caches that lookup in curRecipe and never
  // asks the RecipeManager again, so without a context here Polymorph never sees the furnace.
  // NeoForge routes setItem(int, ItemStack) through setItem(int, ItemStack, boolean); Fabric
  // calls getTotalCookTime from the two-argument form.
  @WrapOperation(
      method = {"setItem(ILnet/minecraft/world/item/ItemStack;)V",
          "setItem(ILnet/minecraft/world/item/ItemStack;Z)V"},
      require = 0,
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;getTotalCookTime(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I"
      )
  )
  private int polymorph$cookTimeWithContext(ServerLevel level, AbstractFurnaceBlockEntity entity,
                                            Operation<Integer> original) {

    if (!(level.recipeAccess() instanceof IRecipeContext recipeContext)) {
      return original.call(level, entity);
    }
    Object previous = recipeContext.polymorph$getContext();
    recipeContext.polymorph$setContext(entity);

    try {
      return original.call(level, entity);
    } finally {
      recipeContext.polymorph$setContext(previous);
    }
  }
}
