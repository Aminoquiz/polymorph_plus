package com.illusivesoulworks.polymorph.mixin.core;

import java.util.List;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PotionBrewing.class)
public interface AccessorPotionBrewing {

  // Elements are the private PotionBrewing.Mix record, read through AccessorPotionMix.
  @Accessor("potionMixes")
  List<?> getPotionMixes();

  @Accessor("containerMixes")
  List<?> getContainerMixes();
}
