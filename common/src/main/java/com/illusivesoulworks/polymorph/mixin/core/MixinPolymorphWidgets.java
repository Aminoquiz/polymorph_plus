package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.client.PolymorphWidgetsImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PolymorphWidgets.class, remap = false)
public class MixinPolymorphWidgets {

  @Inject(at = @At("HEAD"), method = "getInstance", cancellable = true)
  private static void polymorph$getInstance(final CallbackInfoReturnable<PolymorphWidgets> cir) {
    cir.setReturnValue(PolymorphWidgetsImpl.INSTANCE);
  }
}
