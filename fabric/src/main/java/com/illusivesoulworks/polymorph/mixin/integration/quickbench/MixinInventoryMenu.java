package com.illusivesoulworks.polymorph.mixin.integration.quickbench;

import com.illusivesoulworks.polymorph.mixin.PlayerHolder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryMenu.class, priority = 2000)
public class MixinInventoryMenu {

  @Shadow
  @Final
  private Player owner;


  @Inject(
      method = "slotsChanged",
      at = @At("HEAD"))
  private void polymorph$slotsChangedPre(Container container, CallbackInfo ci) {

    if (!this.owner.level().isClientSide()) {
      PlayerHolder.setPlayer(this.owner);
    }
  }

  @Inject(
      method = "slotsChanged",
      at = @At("RETURN"))
  private void polymorph$slotsChangedPost(Container container, CallbackInfo ci) {
    PlayerHolder.setPlayer(null);
  }
}
