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

package com.illusivesoulworks.polymorph.client;

import com.illusivesoulworks.polymorph.client.inworld.ClientInWorldStates;
import com.illusivesoulworks.polymorph.client.inworld.InWorldClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("unused")
public class ClientEventsListener {

  @SubscribeEvent
  public void loggingIn(ClientPlayerNetworkEvent.LoggingIn evt) {
    PolymorphClientEvents.syncPriority();
  }

  @SubscribeEvent
  public void loggingOut(ClientPlayerNetworkEvent.LoggingOut evt) {
    PolymorphClientEvents.clearPackPriority();
    ClientInWorldStates.clear();
  }

  @SubscribeEvent
  public void chunkUnload(ChunkEvent.Unload evt) {

    if (evt.getLevel().isClientSide()) {
      ClientInWorldStates.chunkUnloaded(evt.getChunk().getPos());
    }
  }

  @SubscribeEvent
  public void renderLevel(RenderLevelStageEvent evt) {

    if (evt.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
      InWorldClient.render(evt.getPoseStack(),
          Minecraft.getInstance().renderBuffers().bufferSource(), evt.getCamera());
    }
  }

  // Forge 47 has no action on this event: it fires on the first hit, and again during
  // creative mining, where the server cooldown absorbs the repeats.
  @SubscribeEvent
  public void leftClickBlock(PlayerInteractEvent.LeftClickBlock evt) {

    if (evt.getLevel().isClientSide() && evt.getEntity() instanceof LocalPlayer player &&
        InWorldClient.attack(player, evt.getPos())) {
      evt.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void tick(TickEvent.ClientTickEvent evt) {

    if (evt.phase == TickEvent.Phase.END) {
      PolymorphClientEvents.tick();
    }
  }

  @SubscribeEvent
  public void initScreen(ScreenEvent.Init.Post evt) {
    PolymorphClientEvents.initScreen(evt.getScreen());
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void render(ScreenEvent.Render.Post evt) {
    PolymorphClientEvents.renderIfMissed(evt.getScreen(), evt.getGuiGraphics(), evt.getMouseX(),
        evt.getMouseY(), evt.getPartialTick());
  }

  @SubscribeEvent
  public void mouseClick(ScreenEvent.MouseButtonPressed.Pre evt) {

    if (PolymorphClientEvents.mouseClick(evt.getScreen(), evt.getMouseX(), evt.getMouseY(),
        evt.getButton())) {
      evt.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void mouseScroll(ScreenEvent.MouseScrolled.Pre evt) {

    if (PolymorphClientEvents.mouseScroll(evt.getScreen(), evt.getMouseX(), evt.getMouseY(),
        evt.getScrollDelta())) {
      evt.setCanceled(true);
    }
  }
}
