package com.shiver.polymorphlegacy.crafting;

import com.shiver.polymorphlegacy.api.CraftingContext;
import com.shiver.polymorphlegacy.api.RecipeChoice;
import com.shiver.polymorphlegacy.network.PolymorphNetwork;
import com.shiver.polymorphlegacy.network.RecipesPacket;
import com.shiver.polymorphlegacy.priority.PlayerPreferences;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class CraftingService {
    private CraftingService() {
    }

    @Nullable
    public static IRecipe resolve(Container container, InventoryCrafting matrix, World world,
            EntityPlayer player) {
        CraftingContext context = CraftingContext.of(container);
        if (context == null || !context.ownsMatrix(matrix) || world.isRemote) {
            return CraftingManager.findMatchingRecipe(matrix, world);
        }
        return resolve(context, matrix, world, player);
    }

    @Nullable
    public static IRecipe resolve(CraftingContext context, InventoryCrafting matrix, World world,
            EntityPlayer player) {
        List<IRecipe> recipes = new ArrayList<>();
        List<ResourceLocation> ids = new ArrayList<>();
        List<RecipeChoice> choices = new ArrayList<>();
        List<IRecipe> candidates = context.recipeCandidates();
        Iterable<IRecipe> source = candidates != null ? candidates : ForgeRegistries.RECIPES;
        int index = -1;
        for (IRecipe recipe : source) {
            index++;
            if (!recipe.matches(matrix, world) || candidates == null && !canCraft(recipe, player)) {
                continue;
            }
            ItemStack output = recipe.getCraftingResult(matrix);
            if (output.isEmpty()) {
                continue;
            }
            ResourceLocation id = candidates != null ? context.recipeId(recipe, index) : recipe.getRegistryName();
            recipes.add(recipe);
            ids.add(id);
            choices.add(new RecipeChoice(id, output));
        }
        CraftingState state = state(context);
        ResourceLocation selected = state.getSelection().resolve(ids, PlayerPreferences.ranking(player));
        IRecipe recipe = selected == null ? null : recipes.get(ids.indexOf(selected));
        state.resolved(choices, recipe);
        return recipe;
    }

    private static boolean canCraft(IRecipe recipe, EntityPlayer player) {
        return recipe.isDynamic() || !player.world.getGameRules().getBoolean("doLimitedCrafting")
                || ((EntityPlayerMP) player).getRecipeBook().isUnlocked(recipe);
    }

    public static void openView(EntityPlayerMP player, int windowId, int viewId) {
        if (FurnaceService.isFurnace(player, windowId)) {
            FurnaceService.openView(player, viewId);
            return;
        }
        CraftingContext context = currentContext(player, windowId);
        if (context == null || viewId == 0) {
            return;
        }
        state(context).openView(viewId);
        refresh(context);
    }

    public static boolean select(EntityPlayerMP player, int windowId, int viewId, ResourceLocation id) {
        if (FurnaceService.isFurnace(player, windowId)) {
            return FurnaceService.select(player, viewId, id);
        }
        CraftingContext context = currentContext(player, windowId);
        if (context == null || viewId == 0 || state(context).getViewId() != viewId) {
            return false;
        }
        // 材料可能已被移动；先基于服务端当前输入重新计算，再接受选择。
        resolve(context, context.getMatrix(), player.world, player);
        List<ResourceLocation> ids = new ArrayList<>();
        for (RecipeChoice choice : state(context).getChoices()) {
            ids.add(choice.getId());
        }
        if (!state(context).getSelection().select(id, ids)) {
            refresh(context);
            return false;
        }
        PlayerPreferences.remember(player, id);
        refresh(context);
        return true;
    }

    @Nullable
    private static CraftingContext currentContext(EntityPlayerMP player, int windowId) {
        Container container = player.openContainer;
        return container.windowId == windowId && container.canInteractWith(player)
                ? CraftingContext.of(container) : null;
    }

    public static void refresh(CraftingContext context) {
        context.refresh();
        context.container.detectAndSendChanges();
    }

    public static CraftingState state(CraftingContext context) {
        return ((CraftingStateHolder) context.container).polymorph$getCraftingState();
    }

    public static void sync(Container container, EntityPlayer player) {
        CraftingContext context = CraftingContext.of(container);
        if (context == null || !(player instanceof EntityPlayerMP) || player.openContainer != container) {
            return;
        }
        CraftingState state = state(context);
        if (state.getViewId() != 0 && state.needsSync()) {
            PolymorphNetwork.CHANNEL.sendTo(new RecipesPacket(container.windowId, state.getViewId(),
                    state.getChoices(), state.getSelection().getSelected()), (EntityPlayerMP) player);
            state.markSynced();
        }
    }

    @Nullable
    public static CraftingContext beginCraft(EntityPlayer player, InventoryCrafting matrix) {
        CraftingContext context = CraftingContext.of(player.openContainer);
        if (player.world.isRemote || context == null || !context.ownsMatrix(matrix)) {
            return null;
        }
        state(context).beginCraft();
        return context;
    }

    public static NonNullList<ItemStack> remainingItems(@Nullable CraftingContext context,
            InventoryCrafting matrix, World world) {
        IRecipe recipe = context == null ? null : state(context).getCraftingRecipe();
        return recipe == null ? CraftingManager.getRemainingItems(matrix, world)
                : recipe.getRemainingItems(matrix);
    }

    public static void finishCraft(@Nullable CraftingContext context) {
        if (context != null) {
            state(context).endCraft();
            if (!state(context).getSelection().isCrafting()) {
                refresh(context);
            }
        }
    }
}
