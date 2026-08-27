package com.illusivesoulworks.polymorph.mixin.core;

import java.util.Optional;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
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
          target = "net/minecraft/world/item/crafting/RecipeCache.validateRecipeManager(Lnet/minecraft/world/level/Level;)V",
          shift = At.Shift.AFTER
      ),
      method = "get(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/CraftingInput;)Ljava/util/Optional;",
      cancellable = true
  )
  private void polymorph$get(Level level, CraftingInput craftingInput,
                             CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
    cir.setReturnValue(this.compute(craftingInput, level));
  }

  @Shadow
  protected abstract Optional<RecipeHolder<CraftingRecipe>> compute(CraftingInput craftingInput,
                                                                    Level level);
}
