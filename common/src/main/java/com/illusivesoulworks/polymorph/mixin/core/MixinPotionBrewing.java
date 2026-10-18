package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.common.brewing.BrewingCandidates;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public class MixinPotionBrewing {

  @Inject(at = @At("HEAD"), method = "mix", cancellable = true)
  private void polymorph$mix(ItemStack ingredient, ItemStack source,
                             CallbackInfoReturnable<ItemStack> cir) {
    ItemStack chosen =
        BrewingCandidates.resolveForCurrentStand((PotionBrewing) (Object) this, ingredient, source);

    if (chosen != null) {
      cir.setReturnValue(chosen);
    }
  }
}
