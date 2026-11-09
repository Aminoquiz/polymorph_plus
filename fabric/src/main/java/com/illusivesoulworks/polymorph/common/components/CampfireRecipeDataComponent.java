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

import com.illusivesoulworks.polymorph.common.capability.CampfireRecipeData;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

// Extends the common data so the in-world provider finds the slot picks on both loaders.
public class CampfireRecipeDataComponent extends CampfireRecipeData implements BlockEntityRecipeDataComponent {

  public CampfireRecipeDataComponent(CampfireBlockEntity owner) {
    super(owner);
  }

  @Override
  public void readFromNbt(@Nonnull CompoundTag tag) {
    this.readNBT(tag.getCompound("Data"));
  }

  @Override
  public void writeToNbt(@Nonnull CompoundTag tag) {
    tag.put("Data", this.writeNBT());
  }
}
