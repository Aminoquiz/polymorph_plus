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

package com.illusivesoulworks.polymorph.common.network.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class SPacketBlockEntityRecipeSync {
  private final BlockPos blockPos;
  private final ResourceLocation selected;

  public SPacketBlockEntityRecipeSync(BlockPos blockPos, ResourceLocation selected) {
    this.blockPos = blockPos;
    this.selected = selected;
  }

  public BlockPos blockPos() {
    return this.blockPos;
  }

  public ResourceLocation selected() {
    return this.selected;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SPacketBlockEntityRecipeSync)) {
      return false;
    }
    SPacketBlockEntityRecipeSync other = (SPacketBlockEntityRecipeSync) o;
    return java.util.Objects.equals(this.blockPos, other.blockPos) && java.util.Objects.equals(this.selected, other.selected);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(this.blockPos, this.selected);
  }


  public BlockPos getBlockPos() {
    return this.blockPos;
  }

  public ResourceLocation getSelected() {
    return this.selected;
  }

  public static void encode(SPacketBlockEntityRecipeSync packet, FriendlyByteBuf buffer) {
    buffer.writeBlockPos(packet.getBlockPos());
    buffer.writeResourceLocation(packet.getSelected());
  }

  public static SPacketBlockEntityRecipeSync decode(FriendlyByteBuf buffer) {
    return new SPacketBlockEntityRecipeSync(buffer.readBlockPos(), buffer.readResourceLocation());
  }

  public static void handle(SPacketBlockEntityRecipeSync packet) {
    ClientPacketHandler.handle(packet);
  }
}
