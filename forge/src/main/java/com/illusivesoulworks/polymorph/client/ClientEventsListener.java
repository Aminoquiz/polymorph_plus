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

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("unused")
public class ClientEventsListener {

  @SubscribeEvent
  public void loggingIn(ClientPlayerNetworkEvent.LoggedInEvent evt) {
    PolymorphClientEvents.syncPriority();
  }

  @SubscribeEvent
  public void loggingOut(ClientPlayerNetworkEvent.LoggedOutEvent evt) {
    PolymorphClientEvents.clearPackPriority();
  }

  @SubscribeEvent
  public void tick(TickEvent.ClientTickEvent evt) {

    if (evt.phase == TickEvent.Phase.END) {
      PolymorphClientEvents.tick();
    }
  }

  @SubscribeEvent
  public void initScreen(ScreenEvent.InitScreenEvent.Post evt) {
    PolymorphClientEvents.initScreen(evt.getScreen());
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void render(ScreenEvent.DrawScreenEvent.Post evt) {
    PolymorphClientEvents.renderIfMissed(evt.getScreen(), evt.getPoseStack(), evt.getMouseX(),
        evt.getMouseY(), evt.getPartialTicks());
  }

  @SubscribeEvent
  public void mouseClick(ScreenEvent.MouseClickedEvent.Pre evt) {

    if (PolymorphClientEvents.mouseClick(evt.getScreen(), evt.getMouseX(), evt.getMouseY(),
        evt.getButton())) {
      evt.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void mouseScroll(ScreenEvent.MouseScrollEvent.Pre evt) {

    if (PolymorphClientEvents.mouseScroll(evt.getScreen(), evt.getMouseX(), evt.getMouseY(),
        evt.getScrollDelta())) {
      evt.setCanceled(true);
    }
  }
}
