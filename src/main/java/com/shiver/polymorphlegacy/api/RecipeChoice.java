package com.shiver.polymorphlegacy.api;

import java.util.Objects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class RecipeChoice {
    private final ResourceLocation id;
    private final ItemStack output;

    public RecipeChoice(ResourceLocation id, ItemStack output) {
        this.id = id;
        this.output = output.copy();
    }

    public ResourceLocation getId() {
        return id;
    }

    public ItemStack getOutput() {
        return output;
    }

    public boolean sameAs(RecipeChoice other) {
        return Objects.equals(id, other.id) && ItemStack.areItemStacksEqual(output, other.output);
    }
}
