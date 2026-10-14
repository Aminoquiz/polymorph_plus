package com.shiver.polymorphlegacy.mixin.ae2;

import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.slot.SlotCraftingTerm;
import appeng.helpers.IContainerCraftingPacket;
import com.shiver.polymorphlegacy.compat.ae2.Ae2CraftingContainer;
import com.shiver.polymorphlegacy.compat.ae2.Ae2CraftingContext;
import com.shiver.polymorphlegacy.crafting.CraftingService;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
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

@Mixin(value = SlotCraftingTerm.class, remap = false)
public abstract class MixinSlotCraftingTerm {
    @Shadow(remap = false) @Final private IContainerCraftingPacket container;
    @Unique private Ae2CraftingContext polymorph$craftingContext;

    @Unique
    @Nullable
    private Ae2CraftingContext polymorph$getContext() {
        // SlotPatternTerm 也继承此槽位；只有两个已适配的合成容器才能进入这里。
        return container instanceof Ae2CraftingContainer
                ? (Ae2CraftingContext) ((Ae2CraftingContainer) container).polymorph$getCraftingContext() : null;
    }

    @Inject(method = "craftItem", at = @At("HEAD"))
    private void polymorph$begin(EntityPlayer player, ItemStack request, IMEMonitor<IAEItemStack> inventory,
            IItemList<?> items, CallbackInfoReturnable<ItemStack> cir) {
        Ae2CraftingContext context = polymorph$getContext();
        if (context != null && !player.world.isRemote && player.openContainer == context.container) {
            context.detectChanges();
            CraftingService.state(context).beginCraft();
            polymorph$craftingContext = context;
        }
    }

    @Inject(method = "craftItem", at = @At("RETURN"))
    private void polymorph$finish(EntityPlayer player, ItemStack request, IMEMonitor<IAEItemStack> inventory,
            IItemList<?> items, CallbackInfoReturnable<ItemStack> cir) {
        CraftingService.finishCraft(polymorph$craftingContext);
        polymorph$craftingContext = null;
    }

    @Redirect(method = "findRecipe", at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/item/crafting/CraftingManager;findMatchingRecipe(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Lnet/minecraft/item/crafting/IRecipe;"))
    private IRecipe polymorph$findRecipe(InventoryCrafting matrix, World world) {
        Ae2CraftingContext context = polymorph$getContext();
        // 保留 findRecipe 后续的 handleRecipe 阶段校验。
        return context == null ? CraftingManager.findMatchingRecipe(matrix, world) : context.recipeForCrafting(matrix, world);
    }

    @Redirect(method = "getRemainingItems", at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/item/crafting/CraftingManager;getRemainingItems(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Lnet/minecraft/util/NonNullList;"))
    private NonNullList<ItemStack> polymorph$remainders(InventoryCrafting matrix, World world) {
        return CraftingService.remainingItems(polymorph$craftingContext, matrix, world);
    }
}
