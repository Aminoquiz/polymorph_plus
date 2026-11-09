package com.illusivesoulworks.polymorph.mixin.integration.hephaestus;

import com.illusivesoulworks.polymorph.common.integration.hephaestus.HephaestusModule;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "com.titammods.block.SmelteryTankBlockEntity$1", remap = false)
public abstract class MixinSmelteryTankContents {

  @Inject(method = "onContentsChanged", at = @At("TAIL"))
  private void polymorph$afterContentsChanged(CallbackInfo ci) {
    BlockEntity tank = HephaestusModule.owner(this);

    if (tank != null) {
      HephaestusModule.markAroundSource(tank.getLevel(), tank.getBlockPos());
    }
  }
}
