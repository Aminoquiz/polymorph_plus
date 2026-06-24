/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.mixin.core;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import org.spongepowered.asm.mixin.Mixin;

/**
 * MC 26.1 fork — STUB. Two unrelated breakages on 26.x make the upstream injects
 * untenable:
 * <ul>
 *   <li>{@link ItemCombinerMenu} constructor gained an {@link
 *       ItemCombinerMenuSlotDefinition} parameter, so the shadow constructor signature
 *       changed.</li>
 *   <li>{@link SmithingMenu#createResult()} no longer routes through {@code
 *       RecipeManager.getRecipesFor(List)} — it now calls the singular {@code
 *       getRecipeFor(Optional)} on {@code serverLevel.recipeAccess()} and uses {@code
 *       resultSlots.setRecipeUsed} instead of a {@code selectedRecipe} field.</li>
 * </ul>
 * Polymorph's selection logic for smithing is therefore disabled in this initial port;
 * recipe conflicts on the smithing table fall back to vanilla's first-match. Restoring
 * full selection requires reimplementing {@code createResult} via {@code @Inject(at =
 * HEAD, cancellable = true)} and threading the candidate list through the player recipe
 * data. Not done here — out of scope for the initial compile.
 */
@Mixin(SmithingMenu.class)
public abstract class MixinSmithingMenu extends ItemCombinerMenu {

  public MixinSmithingMenu(@Nullable MenuType<?> type, int id, Inventory inventory,
                           ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slots) {
    super(type, id, inventory, access, slots);
  }
}
