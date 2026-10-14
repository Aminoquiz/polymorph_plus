package com.shiver.polymorphlegacy.api;

import com.shiver.polymorphlegacy.crafting.CraftingService;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

/** 自定义合成容器的服务端接入入口。 */
public final class PolymorphApi {
    private PolymorphApi() {
    }

    /** 计算结果槽时替代 CraftingManager.findMatchingRecipe。 */
    @Nullable
    public static IRecipe resolve(Container container, InventoryCrafting matrix, World world,
            EntityPlayer player) {
        return CraftingService.resolve(container, matrix, world, player);
    }

    /** 自定义容器更新结果槽后，同步候选配方。 */
    public static void sync(Container container, EntityPlayer player) {
        CraftingService.sync(container, player);
    }

    /** Recipe Polymorph resolved last for this container (server side), or null. */
    @Nullable
    public static IRecipe getActiveRecipe(Container container) {
        CraftingContext context = CraftingContext.of(container);
        return context == null ? null : CraftingService.state(context).getActiveRecipe();
    }

    /** 自定义结果槽取物时，固定本次使用的配方及返还物。 */
    public static CraftingSession beginCraft(EntityPlayer player, InventoryCrafting matrix) {
        return new CraftingSession(CraftingService.beginCraft(player, matrix));
    }
}
