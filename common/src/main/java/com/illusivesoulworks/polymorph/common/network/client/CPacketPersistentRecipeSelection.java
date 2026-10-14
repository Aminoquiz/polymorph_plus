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
import com.illusivesoulworks.polymorph.common.integration.PolymorphIntegrations;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

public record CPacketPersistentRecipeSelection(ResourceLocation recipe) implements
    CustomPacketPayload {

  public static final Type<CPacketPersistentRecipeSelection> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID,
          "persistent_recipe_selection"));
  public static final StreamCodec<FriendlyByteBuf, CPacketPersistentRecipeSelection> STREAM_CODEC =
      StreamCodec.composite(
          ResourceLocation.STREAM_CODEC,
          CPacketPersistentRecipeSelection::recipe,
          CPacketPersistentRecipeSelection::new);

  public static void handle(CPacketPersistentRecipeSelection packet, ServerPlayer player) {
    Level world = player.getCommandSenderWorld();
    Optional<RecipeHolder<?>> maybeRecipe =
        world.getRecipeManager().byKey(packet.recipe);
    maybeRecipe.ifPresent(recipe -> {
      AbstractContainerMenu container = player.containerMenu;
      IBlockEntityRecipeData recipeData =
          PolymorphApi.getInstance().getBlockEntityRecipeData(container);

      if (recipeData != null) {
        recipeData.selectRecipe(recipe);
        PolymorphIntegrations.selectRecipe(recipeData.getOwner(), container, recipe);
      }
    });
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
