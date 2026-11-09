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
import com.illusivesoulworks.polymorph.common.priority.PriorityReloadListener;
import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import com.illusivesoulworks.polymorph.server.PolymorphCommands;
import com.illusivesoulworks.polymorph.common.inworld.InWorldServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@SuppressWarnings("unused")
public class CommonEventsListener {

  @SubscribeEvent
  public void addReloadListeners(final AddReloadListenerEvent evt) {
    evt.addListener(new PriorityReloadListener());
  }

  @SubscribeEvent
  public void playerLoggedIn(final PlayerEvent.PlayerLoggedInEvent evt) {

    if (evt.getEntity() instanceof ServerPlayer serverPlayer) {
      // The pack list is server-authoritative but the client resolves conflicts too (AE2's
      // pattern terminal does it client-side), so both sides need the same ranking.
      PolymorphApi.getInstance().getNetwork()
          .sendPackPriorityS2C(serverPlayer, RecipePriority.getPackNamespaces());
    }
  }

  @SubscribeEvent
  public void registerCommands(final RegisterCommandsEvent evt) {
    PolymorphCommands.register(evt.getDispatcher());
  }

  @SubscribeEvent
  public void playerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent evt) {

    if (evt.getEntity() instanceof ServerPlayer serverPlayer) {
      PolymorphCommonEvents.playerDisconnected(serverPlayer);
    }
  }

  @SubscribeEvent
  public void openContainer(final PlayerContainerEvent.Open evt) {
    PolymorphCommonEvents.openContainer(evt.getEntity(), evt.getContainer());
  }

  @SubscribeEvent
  public void chunkSent(final ChunkWatchEvent.Sent evt) {
    InWorldServer.chunkWatched(evt.getPlayer(), evt.getLevel(), evt.getChunk());
  }

  @SubscribeEvent
  public void chunkUnload(final ChunkEvent.Unload evt) {

    if (evt.getLevel() instanceof ServerLevel serverLevel) {
      InWorldServer.chunkUnloaded(serverLevel, evt.getChunk().getPos());
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void blockBreak(final BlockEvent.BreakEvent evt) {

    if (!evt.isCanceled() && evt.getLevel() instanceof ServerLevel serverLevel &&
        InWorldServer.provider(evt.getState().getBlock()) != null) {
      InWorldServer.blockBroken(serverLevel, evt.getPos());
    }
  }

  @SubscribeEvent
  public void leftClickBlock(final PlayerInteractEvent.LeftClickBlock evt) {

    if (evt.getEntity() instanceof ServerPlayer serverPlayer &&
        InWorldServer.isAttackOnIndicator(serverPlayer, evt.getPos())) {
      evt.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void levelTick(final LevelTickEvent.Post evt) {
    PolymorphCommonEvents.levelTick(evt.getLevel());
  }
}
