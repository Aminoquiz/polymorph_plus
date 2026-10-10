package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.client.PolymorphClientEvents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(AbstractRecipeBookScreen.class)
public class MixinAbstractRecipeBookScreen {

  // Crafting table, inventory and furnaces render here and never reach the hook in
  // MixinAbstractContainerScreen.
  @Inject(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/gui/screens/inventory/AbstractRecipeBookScreen;extractCarriedItem(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"),
      method = "extractRenderState")
  private void polymorph$extractSelector(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                         float partialTicks, CallbackInfo ci) {
    graphics.nextStratum();
    PolymorphClientEvents.render((Screen) (Object) this, graphics, mouseX, mouseY, partialTicks);
  }
}
