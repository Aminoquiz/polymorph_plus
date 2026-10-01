package com.illusivesoulworks.polymorph.mixin.integration.jei;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import mezz.jei.common.network.ServerPacketContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// JEI 31 split the transfer into four server-bound packets, two of them kept for older clients.
@Mixin(targets = {
    "mezz.jei.common.network.packets.PacketRecipeTransferWithResult",
    "mezz.jei.common.network.packets.PacketRecipeTransferCountedWithResult",
    "mezz.jei.common.network.packets.legacy.PacketRecipeTransfer",
    "mezz.jei.common.network.packets.legacy.PacketRecipeTransferCounted"}, remap = false)
public class MixinPacketRecipeTransfer {

  @Inject(
      at = @At("TAIL"),
      method = "process"
  )
  private void polymorph$process(ServerPacketContext context, CallbackInfo ci) {
    PolymorphApi.getInstance().getNetwork().sendRecipeHandshakeS2C(context.player());
  }
}
