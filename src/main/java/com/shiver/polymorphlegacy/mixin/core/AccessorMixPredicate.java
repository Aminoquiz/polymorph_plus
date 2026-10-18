package com.shiver.polymorphlegacy.mixin.core;

import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.potion.PotionHelper$MixPredicate")
public interface AccessorMixPredicate {
    @Accessor("input")
    IRegistryDelegate<?> polymorph$getInput();

    @Accessor("reagent")
    Ingredient polymorph$getReagent();

    @Accessor("output")
    IRegistryDelegate<?> polymorph$getOutput();
}
