package com.shiver.polymorphlegacy.api;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

/** 在服务端和客户端描述容器的合成矩阵。 */
public abstract class CraftingContext {
    public final Container container;

    protected CraftingContext(Container container) {
        this.container = container;
    }

    /** 获取已接入容器的上下文；其他容器返回 null。 */
    @Nullable
    public static CraftingContext of(Container container) {
        if (container instanceof CraftingContextProvider) {
            return ((CraftingContextProvider) container).polymorph$getCraftingContext();
        }
        if (container != null && container.getClass() == ContainerWorkbench.class) {
            ContainerWorkbench workbench = (ContainerWorkbench) container;
            return new VanillaContext(container, workbench.craftMatrix);
        }
        if (container != null && container.getClass() == ContainerPlayer.class) {
            ContainerPlayer inventory = (ContainerPlayer) container;
            return new VanillaContext(container, inventory.craftMatrix);
        }
        return null;
    }

    public abstract InventoryCrafting getMatrix();

    public abstract Slot getOutputSlot();

    public boolean ownsMatrix(InventoryCrafting matrix) {
        return getMatrix() == matrix;
    }

    /**
     * Recipes this matrix can craft from, for containers whose recipes are not in
     * ForgeRegistries.RECIPES. Null (the default) scans the registry and applies the recipe book
     * rule; a returned list is used as is, without the recipe book check.
     */
    @Nullable
    public List<IRecipe> recipeCandidates() {
        return null;
    }

    /** Stable id of a candidate from {@link #recipeCandidates()}; index is its position in that list. */
    public ResourceLocation recipeId(IRecipe recipe, int index) {
        return recipe.getRegistryName();
    }

    /** 玩家切换配方后重新计算产物。 */
    public void refresh() {
        container.onCraftMatrixChanged(getMatrix());
    }

    /** 容器发送槽位变化前调用，可用于更新非标准合成矩阵。 */
    public void detectChanges() {
    }

    /** 客户端收到候选配方和当前选择后调用。 */
    public void receive(List<RecipeChoice> choices, @Nullable ResourceLocation selected) {
    }

    private static final class VanillaContext extends CraftingContext {
        private final InventoryCrafting matrix;

        private VanillaContext(Container container, InventoryCrafting matrix) {
            super(container);
            this.matrix = matrix;
        }

        @Override
        public InventoryCrafting getMatrix() {
            return matrix;
        }

        @Override
        public Slot getOutputSlot() {
            return container.getSlot(0);
        }
    }
}
