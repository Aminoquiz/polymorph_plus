package com.illusivesoulworks.polymorph.mixin.core;

import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SmithingScreen.class)
public interface AccessorSmithingScreen {

  @Invoker
  void callUpdateArmorStandPreview(ItemStack stack);
}
