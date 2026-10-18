package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.common.brewing.BrewingCandidates;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public class MixinBrewingStandBlockEntity {

  @Inject(at = @At("HEAD"), method = "serverTick")
  private static void polymorph$preTick(Level level, BlockPos pos, BlockState state,
                                        BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
    BrewingCandidates.setCurrentStand(blockEntity);
  }

  @Inject(at = @At("RETURN"), method = "serverTick")
  private static void polymorph$postTick(Level level, BlockPos pos, BlockState state,
                                         BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
    BrewingCandidates.setCurrentStand(null);
  }
}
