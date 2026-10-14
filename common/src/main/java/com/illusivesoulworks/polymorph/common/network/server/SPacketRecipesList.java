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

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.common.util.RecipePair;
import java.util.HashSet;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SPacketRecipesList(Optional<HashSet<IRecipePair>> recipeList,
                                 Optional<ResourceLocation> selected)
    implements CustomPacketPayload {

  public static final Type<SPacketRecipesList> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "recipes_list"));

  public static final StreamCodec<RegistryFriendlyByteBuf, SPacketRecipesList> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.optional(
              RecipePair.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new))),
          SPacketRecipesList::recipeList,
          ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
          SPacketRecipesList::selected,
          SPacketRecipesList::new);

  public static void handle(SPacketRecipesList packet) {
    ClientPacketHandler.handle(packet);
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
