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

package com.illusivesoulworks.polymorph.common.components;

import com.illusivesoulworks.polymorph.common.brewing.BrewingSelector;
import com.illusivesoulworks.polymorph.common.brewing.IBrewingStandRecipeData;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public class BrewingStandRecipeDataComponent
    extends AbstractBlockEntityRecipeDataComponent<BrewingStandBlockEntity>
    implements IBrewingStandRecipeData {

  private final BrewingSelector brewing;

  public BrewingStandRecipeDataComponent(BrewingStandBlockEntity owner) {
    super(owner);
    this.brewing = new BrewingSelector(owner);
  }

  @Override
  public BrewingSelector getBrewing() {
    return this.brewing;
  }

  @Override
  protected NonNullList<ItemStack> getInput() {
    NonNullList<ItemStack> input = NonNullList.create();

    for (int i = 0; i < 4; i++) {
      input.add(this.getOwner().getItem(i));
    }
    return input;
  }

  @Override
  public void tick() {
    this.brewing.tick(this.getListeners());
  }

  @Override
  public void readNBT(CompoundTag compoundTag) {
    super.readNBT(compoundTag);
    this.brewing.read(compoundTag);
  }

  @Nonnull
  @Override
  public CompoundTag writeNBT() {
    CompoundTag tag = super.writeNBT();
    this.brewing.write(tag);
    return tag;
  }
}
