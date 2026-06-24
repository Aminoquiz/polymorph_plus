package com.illusivesoulworks.polymorph.mixin.integration.quickbench;

import com.illusivesoulworks.polymorph.mixin.PlayerHolder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingMenu.class, priority = 2000)
public class MixinCraftingMenu {

  @Shadow
  @Final
  private Player player;


  @Inject(
      method = "slotsChanged",
      at = @At("HEAD"))
  private void polymorph$slotsChangedPre(Container container, CallbackInfo ci) {

    if (!this.player.level().isClientSide()) {
      PlayerHolder.setPlayer(this.player);
    }
  }

  @Inject(
      method = "slotsChanged",
      at = @At("RETURN"))
  private void polymorph$slotsChangedPost(Container container, CallbackInfo ci) {
    PlayerHolder.setPlayer(null);
  }
}
