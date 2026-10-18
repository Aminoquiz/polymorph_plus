package com.shiver.polymorphlegacy.mixin.core;

import com.shiver.polymorphlegacy.crafting.BrewingService;
import com.shiver.polymorphlegacy.crafting.FurnaceSelectionHolder;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityBrewingStand.class)
public abstract class MixinTileEntityBrewingStand implements FurnaceSelectionHolder {
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

    // canBrew needs no hook: the pick changes which output a bottle gets, never whether it brews.
    @Redirect(method = "brewPotions", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/common/brewing/BrewingRecipeRegistry;brewPotions(Lnet/minecraft/util/NonNullList;Lnet/minecraft/item/ItemStack;[I)V",
            remap = false))
    private void polymorph$brew(NonNullList<ItemStack> inputs, ItemStack reagent, int[] slots) {
        for (int i : slots) {
            ItemStack output = BrewingService.result((TileEntityBrewingStand) (Object) this, inputs.get(i), reagent);
            if (!output.isEmpty()) {
                inputs.set(i, output);
            }
        }
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
