package com.shiver.polymorphlegacy.mixin.core;

import com.shiver.polymorphlegacy.crafting.FurnaceSelectionHolder;
import com.shiver.polymorphlegacy.crafting.FurnaceService;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityFurnace.class)
public abstract class MixinTileEntityFurnace implements FurnaceSelectionHolder {
    @Unique private ResourceLocation polymorph$selection;

    @Override
    @Nullable
    public ResourceLocation polymorph$getFurnaceSelection() {
        return polymorph$selection;
    }

    @Override
    public void polymorph$setFurnaceSelection(@Nullable ResourceLocation id) {
        polymorph$selection = id;
    }

    @Redirect(method = {"canSmelt", "smeltItem"}, require = 2, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/crafting/FurnaceRecipes;getSmeltingResult(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack polymorph$result(FurnaceRecipes recipes, ItemStack input) {
        return FurnaceService.result((TileEntityFurnace) (Object) this, input, recipes.getSmeltingResult(input));
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void polymorph$read(NBTTagCompound compound, CallbackInfo ci) {
        polymorph$selection = compound.hasKey("polymorph_plus:SelectedRecipe", 8)
                ? new ResourceLocation(compound.getString("polymorph_plus:SelectedRecipe")) : null;
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void polymorph$write(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> cir) {
        if (polymorph$selection != null) {
            cir.getReturnValue().setString("polymorph_plus:SelectedRecipe", polymorph$selection.toString());
        }
    }
}
