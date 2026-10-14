package com.illusivesoulworks.polymorph.mixin.integration.roughlyenoughitems;

import com.illusivesoulworks.polymorph.common.integration.util.RecipeTransfer;
import java.util.Collection;
import java.util.function.Supplier;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.client.gui.widget.InternalWidgets;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InternalWidgets.class, remap = false)
public class MixinInternalWidgets {

  @Inject(
      at = @At("HEAD"),
      method = "lambda$createAutoCraftingButtonWidget$0"
  )
  private static void polymorph$create(Supplier<Display> displaySupplier,
                                       Supplier<Collection<ResourceLocation>> idsSupplier,
                                       Button button, CallbackInfo ci) {
    displaySupplier.get().getDisplayLocation().ifPresent(RecipeTransfer::selectRecipe);
  }
}
