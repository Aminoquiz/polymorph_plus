package com.shiver.polymorphlegacy.mixin.core;

import com.shiver.polymorphlegacy.api.CraftingContext;
import com.shiver.polymorphlegacy.crafting.CraftingService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlotCrafting.class)
public abstract class MixinSlotCrafting {
    @Shadow @Final private InventoryCrafting craftMatrix;
    @Unique private CraftingContext polymorph$craftingContext;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void polymorph$begin(EntityPlayer player, ItemStack output,
            CallbackInfoReturnable<ItemStack> cir) {
        // Shift 路径会提前清空 InventoryCraftResult.recipeUsed，因此从独立状态取配方。
        polymorph$craftingContext = CraftingService.beginCraft(player, craftMatrix);
    }

    @Redirect(method = "onTake", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/crafting/CraftingManager;getRemainingItems(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Lnet/minecraft/util/NonNullList;"))
    private NonNullList<ItemStack> polymorph$remainders(InventoryCrafting matrix, World world) {
        return CraftingService.remainingItems(polymorph$craftingContext, matrix, world);
    }

    @Inject(method = "onTake", at = @At("RETURN"))
    private void polymorph$finish(EntityPlayer player, ItemStack output,
            CallbackInfoReturnable<ItemStack> cir) {
        CraftingService.finishCraft(polymorph$craftingContext);
        polymorph$craftingContext = null;
    }
}
