package com.shiver.polymorphlegacy.mixin.jei;

import com.shiver.polymorphlegacy.client.ClientRecipes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.transfer.RecipeTransferUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeTransferUtil.class, remap = false)
public abstract class MixinRecipeTransferUtil {
    @Inject(method = "transferRecipe(Lnet/minecraft/inventory/Container;Lmezz/jei/gui/recipes/RecipeLayout;Lnet/minecraft/entity/player/EntityPlayer;ZZ)Lmezz/jei/api/recipe/transfer/IRecipeTransferError;",
            at = @At("RETURN"))
    private static void polymorph$afterTransfer(Container container, RecipeLayout layout,
            EntityPlayer player, boolean maxTransfer, boolean doTransfer,
            CallbackInfoReturnable<IRecipeTransferError> cir) {
        if (!doTransfer || cir.getReturnValue() != null
                || !VanillaRecipeCategoryUid.CRAFTING.equals(layout.getRecipeCategory().getUid())) {
            return;
        }
        IRecipeWrapper wrapper = ((AccessorRecipeLayout) layout).polymorph$getRecipeWrapper();
        if (wrapper instanceof ICraftingRecipeWrapper) {
            ResourceLocation id = ((ICraftingRecipeWrapper) wrapper).getRegistryName();
            if (id != null) {
                ClientRecipes.selectFromJei(container, id);
            }
        }
    }
}
