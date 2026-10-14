package com.shiver.polymorphlegacy.mixin.ae2;

import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerWirelessCraftingTerminal;
import appeng.container.slot.SlotCraftingTerm;
import com.shiver.polymorphlegacy.compat.ae2.Ae2CraftingContainer;
import com.shiver.polymorphlegacy.compat.ae2.Ae2CraftingContext;
import javax.annotation.Nullable;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {ContainerCraftingTerm.class, ContainerWirelessCraftingTerminal.class}, remap = false)
public abstract class MixinCraftingTerminal implements Ae2CraftingContainer {
    @Shadow(remap = false) private IRecipe currentRecipe;
    @Shadow(remap = false) @Final private SlotCraftingTerm outputSlot;
    @Unique private Ae2CraftingContext polymorph$context;

    @Override
    @Nullable
    public Ae2CraftingContext polymorph$getCraftingContext() {
        if (outputSlot == null) {
            return null;
        }
        if (polymorph$context == null) {
            polymorph$context = new Ae2CraftingContext((AEBaseContainer) (Object) this, outputSlot);
        }
        return polymorph$context;
    }

    @Override
    public void polymorph$setRecipe(@Nullable IRecipe recipe) {
        currentRecipe = recipe;
    }

    @Inject(method = "onCraftMatrixChanged", at = @At("HEAD"), cancellable = true, remap = true)
    private void polymorph$updateRecipe(IInventory inventory, CallbackInfo ci) {
        Ae2CraftingContext context = polymorph$getCraftingContext();
        if (context != null) {
            context.refresh();
            ci.cancel();
        }
    }
}
