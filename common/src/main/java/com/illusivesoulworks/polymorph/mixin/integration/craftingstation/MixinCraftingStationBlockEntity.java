package com.illusivesoulworks.polymorph.mixin.integration.craftingstation;

import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "tfar.craftingstation.blockentity.CraftingStationBlockEntity", remap = false)
public abstract class MixinCraftingStationBlockEntity {

  // calcResult runs from setChanged with a null player, so the pick lives on the block entity.
  @Inject(at = @At("HEAD"), method = "calcResult")
  private void polymorph$preCalcResult(Player player, CallbackInfoReturnable<ItemStack> cir) {
    polymorph$setContext(this);
  }

  @Inject(at = @At("RETURN"), method = "calcResult")
  private void polymorph$postCalcResult(Player player, CallbackInfoReturnable<ItemStack> cir) {
    polymorph$setContext(null);
  }

  private void polymorph$setContext(Object context) {
    Level level = ((BlockEntity) (Object) this).getLevel();

    if (level != null && level.getRecipeManager() instanceof IRecipeContext recipeContext) {
      recipeContext.polymorph$setContext(context);
    }
  }
}
