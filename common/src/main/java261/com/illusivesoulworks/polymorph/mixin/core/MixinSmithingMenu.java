/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public abstract class MixinSmithingMenu extends ItemCombinerMenu {

  @Shadow
  @Final
  private Level level;

  @Shadow
  protected abstract SmithingRecipeInput createRecipeInput();

  public MixinSmithingMenu(@Nullable MenuType<?> type, int id, Inventory inventory,
                           ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slots) {
    super(type, id, inventory, access, slots);
  }

  @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
  private void polymorph$injectCreateResult(CallbackInfo ci) {
    if (!(this.level instanceof ServerLevel serverLevel)) {
      return;
    }
    SmithingRecipeInput input = this.createRecipeInput();
    RecipeManager rm = serverLevel.recipeAccess();
    RecipeMap map = ((AccessorRecipeManager) rm).polymorph$getRecipeMap();
    List<RecipeHolder<SmithingRecipe>> candidates =
        map.getRecipesFor(RecipeType.SMITHING, input, this.level).toList();

    if (candidates.isEmpty()) {
      if (this.player instanceof ServerPlayer sp) {
        PolymorphApi.getInstance().getNetwork().sendRecipesListS2C(sp);
      }
      return;
    }

    Optional<RecipeHolder<SmithingRecipe>> selected = PolymorphApi.getInstance().getRecipeManager()
        .getPlayerRecipe((SmithingMenu) (Object) this, RecipeType.SMITHING, input, this.level,
            this.player, candidates);
    RecipeHolder<SmithingRecipe> chosen = selected.orElse(candidates.get(0));
    ItemStack result = chosen.value().assemble(input, this.level.registryAccess());
    this.resultSlots.setRecipeUsed((RecipeHolder<?>) chosen);
    this.resultSlots.setItem(0, result);
    ci.cancel();
  }
}
