package com.shiver.polymorphlegacy.mixin.core;

import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ContainerFurnace.class)
public interface AccessorContainerFurnace {
    @Accessor("tileFurnace")
    IInventory polymorph$getTileFurnace();
}
