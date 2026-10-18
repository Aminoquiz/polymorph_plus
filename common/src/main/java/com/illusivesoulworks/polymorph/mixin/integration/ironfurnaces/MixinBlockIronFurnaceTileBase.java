package com.illusivesoulworks.polymorph.mixin.integration.ironfurnaces;

import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "ironfurnaces.tileentity.furnaces.BlockIronFurnaceTileBase", remap = false)
public abstract class MixinBlockIronFurnaceTileBase {

  // getRecipeHolderFor is static and calls the 3-arg RecipeManager.getRecipeFor, which reads this context.
  @Inject(at = @At("HEAD"), method = "getRecipeNonCached")
  private void polymorph$preGetRecipe(ItemStack stack, CallbackInfoReturnable<RecipeHolder<?>> cir) {
    polymorph$setContext(this);
  }

  @Inject(at = @At("RETURN"), method = "getRecipeNonCached")
  private void polymorph$postGetRecipe(ItemStack stack, CallbackInfoReturnable<RecipeHolder<?>> cir) {
    polymorph$setContext(null);
  }

  private void polymorph$setContext(Object context) {
    Level level = ((BlockEntity) (Object) this).getLevel();

    if (level instanceof ServerLevel serverLevel
        && serverLevel.getServer().getRecipeManager() instanceof IRecipeContext recipeContext) {
      recipeContext.polymorph$setContext(context);
    }
  }
}
