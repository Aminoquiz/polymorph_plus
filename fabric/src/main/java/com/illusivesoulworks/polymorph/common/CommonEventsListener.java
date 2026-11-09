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
import com.illusivesoulworks.polymorph.common.inworld.InWorldServer;
import com.illusivesoulworks.polymorph.common.priority.PriorityReloadListener;
import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;

public class CommonEventsListener {

  public static void setup() {
    ServerTickEvents.END_LEVEL_TICK.register(CommonEventsListener::levelTick);
    ServerChunkEvents.CHUNK_UNLOAD.register(
        (level, chunk) -> InWorldServer.chunkUnloaded(level, chunk.getPos()));
    PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {

      if (level instanceof ServerLevel serverLevel &&
          InWorldServer.provider(state.getBlock()) != null) {
        InWorldServer.blockBroken(serverLevel, pos);
      }
    });
    AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
        player instanceof ServerPlayer serverPlayer &&
            InWorldServer.isAttackOnIndicator(serverPlayer, pos) ? InteractionResult.FAIL :
            InteractionResult.PASS);
    ServerPlayConnectionEvents.DISCONNECT.register(CommonEventsListener::disconnect);
    ServerPlayConnectionEvents.JOIN.register(CommonEventsListener::join);
    ServerLifecycleEvents.SERVER_STARTING.register(PriorityReloadListener::setServer);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> PriorityReloadListener.setServer(null));
    ResourceLoader.get(PackType.SERVER_DATA)
        .registerReloadListener(PriorityReloadListener.ID, new PriorityReloadListener());
  }

  private static void join(ServerGamePacketListenerImpl listener, PacketSender sender,
                           MinecraftServer server) {
    PolymorphApi.getInstance().getNetwork()
        .sendPackPriorityS2C(listener.getPlayer(), RecipePriority.getPackNamespaces());
  }

  private static void disconnect(ServerGamePacketListenerImpl serverGamePacketListener,
                                 MinecraftServer server) {
    PolymorphCommonEvents.playerDisconnected(serverGamePacketListener.getPlayer());
  }

  private static void levelTick(final ServerLevel serverLevel) {
    PolymorphCommonEvents.levelTick(serverLevel);
  }
}
