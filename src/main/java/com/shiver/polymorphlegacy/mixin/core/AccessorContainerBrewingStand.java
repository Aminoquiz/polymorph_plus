package com.shiver.polymorphlegacy.mixin.core;

import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.IInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ContainerBrewingStand.class)
public interface AccessorContainerBrewingStand {
    @Accessor("tileBrewingStand")
    IInventory polymorph$getTileBrewingStand();
}
