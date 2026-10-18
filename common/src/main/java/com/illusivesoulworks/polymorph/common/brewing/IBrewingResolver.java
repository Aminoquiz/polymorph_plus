package com.illusivesoulworks.polymorph.common.brewing;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;

public interface IBrewingResolver {

  /**
   * @return the output to brew, or null to leave the stand to vanilla (no conflict).
   */
  @Nullable
  ItemStack resolveBrew(PotionBrewing brewing, ItemStack reagent, ItemStack bottle);
}
