package com.shiver.polymorphlegacy.compat.ae2;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.slot.SlotCraftingTerm;
import appeng.helpers.IContainerCraftingPacket;
import appeng.util.inv.IAEAppEngInventory;
import com.shiver.polymorphlegacy.api.CraftingContext;
import com.shiver.polymorphlegacy.api.RecipeChoice;
import com.shiver.polymorphlegacy.crafting.CraftingService;
import com.shiver.polymorphlegacy.crafting.CraftingState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.IItemHandler;

public final class Ae2CraftingContext extends CraftingContext {
    private final AEBaseContainer terminal;
    private final SlotCraftingTerm outputSlot;
    private ItemStack[] lastInputs;
    private ResourceLocation lastSelection;
    private boolean receivedRecipes;

    public Ae2CraftingContext(AEBaseContainer terminal, SlotCraftingTerm outputSlot) {
        super(terminal);
        this.terminal = terminal;
        this.outputSlot = outputSlot;
    }

    @Override
    public InventoryCrafting getMatrix() {
        // 快照不能以真实终端为回调容器，否则逐格填充会再次触发配方刷新。
        InventoryCrafting matrix = new InventoryCrafting(new ContainerNull(), 3, 3);
        IItemHandler inventory = ((IContainerCraftingPacket) terminal).getInventoryByName("crafting");
        for (int i = 0; i < 9; i++) {
            matrix.setInventorySlotContents(i, inventory.getStackInSlot(i).copy());
        }
        return matrix;
    }

    @Override
    public Slot getOutputSlot() {
        return outputSlot;
    }

    @Override
    public boolean ownsMatrix(InventoryCrafting matrix) {
        // AE2 的实际合成由 SlotCraftingTerm 接管，不接管其他 SlotCrafting 的临时矩阵。
        return false;
    }

    @Override
    public void refresh() {
        if (!CraftingService.state(this).getSelection().isCrafting()) {
            update(getMatrix());
        }
    }

    @Override
    public void detectChanges() {
        CraftingState state = CraftingService.state(this);
        if (terminal.getPlayerInv().player.world.isRemote || state.getSelection().isCrafting()) {
            return;
        }
        InventoryCrafting matrix = getMatrix();
        if (lastInputs == null || !Objects.equals(lastSelection, state.getSelection().getSelected())) {
            update(matrix);
            return;
        }
        for (int i = 0; i < 9; i++) {
            if (!ItemStack.areItemStacksEqual(lastInputs[i], matrix.getStackInSlot(i))) {
                update(matrix);
                return;
            }
        }
    }

    private void update(InventoryCrafting matrix) {
        EntityPlayer player = terminal.getPlayerInv().player;
        CraftingState state = CraftingService.state(this);
        IRecipe recipe;
        if (player.world.isRemote) {
            recipe = receivedRecipes ? state.getActiveRecipe() : CraftingManager.findMatchingRecipe(matrix, player.world);
            if (recipe != null && !recipe.matches(matrix, player.world)) {
                recipe = null;
            }
        } else {
            recipe = CraftingService.resolve(this, matrix, player.world, player);
            lastInputs = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                lastInputs[i] = matrix.getStackInSlot(i).copy();
            }
            lastSelection = state.getSelection().getSelected();
        }
        ((Ae2CraftingContainer) terminal).polymorph$setRecipe(recipe);
        outputSlot.putStack(recipe == null ? ItemStack.EMPTY : recipe.getCraftingResult(matrix));
        if (!player.world.isRemote) {
            // 无线终端依靠该回调将真实合成格保存到手持终端中。
            ((IAEAppEngInventory) terminal).saveChanges();
            CraftingService.sync(container, player);
        }
    }

    @Override
    public void receive(List<RecipeChoice> choices, @Nullable ResourceLocation selected) {
        receivedRecipes = true;
        List<ResourceLocation> ids = new ArrayList<>();
        ItemStack output = ItemStack.EMPTY;
        for (RecipeChoice choice : choices) {
            ids.add(choice.getId());
            if (choice.getId().equals(selected)) {
                output = choice.getOutput().copy();
            }
        }
        IRecipe recipe = selected == null ? null : ForgeRegistries.RECIPES.getValue(selected);
        CraftingState state = CraftingService.state(this);
        state.getSelection().clear();
        state.getSelection().select(selected, ids);
        state.resolved(choices, recipe);
        ((Ae2CraftingContainer) terminal).polymorph$setRecipe(recipe);
        outputSlot.putStack(output);
    }

    @Nullable
    public IRecipe recipeForCrafting(InventoryCrafting matrix, World world) {
        CraftingState state = CraftingService.state(this);
        IRecipe recipe = state.getSelection().isCrafting() ? state.getCraftingRecipe() : state.getActiveRecipe();
        return recipe != null && recipe.matches(matrix, world) ? recipe : null;
    }
}
