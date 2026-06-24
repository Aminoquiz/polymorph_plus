package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
public class MixinLevelChunk<T extends BlockEntity> {

  @Shadow
  @Final
  private T blockEntity;

  @Inject(
      at = @At(
          value = "INVOKE",
          target = "net/minecraft/world/level/block/entity/BlockEntityTicker.tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"
      ),
      method = "tick"
  )
  private void polymorph$preTick(CallbackInfo ci) {
    RecipeManager recipeManager =
        this.blockEntity.getLevel() != null ? this.blockEntity.getLevel().getRecipeManager() : null;

    if (recipeManager instanceof IRecipeContext recipeContext) {
      recipeContext.polymorph$setContext(this.blockEntity);
    }
  }

  @Inject(
      at = @At(
          value = "INVOKE",
          target = "net/minecraft/world/level/block/entity/BlockEntityTicker.tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
          shift = At.Shift.AFTER
      ),
      method = "tick"
  )
  private void polymorph$postTick(CallbackInfo ci) {
    RecipeManager recipeManager =
        this.blockEntity.getLevel() != null ? this.blockEntity.getLevel().getRecipeManager() : null;

    if (recipeManager instanceof IRecipeContext recipeContext) {
      recipeContext.polymorph$setContext(null);
    }
  }
}
