/*
 * Copyright (C) 2020-2022 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Polymorph.  If not, see <https://www.gnu.org/licenses/>.
 */

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
  private static void polymorph$enterTick(Level level, BlockPos pos, BlockState state,
                                          BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
    BrewingCandidates.enter(blockEntity);
  }

  @Inject(at = @At("RETURN"), method = "serverTick")
  private static void polymorph$exitTick(Level level, BlockPos pos, BlockState state,
                                         BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
    BrewingCandidates.exit();
  }
}
