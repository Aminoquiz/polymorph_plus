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

import com.illusivesoulworks.polymorph.common.PolymorphCommonEvents;
import java.util.OptionalInt;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Plain mixin (no `extends Player`) to avoid coupling to the Player constructor whose
// signature shifted in 26.1. containerMenu is declared on Player (parent), so @Shadow
// on a ServerPlayer mixin cannot resolve it. Access it directly through the cast ref.
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer {

  @Inject(
      at = @At("RETURN"),
      method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;")
  private void polymorph$openHandledScreen(CallbackInfoReturnable<OptionalInt> cir) {
    cir.getReturnValue().ifPresent(value -> {
      ServerPlayer self = (ServerPlayer) (Object) this;
      PolymorphCommonEvents.openContainer(self, self.containerMenu);
    });
  }
}
