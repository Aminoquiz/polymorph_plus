package com.shiver.polymorphlegacy.crafting;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

public interface FurnaceSelectionHolder {
    @Nullable
    ResourceLocation polymorph$getFurnaceSelection();

    void polymorph$setFurnaceSelection(@Nullable ResourceLocation id);
}
