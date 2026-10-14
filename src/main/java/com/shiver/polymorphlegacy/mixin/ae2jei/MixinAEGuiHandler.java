package com.shiver.polymorphlegacy.mixin.ae2jei;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.AEGuiHandler;
import com.shiver.polymorphlegacy.client.ClientRecipes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AEGuiHandler.class, remap = false)
public abstract class MixinAEGuiHandler {
    @Inject(method = "getIngredientUnderMouse(Lappeng/client/gui/AEBaseGui;II)Ljava/lang/Object;",
            at = @At("HEAD"), cancellable = true)
    private void polymorph$ingredient(AEBaseGui gui, int mouseX, int mouseY, CallbackInfoReturnable<Object> cir) {
        ItemStack ingredient = ClientRecipes.ingredientUnderMouse(gui, mouseX, mouseY);
        if (ingredient != null) {
            cir.setReturnValue(ingredient);
        }
    }
}
