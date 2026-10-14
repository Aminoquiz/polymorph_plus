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

package com.illusivesoulworks.polymorph.common.network.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.util.BlockEntityTicker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class CPacketBlockEntityListener {
  private final boolean add;

  public CPacketBlockEntityListener(boolean add) {
    this.add = add;
  }

  public boolean add() {
    return this.add;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CPacketBlockEntityListener)) {
      return false;
    }
    CPacketBlockEntityListener other = (CPacketBlockEntityListener) o;
    return java.util.Objects.equals(this.add, other.add);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(this.add);
  }


  public static void encode(CPacketBlockEntityListener packet, FriendlyByteBuf buffer) {
    buffer.writeBoolean(packet.add);
  }

  public static CPacketBlockEntityListener decode(FriendlyByteBuf buffer) {
    return new CPacketBlockEntityListener(buffer.readBoolean());
  }

  public static void handle(CPacketBlockEntityListener packet, ServerPlayer player) {

    if (player != null) {

      if (packet.add) {
        AbstractContainerMenu container = player.containerMenu;
        PolymorphApi.common().getRecipeDataFromBlockEntity(container)
            .ifPresent(recipeData -> BlockEntityTicker.add(player, recipeData));
      } else {
        BlockEntityTicker.remove(player);
      }
    }
  }
}
