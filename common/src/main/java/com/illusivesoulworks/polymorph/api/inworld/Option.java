package com.illusivesoulworks.polymorph.api.inworld;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record Option(ResourceLocation id, ItemStack icon) {
}
