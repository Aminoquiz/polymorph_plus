package com.illusivesoulworks.polymorph.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Ingredient.class)
public interface AccessorIngredient {

  @Invoker("dissolve")
  void callDissolve();

  @Accessor("itemStacks")
  ItemStack[] getItemStacks();
}
