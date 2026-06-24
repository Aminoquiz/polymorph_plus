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
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.common.util.BlockEntityTicker;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

public record CPacketBlockEntityListener(boolean add) implements CustomPacketPayload {

  public static final Type<CPacketBlockEntityListener> TYPE =
      new Type<>(
          ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "block_entity_listener"));
  public static final StreamCodec<FriendlyByteBuf, CPacketBlockEntityListener> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.BOOL,
          CPacketBlockEntityListener::add,
          CPacketBlockEntityListener::new);

  public static void handle(CPacketBlockEntityListener packet, ServerPlayer player) {

    if (player != null) {

      if (packet.add) {
        AbstractContainerMenu container = player.containerMenu;
        PolymorphApi api = PolymorphApi.getInstance();
        IBlockEntityRecipeData recipeData = api.getBlockEntityRecipeData(container);

        if (recipeData != null) {
          BlockEntityTicker.add(player, recipeData);
          ResourceLocation resourceLocation = null;
          RecipeHolder<?> recipeHolder = recipeData.getSelectedRecipe();

          if (recipeHolder != null) {
            resourceLocation = recipeHolder.id();
          }
          api.getNetwork().sendRecipesListS2C(player,
              recipeData.isEmpty() ? new TreeSet<>() : recipeData.getRecipesList(),
              resourceLocation);
        }
      } else {
        BlockEntityTicker.remove(player);
      }
    }
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
