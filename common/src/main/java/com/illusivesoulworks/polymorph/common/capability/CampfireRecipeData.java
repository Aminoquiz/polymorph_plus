/*
 * Copyright (C) 2020-2022 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Polymorph.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.illusivesoulworks.polymorph.common.capability;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.api.inworld.Choices;
import com.illusivesoulworks.polymorph.api.inworld.Option;
import com.illusivesoulworks.polymorph.common.impl.RecipePair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

/**
 * A campfire cooks four items at once, so each slot keeps its own pick.
 */
public class CampfireRecipeData extends AbstractBlockEntityRecipeData<CampfireBlockEntity> {

  private final ResourceLocation[] slotPicks = new ResourceLocation[4];
  private int cookingSlot = -1;
  private int filledSlots;

  public CampfireRecipeData(CampfireBlockEntity owner) {
    super(owner);
  }

  @Override
  protected NonNullList<ItemStack> getInput() {
    return this.getOwner().getItems();
  }

  // The campfire looks the recipe up before it stores the food, so testing its own slots here
  // would call an empty campfire empty and find no candidates until the first item cooked.
  @Override
  public boolean isEmpty(Container inventory) {

    if (inventory != null) {

      for (int i = 0; i < inventory.getContainerSize(); i++) {

        if (!inventory.getItem(i).isEmpty()) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public <T extends Recipe<C>, C extends Container> Optional<T> getRecipe(RecipeType<T> type,
                                                                          C inventory, Level level,
                                                                          List<T> recipesList) {
    // cookTick wraps the slot's own stack, so identity tells which slot is cooking. The lookup
    // made when food is placed passes the player's stack and matches no slot.
    this.cookingSlot = inventory != null && inventory.getContainerSize() == 1
        ? this.slotHolding(inventory.getItem(0)) : -1;

    try {
      return super.getRecipe(type, inventory, level, recipesList);
    } finally {
      this.cookingSlot = -1;
    }
  }

  @Override
  public ResourceLocation getChosenRecipeId() {
    return this.cookingSlot >= 0 ? this.slotPicks[this.cookingSlot] : super.getChosenRecipeId();
  }

  @Nullable
  public ResourceLocation getSlotPick(int slot) {
    return this.slotPicks[slot];
  }

  public void setSlotPick(int slot, @Nullable ResourceLocation id) {
    this.slotPicks[slot] = id;
  }

  /**
   * Drops the picks of empty slots. Returns true when the set of filled slots changed since the
   * last call.
   */
  public boolean syncSlots() {
    NonNullList<ItemStack> items = this.getInput();
    int filled = 0;

    for (int slot = 0; slot < this.slotPicks.length; slot++) {

      if (items.get(slot).isEmpty()) {
        this.slotPicks[slot] = null;
      } else {
        filled |= 1 << slot;
      }
    }
    boolean changed = filled != this.filledSlots;
    this.filledSlots = filled;
    return changed;
  }

  /**
   * The recipes for the item in this slot. With no pick, {@code current} is the one
   * {@link #getRecipe} falls back to for a block entity: the best pack rank, else the first.
   */
  @Nullable
  public Choices slotChoices(Level level, int slot, ResourceLocation key) {
    ItemStack stack = this.getInput().get(slot);

    if (stack.isEmpty()) {
      return null;
    }
    SimpleContainer input = new SimpleContainer(stack);
    SortedSet<IRecipePair> pairs = new TreeSet<>();
    ResourceLocation first = null;
    ResourceLocation ranked = null;
    int bestRank = Integer.MAX_VALUE;

    for (CampfireCookingRecipe recipe : level.getRecipeManager()
        .getRecipesFor(RecipeType.CAMPFIRE_COOKING, input, level)) {
      ItemStack output = recipe.assemble(input, level.registryAccess());

      if (output.isEmpty()) {
        continue;
      }
      ResourceLocation id = recipe.getId();
      int rank = this.sourceRank(id);

      if (first == null) {
        first = id;
      }

      if (rank < bestRank) {
        bestRank = rank;
        ranked = id;
      }
      pairs.add(new RecipePair(id, output));
    }
    List<Option> options = new ArrayList<>();

    for (IRecipePair pair : pairs) {
      options.add(new Option(pair.getResourceLocation(), pair.getOutput()));
    }
    return new Choices(key, options, ranked != null ? ranked : first);
  }

  private int slotHolding(ItemStack stack) {
    NonNullList<ItemStack> items = this.getInput();

    for (int slot = 0; slot < this.slotPicks.length; slot++) {

      if (items.get(slot) == stack) {
        return slot;
      }
    }
    return -1;
  }

  @Override
  public void readNBT(CompoundTag compoundTag) {
    super.readNBT(compoundTag);
    ListTag picks = compoundTag.getList("SlotPicks", Tag.TAG_STRING);

    for (int slot = 0; slot < this.slotPicks.length; slot++) {
      String id = slot < picks.size() ? picks.getString(slot) : "";
      this.slotPicks[slot] = id.isEmpty() ? null : ResourceLocation.tryParse(id);
    }
  }

  @Nonnull
  @Override
  public CompoundTag writeNBT() {
    CompoundTag nbt = super.writeNBT();
    ListTag picks = new ListTag();

    for (ResourceLocation pick : this.slotPicks) {
      picks.add(StringTag.valueOf(pick != null ? pick.toString() : ""));
    }
    nbt.put("SlotPicks", picks);
    return nbt;
  }
}
