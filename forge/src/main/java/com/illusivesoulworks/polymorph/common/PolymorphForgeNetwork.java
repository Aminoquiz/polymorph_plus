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

package com.illusivesoulworks.polymorph.common;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.network.client.CPacketBlockEntityListener;
import com.illusivesoulworks.polymorph.common.network.client.CPacketInWorldCycle;
import com.illusivesoulworks.polymorph.common.network.client.CPacketPersistentRecipeSelection;
import com.illusivesoulworks.polymorph.common.network.client.CPacketPlayerPriority;
import com.illusivesoulworks.polymorph.common.network.server.SPacketPackPriority;
import com.illusivesoulworks.polymorph.common.network.client.CPacketPlayerRecipeSelection;
import com.illusivesoulworks.polymorph.common.network.client.CPacketStackRecipeSelection;
import com.illusivesoulworks.polymorph.common.network.server.SPacketBlockEntityRecipeSync;
import com.illusivesoulworks.polymorph.common.network.server.SPacketHighlightRecipe;
import com.illusivesoulworks.polymorph.common.network.server.SPacketInWorldState;
import com.illusivesoulworks.polymorph.common.network.server.SPacketPlayerRecipeSync;
import com.illusivesoulworks.polymorph.common.network.server.SPacketRecipesList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PolymorphForgeNetwork {

  private static final String PTC_VERSION = "polymorph_plus-2";

  // A peer without the mod is fine; upstream Polymorph ("1") is refused.
  private static boolean accepts(String version) {
    return PTC_VERSION.equals(version) || NetworkRegistry.ABSENT.equals(version)
        || NetworkRegistry.ACCEPTVANILLA.equals(version);
  }

  private static final String INWORLD_VERSION = "1";

  // Its own channel, so a peer from before the in-world selector still connects: the main
  // channel keeps its version and this one is allowed to be absent.
  private static boolean acceptsInWorld(String version) {
    return INWORLD_VERSION.equals(version) || NetworkRegistry.ABSENT.equals(version)
        || NetworkRegistry.ACCEPTVANILLA.equals(version);
  }

  private static SimpleChannel instance;
  private static SimpleChannel inWorld;
  private static int id = 0;

  public static SimpleChannel get() {
    return instance;
  }

  public static SimpleChannel inWorld() {
    return inWorld;
  }

  public static void setup() {
    instance =
        NetworkRegistry.ChannelBuilder.named(new ResourceLocation(PolymorphApi.MOD_ID, "main"))
            .networkProtocolVersion(() -> PTC_VERSION).clientAcceptedVersions(PolymorphForgeNetwork::accepts)
            .serverAcceptedVersions(PolymorphForgeNetwork::accepts).simpleChannel();

    // Server-to-Client
    registerS2C(SPacketRecipesList.class, SPacketRecipesList::encode, SPacketRecipesList::decode,
        SPacketRecipesList::handle);
    registerS2C(SPacketHighlightRecipe.class, SPacketHighlightRecipe::encode,
        SPacketHighlightRecipe::decode, SPacketHighlightRecipe::handle);
    registerS2C(SPacketPlayerRecipeSync.class, SPacketPlayerRecipeSync::encode,
        SPacketPlayerRecipeSync::decode, SPacketPlayerRecipeSync::handle);
    registerS2C(SPacketBlockEntityRecipeSync.class, SPacketBlockEntityRecipeSync::encode,
        SPacketBlockEntityRecipeSync::decode, SPacketBlockEntityRecipeSync::handle);

    // Client-to-Server
    registerC2S(CPacketPlayerRecipeSelection.class, CPacketPlayerRecipeSelection::encode,
        CPacketPlayerRecipeSelection::decode, CPacketPlayerRecipeSelection::handle);
    registerC2S(CPacketPersistentRecipeSelection.class, CPacketPersistentRecipeSelection::encode,
        CPacketPersistentRecipeSelection::decode, CPacketPersistentRecipeSelection::handle);
    registerC2S(CPacketStackRecipeSelection.class, CPacketStackRecipeSelection::encode,
        CPacketStackRecipeSelection::decode, CPacketStackRecipeSelection::handle);
    registerC2S(CPacketBlockEntityListener.class, CPacketBlockEntityListener::encode,
        CPacketBlockEntityListener::decode, CPacketBlockEntityListener::handle);

    // Appended last so the discriminators of the upstream messages above stay unchanged.
    registerC2S(CPacketPlayerPriority.class, CPacketPlayerPriority::encode,
        CPacketPlayerPriority::decode, CPacketPlayerPriority::handle);
    registerS2C(SPacketPackPriority.class, SPacketPackPriority::encode,
        SPacketPackPriority::decode, SPacketPackPriority::handle);

    inWorld =
        NetworkRegistry.ChannelBuilder.named(new ResourceLocation(PolymorphApi.MOD_ID, "inworld"))
            .networkProtocolVersion(() -> INWORLD_VERSION)
            .clientAcceptedVersions(PolymorphForgeNetwork::acceptsInWorld)
            .serverAcceptedVersions(PolymorphForgeNetwork::acceptsInWorld).simpleChannel();
    registerS2C(inWorld, 0, SPacketInWorldState.class, SPacketInWorldState::encode,
        SPacketInWorldState::decode, SPacketInWorldState::handle);
    registerC2S(inWorld, 1, CPacketInWorldCycle.class, CPacketInWorldCycle::encode,
        CPacketInWorldCycle::decode, CPacketInWorldCycle::handle);
  }

  public static <M> void registerC2S(Class<M> clazz, BiConsumer<M, FriendlyByteBuf> encoder,
                                     Function<FriendlyByteBuf, M> decoder,
                                     BiConsumer<M, ServerPlayer> handler) {
    registerC2S(instance, id++, clazz, encoder, decoder, handler);
  }

  private static <M> void registerC2S(SimpleChannel channel, int index, Class<M> clazz,
                                      BiConsumer<M, FriendlyByteBuf> encoder,
                                      Function<FriendlyByteBuf, M> decoder,
                                      BiConsumer<M, ServerPlayer> handler) {
    channel.registerMessage(index, clazz, encoder, decoder, (message, contextSupplier) -> {
      NetworkEvent.Context context = contextSupplier.get();
      context.enqueueWork(() -> {
        ServerPlayer sender = context.getSender();

        if (sender != null) {
          handler.accept(message, sender);
        }
      });
      context.setPacketHandled(true);
    });
  }

  public static <M> void registerS2C(Class<M> clazz, BiConsumer<M, FriendlyByteBuf> encoder,
                                     Function<FriendlyByteBuf, M> decoder, Consumer<M> handler) {
    registerS2C(instance, id++, clazz, encoder, decoder, handler);
  }

  private static <M> void registerS2C(SimpleChannel channel, int index, Class<M> clazz,
                                      BiConsumer<M, FriendlyByteBuf> encoder,
                                      Function<FriendlyByteBuf, M> decoder, Consumer<M> handler) {
    channel.registerMessage(index, clazz, encoder, decoder, (message, contextSupplier) -> {
      NetworkEvent.Context context = contextSupplier.get();
      context.enqueueWork(
          () -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handler.accept(message)));
      context.setPacketHandled(true);
    });
  }
}
