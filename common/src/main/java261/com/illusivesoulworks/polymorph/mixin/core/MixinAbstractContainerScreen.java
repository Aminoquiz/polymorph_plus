package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.client.PolymorphClientEvents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

  // The loaders' after-render hooks run once the tooltip stratum is already laid down, which
  // put the selector on top of every tooltip. Here it lands above the GUI contents and below
  // the carried item and the tooltips.
  @Inject(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractCarriedItem(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"),
      method = "extractRenderState")
  private void polymorph$extractSelector(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                         float partialTicks, CallbackInfo ci) {
    graphics.nextStratum();
    PolymorphClientEvents.render((Screen) (Object) this, graphics, mouseX, mouseY, partialTicks);
  }
}
