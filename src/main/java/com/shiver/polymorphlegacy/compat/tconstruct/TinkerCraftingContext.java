package com.shiver.polymorphlegacy.compat.tconstruct;

import com.shiver.polymorphlegacy.api.CraftingContext;
import com.shiver.polymorphlegacy.api.RecipeChoice;
import com.shiver.polymorphlegacy.crafting.CraftingService;
import com.shiver.polymorphlegacy.crafting.CraftingState;
import com.shiver.polymorphlegacy.crafting.CraftingStateHolder;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import slimeknights.tconstruct.tools.common.inventory.ContainerCraftingStation;

public final class TinkerCraftingContext extends CraftingContext {
    private final ContainerCraftingStation station;

    public TinkerCraftingContext(ContainerCraftingStation station, EntityPlayer player) {
        super(station);
        this.station = station;
        if (!player.world.isRemote) {
            // 同一个方块共享合成结果；新打开的窗口继承已有窗口的选择。
            for (EntityPlayer viewer : player.world.playerEntities) {
                if (viewer.openContainer != station && viewer.openContainer instanceof ContainerCraftingStation
                        && ((ContainerCraftingStation) viewer.openContainer).getTile() == station.getTile()) {
                    inheritSelection(((CraftingStateHolder) viewer.openContainer).polymorph$getCraftingState());
                    break;
                }
            }
        }
    }

    @Override
    public InventoryCrafting getMatrix() {
        return station.getCraftMatrix();
    }

    @Override
    public Slot getOutputSlot() {
        return station.getSlot(0);
    }

    private void inheritSelection(CraftingState source) {
        IRecipe recipe = source.getActiveRecipe();
        CraftingState state = CraftingService.state(this);
        state.getSelection().clear();
        if (recipe != null) {
            ResourceLocation id = recipe.getRegistryName();
            state.getSelection().select(id, Collections.singletonList(id));
        }
        state.resolved(source.getChoices(), recipe);
    }

    public void syncViewers(List<EntityPlayerMP> viewers) {
        CraftingState state = CraftingService.state(this);
        for (EntityPlayerMP viewer : viewers) {
            TinkerCraftingContext context = (TinkerCraftingContext) CraftingContext.of(viewer.openContainer);
            if (context != this) {
                context.inheritSelection(state);
                // 返还物由匠魂的 lastRecipe 决定，必须和其他窗口显示的结果一起更新。
                context.station.updateLastRecipeFromServer(state.getActiveRecipe());
            }
            CraftingService.sync(viewer.openContainer, viewer);
        }
    }

    @Override
    public void receive(List<RecipeChoice> choices, @Nullable ResourceLocation selected) {
        IRecipe recipe = selected == null ? null : ForgeRegistries.RECIPES.getValue(selected);
        station.updateLastRecipeFromServer(recipe);
        ItemStack output = ItemStack.EMPTY;
        for (RecipeChoice choice : choices) {
            if (choice.getId().equals(selected)) {
                output = choice.getOutput().copy();
                break;
            }
        }
        getOutputSlot().putStack(output);
    }
}
