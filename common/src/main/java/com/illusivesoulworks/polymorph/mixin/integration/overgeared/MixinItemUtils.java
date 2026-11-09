package com.illusivesoulworks.polymorph.mixin.integration.overgeared;

import com.illusivesoulworks.polymorph.common.integration.overgeared.OvergearedModule;
import java.util.Optional;
import java.util.stream.Stream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "net.stirdrem.overgeared.util.ItemUtils", remap = false)
public abstract class MixinItemUtils {

  @Redirect(
      method = "getCooledItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/item/ItemStack;",
      at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;findFirst()Ljava/util/Optional;"))
  private static Optional<?> polymorph$pickCoolingRecipe(Stream<?> matches) {
    return OvergearedModule.pickCoolingRecipe(matches);
  }
}
