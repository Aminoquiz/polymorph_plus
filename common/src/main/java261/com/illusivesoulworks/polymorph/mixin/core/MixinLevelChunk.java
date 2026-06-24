/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * MC 26.1 fork. Level.getRecipeManager() gone — route via ServerLevel.recipeAccess()
 * or MinecraftServer.getRecipeManager().
 */
package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.Level;
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
    RecipeAccess recipeAccess = serverRecipeAccess(this.blockEntity.getLevel());

    if (recipeAccess instanceof IRecipeContext recipeContext) {
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
    RecipeAccess recipeAccess = serverRecipeAccess(this.blockEntity.getLevel());

    if (recipeAccess instanceof IRecipeContext recipeContext) {
      recipeContext.polymorph$setContext(null);
    }
  }

  private static RecipeAccess serverRecipeAccess(Level level) {
    if (level == null) {
      return null;
    }
    if (level instanceof ServerLevel sl) {
      return sl.recipeAccess();
    }
    MinecraftServer server = level.getServer();
    return server != null ? server.getRecipeManager() : null;
  }
}
