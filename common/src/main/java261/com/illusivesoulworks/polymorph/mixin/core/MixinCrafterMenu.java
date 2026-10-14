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
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrafterMenu.class)
public class MixinCrafterMenu {

  @Shadow
  @Final
  private CraftingContainer container;

  @Inject(
      at = @At("HEAD"),
      method = "refreshRecipeResult"
  )
  private void polymorph$preRefreshRecipeResult(CallbackInfo ci) {

    if (this.container instanceof BlockEntity blockEntity) {
      RecipeAccess recipeAccess = serverRecipeAccess(blockEntity.getLevel());

      if (recipeAccess instanceof IRecipeContext recipeContext) {
        recipeContext.polymorph$setContext(blockEntity);
      }
    }
  }

  @Inject(
      at = @At("TAIL"),
      method = "refreshRecipeResult"
  )
  private void polymorph$postRefreshRecipeResult(CallbackInfo ci) {

    if (this.container instanceof BlockEntity blockEntity) {
      RecipeAccess recipeAccess = serverRecipeAccess(blockEntity.getLevel());

      if (recipeAccess instanceof IRecipeContext recipeContext) {
        recipeContext.polymorph$setContext(null);
      }
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
