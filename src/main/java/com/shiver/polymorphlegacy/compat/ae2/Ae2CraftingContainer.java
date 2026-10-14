package com.shiver.polymorphlegacy.compat.ae2;

import com.shiver.polymorphlegacy.api.CraftingContextProvider;
import javax.annotation.Nullable;
import net.minecraft.item.crafting.IRecipe;

public interface Ae2CraftingContainer extends CraftingContextProvider {
    void polymorph$setRecipe(@Nullable IRecipe recipe);
}
