/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * MC 26.1 fork. RecipeCache.get / compute / validateRecipeManager all took
 * Level in 1.21; in 26.1 they take ServerLevel. The @Shadow + @Inject targets
 * must match the new descriptors or the mixin is rejected at apply time.
 */
package com.illusivesoulworks.polymorph.mixin.core;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeCache.class)
public abstract class MixinRecipeCache {

  @Inject(
      at = @At(
          value = "INVOKE",
          target = "net/minecraft/world/item/crafting/RecipeCache.validateRecipeManager(Lnet/minecraft/server/level/ServerLevel;)V",
          shift = At.Shift.AFTER
      ),
      method = "get(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/crafting/CraftingInput;)Ljava/util/Optional;",
      cancellable = true
  )
  private void polymorph$get(ServerLevel level, CraftingInput craftingInput,
                             CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
    cir.setReturnValue(this.compute(craftingInput, level));
  }

  @Shadow
  protected abstract Optional<RecipeHolder<CraftingRecipe>> compute(CraftingInput craftingInput,
                                                                    ServerLevel level);
}
