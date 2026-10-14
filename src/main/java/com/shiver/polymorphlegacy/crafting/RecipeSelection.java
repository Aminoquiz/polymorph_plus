package com.shiver.polymorphlegacy.crafting;

import com.shiver.polymorphlegacy.priority.RecipeRanking;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

public final class RecipeSelection {
    private ResourceLocation selected;
    // The click made in this menu. It outlives input changes, so the pick comes back when the
    // same ingredients return, and only the menu closing clears it.
    private ResourceLocation explicit;
    private int craftingDepth;

    @Nullable
    public ResourceLocation resolve(List<ResourceLocation> candidates) {
        return resolve(candidates, RecipeRanking.NONE);
    }

    @Nullable
    public ResourceLocation resolve(List<ResourceLocation> candidates, RecipeRanking ranking) {
        ResourceLocation result;
        if (candidates.isEmpty()) {
            result = null;
        } else if (explicit != null && candidates.contains(explicit)) {
            result = explicit;
        } else {
            List<String> ids = new ArrayList<>(candidates.size());
            for (ResourceLocation candidate : candidates) {
                ids.add(candidate.toString());
            }
            result = candidates.get(ranking.choose(ids));
        }
        if (!isCrafting()) {
            selected = result;
        }
        return result;
    }

    public boolean select(ResourceLocation id, List<ResourceLocation> candidates) {
        if (id == null || !candidates.contains(id)) {
            return false;
        }
        selected = id;
        explicit = id;
        return true;
    }

    @Nullable
    public ResourceLocation getSelected() {
        return selected;
    }

    public void beginCraft() {
        craftingDepth++;
    }

    public void endCraft() {
        craftingDepth--;
    }

    public boolean isCrafting() {
        return craftingDepth > 0;
    }

    public void clear() {
        selected = null;
        explicit = null;
        craftingDepth = 0;
    }
}
