package com.shiver.polymorphlegacy.mixin.tconstruct;

import com.shiver.polymorphlegacy.api.client.CraftingGuiOrigin;
import org.spongepowered.asm.mixin.Mixin;
import slimeknights.tconstruct.tools.common.client.GuiCraftingStation;

@Mixin(value = GuiCraftingStation.class, remap = false)
public abstract class MixinGuiCraftingStation implements CraftingGuiOrigin {
    @Override
    public int polymorph$getCraftingLeft() {
        return ((GuiCraftingStation) (Object) this).cornerX;
    }

    @Override
    public int polymorph$getCraftingTop() {
        return ((GuiCraftingStation) (Object) this).cornerY;
    }
}
