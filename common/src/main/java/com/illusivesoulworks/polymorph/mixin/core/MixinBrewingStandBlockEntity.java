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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public class MixinBrewingStandBlockEntity {

  @Inject(at = @At("HEAD"), method = "tick")
  private void polymorph$enterTick(CallbackInfo ci) {
    BrewingCandidates.exit();
    BrewingStandBlockEntity blockEntity = (BrewingStandBlockEntity) (Object) this;
    Level level = blockEntity.getLevel();

    if (level != null && !level.isClientSide()) {
      BrewingCandidates.enter(blockEntity);
    }
  }

  @Inject(at = @At("RETURN"), method = "tick")
  private void polymorph$exitTick(CallbackInfo ci) {
    BrewingCandidates.exit();
  }
}
