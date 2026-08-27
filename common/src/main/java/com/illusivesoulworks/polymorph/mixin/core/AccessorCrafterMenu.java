package com.illusivesoulworks.polymorph.mixin.core;

import net.minecraft.world.inventory.CrafterMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrafterMenu.class)
public interface AccessorCrafterMenu {

  @Invoker
  void callRefreshRecipeResult();
}
