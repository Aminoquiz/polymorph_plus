package com.shiver.polymorphlegacy.crafting;

import com.shiver.polymorphlegacy.api.RecipeChoice;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

public final class CraftingState {
    private final RecipeSelection selection = new RecipeSelection();
    private List<RecipeChoice> choices = Collections.emptyList();
    private IRecipe activeRecipe;
    private IRecipe craftingRecipe;
    private int viewId;
    private List<RecipeChoice> lastSent;
    private ResourceLocation lastSelected;

    public RecipeSelection getSelection() {
        return selection;
    }

    public List<RecipeChoice> getChoices() {
        return choices;
    }

    @Nullable
    public IRecipe getActiveRecipe() {
        return activeRecipe;
    }

    public void resolved(List<RecipeChoice> choices, @Nullable IRecipe recipe) {
        this.choices = Collections.unmodifiableList(choices);
        activeRecipe = recipe;
    }

    public void beginCraft() {
        if (!selection.isCrafting()) {
            craftingRecipe = activeRecipe;
        }
        selection.beginCraft();
    }

    public void endCraft() {
        selection.endCraft();
        if (!selection.isCrafting()) {
            craftingRecipe = null;
        }
    }

    @Nullable
    public IRecipe getCraftingRecipe() {
        return craftingRecipe;
    }

    public void openView(int viewId) {
        this.viewId = viewId;
        lastSent = null;
    }

    public int getViewId() {
        return viewId;
    }

    public boolean needsSync() {
        if (lastSent == null || !Objects.equals(lastSelected, selection.getSelected())
                || lastSent.size() != choices.size()) {
            return true;
        }
        for (int i = 0; i < choices.size(); i++) {
            if (!choices.get(i).sameAs(lastSent.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void markSynced() {
        lastSent = choices;
        lastSelected = selection.getSelected();
    }

    public void clear() {
        selection.clear();
        choices = Collections.emptyList();
        activeRecipe = null;
        craftingRecipe = null;
        viewId = 0;
        lastSent = null;
    }
}
