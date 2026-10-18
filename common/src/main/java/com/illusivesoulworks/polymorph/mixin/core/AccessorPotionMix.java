package com.illusivesoulworks.polymorph.mixin.core;

import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.item.alchemy.PotionBrewing$Mix")
public interface AccessorPotionMix {

  @Accessor("from")
  Holder<?> getFrom();

  @Accessor("ingredient")
  Ingredient getIngredient();

  @Accessor("to")
  Holder<?> getTo();
}
