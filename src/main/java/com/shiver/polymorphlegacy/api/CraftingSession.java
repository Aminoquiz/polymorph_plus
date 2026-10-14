package com.shiver.polymorphlegacy.api;

import com.shiver.polymorphlegacy.crafting.CraftingService;
import javax.annotation.Nullable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

/** 自定义结果槽取物结束后应关闭的合成会话。 */
public final class CraftingSession implements AutoCloseable {
    @Nullable
    private CraftingContext context;

    CraftingSession(@Nullable CraftingContext context) {
        this.context = context;
    }

    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting matrix, World world) {
        return CraftingService.remainingItems(context, matrix, world);
    }

    @Override
    public void close() {
        CraftingService.finishCraft(context);
        context = null;
    }
}
