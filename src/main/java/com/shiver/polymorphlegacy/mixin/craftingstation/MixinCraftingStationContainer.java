package com.shiver.polymorphlegacy.mixin.craftingstation;

import com.shiver.polymorphlegacy.compat.craftingstation.StationCraftingContext;
import com.shiver.polymorphlegacy.compat.craftingstation.CraftingStationProvider;
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
import com.tfar.craftingstation.CraftingInventoryPersistant;
import com.tfar.craftingstation.CraftingStationContainer;
import com.tfar.craftingstation.CraftingStationTile;

@Mixin(value = CraftingStationContainer.class, remap = false)
public abstract class MixinCraftingStationContainer implements CraftingContextProvider, CraftingStationProvider {
    @Shadow @Final private EntityPlayer player;
    @Shadow @Final public CraftingInventoryPersistant craftMatrix;
    @Shadow @Final private CraftingStationTile tileEntity;
    @Unique private StationCraftingContext polymorph$context;

    @Override
    public CraftingStationTile polymorph$getTile() {
        return tileEntity;
    }

    @Override
    @Nullable
    public StationCraftingContext polymorph$getCraftingContext() {
        if (craftMatrix == null) {
            return null;
        }
        if (polymorph$context == null) {
            polymorph$context = new StationCraftingContext((CraftingStationContainer) (Object) this, player);
        }
        return polymorph$context;
    }

    @Redirect(method = "slotChangedCraftingGrid", remap = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/crafting/IRecipe;matches(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Z"))
    private boolean polymorph$checkCachedRecipe(IRecipe recipe, InventoryCrafting matrix, World world) {
        // The server lists candidates again on every refresh, so a still-valid cached recipe cannot hide the player's pick.
        return world.isRemote && recipe.matches(matrix, world);
    }

    @Redirect(method = "slotChangedCraftingGrid", remap = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/crafting/CraftingManager;findMatchingRecipe(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Lnet/minecraft/item/crafting/IRecipe;"))
    private IRecipe polymorph$resolve(InventoryCrafting matrix, World world) {
        return CraftingService.resolve((Container) (Object) this, matrix, world, player);
    }

    @Inject(method = "syncResultToAllOpenWindows", at = @At("RETURN"))
    private void polymorph$syncViewers(ItemStack stack, List<EntityPlayerMP> viewers, CallbackInfo ci) {
        StationCraftingContext context = polymorph$getCraftingContext();
        if (context != null) {
            context.syncViewers(viewers);
        }
    }
}
