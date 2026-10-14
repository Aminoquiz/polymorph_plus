package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.client.PolymorphClientEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

  // Subclasses call renderTooltip after super.render returns, so drawing here keeps the
  // selector under the tooltips. Screens that skip super.render are caught by renderIfMissed.
  @Inject(at = @At("TAIL"), method = "render")
  private void polymorph$renderSelector(PoseStack poseStack, int mouseX, int mouseY,
                                        float partialTicks, CallbackInfo ci) {
    PolymorphClientEvents.render((Screen) (Object) this, poseStack, mouseX, mouseY, partialTicks);
  }
}
