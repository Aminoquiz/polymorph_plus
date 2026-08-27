package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level {

  protected MixinServerLevel(WritableLevelData $$0,
                             ResourceKey<Level> $$1,
                             RegistryAccess $$2,
                             Holder<DimensionType> $$3,
                             Supplier<ProfilerFiller> $$4,
                             boolean $$5, boolean $$6, long $$7, int $$8) {
    super($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7, $$8);
  }

  @Inject(
      at = @At(
          value = "INVOKE",
          target = "net/minecraft/world/level/block/state/BlockState.tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"
      ),
      method = "tickBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V"
  )
  private void polymorph$preTickBlock(BlockPos pos, Block block, CallbackInfo ci) {

    if (this.getRecipeManager() instanceof IRecipeContext recipeContext) {
      BlockEntity blockEntity = this.getBlockEntity(pos);

      if (blockEntity != null) {
        recipeContext.polymorph$setContext(blockEntity);
      }
    }
  }

  @Inject(
      at = @At(
          value = "INVOKE",
          target = "net/minecraft/world/level/block/state/BlockState.tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
          shift = At.Shift.AFTER
      ),
      method = "tickBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V"
  )
  private void polymorph$postTickBlock(BlockPos pos, Block block, CallbackInfo ci) {

    if (this.getRecipeManager() instanceof IRecipeContext recipeContext) {
      recipeContext.polymorph$setContext(null);
    }
  }

  @Nonnull
  @Shadow
  public abstract RecipeManager getRecipeManager();
}
