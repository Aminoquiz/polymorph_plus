package com.illusivesoulworks.polymorph.common.integration.util;

import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import java.util.function.Function;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SlotRecipeData extends AbstractBlockEntityRecipeData<BlockEntity> {

  private final Function<BlockEntity, NonNullList<ItemStack>> input;
  private final boolean refreshOnSelect;

  public SlotRecipeData(BlockEntity owner, Function<BlockEntity, NonNullList<ItemStack>> input,
                        boolean refreshOnSelect) {
    super(owner);
    this.input = input;
    this.refreshOnSelect = refreshOnSelect;
  }

  @Override
  public void selectRecipe(@Nonnull RecipeHolder<?> recipe) {
    super.selectRecipe(recipe);

    if (this.refreshOnSelect) {
      this.getOwner().setChanged();
    }
  }

  @Override
  protected NonNullList<ItemStack> getInput() {
    NonNullList<ItemStack> stacks = this.input.apply(this.getOwner());
    return stacks != null ? stacks : NonNullList.create();
  }
}
