package com.illusivesoulworks.polymorph.mixin.integration.fastsuite;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import dev.shadowsoffire.fastsuite.AuxRecipeManager;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(value = AuxRecipeManager.class, remap = false)
public class MixinAuxRecipeManager {

  // AuxRecipeManager overrides getRecipeFor, so the RecipeManager hook never runs for it. The
  // block entity context is still set on this instance through the RecipeManager mixin.
  @Inject(
      at = @At("HEAD"),
      method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;",
      cancellable = true)
  private <I extends RecipeInput, T extends Recipe<I>> void polymorph$getRecipe(
      RecipeType<T> recipeType, I input, Level level, @Nullable RecipeHolder<T> last,
      CallbackInfoReturnable<Optional<RecipeHolder<T>>> cb) {

    if (((IRecipeContext) this).polymorph$getContext() instanceof BlockEntity blockEntity &&
        PolymorphApi.getInstance().getBlockEntityRecipeData(blockEntity) != null) {
      PolymorphApi.getInstance().getRecipeManager()
          .getBlockEntityRecipe(recipeType, input, level, blockEntity)
          .ifPresent(recipe -> cb.setReturnValue(Optional.of(recipe)));
    }
  }
}
