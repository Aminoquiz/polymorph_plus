package com.shiver.polymorphlegacy.mixin.tconstruct;

import com.shiver.polymorphlegacy.compat.tconstruct.TinkerCraftingContext;
import com.shiver.polymorphlegacy.api.CraftingContextProvider;
import com.shiver.polymorphlegacy.crafting.CraftingService;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.shared.inventory.InventoryCraftingPersistent;
import slimeknights.tconstruct.tools.common.inventory.ContainerCraftingStation;

@Mixin(value = ContainerCraftingStation.class, remap = false)
public abstract class MixinCraftingStation implements CraftingContextProvider {
    @Shadow @Final private EntityPlayer player;
    @Shadow @Final private InventoryCraftingPersistent craftMatrix;
    @Unique private TinkerCraftingContext polymorph$context;

    @Override
    @Nullable
    public TinkerCraftingContext polymorph$getCraftingContext() {
        if (craftMatrix == null) {
            return null;
        }
        if (polymorph$context == null) {
            polymorph$context = new TinkerCraftingContext((ContainerCraftingStation) (Object) this, player);
        }
        return polymorph$context;
    }

    @Redirect(method = "slotChangedCraftingGrid", remap = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/crafting/IRecipe;matches(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Z"))
    private boolean polymorph$checkCachedRecipe(IRecipe recipe, InventoryCrafting matrix, World world) {
        // 服务端每次刷新都重新枚举候选，避免仍然有效的缓存配方挡住用户选择。
        return world.isRemote && recipe.matches(matrix, world);
    }

    @Redirect(method = "slotChangedCraftingGrid", remap = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/crafting/CraftingManager;findMatchingRecipe(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Lnet/minecraft/item/crafting/IRecipe;"))
    private IRecipe polymorph$resolve(InventoryCrafting matrix, World world) {
        return CraftingService.resolve((Container) (Object) this, matrix, world, player);
    }

    @Inject(method = "syncResultToAllOpenWindows", at = @At("RETURN"))
    private void polymorph$syncViewers(ItemStack stack, List<EntityPlayerMP> viewers, CallbackInfo ci) {
        polymorph$getCraftingContext().syncViewers(viewers);
    }
}
