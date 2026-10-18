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

package com.illusivesoulworks.polymorph.mixin.integration.forge;

import com.illusivesoulworks.polymorph.common.brewing.BrewingCandidates;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BrewingRecipeRegistry.class, remap = false)
public class MixinBrewingRecipeRegistry {

  @Inject(at = @At("HEAD"), method = "getOutput", cancellable = true)
  private static void polymorph$getOutput(ItemStack input, ItemStack ingredient,
                                          CallbackInfoReturnable<ItemStack> cir) {
    ItemStack picked = BrewingCandidates.override(input, ingredient);

    if (picked != null) {
      cir.setReturnValue(picked);
    }
  }
}
