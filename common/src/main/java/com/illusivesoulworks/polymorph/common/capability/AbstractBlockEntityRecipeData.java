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

import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import java.util.TreeSet;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AbstractBlockEntityRecipeData<E extends BlockEntity>
    extends AbstractRecipeData<BlockEntity> implements IBlockEntityRecipeData {

  private boolean wasEmptyBefore = false;

  public AbstractBlockEntityRecipeData(BlockEntity owner) {
    super(owner);
  }

  protected abstract NonNullList<ItemStack> getInput();

  @Override
  public void tick() {

    if (this.getListeners().isEmpty()) {
      return;
    }
    NonNullList<ItemStack> currentInput = this.getInput();
    boolean isEmpty = true;

    for (ItemStack currentStack : currentInput) {

      if (!currentStack.isEmpty()) {
        isEmpty = false;
        break;
      }
    }

    if (!this.wasEmptyBefore && isEmpty) {
      this.updateRecipesList(new TreeSet<>());
    }
    this.wasEmptyBefore = isEmpty;
  }

  @Override
  public boolean isEmpty() {
    NonNullList<ItemStack> currentInput = this.getInput();

    for (ItemStack currentStack : currentInput) {

      if (!currentStack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public E getOwner() {
    return (E) super.getOwner();
  }
}
