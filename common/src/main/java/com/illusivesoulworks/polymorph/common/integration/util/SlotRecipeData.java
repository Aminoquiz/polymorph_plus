package com.illusivesoulworks.polymorph.common.integration.util;

import com.illusivesoulworks.polymorph.common.capability.AbstractHighlightedRecipeData;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SlotRecipeData extends AbstractHighlightedRecipeData<BlockEntity> {

  private final Function<BlockEntity, ItemStack> input;

  public SlotRecipeData(BlockEntity owner, Function<BlockEntity, ItemStack> input) {
    super(owner);
    this.input = input;
  }

  @Override
  protected NonNullList<ItemStack> getInput() {
    ItemStack stack = this.input.apply(this.getOwner());
    return stack != null ? NonNullList.of(ItemStack.EMPTY, stack) : NonNullList.create();
  }

  @Override
  public boolean isEmpty() {
    NonNullList<ItemStack> stacks = this.getInput();
    return stacks.isEmpty() || stacks.get(0).isEmpty();
  }
}
