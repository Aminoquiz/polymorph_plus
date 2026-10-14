package com.illusivesoulworks.polymorph.mixin.integration.jei;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PacketRecipeTransfer.class, remap = false)
public class MixinPacketRecipeTransfer {

  @Inject(
      at = @At("TAIL"),
      method = "process"
  )
  private void polymorph$process(ServerPacketContext context, CallbackInfo ci) {
    PolymorphApi.getInstance().getNetwork().sendRecipeHandshakeS2C(context.player());
  }
}
