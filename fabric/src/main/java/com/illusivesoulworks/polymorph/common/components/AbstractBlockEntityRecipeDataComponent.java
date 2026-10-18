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

import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.Component;

public abstract class AbstractBlockEntityRecipeDataComponent<M extends BlockEntity>
    extends AbstractBlockEntityRecipeData<M> implements Component {

  public AbstractBlockEntityRecipeDataComponent(M owner) {
    super(owner);
  }

  // Same layout as the NeoForge attachment: the NBT from writeNBT stored under one key.
  private static final String DATA_KEY = "data";

  @Override
  public void readData(ValueInput readView) {
    readView.read(DATA_KEY, CompoundTag.CODEC)
        .ifPresent(tag -> this.readNBT(readView.lookup(), tag));
  }

  @Override
  public void writeData(ValueOutput writeView) {
    Level level = this.getOwner().getLevel();
    CompoundTag tag = this.writeNBT(level != null ? level.registryAccess() : null);

    if (!tag.isEmpty()) {
      writeView.store(DATA_KEY, CompoundTag.CODEC, tag);
    }
  }
}
